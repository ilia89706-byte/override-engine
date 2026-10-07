package com.kakos.override.engine.Core.GameObjects.Primitives;

import java.io.File;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kakos.override.engine.Core.*;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaTable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Image extends GameObject {

    public float width;
    public float height;
    public Sprite sprite;
    public LuaTable offsets = new LuaTable();

    public Pixmap pixmap;
    public Texture texture;

    public Integer sourceX = null;
    public Integer sourceY = null;
    public Integer sourceWidth = null;
    public Integer sourceHeight = null;

    public boolean wrapX = false;
    public boolean wrapY = false;

    public void updateTexture(String texturePath) {
        File tex = new File(Project.projectRootPath + File.separator + texturePath);
        if (tex.exists()) {
            texture = ResourceManager.getTexture(tex.getPath());
            sprite = new Sprite(texture);

            if (pixmap != null)
                pixmap.dispose();

            if (!texture.getTextureData().isPrepared()) {
                texture.getTextureData().prepare();
            }
            pixmap = texture.getTextureData().consumePixmap();

            resetSource();
            setWrap(false, false);
        } else {
            System.err.println("Texture not found: " + tex.getPath());
        }
    }

    public void resetSource() {
        this.sourceX = null;
        this.sourceY = null;
        this.sourceWidth = null;
        this.sourceHeight = null;
    }

    public LuaTable getPixel(int px, int py) {
        LuaTable colorTable = new LuaTable();
        if (pixmap == null || px < 0 || px >= pixmap.getWidth() || py < 0 || py >= pixmap.getHeight()) {
            colorTable.set("r", 0);
            colorTable.set("g", 0);
            colorTable.set("b", 0);
            colorTable.set("a", 0);
            return colorTable;
        }

        int intColor = pixmap.getPixel(px, py);
        Color color = new Color(intColor);

        colorTable.set("r", (int) (color.r * 255));
        colorTable.set("g", (int) (color.g * 255));
        colorTable.set("b", (int) (color.b * 255));
        colorTable.set("a", (int) (color.a * 255));

        return colorTable;
    }

    public void setPixel(int px, int py, int r, int g, int b, int a) {
        if (pixmap == null || px < 0 || px >= pixmap.getWidth() || py < 0 || py >= pixmap.getHeight())
            return;

        int intColor = Color.rgba8888(r / 255f, g / 255f, b / 255f, a / 255f);
        pixmap.drawPixel(px, py, intColor);
    }

    public void applyChanges() {
        if (texture != null && pixmap != null) {
            texture.load(texture.getTextureData());
        }
    }

    public Image(String imagePath, float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;

        offsets.set("x", 0.0);
        offsets.set("y", 0.0);
        offsets.set("width", 1.0);
        offsets.set("height", 1.0);
        updateTexture(imagePath);
    }

    public void setMirrored(boolean x, boolean y) {
        this.sprite.flip(x, y);
    }

    public void setWrap(boolean repeatX, boolean repeatY) {
        this.wrapX = repeatX;
        this.wrapY = repeatY;
        if (texture != null) {
            TextureWrap uWrap = repeatX ? TextureWrap.Repeat : TextureWrap.ClampToEdge;
            TextureWrap vWrap = repeatY ? TextureWrap.Repeat : TextureWrap.ClampToEdge;
            texture.setWrap(uWrap, vWrap);
        }
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        return MathShape.rectangleContainsPoint(this.width, this.height, this.anchorX, this.anchorY, localX, localY);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {
        nrs.beginSprite();

        this.sprite.setColor(
                this.color.get("r").tofloat(),
                this.color.get("g").tofloat(),
                this.color.get("b").tofloat(),
                this.color.get("a").tofloat());

        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;

        this.sprite.setRotation(0);
        this.sprite.setScale(1f, 1f);

        int x = (sourceX != null) ? sourceX : 0;
        int y = (sourceY != null) ? sourceY : 0;
        int w = (sourceWidth != null) ? sourceWidth : texture.getWidth();
        int h = (sourceHeight != null) ? sourceHeight : texture.getHeight();

        float scaleW = offsets.get("width").isnil() ? 1f : offsets.get("width").tofloat();
        float scaleH = offsets.get("height").isnil() ? 1f : offsets.get("height").tofloat();
        if (scaleW == 0)
            scaleW = 1f;
        if (scaleH == 0)
            scaleH = 1f;

        float scrollX = offsets.get("x").tofloat() / (float) w;
        float scrollY = offsets.get("y").tofloat() / (float) h;

        float u = (x / (float) texture.getWidth()) + scrollX;
        float u2 = ((x + w) / (float) texture.getWidth()) + scrollX;
        float v = (y / (float) texture.getHeight()) + scrollY;
        float v2 = ((y + h) / (float) texture.getHeight()) + scrollY;

        if (wrapX) {
            u2 = u + (this.width / ((float) w * scaleW));
        } else if (scaleW != 1f) {
            u2 = u + ((u2 - u) / scaleW);
        }

        if (wrapY) {
            v2 = v + (this.height / ((float) h * scaleH));
        } else if (scaleH != 1f) {
            v2 = v + ((v2 - v) / scaleH);
        }

        this.sprite.setRegion(u, v, u2, v2);
        this.sprite.setBounds(offsetX, offsetY, this.width, this.height);

        this.sprite.draw(spriteBatch);
    }
}
