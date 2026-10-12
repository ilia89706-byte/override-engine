package com.kakos.override.engine.Core.Tween;

import org.luaj.vm2.LuaValue;

import com.kakos.override.editor.Console;

import org.luaj.vm2.LuaError;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TweenManager {

    private static class Tween {
        LuaValue targetObject;
        String key;
        float startValue;
        float endValue;
        float duration;
        float elapsedTime = 0f;
        EaseType easeType;
        LuaValue onComplete;
        
        Field cachedField; 
    }

    private static final Object LOCK = new Object();
    private static final List<Tween> activeTweens = new ArrayList<>();
    private static final List<Tween> toAdd = new ArrayList<>();
    private static final List<Tween> pendingCallbacks = new ArrayList<>();

    public static void to(LuaValue target, String key, float duration, float endValue, String easeTypeStr, LuaValue onComplete) {
        if (target == null || target.isnil() || key == null || duration <= 0) {
            return;
        }

        Tween tween = new Tween();
        tween.targetObject = target;
        tween.key = key;
        tween.endValue = endValue;
        tween.duration = duration;
        tween.onComplete = (onComplete != null && onComplete.isfunction()) ? onComplete : null;

        if (target.istable()) {
            LuaValue val = target.get(key);
            tween.startValue = val.isnumber() ? val.tofloat() : 0f;
        } else if (target.isuserdata()) {
            Object javaObj = target.checkuserdata();
            if (javaObj == null) return;
            try {
                Field field = javaObj.getClass().getField(key);
                field.setAccessible(true); 
                tween.cachedField = field;
                
                Number num = (Number) field.get(javaObj);
                tween.startValue = num != null ? num.floatValue() : 0f;
            } catch (Exception e) {
                System.err.println("[TweenManager] Error accessing field '" + key + "' on object: " + javaObj.getClass().getName());
                return;
            }
        } else {
            return;
        }

        try {
            if (easeTypeStr != null) {
                tween.easeType = EaseType.valueOf(easeTypeStr.toUpperCase());
            } else {
                tween.easeType = EaseType.LINEAR;
            }
        } catch (Exception e) {
            tween.easeType = EaseType.LINEAR;
        }

        synchronized (LOCK) {
            toAdd.add(tween);
        }
    }

  public static void update(float deltaTime) {
    synchronized (LOCK) {
        if (!toAdd.isEmpty()) {
            activeTweens.addAll(toAdd);
            toAdd.clear();
        }
    }

    if (activeTweens.isEmpty()) return;

    for (int i = activeTweens.size() - 1; i >= 0; i--) {
        if (i >= activeTweens.size()) {
            continue; 
        }

        Tween tween = activeTweens.get(i);
        tween.elapsedTime += deltaTime;

        float alpha = Math.min(1.0f, tween.elapsedTime / tween.duration);
        float easedAlpha = tween.easeType.apply(alpha);
        float currentValue = tween.startValue + (tween.endValue - tween.startValue) * easedAlpha;

        if (tween.targetObject.istable()) {
            tween.targetObject.set(tween.key, LuaValue.valueOf(currentValue));
        } else if (tween.targetObject.isuserdata() && tween.cachedField != null) {
            Object javaObj = tween.targetObject.checkuserdata();
            if (javaObj != null) {
                try {
                    Class<?> fieldType = tween.cachedField.getType();
                    if (fieldType == float.class || fieldType == Float.class) {
                        tween.cachedField.setFloat(javaObj, currentValue);
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        tween.cachedField.setDouble(javaObj, currentValue);
                    } else if (fieldType == int.class || fieldType == Integer.class) {
                        tween.cachedField.setInt(javaObj, (int) currentValue);
                    } else {
                        tween.cachedField.set(javaObj, currentValue);
                    }
                } catch (Exception e) {
                }
            }
        }

        if (alpha >= 1.0f) {
            activeTweens.remove(i); 
            if (tween.onComplete != null) {
                pendingCallbacks.add(tween);
            }
        }
    }

    if (!pendingCallbacks.isEmpty()) {
        for (int i = 0; i < pendingCallbacks.size(); i++) {
            Tween finishedTween = pendingCallbacks.get(i);
            try {
                if (finishedTween.onComplete != null && finishedTween.onComplete.isfunction()) {
                    finishedTween.onComplete.call(finishedTween.targetObject);
                }
            } catch (LuaError le) {
                Console.log(Console.Modifier.LUA,le.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pendingCallbacks.clear();
    }
}


    public static void clear() {
        synchronized (LOCK) {
            activeTweens.clear();
            toAdd.clear();
            pendingCallbacks.clear();
        }
    }
}
