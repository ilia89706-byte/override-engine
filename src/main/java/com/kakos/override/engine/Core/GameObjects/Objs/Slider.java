package com.kakos.override.engine.Core.GameObjects.Objs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import com.kakos.override.engine.Core.*;

public class Slider extends GameObject {
    public float width;
    public float height;

    public float min = 0.0f;
    public float max = 1.0f;
    public float value = 0.5f;

    public float knobWidth = -1f;
    public float knobHeight = -1f;
    public LuaTable knobColor;

    public boolean hasBorder = false;
    public float borderWidth = 1.0f;
    public LuaTable borderColor;

    public float touchPaddingX = 10f;
    public float touchPaddingY = 15f;

    public Slider(float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;

        this.color.set("r", 0.2f);
        this.color.set("g", 0.2f);
        this.color.set("b", 0.2f);
        this.color.set("a", 1.0f);

        knobColor = new LuaTable();
        knobColor.set("r", 0.8f);
        knobColor.set("g", 0.8f);
        knobColor.set("b", 0.8f);
        knobColor.set("a", 1.0f);

        borderColor = new LuaTable();
        borderColor.set("r", 1.0f);
        borderColor.set("g", 1.0f);
        borderColor.set("b", 1.0f);
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
                
                if (phase.equals("began") || phase.equals("moved")) {
                    float clickWorldX = data.get("x").tofloat();
                    float clickWorldY = data.get("y").tofloat();

                    com.badlogic.gdx.math.Vector3 localPoint = MathShape.getLocalPoint(Slider.this, clickWorldX, clickWorldY);
                    
                    float leftBound = -width * anchorX;
                    float relativeX = localPoint.x - leftBound;
                    float percentage = relativeX / width;
                    
                    if (percentage < 0f) percentage = 0f;
                    if (percentage > 1f) percentage = 1f;

                    float newValue = min + percentage * (max - min);
                    
                    if (newValue != value) {
                        value = newValue;
                        LuaTable changeData = new LuaTable();
                        changeData.set("value", value);
                        emit("changed", changeData);
                    }
                }
                return LuaValue.NIL;
            }
        });
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;

        float currentKnobHeight = (this.knobHeight > 0) ? this.knobHeight : this.height * 1.5f;
        float currentKnobWidth = (this.knobWidth > 0) ? this.knobWidth : currentKnobHeight * 0.6f;

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

        float percentage = (value - min) / (max - min);
        if (Float.isNaN(percentage) || Float.isInfinite(percentage)) {
            percentage = 0.0f;
        }
        
        float knobLocalCenterX = offsetX + (this.width * percentage);
        float knobCenterY = offsetY + (this.height / 2f);

        shapeRenderer.setColor(
            this.knobColor.get("r").tofloat(), 
            this.knobColor.get("g").tofloat(),
            this.knobColor.get("b").tofloat(), 
            this.knobColor.get("a").tofloat()
        );

        float knobX = knobLocalCenterX - (currentKnobWidth / 2f);
        float knobY = knobCenterY - (currentKnobHeight / 2f);
        shapeRenderer.rect(knobX, knobY, currentKnobWidth, currentKnobHeight);
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        float currentKnobHeight = (this.knobHeight > 0) ? this.knobHeight : this.height * 1.5f;
        float currentKnobWidth = (this.knobWidth > 0) ? this.knobWidth : currentKnobHeight * 0.6f;

        float maxW = Math.max(this.width, currentKnobWidth) + (touchPaddingX * 2);
        float maxH = Math.max(this.height, currentKnobHeight) + (touchPaddingY * 2);

        return MathShape.rectangleContainsPoint(maxW, maxH, this.anchorX, this.anchorY, localX, localY);
    }
}
