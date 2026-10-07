package com.kakos.override.engine.Core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Events implements InputProcessor {

    private final Map<Integer, GameObject> activeTouches = new HashMap<>();
    private final Map<Integer, Integer> touchButtons = new HashMap<>();
    private GameObject hoveredObject = null;

    private final Vector3 tmpVector = new Vector3();

    public static void callEvent(String eventName, LuaTable data) {
        ArrayList<GameObject> allObjects = new ArrayList<>();
        flattenObjects(DisplayManager.gameObjects, allObjects);
        for (GameObject obj : allObjects) {
            callEventToObject(obj, eventName, data);
        }
    }

    public static void callEventToObject(GameObject obj, String eventName, LuaTable data) {
        if (obj != null && obj.events != null) {
            LuaValue callback = obj.events.get(eventName);
            if (callback != null && callback.isfunction()) {
                LuaValue luaSelf = CoerceJavaToLua.coerce(obj);
                ErrorHandler.processCall((LuaFunction) callback, luaSelf, data);
            }
        }
    }

    private Vector3 getCombinedWorldCoords(int screenX, int screenY) {
        tmpVector.set(screenX, screenY, 0);
        if (nrs.camera != null) {
            nrs.camera.unproject(tmpVector);
        } else {
            tmpVector.y = Gdx.graphics.getHeight() - screenY;
        }
        return tmpVector;
    }

    private String getButtonName(int button) {
        return button == Input.Buttons.LEFT ? "left" : "right";
    }

    private static void flattenObjects(ArrayList<GameObject> source, ArrayList<GameObject> target) {
        if (source == null)
            return;
        for (Object item : source) {
            if (item instanceof GameObject) {
                GameObject obj = (GameObject) item;
                target.add(obj);
                if (obj.childs != null && !obj.childs.isEmpty()) {
                    ArrayList<GameObject> castedChilds = new ArrayList<>();
                    for (Object c : obj.childs) {
                        if (c instanceof GameObject)
                            castedChilds.add((GameObject) c);
                    }
                    flattenObjects(castedChilds, target);
                }
            }
        }
    }

    private GameObject findClickedObject(float worldX, float worldY) {
        ArrayList<GameObject> allObjects = new ArrayList<>();
        flattenObjects(DisplayManager.gameObjects, allObjects);

        allObjects.sort((o1, o2) -> Integer.compare(o1.getLayer(), o2.getLayer()));

        for (int i = allObjects.size() - 1; i >= 0; i--) {
            GameObject obj = allObjects.get(i);
            if (obj.isVisible && isPointInsideObject(obj, worldX, worldY)) {
                return obj;
            }
        }
        return null;
    }

    private boolean isPointInsideObject(GameObject obj, float worldX, float worldY) {
        Matrix4 globalTransform = obj.getGlobalTransform();
        if (globalTransform == null)
            return false;

        Matrix4 invTransform = globalTransform.cpy().inv();
        Vector3 localPoint = new Vector3(worldX, worldY, 0).mul(invTransform);

        return obj.containsPoint(localPoint.x, localPoint.y);
    }

    private void checkHover(int screenX, int screenY) {
        Vector3 worldCoords = getCombinedWorldCoords(screenX, screenY);
        GameObject currentObjectUnderCursor = findClickedObject(worldCoords.x, worldCoords.y);

        if (currentObjectUnderCursor != hoveredObject) {
            if (hoveredObject != null) {
                LuaTable dataEnd = new LuaTable();
                dataEnd.set("x", LuaValue.valueOf(worldCoords.x));
                dataEnd.set("y", LuaValue.valueOf(worldCoords.y));
                dataEnd.set("phase", "hoverEnd");
                dataEnd.set("inner", LuaValue.valueOf(false));
                callEventToObject(hoveredObject, "touched", dataEnd);
            }

            hoveredObject = currentObjectUnderCursor;

            if (hoveredObject != null) {
                LuaTable dataStart = new LuaTable();
                dataStart.set("x", LuaValue.valueOf(worldCoords.x));
                dataStart.set("y", LuaValue.valueOf(worldCoords.y));
                dataStart.set("phase", "hoverStart");
                dataStart.set("inner", LuaValue.valueOf(true));
                callEventToObject(hoveredObject, "touched", dataStart);
            }
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        checkHover(screenX, screenY);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldCoords = getCombinedWorldCoords(screenX, screenY);
        GameObject clickedObject = findClickedObject(worldCoords.x, worldCoords.y);

        if (clickedObject != null) {
            activeTouches.put(pointer, clickedObject);
            touchButtons.put(pointer, button);

            LuaTable data = new LuaTable();
            data.set("x", LuaValue.valueOf(worldCoords.x));
            data.set("y", LuaValue.valueOf(worldCoords.y));
            data.set("phase", "began");
            data.set("inner", LuaValue.valueOf(true));
            data.set("button", LuaValue.valueOf(getButtonName(button)));

            callEventToObject(clickedObject, "touched", data);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        checkHover(screenX, screenY);

        GameObject targetObject = activeTouches.get(pointer);
        if (targetObject != null) {
            Vector3 worldCoords = getCombinedWorldCoords(screenX, screenY);
            int button = touchButtons.getOrDefault(pointer, Input.Buttons.LEFT);
            boolean isInner = targetObject.isVisible && isPointInsideObject(targetObject, worldCoords.x, worldCoords.y);

            LuaTable data = new LuaTable();
            data.set("x", LuaValue.valueOf(worldCoords.x));
            data.set("y", LuaValue.valueOf(worldCoords.y));
            data.set("phase", "moved");
            data.set("inner", LuaValue.valueOf(isInner));
            data.set("button", LuaValue.valueOf(getButtonName(button)));

            callEventToObject(targetObject, "touched", data);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        GameObject targetObject = activeTouches.remove(pointer);
        touchButtons.remove(pointer);

        if (targetObject != null) {
            Vector3 worldCoords = getCombinedWorldCoords(screenX, screenY);
            boolean isInner = targetObject.isVisible && isPointInsideObject(targetObject, worldCoords.x, worldCoords.y);

            LuaTable data = new LuaTable();
            data.set("x", LuaValue.valueOf(worldCoords.x));
            data.set("y", LuaValue.valueOf(worldCoords.y));
            data.set("phase", "ended");
            data.set("inner", LuaValue.valueOf(isInner));
            data.set("button", LuaValue.valueOf(getButtonName(button)));

            callEventToObject(targetObject, "touched", data);
        }

        checkHover(screenX, screenY);
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        GameObject targetObject = activeTouches.remove(pointer);
        touchButtons.remove(pointer);

        if (targetObject != null) {
            Vector3 worldCoords = getCombinedWorldCoords(screenX, screenY);
            boolean isInner = targetObject.isVisible && isPointInsideObject(targetObject, worldCoords.x, worldCoords.y);

            LuaTable data = new LuaTable();
            data.set("x", LuaValue.valueOf(worldCoords.x));
            data.set("y", LuaValue.valueOf(worldCoords.y));
            data.set("phase", "cancelled");
            data.set("inner", LuaValue.valueOf(isInner));
            data.set("button", LuaValue.valueOf(getButtonName(button)));

            callEventToObject(targetObject, "touched", data);
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        LuaTable data = new LuaTable();
        data.set("keycode", LuaValue.valueOf(Input.Keys.toString(keycode)));
        data.set("phase", "down");
        callEvent("keyEvent", data);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        LuaTable data = new LuaTable();
        data.set("keycode", LuaValue.valueOf(Input.Keys.toString(keycode)));
        data.set("phase", "up");
        callEvent("keyEvent", data);
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }
}
