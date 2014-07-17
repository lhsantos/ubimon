using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UOS;


public struct WorldEntity
{
    public enum Type
    {
        Player,
        Station,
        GatheringPoint
    }

    public int id;
    public string name;
    public string deviceDesc;
    public GlobalPosition pos;
    public double distance;
    public Type type;

    public override string ToString()
    {
        return id.ToString() + "," + name + "," + deviceDesc + "," + pos.ToString() + "," + type.ToString();
    }
}


[RequireComponent(typeof(GlobalPositionDriver))]
public class WorldMapController : MonoBehaviour, UOSEventListener
{
    public float neighbourSearchRange = 100f;


    public static WorldMapController main { get; private set; }

    public GlobalPosition pos { get; private set; }

    public List<WorldEntity> neighbours { get; private set; }


    private float timer = 0f;
    private const float timerInterval = 5f;
    private object _timer_lock = new object();
    private Queue<Notify> eventQueue = new Queue<Notify>();
    private object _event_queue_lock = new object();
    private bool running;

    private UpDevice webHost = null;
    private const string POSITION_REG_DRIVER = "ubimon.PositionRegistryDriver";
    private string clientName = "client";
    private int? myPosRegId = null;

    private UnityGateway gateway = null;


    /// <summary>
    /// Called when this component is created in the scene.
    /// </summary>
    void Awake()
    {
        main = this;
    }

    /// <summary>
    /// Called when this component is enabled.
    /// </summary>
    void OnEnable()
    {
        running = true;
    }

    /// <summary>
    /// Called when this component is disabled.
    /// </summary>
    void OnDisable()
    {
        running = false;
    }

    /// <summary>
    /// Called once every frame.
    /// </summary>
    void Update()
    {
        lock (_timer_lock) { timer -= Time.deltaTime; }
    }

    public Vector2 PixelPosition(GlobalPosition pos)
    {
        return (1 << GoogleMapsDriver.main.zoom) * Mercator.CoordToPoint(pos);
    }


    #region uOS Interfaces
    /// <summary>
    /// Called by GameController when the middleware is initiated.
    /// </summary>
    /// <param name="gateway"></param>
    /// <param name="settings"></param>
    public void Init(IGateway gateway, uOSSettings settings)
    {
        this.gateway = (UnityGateway)gateway;
        this.clientName = SystemInfo.deviceUniqueIdentifier;
        (new Thread(PositionUpdateThread)).Start();
    }

    public void HideMap()
    {
        GoogleMapsDriver.main.enabled = false;
        GoogleMapsDriver.main.renderer.enabled = false;
        RadarController.main.enabled = false;
    }

    public void ShowMap()
    {
        GoogleMapsDriver.main.enabled = true;
        GoogleMapsDriver.main.renderer.enabled = true;
        RadarController.main.enabled = true;
    }

    /// <summary>
    /// Position updated event.
    /// </summary>
    /// <param name="evt"></param>
    void UOSEventListener.HandleEvent(Notify evt)
    {
        Enqueue(evt);
    }
    #endregion


    private void PositionUpdateThread()
    {
        Notify evt;

        // Runs.
        while (running)
        {
            // Handles events...
            while ((evt = Dequeue()) != null)
                ExtractPositionData(evt);

            // Are we waiting for too long?
            if (timer < 0f)
                RequestGlobalPosition();
        }
    }

    private void Enqueue(Notify n)
    {
        lock (_event_queue_lock) { eventQueue.Enqueue(n); }
    }

    private Notify Dequeue()
    {
        lock (_event_queue_lock)
        {
            if (eventQueue.Count > 0)
                return eventQueue.Dequeue();
            return null;
        }
    }

    private void ResetTimer()
    {
        lock (_timer_lock) { timer = timerInterval; }
    }

    private void UpdatePositionRegistry()
    {
        if (myPosRegId != null)
        {
            Call call = new Call(POSITION_REG_DRIVER, "update");
            call.AddParameter("clientId", myPosRegId)
                .AddParameter("latitude", pos.latitude)
                .AddParameter("longitude", pos.longitude)
                .AddParameter("delta", pos.delta);

            Response r = gateway.CallService(webHost, call);
            if (r == null)
                gateway.logger.LogError("No responce to update pos.");
            else if (!string.IsNullOrEmpty(r.error))
                gateway.logger.LogError("Update pos error: " + r.error + ".");
            else
                UpdateNeighbours();
        }
        else
            CheckIn();
    }

    private void CheckIn()
    {
        if (webHost == null)
            webHost = gateway.ListDevices().Find(d => d.name == "ubimon-server");

        if (webHost != null)
        {
            Call call = new Call(POSITION_REG_DRIVER, "checkIn");
            call.AddParameter("clientName", clientName)
                .AddParameter("latitude", pos.latitude)
                .AddParameter("longitude", pos.longitude)
                .AddParameter("delta", pos.delta)
                .AddParameter("metadata", "ubimon");

            Response r = gateway.CallService(webHost, call);
            if ((r != null) && string.IsNullOrEmpty(r.error))
                myPosRegId = int.Parse(r.GetResponseData("clientId").ToString());
            else
                gateway.logger.LogError(r == null ? "No response for check-in." : "Error on check-in: " + r.error);
        }
    }

    private void UpdateNeighbours()
    {
        Call call = new Call(POSITION_REG_DRIVER, "listNeighbours");
        call.AddParameter("latitude", pos.latitude)
            .AddParameter("longitude", pos.longitude)
            .AddParameter("delta", pos.delta)
            .AddParameter("range", neighbourSearchRange);

        Response r = gateway.CallService(webHost, call);
        if ((r != null) && string.IsNullOrEmpty(r.error))
        {
            try
            {
                var list = new List<WorldEntity>();
                var clients = (IList<object>)r.GetResponseData("clients");
                foreach (var client in clients)
                {
                    var data = (IDictionary<string, object>)client;
                    int id = UOS.Util.ConvertOrParse<int>(data["id"]);
                    string meta = "";
                    object metaObj;
                    if (data.TryGetValue("metadata", out metaObj))
                        meta = metaObj.ToString().Trim().ToLower();

                    if ((id != (int)myPosRegId) && meta.Contains("ubimon"))
                    {
                        WorldEntity e = new WorldEntity();
                        e.id = id;
                        e.name = data["name"] as string;
                        e.pos = GlobalPosition.FromJSON(data);
                        e.distance = UOS.Util.ConvertOrParse<double>(data["distance"]);

                        object deviceDesc = data["device"];
                        e.deviceDesc = (deviceDesc is string) ? (string)deviceDesc : MiniJSON.Json.Serialize(deviceDesc);

                        e.type = WorldEntity.Type.Player;
                        if (meta.Contains("station"))
                            e.type = WorldEntity.Type.Station;
                        else if (meta.Contains("gathering"))
                            e.type = WorldEntity.Type.GatheringPoint;

                        list.Add(e);
                    }
                }
                list.Sort((a, b) => a.distance.CompareTo(b.distance));
                neighbours = list;
            }
            catch (System.Exception e)
            {
                ((UnityGateway)uOS.gateway).logger.Log(e.ToString());
            }
        }
    }


    private void RequestGlobalPosition()
    {
        Call call = new Call(GlobalPositionDriver.DRIVER_ID, "getPos");
        Response r = gateway.CallService(gateway.currentDevice, call);
        if (r == null)
            gateway.logger.LogError("No response!");
        else if (!string.IsNullOrEmpty(r.error))
            gateway.logger.LogError("Get pos error: " + r.error + ".");
        else
            ExtractPositionData(r);
    }

    private void ExtractPositionData(object message)
    {
        try
        {
            GlobalPosition newPos = GlobalPosition.zero;
            object latitudeobj, longitudeobj, deltaobj;
            if (message is Response)
            {
                Response r = (Response)message;
                latitudeobj = r.GetResponseData("latitude");
                longitudeobj = r.GetResponseData("longitude");
                deltaobj = r.GetResponseData("delta");
            }
            else
            {
                Notify n = (Notify)message;
                latitudeobj = n.GetParameter("latitude");
                longitudeobj = n.GetParameter("longitude");
                deltaobj = n.GetParameter("delta");
            }

            newPos.latitude = UOS.Util.ConvertOrParse<double>(latitudeobj);
            newPos.longitude = UOS.Util.ConvertOrParse<double>(longitudeobj);
            newPos.delta = UOS.Util.ConvertOrParse<double>(deltaobj);
            pos = newPos;

            ResetTimer();
            UpdatePositionRegistry();
            UpdateMap();
        }
        catch (System.Exception e) { gateway.logger.LogException(e); }
    }

    private void UpdateMap()
    {
        Call call = new Call(GoogleMapsDriver.DRIVER_ID, "updatePos", null);
        call.AddParameter("latitude", pos.latitude);
        call.AddParameter("longitude", pos.longitude);
        gateway.CallService(gateway.currentDevice, call);
        call.service = "render";
        call.parameters = null;
        gateway.CallService(gateway.currentDevice, call);
    }
}
