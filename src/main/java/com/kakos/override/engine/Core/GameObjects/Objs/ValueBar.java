package com.kakos.override.engine.Core.GameObjects.Objs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kakos.override.engine.Core.*;
import com.kakos.override.engine.Core.GameObjects.Primitives.Rectangle;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class ValueBar extends Rectangle {
    

    public float value = 100f;
    public float maxValue = 100f;


    public boolean isVertical = false;
    public boolean invertDirection = false;


    public boolean drawBackground = true;
    public boolean backgroundAsLine = false;
    public LuaTable bgColor;


    public boolean drawBorder = false;
    public float borderWidth = 1f;
    public LuaTable borderColor;

    public ValueBar(float x, float y, float width, float height) {
        super(x, y, width, height);
        

        this.bgColor = new LuaTable();
        this.bgColor.set("r", LuaValue.valueOf(0.2f));
        this.bgColor.set("g", LuaValue.valueOf(0.2f));
        this.bgColor.set("b", LuaValue.valueOf(0.2f));
        this.bgColor.set("a", LuaValue.valueOf(1.0f));


        this.borderColor = new LuaTable();
        this.borderColor.set("r", LuaValue.valueOf(1.0f));
        this.borderColor.set("g", LuaValue.valueOf(1.0f));
        this.borderColor.set("b", LuaValue.valueOf(1.0f));
        this.borderColor.set("a", LuaValue.valueOf(1.0f));
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {

        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;


        if (drawBackground) {
            ShapeRenderer.ShapeType bgType = backgroundAsLine ? ShapeRenderer.ShapeType.Line : ShapeRenderer.ShapeType.Filled;
            nrs.beginShape(bgType);
            shapeRenderer.setColor(
                this.bgColor.get("r").tofloat(), 
                this.bgColor.get("g").tofloat(),
                this.bgColor.get("b").tofloat(), 
                this.bgColor.get("a").tofloat()
            );
            shapeRenderer.rect(offsetX, offsetY, this.width, this.height);
        }


        float percentage = 0f;
        if (this.maxValue > 0) {
            percentage = Math.max(0f, Math.min(1f, this.value / this.maxValue));
        }


        if (percentage > 0) {
            nrs.beginShape(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(
                this.color.get("r").tofloat(), 
                this.color.get("g").tofloat(),
                this.color.get("b").tofloat(), 
                this.color.get("a").tofloat()
            );

            float fillX = offsetX;
            float fillY = offsetY;
            float fillWidth = this.width;
            float fillHeight = this.height;

            if (isVertical) {
                fillHeight = this.height * percentage;
                if (invertDirection) {
                    fillY = offsetY + (this.height - fillHeight);
                }
            } else {
                fillWidth = this.width * percentage;
                if (invertDirection) {
                    fillX = offsetX + (this.width - fillWidth);
                }
            }

            shapeRenderer.rect(fillX, fillY, fillWidth, fillHeight);
        }


        if (drawBorder && borderWidth > 0) {
            nrs.beginShape(ShapeRenderer.ShapeType.Line);
            com.badlogic.gdx.Gdx.gl.glLineWidth(borderWidth); 
            
            shapeRenderer.setColor(
                this.borderColor.get("r").tofloat(), 
                this.borderColor.get("g").tofloat(),
                this.borderColor.get("b").tofloat(), 
                this.borderColor.get("a").tofloat()
            );
            shapeRenderer.rect(offsetX, offsetY, this.width, this.height);
            com.badlogic.gdx.Gdx.gl.glLineWidth(1f);
        }
    }
}
