using UnityEngine;


[RequireComponent(typeof(GoogleMapsDriver))]
public class BackgroundController : MonoBehaviour
{
    private GoogleMapsDriver gmaps;

    /// <summary>
    /// Called when this component is created.
    /// </summary>
    void Awake()
    {
        gmaps = GetComponent<GoogleMapsDriver>();
    }

    /// <summary>
    /// Called once at the end of the frame.
    /// </summary>
    void LateUpdate()
    {
        // Updates game object size.
        float nh = 2 * Camera.main.orthographicSize;
        float nw = Camera.main.aspect * nh;
        transform.localScale = new Vector3(nw, nh, 1);

        // Updates gmaps texture size.
        int w = Screen.width, h = Screen.height;
        if (w > GoogleMapsDriver.MAX_MAP_WIDTH)
        {
            w = GoogleMapsDriver.MAX_MAP_WIDTH;
            h = Mathf.FloorToInt((float)GoogleMapsDriver.MAX_MAP_WIDTH * h / w);
        }
        if (h > GoogleMapsDriver.MAX_MAP_HEIGHT)
        {
            h = GoogleMapsDriver.MAX_MAP_HEIGHT;
            h = Mathf.FloorToInt((float)GoogleMapsDriver.MAX_MAP_HEIGHT * w / h);
        }
        gmaps.mapWidth = w;
        gmaps.mapHeight = h;
    }
}
