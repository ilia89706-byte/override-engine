package com.kakos.override.engine.luaAPI;

public class LuaMath {
    public static final double PI = 3.14;
    public static final double TWO_PI = PI * 2;

    public static float lerp(float startVal, float endVal, float t) {
        return startVal + (endVal - startVal) * t;
    }

    public static float smoothLerp(float t) {
        t = clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    public static float lerpAngle(float startRad, float endRad, float t) {
        float difference = endRad - startRad;

        while (difference < -Math.PI)
            difference += Math.PI * 2;
        while (difference > Math.PI)
            difference -= Math.PI * 2;

        return startRad + difference * clamp(t, 0.0f, 1.0f);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static float percentClamp(float startVal, float endVal, float value) {
        if (startVal == endVal)
            return 0.0f;
        return clamp((value - startVal) / (endVal - startVal), 0.0f, 1.0f);
    }

    public static float remap(float inMin, float inMax, float outMin, float outMax, float value) {
        float t = percentClamp(inMin, inMax, value);
        return lerp(outMin, outMax, t);
    }

    public static float toRadians(float degrees) {
        return (float) degrees * (float) (PI / 180.0f);
    }

    public static float toDegrees(float radians) {
        return (float) radians * (float) (180.0f / PI);
    }

    public static float sin(float radians) {
        return (float) Math.sin(radians);
    }

    public static float cos(float radians) {
        return (float) Math.cos(radians);
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float normalizeRadians(float radians) {
        float result = radians % (float) TWO_PI;
        if (result < 0) {
            result += TWO_PI;
        }
        return result;
    }

    public static float normalizeDegrees(float degrees) {
        float result = degrees % 360.0f;
        if (result < 0) {
            result += 360.0f;
        }
        return result;
    }

    public static float[] normalize(float x, float y) {
        float length = (float) Math.sqrt(x * x + y * y);
        if (length == 0)
            return new float[] { 0, 0 };
        return new float[] { x / length, y / length };
    }

    public static float[] reflect(float vx, float vy, float nx, float ny) {
        float dotProduct = dot(vx, vy, nx, ny);
        return new float[] {
                vx - 2 * dotProduct * nx,
                vy - 2 * dotProduct * ny
        };
    }

    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    private static final java.util.Random rand = new java.util.Random();

    public static float random(float min, float max) {
        return min + rand.nextFloat() * (max - min);
    }

    public static float round(float value) {
        return (float) Math.round(value);
    }

    public static float floor(float value) {
        return (float) Math.floor(value);
    }

    public static float ceil(float value) {
        return (float) Math.ceil(value);
    }

    public static float roundToPlaces(float value, int places) {
        float scale = (float) Math.pow(10, places);
        return (float) Math.round(value * scale) / scale;
    }

    public static float snap(float value, float cellSize) {
        if (cellSize == 0)
            return value;
        return (float) Math.round(value / cellSize) * cellSize;
    }
}
