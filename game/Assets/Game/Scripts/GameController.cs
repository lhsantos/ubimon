using System.Collections.Generic;
using UnityEngine;
using UOS;


[RequireComponent(typeof(uOS))]
public class GameController : MonoBehaviour, UOSApplication, Logger
{
    /// <summary>
    /// The singleton instance of this component.
    /// </summary>
    public static GameController main { get; private set; }

    /// <summary>
    /// The uOS gateway instance.
    /// </summary>
    public UnityGateway gateway { get; private set; }


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
        Screen.sleepTimeout = SleepTimeout.NeverSleep;

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
    }



    private static string myLog = "";
    void OnGUI()
    {
        return;

        int w = Screen.width, h = Screen.height;
        Vector2 border = new Vector2(10, 10);
        Vector2 area = new Vector2(w - 2 * border.x, h - 2 * border.y);
        Rect posRect = new Rect(border.x, border.y, area.x, 0.1f * area.y);
        Rect logRect = new Rect(border.x, border.y + 0.1f * area.y, area.x, 0.9f * area.y);

        var pos = WorldMapController.main.pos;
        GUI.TextArea(
            posRect,
            "Pos: " + pos.latitude + "," + pos.longitude + ": " + pos.delta + "\n"
            + MiniJSON.Json.Serialize(gateway.currentDevice.ToJSON()));
        GUI.TextArea(logRect, myLog);
    }


    #region uOS Interfaces
    void UOSApplication.Init(IGateway gateway, uOSSettings settings)
    {
        this.gateway = (UnityGateway)gateway;

        this.gateway.Register(
            WorldMapController.main,
            gateway.currentDevice,
            GlobalPositionDriver.DRIVER_ID, null, GlobalPositionDriver.EVENT_POS_CHANGE);
    }

    void UOSApplication.TearDown()
    {
        this.gateway = null;
    }

    public void Log(object message)
    {
        return;

        DoLog("INFO:" + message.ToString());
    }

    public void LogError(object message)
    {
        DoLog("ERROR: " + message);
    }

    public void LogException(System.Exception e)
    {
        DoLog("ERROR: " + e.ToString());
    }

    public void LogWarning(object message)
    {
        DoLog("WARNING: " + message);
    }

    public static void DoLog(string msg)
    {
        Debug.Log(msg);
        myLog = msg + "\n" + myLog;
    }
    #endregion
}
