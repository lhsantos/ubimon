using System.Collections.Generic;
using UnityEngine;
using UOS;


[RequireComponent(typeof(uOS))]
public class GameController : MonoBehaviour, UOSApplication, UOSEventListener, Logger
{
    /// <summary>
    /// The singleton instance of this component.
    /// </summary>
    public static GameController main { get; private set; }

    /// <summary>
    /// The uOS gateway instance.
    /// </summary>
    public UnityGateway gateway { get; private set; }

    private double latitude, longitude, delta;
    private float timer = 0f;
    private const float timerInterval = 5f;

    /// <summary>
    /// Called when this component is created in the scene.
    /// </summary>
    void Awake()
    {
        main = this;
    }

    /// <summary>
    /// Called right before the first Update.
    /// </summary>
    void Start()
    {
        uOS.Init(this, this);
    }

    /// <summary>
    /// Called once every frame.
    /// </summary>
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            Application.Quit();
            return;
        }

        timer -= Time.deltaTime;
        if (timer < 0f)
        {
            ResetTimer();

            Call call = new Call(GlobalPositionDriver.DRIVER_ID, "getPos");
            Response r = gateway.CallService(gateway.currentDevice, call);
            if (r == null)
                gateway.logger.LogError("No response!");
            else if (!string.IsNullOrEmpty(r.error))
                gateway.logger.LogError(r.error);
            else
                ExtractPos(r);
        }
    }

    private void ResetTimer()
    {
        timer += timerInterval;
    }

    private string myLog = "";
    void OnGUI()
    {
        int w = Screen.width, h = Screen.height;
        Vector2 border = new Vector2(10, 10);
        Vector2 area = new Vector2(w - 2 * border.x, h - 2 * border.y);
        Rect posRect = new Rect(border.x, border.y, area.x, 0.1f * area.y);
        Rect logRect = new Rect(border.x, border.y + 0.1f * area.y, area.x, 0.9f * area.y);

        GUI.TextArea(posRect, "Pos: " + latitude + "," + longitude + ": " + delta);
        GUI.TextArea(logRect, myLog);
    }



    #region uOS Interfaces
    void UOSApplication.Init(IGateway gateway, uOSSettings settings)
    {
        this.gateway = (UnityGateway)gateway;
        this.gateway.driverManager.DeployDriver(GoogleMapsDriver.main.GetDriver(), GoogleMapsDriver.main);
        this.gateway.driverManager.DeployDriver(GlobalPositionDriver.main.GetDriver(), GlobalPositionDriver.main);
        this.gateway.driverManager.InitDrivers();

        this.gateway.Register(
            this, gateway.currentDevice, GlobalPositionDriver.DRIVER_ID, null, GlobalPositionDriver.EVENT_POS_CHANGE);
    }

    void UOSApplication.TearDown()
    {
        this.gateway = null;
    }

    public void HandleEvent(Notify evt)
    {
        ResetTimer();
        ExtractPos(evt);
    }

    private void ExtractPos(object message)
    {
        try
        {
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

            latitude = Util.ConvertOrParse<double>(latitudeobj);
            longitude = Util.ConvertOrParse<double>(longitudeobj);
            delta = Util.ConvertOrParse<double>(deltaobj);

            Call call = new Call(GoogleMapsDriver.DRIVER_ID, "updatePos", null);
            call.AddParameter("latitude", latitude);
            call.AddParameter("longitude", longitude);
            gateway.CallService(gateway.currentDevice, call);
            call.service = "render";
            call.parameters = null;
            gateway.CallService(gateway.currentDevice, call);
        }
        catch (System.Exception e) { gateway.logger.LogException(e); }
    }


    public void Log(object message)
    {
        DoLog(message.ToString());
    }

    public void LogError(object message)
    {
        DoLog("ERROR: " + message);
    }

    public void LogException(System.Exception exception)
    {
        DoLog("ERROR: " + exception.StackTrace);
    }

    public void LogWarning(object message)
    {
        DoLog("WARNING: " + message);
    }

    private void DoLog(string msg)
    {
        Debug.Log(msg);
        myLog = msg + "\n" + myLog;
    }
    #endregion
}
