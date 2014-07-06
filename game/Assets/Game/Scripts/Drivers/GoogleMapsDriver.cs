using MiniJSON;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UOS;


public class GoogleMapsDriver : MonoBehaviour, UOSDriver
{
    public const string DRIVER_ID = "ubimon.GoogleMapsDriver";
    public const string GMAPS_BASE_URL = "http://maps.googleapis.com/maps/api/staticmap";
    public const int MAX_MAP_WIDTH = 640;
    public const int MAX_MAP_HEIGHT = 640;

    public enum MapType
    {
        RoadMap,
        Satellite,
        Terrain,
        Hybrid
    }

    public float minUpdateInterval = 4f;
    public int zoom = 13;
    public MapType mapType;
    public int mapWidth = MAX_MAP_WIDTH;
    public int mapHeight = MAX_MAP_HEIGHT;
    public bool doubleResolution = false;


    /// <summary>
    /// The singleton instance of this component.
    /// </summary>
    public static GoogleMapsDriver main { get; private set; }

    /// <summary>
    /// In which position is this user right now?
    /// </summary>
    public Vector2 position { get { return pos; } }


    private Vector2 pos = Vector2.zero;
    private WWW req = null;
    private bool updateTexture = false;
    private float lastUpdate = 0f;


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
        // Is currently loading a render request?
        if (req != null)
        {
            if (req.isDone)
            {
                if (string.IsNullOrEmpty(req.error))
                {
                    var tex = new Texture2D(mapWidth, mapHeight);
                    tex.LoadImage(req.bytes);
                    renderer.material.mainTexture = tex;
                }
                else
                    Debug.Log(req.error);

                req = null;
            }
        }
        // Is there a render request in the queue?
        else if (updateTexture && (Time.time - lastUpdate > minUpdateInterval))
        {
            lastUpdate = Time.time;

            var url = new System.Text.StringBuilder();

            url.Append(GMAPS_BASE_URL);
            url.Append("?");

            url.Append("center=");
            url.Append(WWW.UnEscapeURL(string.Format("{0},{1}", pos.x, pos.y)));

            url.Append("&zoom=");
            url.Append(zoom.ToString());

            url.Append("&size=");
            url.Append(WWW.UnEscapeURL(string.Format("{0}x{1}", mapWidth, mapHeight)));

            url.Append("&scale=");
            url.Append(doubleResolution ? "2" : "1");

            url.Append("&maptype=");
            url.Append(mapType.ToString().ToLower());

            var usingSensor = false;
#if UNITY_IPHONE
		    usingSensor = Input.location.isEnabledByUser && Input.location.status == LocationServiceStatus.Running;
#endif
            url.Append("&sensor=");
            url.Append(usingSensor ? "true" : "false");

            req = new WWW(url.ToString());
            updateTexture = false;
        }
    }

    /// <summary>
    /// Called when this component is disabled.
    /// </summary>
    void OnDisable()
    {
        req = null;
        updateTexture = false;
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
                _driver.AddService("updatePos")
                    .AddParameter("latitude", UpService.ParameterType.MANDATORY)
                    .AddParameter("longitude", UpService.ParameterType.MANDATORY);
                _driver.AddService("render");
            }
            return _driver;
        }
    }

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
        this.enabled = true;
    }

    /// <summary>
    /// Destroys this driver.
    /// </summary>
    public void Destroy()
    {
        this.enabled = false;
    }


    public void UpdatePos(Call serviceCall, Response serviceResponse, CallContext messageContext)
    {
        try
        {
            // Were the parameters sent correctly and are they valid?
            object xobj, yobj;
            double x, y;

            xobj = serviceCall.GetParameter("latitude");
            if (xobj == null)
            {
                serviceResponse.error = "No 'latitude' parameter informed.";
                return;
            }
            if (xobj is double) x = (double)xobj;
            else if (!double.TryParse(xobj.ToString(), out x))
            {
                serviceResponse.error = "'latitute' parameter is not a valid float value";
                return;
            }

            yobj = serviceCall.GetParameter("longitude");
            if (xobj == null)
            {
                serviceResponse.error = "No 'longitude' parameter informed.";
                return;
            }
            if (yobj is double) y = (double)yobj;
            else if (!double.TryParse(yobj.ToString(), out y))
            {
                serviceResponse.error = "'longitude' parameter is not a valid float value";
                return;
            }

            // Updates internal state.
            this.pos = new Vector2((float)x, (float)y);
        }
        catch (System.Exception e) { serviceResponse.error = e.Message; }
    }

    public void Render(Call serviceCall, Response serviceResponse, CallContext messageContext)
    {
        // Enqueues a texture update.
        updateTexture = true;
    }
    #endregion
}
