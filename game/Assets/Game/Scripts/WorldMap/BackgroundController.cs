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
        Camera.main.orthographicSize = Screen.height / 2;
        int size = Mathf.Max(Screen.width, Screen.height);

        // Updates game object size.
        transform.localScale = new Vector3(size, size, 1);

        // Updates gmaps texture size.
        size = Mathf.Min(GoogleMapsDriver.MAX_MAP_WIDTH, size);
        size = Mathf.Min(GoogleMapsDriver.MAX_MAP_HEIGHT, size);

        gmaps.mapWidth = gmaps.mapHeight = size;
    }
}
