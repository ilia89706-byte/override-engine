package com.kakos.override.engine.Core.Tween;

public enum EaseType {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    QUAD_IN,
    QUAD_OUT,
    QUAD_IN_OUT,
    SINE_IN,
    SINE_OUT,
    SINE_IN_OUT,
    EXP_IN,
    EXP_OUT,
    EXP_IN_OUT,
    BACK_IN,
    BACK_OUT,
    BACK_IN_OUT,
    BOUNCE_IN,
    BOUNCE_OUT,
    BOUNCE_IN_OUT,
    ELASTIC_IN,
    ELASTIC_OUT,
    ELASTIC_IN_OUT;

    public static EaseType fromString(String type) {
        if (type == null)
            return LINEAR;

        String formatted = type.trim().replace("-", "_").replace(" ", "_").toUpperCase();
        try {
            return EaseType.valueOf(formatted);
        } catch (IllegalArgumentException e) {
            System.out.println("[Tween Warning] Неизвестный тип плавности: '" + type + "'. Используется LINEAR.");
            return LINEAR;
        }
    }

    public float apply(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));

        switch (this) {
            case LINEAR:
                return t;

            case EASE_IN:
                return t * t * t;
            case EASE_OUT:
                return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
            case EASE_IN_OUT:
                return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3) / 2.0f;

            case QUAD_IN:
                return t * t;
            case QUAD_OUT:
                return t * (2.0f - t);
            case QUAD_IN_OUT:
                return t < 0.5f ? 2.0f * t * t : -1.0f + (4.0f - 2.0f * t) * t;

            case SINE_IN:
                return 1.0f - (float) Math.cos((t * Math.PI) / 2.0f);
            case SINE_OUT:
                return (float) Math.sin((t * Math.PI) / 2.0f);
            case SINE_IN_OUT:
                return -(float) (Math.cos(Math.PI * t) - 1.0f) / 2.0f;

            case EXP_IN:
                return t == 0.0f ? 0.0f : (float) Math.pow(2.0f, 10.0f * t - 10.0f);
            case EXP_OUT:
                return t == 1.0f ? 1.0f : 1.0f - (float) Math.pow(2.0f, -10.0f * t);
            case EXP_IN_OUT:
                if (t == 0.0f)
                    return 0.0f;
                if (t == 1.0f)
                    return 1.0f;
                return t < 0.5f
                        ? (float) Math.pow(2.0f, 20.0f * t - 10.0f) / 2.0f
                        : (2.0f - (float) Math.pow(2.0f, -20.0f * t + 10.0f)) / 2.0f;

            case BACK_IN:
                return 2.70158f * t * t * t - 1.70158f * t * t;
            case BACK_OUT:
                float f = t - 1.0f;
                return f * f * ((1.70158f + 1.0f) * f + 1.70158f) + 1.0f;
            case BACK_IN_OUT:
                float c1 = 1.70158f * 1.525f;
                return t < 0.5f
                        ? ((float) Math.pow(2.0f * t, 2.0f) * ((c1 + 1.0f) * 2.0f * t - c1)) / 2.0f
                        : ((float) Math.pow(2.0f * t - 2.0f, 2.0f) * ((c1 + 1.0f) * (t * 2.0f - 2.0f) + c1) + 2.0f)
                                / 2.0f;

            case BOUNCE_OUT:
                return easeOutBounce(t);
            case BOUNCE_IN:
                return 1.0f - easeOutBounce(1.0f - t);
            case BOUNCE_IN_OUT:
                return t < 0.5f
                        ? (1.0f - easeOutBounce(1.0f - 2.0f * t)) / 2.0f
                        : (1.0f + easeOutBounce(2.0f * t - 1.0f)) / 2.0f;

            case ELASTIC_IN:
                if (t == 0.0f)
                    return 0.0f;
                if (t == 1.0f)
                    return 1.0f;
                return -(float) (Math.pow(2.0, 10.0 * t - 10.0)
                        * Math.sin((t * 10.0 - 10.75) * ((2.0 * Math.PI) / 3.0)));
            case ELASTIC_OUT:
                if (t == 0.0f)
                    return 0.0f;
                if (t == 1.0f)
                    return 1.0f;
                return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0 - 0.75) * ((2.0 * Math.PI) / 3.0)))
                        + 1.0f;
            case ELASTIC_IN_OUT:
                if (t == 0.0f)
                    return 0.0f;
                if (t == 1.0f)
                    return 1.0f;
                double c5 = (2.0 * Math.PI) / 4.5;
                return t < 0.5f
                        ? -(float) (Math.pow(2.0, 20.0 * t - 10.0) * Math.sin((20.0 * t - 11.125) * c5)) / 2.0f
                        : (float) (Math.pow(2.0, -20.0 * t + 10.0) * Math.sin((20.0 * t - 11.125) * c5)) / 2.0f + 1.0f;

            default:
                return t;
        }
    }

    private float easeOutBounce(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (t < 1.0f / d1) {
            return n1 * t * t;
        } else if (t < 2.0f / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5f / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        } else {
            t -= 2.625f / d1;
            return n1 * t * t + 0.984375f;
        }
    }
}
