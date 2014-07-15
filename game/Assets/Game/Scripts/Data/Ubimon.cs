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

    public UbimonData prototype { get; private set; }
    public string trainer;
    public string name;
    public int level;
    public List<Move> moves { get; private set; }


    public static Ubimon Deserialise(IDictionary<string, object> json)
    {
        var ubimon = new Ubimon();
        ubimon.prototype = UbimonDatabase.instance.GetUbimonData(json["prototype"] as string);
        ubimon.trainer = json["trainer"] as string;
        ubimon.name = json["name"] as string;
        ubimon.level = int.Parse(json["level"].ToString());
        ubimon.moves = new List<Move>();
        foreach (var move in (json["moves"] as IList<object>))
            ubimon.moves.Add(UbimonDatabase.instance.GetMove(move as string));

        return ubimon;
    }

    public static Ubimon Deserialise(string json)
    {
        return Deserialise((IDictionary<string, object>)Json.Deserialize(json));
    }

    public IDictionary<string, object> Serialise()
    {
        var json = new Dictionary<string, object>();
        json["prototype"] = prototype.name;
        json["trainer"] = trainer;
        json["name"] = name;
        json["level"] = level;
        List<object> movesNames = new List<object>();
        foreach (var move in moves)
            movesNames.Add(move.name);
        json["moves"] = movesNames;

        return json;
    }

    /// <summary>
    /// Returns this Ubimon object as a JSON serialized value.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
        return Json.Serialize(Serialise());
    }

    private Ubimon() { }
}
