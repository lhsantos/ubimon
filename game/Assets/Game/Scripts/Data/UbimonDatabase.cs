using System.IO;
#if UNITY_EDITOR
using UnityEditor;
#endif
using UnityEngine;


/// <summary>
/// A Ubimon's move description.
/// </summary>
[System.Serializable]
public class Move
{
    public string name;
    public Ubimon.Type[] compatibleTypes;
    public int minLevel;
}

[System.Serializable]
public class UbimonData
{
    public string name;
    public Sprite sprite;
    public Ubimon.Type[] types;
    public Vector2 refResolution = new Vector2(1366, 768);

    public Texture2D texture { get; set; }
    public Vector2 textureSize { get; set; }
}

#if UNITY_EDITOR
[InitializeOnLoad]
#endif
public class UbimonDatabase : ScriptableObject
{
    public UbimonData[] ubimons;
    public Move[] moves;

    public UbimonData GetUbimonData(string name)
    {
        return System.Array.Find<UbimonData>(
                ubimons,
                u => u.name.Equals(name, System.StringComparison.InvariantCultureIgnoreCase)
            );
    }

    public Move GetMove(string name)
    {
        return System.Array.Find<Move>(
                moves,
                m => m.name.Equals(name, System.StringComparison.InvariantCultureIgnoreCase)
            );
    }


    const string ASSET_NAME = "UbimonDB";
    const string ASSET_PATH = "Resources";
    const string ASSET_EXT = ".asset";

    private static UbimonDatabase _instance;
    public static UbimonDatabase instance
    {
        get
        {
            if (_instance == null)
            {
                _instance = Resources.Load(ASSET_NAME) as UbimonDatabase;
                if (_instance == null)
                {
                    // If not found, autocreate the asset object.
                    _instance = CreateInstance<UbimonDatabase>();
#if UNITY_EDITOR
                    string properPath = Path.Combine(Application.dataPath, ASSET_PATH);
                    if (!Directory.Exists(properPath))
                    {
                        AssetDatabase.CreateFolder("Assets", "Resources");
                    }

                    string fullPath = Path.Combine(Path.Combine("Assets", ASSET_PATH), ASSET_NAME + ASSET_EXT);
                    AssetDatabase.CreateAsset(_instance, fullPath);
#endif
                }

                foreach (var ud in _instance.ubimons)
                {
                    ud.texture = ud.sprite.texture;
                    ud.textureSize = new Vector2(ud.texture.width, ud.texture.height);
                }
            }

            return _instance;
        }
    }

#if UNITY_EDITOR
    [MenuItem("Ubimon/Edit DB")]
    public static void Edit()
    {
        Selection.activeObject = instance;
    }
#endif
}
