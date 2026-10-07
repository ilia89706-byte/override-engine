package com.kakos.override.engine.Core.GameObjects.Objs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import com.kakos.override.engine.Core.*;

public class Checkbox extends GameObject {
    public float width;
    public float height;
    
    public boolean checked = false;

    public float markPadding = 4f;
    public LuaTable markColor;

    public boolean hasBorder = true;
    public float borderWidth = 1.0f;
    public LuaTable borderColor;

    public float touchPaddingX = 10f;
    public float touchPaddingY = 10f;

    public Checkbox(float x, float y, float size) {
        super(x, y);
        this.width = size;
        this.height = size;

        this.color.set("r", 0.2f);
        this.color.set("g", 0.2f);
        this.color.set("b", 0.2f);
        this.color.set("a", 1.0f);

        markColor = new LuaTable();
        markColor.set("r", 1.0f);
        markColor.set("g", 1.0f);
        markColor.set("b", 1.0f);
        markColor.set("a", 1.0f);

        borderColor = new LuaTable();
        borderColor.set("r", 0.8f);
        borderColor.set("g", 0.8f);
        borderColor.set("b", 0.8f);
        borderColor.set("a", 1.0f);

        this.on("touched", new org.luaj.vm2.lib.ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.NIL;
            }
        });

        setupInput();
    }

    private void setupInput() {
        this.events.put("touched", new org.luaj.vm2.LuaFunction() {
            @Override
            public LuaValue call(LuaValue luaSelf, LuaValue arg2) {
                if (arg2 == null || arg2.isnil()) return LuaValue.NIL;

                LuaTable data = (LuaTable) arg2;
                String phase = data.get("phase").toString();

                if (phase.equals("began")) {
                    checked = !checked;
                    
                    LuaTable changeData = new LuaTable();
                    changeData.set("checked", LuaValue.valueOf(checked));
                    emit("changed", changeData);
                }
                return LuaValue.NIL;
            }
        });
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;

        if (hasBorder) {
            nrs.beginShape(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(
                this.borderColor.get("r").tofloat(),
                this.borderColor.get("g").tofloat(),
                this.borderColor.get("b").tofloat(),
                this.borderColor.get("a").tofloat()
            );
            shapeRenderer.rect(
                offsetX - borderWidth, offsetY - borderWidth,
                this.width + borderWidth * 2, this.height + borderWidth * 2
            );
        }

        nrs.beginShape(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(
            this.color.get("r").tofloat(),
            this.color.get("g").tofloat(),
            this.color.get("b").tofloat(),
            this.color.get("a").tofloat()
        );
        shapeRenderer.rect(offsetX, offsetY, this.width, this.height);

        if (checked) {
            shapeRenderer.setColor(
                this.markColor.get("r").tofloat(),
                this.markColor.get("g").tofloat(),
                this.markColor.get("b").tofloat(),
                this.markColor.get("a").tofloat()
            );
            shapeRenderer.rect(
                offsetX + markPadding, 
                offsetY + markPadding, 
                this.width - (markPadding * 2), 
                this.height - (markPadding * 2)
            );
        }
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        float maxW = this.width + (touchPaddingX * 2);
        float maxH = this.height + (touchPaddingY * 2);
        return MathShape.rectangleContainsPoint(maxW, maxH, this.anchorX, this.anchorY, localX, localY);
    }
}
