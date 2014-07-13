using UnityEngine;
using System.Collections;

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
}
