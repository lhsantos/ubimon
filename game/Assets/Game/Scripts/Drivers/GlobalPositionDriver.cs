using System.Collections;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Threading;
using UnityEngine;
using UOS;


public class GlobalPositionDriver : MonoBehaviour, UOSEventDriver
{
    public const string DRIVER_ID = "ubimon.GlobalPositionDriver";
    public const string EVENT_POS_CHANGE = "POS_CHANGE";


    /// <summary>
    /// If no location service is available, which value should be returned for latitude?
    /// </summary>
    public float defaultLatitude;

    /// <summary>
    /// If no location service is available, which value should be returned for longitude?
    /// </summary>
    public float defaultLongitude;

    /// <summary>
    /// If no location service is available, which value should be returned for delta?
    /// </summary>
    public float defaultDelta;


    /// <summary>
    /// Desired accuracy, in meters.
    /// </summary>
    public float desiredAccuracy = 5f;

    /// <summary>
    /// Minimun distance to update position, in meters.
    /// </summary>
    public float updateDistance = 5f;

    public enum Status
    {
        UNITIALISED,
        USER_DISABLED,
        TIMEOUT,
        FAILED,
        READY
    }


    /// <summary>
    /// The singleton instance of this component.
    /// </summary>
    public static GlobalPositionDriver main { get; private set; }

    public Status status { get; private set; }


    private GlobalPosition pos;
    private double lastTime = 0d;

    /// <summary>
    /// Called when this component is created.
    /// </summary>
    void Awake()
    {
        main = this;
    }

    /// <summary>
    /// Called once per frame.
    /// </summary>
    void Update()
    {
        if (status == Status.READY)
        {
#if (UNITY_EDITOR || UNITY_STANDALONE)
            if (Time.time > lastTime)
            {
                lastTime = Time.time + 5f;
                pos = new GlobalPosition(defaultLatitude, defaultLongitude, defaultDelta);

                (new Thread(new ThreadStart(NotifyListeners))).Start();
            }
#else
            if (Input.location.status == LocationServiceStatus.Running)
            {
                var lastData = Input.location.lastData;
                if (lastData.timestamp > lastTime)
                {
                    lastTime = lastData.timestamp;
                    float delta = (new Vector2(lastData.horizontalAccuracy, lastData.verticalAccuracy)).magnitude;
                    pos = new GlobalPosition(lastData.latitude, lastData.longitude, delta);

                    (new Thread(new ThreadStart(NotifyListeners))).Start();
                }
            }
            else
            {
                status = Status.UNITIALISED;
                StartCoroutine("StartLocationService");
            }
#endif
        }
    }

    IEnumerator StartLocationService()
    {
        if (!Input.location.isEnabledByUser)
        {
            status = Status.USER_DISABLED;
            yield break;
        }

        Input.location.Start(desiredAccuracy, updateDistance);
        float maxWait = 20;
        while (
            (Input.location.status != LocationServiceStatus.Running) &&
            (Input.location.status != LocationServiceStatus.Failed) &&
            (maxWait > 0))
        {
            yield return new WaitForSeconds(1);
            maxWait--;
        }
        if (maxWait == 0)
        {
            status = Status.TIMEOUT;
            yield break;
        }
        if (Input.location.status == LocationServiceStatus.Failed)
        {
            status = Status.FAILED;
            yield break;
        }

        status = Status.READY;
    }


    #region UOSDriver Interface
    private static UpDriver _driver = null;
    private static UpDriver driver
    {
        get
        {
            if (_driver == null)
            {
                _driver = new UpDriver(DRIVER_ID);

                _driver.AddService("getPos");

                _driver.AddService("registerListener")
                    .AddParameter("eventKey", UpService.ParameterType.OPTIONAL);

                _driver.AddService("unregisterListener")
                    .AddParameter("eventKey", UpService.ParameterType.OPTIONAL);
            }

            return _driver;
        }
    }

    private UnityGateway gateway;
    private string instanceId;
    private Dictionary<string, UpDevice> listeners = new Dictionary<string, UpDevice>();

    /// <summary>
    /// Returns a description of this driver as an UpDriver object.
    /// </summary>
    /// <returns></returns>
    public UpDriver GetDriver()
    {
        return driver;
    }

    /// <summary>
    /// Returns a list of parent drivers, which may be empty or null.
    /// </summary>
    /// <returns></returns>
    public List<UpDriver> GetParent()
    {
        return null;
    }

    /// <summary>
    /// Initialises this driver.
    /// </summary>
    /// <param name="gateway"></param>
    /// <param name="settings"></param>
    /// <param name="instanceId"></param>
    public void Init(IGateway gateway, uOSSettings settings, string instanceId)
    {
        this.gateway = (UnityGateway)gateway;
        this.instanceId = instanceId;

        this.enabled = true;

#if (UNITY_EDITOR || UNITY_STANDALONE)
        status = Status.READY;
#else
        status = Status.UNITIALISED;
        StartCoroutine("StartLocationService");
#endif
    }

    /// <summary>
    /// Destroys this driver.
    /// </summary>
    public void Destroy()
    {
#if !(UNITY_EDITOR || UNITY_STANDALONE)
        if (status == Status.READY)
            Input.location.Stop();
#endif
        this.enabled = false;
    }

    public void GetPos(Call serviceCall, Response serviceResponse, CallContext messageContext)
    {
        if (status == Status.READY)
        {
            serviceResponse.AddParameter("latitude", pos.latitude);
            serviceResponse.AddParameter("longitude", pos.longitude);
            serviceResponse.AddParameter("delta", pos.delta);
        }
        else
        {
            switch (status)
            {
                case Status.UNITIALISED:
                    serviceResponse.error = "The location service is not initialized yet.";
                    break;

                case Status.USER_DISABLED:
                    serviceResponse.error = "The location service was disabled by the user in this device.";
                    break;

                case Status.TIMEOUT:
                    serviceResponse.error = "The location service couldn't read the position due to a time out.";
                    break;

                default:
                    serviceResponse.error = "Failed to get current position.";
                    break;
            }
        }
    }

    [MethodImpl(MethodImplOptions.Synchronized)]
    public void RegisterListener(Call serviceCall, Response serviceResponse, CallContext messageContext)
    {
        var device = messageContext.callerDevice;
        string key = device.ToString().ToLower();
        if (listeners.ContainsKey(key))
            serviceResponse.error = "Device already registered.";
        else
            listeners[key] = device;
    }

    [MethodImpl(MethodImplOptions.Synchronized)]
    public void UnregisterListener(Call serviceCall, Response serviceResponse, CallContext messageContext)
    {
        string key = messageContext.callerDevice.ToString().ToLower();
        if (listeners.ContainsKey(key))
            listeners.Remove(key);
        else
            serviceResponse.error = "Device not found on registry.";
    }

    [MethodImpl(MethodImplOptions.Synchronized)]
    private void NotifyListeners()
    {
        Notify notify = new Notify(EVENT_POS_CHANGE, DRIVER_ID, instanceId);
        notify.AddParameter("latitude", pos.latitude);
        notify.AddParameter("longitude", pos.longitude);
        notify.AddParameter("delta", pos.delta);

        foreach (var device in listeners.Values)
            gateway.Notify(notify, device);
    }
    #endregion
}
