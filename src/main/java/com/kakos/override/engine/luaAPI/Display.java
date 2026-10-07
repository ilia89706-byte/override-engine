package com.kakos.override.engine.luaAPI;

import com.badlogic.gdx.Gdx;
import com.kakos.override.engine.Core.DisplayManager;
import com.kakos.override.engine.Core.GameObjects.*;
import com.kakos.override.engine.Core.GameObjects.Objs.Checkbox;
import com.kakos.override.engine.Core.GameObjects.Objs.InputField;
import com.kakos.override.engine.Core.GameObjects.Objs.ScrollPanel;
import com.kakos.override.engine.Core.GameObjects.Objs.Slider;
import com.kakos.override.engine.Core.GameObjects.Objs.ValueBar;
import com.kakos.override.engine.Core.GameObjects.Objs.ValueCircleBar;
import com.kakos.override.engine.Core.GameObjects.Primitives.Circle;
import com.kakos.override.engine.Core.GameObjects.Primitives.Image;
import com.kakos.override.engine.Core.GameObjects.Primitives.Layer;
import com.kakos.override.engine.Core.GameObjects.Primitives.Rectangle;
import com.kakos.override.engine.Core.GameObjects.Primitives.SpriteSheet;
import com.kakos.override.engine.Core.GameObjects.Primitives.Text;
import com.kakos.override.engine.Core.GameObjects.Primitives.Video;
import com.kakos.override.engine.Core.GameObject;
import com.kakos.override.engine.Core.LuaBridge;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class Display {
    public static LuaTable color = new LuaTable();

    public static void init() {
        color.set("r", 0);
        color.set("g", 0);
        color.set("b", 0);
        color.set("a", 0);
    }

    public static void removeAll() {
        for (int i = DisplayManager.gameObjects.size() - 1; i != -1; i--) {
            DisplayManager.gameObjects.remove(i);
        }

        DisplayManager.gameObjects.clear();
    }

    public static LuaValue processObject(GameObject obj) {

        return LuaBridge.wrap(obj, (javaObj, fieldName, newValue) -> {
            GameObject go = (GameObject) javaObj;
            float val = newValue.isnumber() ? newValue.tofloat() : 0.0f;

            switch (fieldName) {
                case "x":
                    go.setPosition(val, go.y);
                    break;
                case "y":
                    go.setPosition(go.x, val);
                    break;
                case "angle":
                    float offset = val - go.angle;
                    go.rotate(offset);
                    break;

                case "width":
                    go.setWidth(val);
                    break;

                case "height":
                    go.setHeight(val);
                    break;
            }

            if (fieldName.equals("x") || fieldName.equals("y") ||
                    fieldName.equals("scale") || fieldName.equals("anchorX") ||
                    fieldName.equals("anchorY") || fieldName.equals("width") ||
                    fieldName.equals("height")) {

            }
        });
    }

    public static LuaValue rect(float x, float y, float width, float height) {
        Rectangle rect = new Rectangle(x, y, width, height);
        return processObject(rect);
    }

    public static LuaValue spriteSheet(String texturePath, LuaTable offsets, float x, float y, float width,
            float height) {
        SpriteSheet sh = new SpriteSheet(texturePath, offsets, x, y, width, height);
        return processObject(sh);
    }

    public static LuaValue circle(float x, float y, float radius) {
        Circle circle = new Circle(x, y, radius);
        return processObject(circle);
    }

    public static LuaValue image(String path, float x, float y, float width, float height) {
        Image image = new Image(path, x, y, width, height);
        return processObject(image);
    }

    public static LuaValue text(String text, String fontPath, float x, float y) {
        Text textObj = new Text(text, fontPath, x, y);
        return processObject(textObj);
    }

    public static LuaValue video(String videoPath, float x, float y, float width, float height) {
        Video video = new Video(videoPath, x, y, width, height);
        return processObject(video);
    }

    public static LuaValue valueBar(float x, float y, float width, float height) {
        ValueBar valueBar = new ValueBar(x, y, width, height);
        return processObject(valueBar);
    }

    public static LuaValue valueCircleBar(float x, float y, float radius) {
        ValueCircleBar circleBar = new ValueCircleBar(x, y, radius);
        return processObject(circleBar);
    }

    public static LuaValue slider(float x, float y, float width, float height) {
        Slider slider = new Slider(x, y, width, height);
        return processObject(slider);
    }

        public static LuaValue checkbox(float x, float y, float size) {
        Checkbox checkbox = new Checkbox(x, y, size);
        return processObject(checkbox);
    }

            public static LuaValue inputField(float x, float y, float width,float height) {
        InputField inputField = new InputField(x, y, width, height);
        return processObject(inputField);
    }


    public static LuaValue layer(float x, float y) {
        Layer layer = new Layer(x, y);
        return processObject(layer);
    }

    public static LuaValue node() {
        GameObject node = new GameObject(0, 0);
        return processObject(node);
    }

    public static void setBackgroundColor(String hexString) {
        if (hexString == null || Display.color == null)
            return;
        String hex = hexString.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;

            if (hex.length() == 6) { 
                r = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
                g = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
                b = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
                a = 1.0f;
            } else if (hex.length() == 8) {
                r = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
                g = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
                b = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
                a = Integer.parseInt(hex.substring(6, 8), 16) / 255f;
            } else {
                Gdx.app.error("ColorError", "Неверная длина HEX строки: " + hex);
                return;
            }

            Display.color.set("r", r);
            Display.color.set("g", g);
            Display.color.set("b", b);
            Display.color.set("a", a);

        } catch (NumberFormatException e) {
            Gdx.app.error("ColorError", "Ошибка парсинга HEX цвета: " + hex, e);
        }
    }

    public static void setBackgroundColor(LuaTable rgbaTable) {
        if (rgbaTable == null || Display.color == null)
            return;

        if (!rgbaTable.get("r").isnil())
            Display.color.set("r", rgbaTable.get("r").tofloat());
        if (!rgbaTable.get("g").isnil())
            Display.color.set("g", rgbaTable.get("g").tofloat());
        if (!rgbaTable.get("b").isnil())
            Display.color.set("b", rgbaTable.get("b").tofloat());
        if (!rgbaTable.get("a").isnil())
            Display.color.set("a", rgbaTable.get("a").tofloat());
    }

}
