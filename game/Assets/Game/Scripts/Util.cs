using UnityEngine;


public static class Util
{
    public static Vector3 SpriteScale(Vector2 refResolution, Vector2 targetResolution, bool keepRefAspect = true)
    {
        float propY = targetResolution.y / refResolution.y;
        float propX = keepRefAspect ? propY : (targetResolution.x / refResolution.x);

        return new Vector3(propX, propY, 1);
    }
}
