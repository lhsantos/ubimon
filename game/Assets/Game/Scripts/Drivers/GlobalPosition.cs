using System.Collections.Generic;
using UOS;


public struct GlobalPosition
{
    public static GlobalPosition zero { get { return new GlobalPosition(0, 0); } }

    public double latitude;
    public double longitude;
    public double delta;

    public GlobalPosition(double latitude, double longitude, double delta = 0)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.delta = delta;
    }

    public static GlobalPosition FromJSON(IDictionary<string, object> json, bool getDelta = true)
    {
        double latitude = ExtractDouble(json, "latitude");
        double longitude = ExtractDouble(json, "longitude");
        double delta = 0;
        if (getDelta)
            delta = ExtractDouble(json, "delta");

        return new GlobalPosition(latitude, longitude, delta);
    }

    private static double ExtractDouble(IDictionary<string, object> json, string name)
    {
        object v = null;
        if (json.TryGetValue(name, out v))
        {
            if (v is double)
                return (double)v;
            else
            {
                double d = 0;
                if (double.TryParse(v.ToString(), out d))
                    return d;
                else
                    throw new System.ArgumentException("parameter '" + name + "' is not a valid double");
            }
        }
        else
            throw new System.ArgumentException("parameter '" + name + "' not informed");
    }

    public override string ToString()
    {
        return string.Format("{0:f6},{1:f6},{2:f6}", latitude, longitude, delta);
    }
}
