using MiniJSON;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UOS;


public class Mercator
{
    private const double TILE_SIZE = 256;
    private const double originX = TILE_SIZE / 2;
    private const double originY = TILE_SIZE / 2;
    private const double PX_PER_LNG_DEG = TILE_SIZE / 360;
    private const double PX_PER_LNG_RAD = TILE_SIZE / (2 * System.Math.PI);
    private const double DEG2RAD = System.Math.PI / 180;
    private const double RAD2DEG = 180 / System.Math.PI;

    /// <summary>
    /// Converts a (latitude, longitude) coordinate to a point in Mercator plane of size {256, 256}.
    /// </summary>
    /// <param name="coord"></param>
    /// <returns></returns>
    public static Vector2 CoordToPoint(GlobalPosition coord)
    {
        double x = 0;
        double y = 0;

        x = originX + coord.longitude * PX_PER_LNG_DEG;

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        var siny = Clamp(System.Math.Sin(DEG2RAD * coord.latitude), -0.9999, 0.9999);
        y = originY + 0.5 * System.Math.Log((1 + siny) / (1 - siny)) * -PX_PER_LNG_RAD;

        return new Vector2((float)x, (float)y);
    }

    /// <summary>
    /// Converts a point in Mercartor plane of size {256, 256} to a (latitude, longitude) coordinate.
    /// </summary>
    /// <param name="point"></param>
    /// <returns></returns>
    public static GlobalPosition PointToCoord(Vector2 point)
    {
        var lng = (point.x - originX) / PX_PER_LNG_DEG;
        var latRadians = (point.y - originY) / -PX_PER_LNG_RAD;
        var lat = RAD2DEG * (2 * System.Math.Atan(System.Math.Exp(latRadians)) - System.Math.PI / 2);

        return new GlobalPosition(lat, lng);
    }

    private static double Clamp(double v, double min, double max)
    {
        return System.Math.Min(max, System.Math.Max(min, v));
    }
}

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

    public string apiKey = "";
    public float minUpdateInterval = 2f;
    public int zoom = 20;
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
    public GlobalPosition pos { get; private set; }

    public float metersPerPixel { get; private set; }


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
                    renderer.material.color = Color.white;
                    renderer.material.mainTexture = tex;
                }
                else
                    ((UnityGateway)uOS.gateway).logger.Log(req.error);

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
            url.Append(WWW.UnEscapeURL(string.Format("{0:F6},{1:F6}", pos.latitude, pos.longitude)));

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

            string key = apiKey ?? apiKey.Trim();
            if (key.Length > 0)
            {
                url.Append("&key=");
                url.Append(key);
            }

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
            object latObj, lngObj;
            double lat, lng;

            latObj = serviceCall.GetParameter("latitude");
            if (latObj == null)
            {
                serviceResponse.error = "No 'latitude' parameter informed.";
                return;
            }
            if (latObj is double) lat = (double)latObj;
            else if (!double.TryParse(latObj.ToString(), out lat))
            {
                serviceResponse.error = "'latitute' parameter is not a valid float value";
                return;
            }

            lngObj = serviceCall.GetParameter("longitude");
            if (lngObj == null)
            {
                serviceResponse.error = "No 'longitude' parameter informed.";
                return;
            }
            if (lngObj is double) lng = (double)lngObj;
            else if (!double.TryParse(lngObj.ToString(), out lng))
            {
                serviceResponse.error = "'longitude' parameter is not a valid float value";
                return;
            }

            // Updates internal state.
            this.pos = new GlobalPosition(lat, lng);
            UpdateMetersPerPixel();
        }
        catch (System.Exception e) { serviceResponse.error = e.Message; }
    }

    public void Render(Call serviceCall, Response serviceResponse, CallContext messageContext)
    {
        // Enqueues a texture update.
        updateTexture = true;
    }
    #endregion

    private void UpdateMetersPerPixel()
    {
        const float refD = 1000.0f; // one kilometer

        double earthRadius = WGS84EarthRadius(pos.latitude);
        int numTiles = (1 << zoom);
        var pxPos = numTiles * Mercator.CoordToPoint(pos);
        var refPos = new GlobalPosition(pos.latitude, pos.longitude + Mathf.Rad2Deg * refD / earthRadius);
        var pxRef = numTiles * Mercator.CoordToPoint(refPos);

        metersPerPixel = refD / Mathf.Abs(pxPos.x - pxRef.x);
    }

    private static double WGS84EarthRadius(double lat)
    {
        const double WGS84_a = 6378137.0; // Major semiaxis.
        const double WGS84_b = 6356752.3; // Minor semiaxis.

        var An = WGS84_a * WGS84_a * System.Math.Cos(lat);
        var Bn = WGS84_b * WGS84_b * System.Math.Sin(lat);
        var Ad = WGS84_a * System.Math.Cos(lat);
        var Bd = WGS84_b * System.Math.Sin(lat);

        return System.Math.Sqrt((An * An + Bn * Bn) / (Ad * Ad + Bd * Bd));
    }
}
