using System.Collections.Generic;
using UnityEngine;
using UOS;


[RequireComponent(typeof(GlobalPositionDriver))]
public class WorldMapController : MonoBehaviour, UOSEventListener
{
    public static WorldMapController main { get; private set; }

    public GlobalPosition pos { get; private set; }

    private float timer = 0f;
    private const float timerInterval = 5f;

    private UpDevice webHost = null;
    private const string POSITION_REG_DRIVER = "ubimon.PositionRegistryDriver";
    private int? myPosRegId = null;

    private static UnityGateway _gateway = null;
    private static UnityGateway gateway
    {
        get
        {
            if (_gateway == null)
                _gateway = (UnityGateway)uOS.gateway;
            return _gateway;
        }
    }


    /// <summary>
    /// Called when this component is created in the scene.
    /// </summary>
    void Awake()
    {
        main = this;
    }

    /// <summary>
    /// Called once every frame.
    /// </summary>
    void Update()
    {
        timer -= Time.deltaTime;
        if (timer < 0f)
        {
            ResetTimer();
            RequestGlobalPosition();
        }
    }

    #region uOS Interfaces
    /// <summary>
    /// Position updated event.
    /// </summary>
    /// <param name="evt"></param>
    void UOSEventListener.HandleEvent(Notify evt)
    {
        ResetTimer();
        ExtractPositionData(evt);
    }
    #endregion


    private void ResetTimer()
    {
        timer += timerInterval;
    }

    private void RegisterPosition()
    {
        if (myPosRegId != null)
        {
            Call call = new Call(POSITION_REG_DRIVER, "update");
            call.channels = 0;
            call.AddParameter("clientId", myPosRegId)
                .AddParameter("latitude", pos.latitude)
                .AddParameter("longitude", pos.longitude)
                .AddParameter("delta", pos.delta);

            Response r = gateway.CallService(webHost, call);
            if (r == null)
                gateway.logger.LogError("No responce to update pos.");
            else if (!string.IsNullOrEmpty(r.error))
                gateway.logger.LogError("Update pos error: " + r.error + ".");
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
            call.channels = 0;
            call.AddParameter("clientName", SystemInfo.deviceUniqueIdentifier)
                .AddParameter("latitude", pos.latitude)
                .AddParameter("longitude", pos.longitude)
                .AddParameter("delta", pos.delta)
                .AddParameter("metadata", "ubimon");

            Response r = gateway.CallService(webHost, call);
            if ((r != null) && string.IsNullOrEmpty(r.error))
            {
                myPosRegId = int.Parse(r.GetResponseData("clientId").ToString());
                Debug.Log(myPosRegId);
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

            newPos.latitude = Util.ConvertOrParse<double>(latitudeobj);
            newPos.longitude = Util.ConvertOrParse<double>(longitudeobj);
            newPos.delta = Util.ConvertOrParse<double>(deltaobj);
            pos = newPos;

            RegisterPosition();
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
