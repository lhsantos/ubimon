using System.Collections;
using UnityEngine;


public class RadarController : MonoBehaviour
{
    public Sprite rangeSprite;
    public Sprite waveSprite;
    public float waveEmissionTime = 4f;
    public float waveDuration = 1.5f;

    private float waveTimer = 0f;
    private Transform range = null;


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
        GameObject go = new GameObject("waveBG", typeof(SpriteRenderer));
        go.GetComponent<SpriteRenderer>().sprite = rangeSprite;
        go.transform.parent = this.transform;
        go.transform.localPosition = Vector3.zero;
        go.transform.localRotation = Quaternion.identity;
        go.transform.localScale = Vector3.zero;
        range = go.transform;
    }

    /// <summary>
    /// Called when this component is disabled in the game scene.
    /// </summary>
    void OnDisable()
    {
        Destroy(range.gameObject);
    }

    /// <summary>
    /// Called once per frame.
    /// </summary>
    void Update()
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
            int size = Mathf.Min(Screen.width, Screen.height);
            Vector3 targetScale = (0.8f * 2 * size * Camera.main.orthographicSize / Screen.height) * Vector3.one;
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
        GameObject go = new GameObject("wave", typeof(SpriteRenderer));
        go.GetComponent<SpriteRenderer>().sprite = waveSprite;
        go.transform.parent = this.transform;
        go.transform.localPosition = Vector3.zero;
        go.transform.localRotation = Quaternion.identity;
        go.transform.localScale = Vector3.zero;

        return go;
    }

    private void OnWaveDone(GameObject wave)
    {
        Destroy(wave);
    }
}
