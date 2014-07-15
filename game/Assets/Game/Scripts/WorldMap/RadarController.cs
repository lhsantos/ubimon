using System.Collections.Generic;
using UnityEngine;


public class RadarController : MonoBehaviour
{
    public Sprite playerIcon;
    public Sprite stationIcon;
    public Sprite gatheringIcon;
    public GameObject labelPrefab;
    public Sprite rangeSprite;
    public Sprite waveSprite;
    public float waveEmissionTime = 4f;
    public float waveDuration = 1.5f;

    private float waveTimer = 0f;
    private Transform range = null;
    private Dictionary<string, GameObject> markers = new Dictionary<string, GameObject>();


    /// <summary>
    /// Called when this component is created.
    /// </summary>
    void Awake()
    {
    }

    /// <summary>
    /// Called when this component is enabled in the game scene.
    /// </summary>
    void OnEnable()
    {
        GameObject go = CreateChildSprite("range", rangeSprite, Vector3.zero, Quaternion.identity, Vector3.forward);
        range = go.transform;
    }

    /// <summary>
    /// Called when this component is disabled in the game scene.
    /// </summary>
    void OnDisable()
    {
        Destroy(range.gameObject);
    }

    void LateUpdate()
    {
        var screenSize = new Vector2(Screen.width, Screen.height);
        var screenCenter = WorldMapController.main.PixelPosition(WorldMapController.main.pos);

        var entities = WorldMapController.main.neighbours ?? new List<WorldEntity>();
        var newMarkers = new Dictionary<string, GameObject>();
        foreach (var entity in entities)
        {
            GameObject marker = null;
            if (!markers.TryGetValue(entity.name, out marker))
                marker = CreateMarker(entity);
            else
                markers.Remove(entity.name);

            // Sets the marker position on map.
            Vector2 p = WorldMapController.main.PixelPosition(entity.pos);
            Vector2 newPos = new Vector2(p.x - screenCenter.x, screenCenter.y - p.y); // the coordinate system is inverted on y!
            marker.transform.localScale = Util.SpriteScale(GameController.refResolution, screenSize);
            marker.transform.localPosition = newPos;

            // Fixes label position.
            Transform label = marker.transform.FindChild("label");
            float sign = Mathf.Sign(newPos.y);
            label.localPosition = sign * Mathf.Abs(label.localPosition.y) * Vector3.up;
            label.GetComponent<TextMesh>().anchor = (sign > 0) ? TextAnchor.LowerCenter : TextAnchor.UpperCenter;

            newMarkers[entity.name] = marker;
        }
        foreach (var pair in markers)
            GameObject.Destroy(pair.Value);

        markers = newMarkers;
    }

    /// <summary>
    /// Called once per frame.
    /// </summary>
    void FixedUpdate()
    {
        ProcessWave();
    }

    private void ProcessWave()
    {
        waveTimer -= Time.deltaTime;
        if (waveTimer <= 0)
        {
            waveTimer += waveEmissionTime;

            GameObject wave = CreateWave();
            Vector3 targetScale = new Vector3(0.95f, 0.95f, 1.0f);
            range.localScale = targetScale;

            iTween.ScaleTo(
                wave.gameObject,
                iTween.Hash(
                    "scale", targetScale,
                    "z", 1,
                    "time", waveDuration,
                    "easetype", iTween.EaseType.easeOutQuad,
                    "oncomplete", "OnWaveDone",
                    "oncompletetarget", this.gameObject,
                    "oncompleteparams", wave));
        }
    }

    private GameObject CreateWave()
    {
        return CreateChildSprite("wave", waveSprite, Vector3.zero, Quaternion.identity, Vector3.forward);
    }

    private GameObject CreateChildSprite(string name, Sprite sprite, Vector3 pos, Quaternion rot, Vector3 scale)
    {
        GameObject go = new GameObject(name, typeof(SpriteRenderer));
        go.GetComponent<SpriteRenderer>().sprite = sprite;
        go.transform.parent = transform;
        go.transform.localPosition = pos;
        go.transform.localRotation = rot;
        go.transform.localScale = scale;

        return go;
    }

    private GameObject CreateMarker(WorldEntity entity)
    {
        Sprite s = null;
        switch (entity.type)
        {
            case WorldEntity.Type.Player:
                s = playerIcon;
                break;

            case WorldEntity.Type.Station:
                s = stationIcon;
                break;

            case WorldEntity.Type.GatheringPoint:
                s = gatheringIcon;
                break;
        }
        GameObject marker = CreateChildSprite(entity.name, s, Vector3.zero, Quaternion.identity, Vector3.one);
        GameObject label = (GameObject)GameObject.Instantiate(labelPrefab);
        label.name = "label";
        label.transform.parent = marker.transform;
        label.transform.localPosition = (s.bounds.extents.y + 5) * Vector3.up;
        label.transform.localRotation = Quaternion.identity;
        label.transform.localScale = Vector3.one;
        label.GetComponent<TextMesh>().text = entity.name;

        return marker;
    }

    private void OnWaveDone(GameObject wave)
    {
        Destroy(wave);
    }
}
