using MiniJSON;
using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class Ubimon
{
    public enum Type
    {
        Fire,
        Water,
        Eletric,
        Flying,
        Grass
    }

    public UbimonData prototype { get; set; }
    public string id;
    public string trainer;
    public string name;
    public int level;
    public List<Move> moves { get; set; }
    public int life;
    public int maxLife;

    public Ubimon()
    {
    }

    public static Ubimon FromJSON(IDictionary<string, object> json, UbimonDatabase db)
    {
        var ubimon = new Ubimon();
        ubimon.prototype = db.GetUbimonData(json["prototype"] as string);
        ubimon.id = json["id"] as string;
        ubimon.trainer = json["trainer"] as string;
        ubimon.name = json["name"] as string;
        ubimon.level = int.Parse(json["level"].ToString());
        ubimon.moves = new List<Move>();
        foreach (var move in (json["moves"] as IList<object>))
            ubimon.moves.Add(db.GetMove(move as string));
        ubimon.life = int.Parse(json["life"].ToString());
        ubimon.maxLife = int.Parse(json["maxLife"].ToString());

        return ubimon;
    }

    public static Ubimon FromJSON(string json, UbimonDatabase db)
    {
        return FromJSON((IDictionary<string, object>)Json.Deserialize(json), db);
    }

    public IDictionary<string, object> ToJSON()
    {
        var json = new Dictionary<string, object>();
        json["prototype"] = prototype.name;
        json["id"] = id;
        json["trainer"] = trainer;
        json["name"] = name;
        json["level"] = level;
        List<object> movesNames = new List<object>();
        foreach (var move in moves)
            movesNames.Add(move.name);
        json["moves"] = movesNames;
        json["life"] = life;
        json["maxLife"] = maxLife;

        return json;
    }

    /// <summary>
    /// Returns this Ubimon object as a JSON serialized value.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
        return Json.Serialize(ToJSON());
    }
}
