package com.kakos.override.engine.Core.GameObjects.Objs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.kakos.override.engine.Core.*;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class ValueCircleBar extends GameObject {
    
    public float value = 100f;
    public float maxValue = 100f;
    public float radius = 50f;
    public float innerRadius = 0f;   

    public float startAngle = 90f;    
    public boolean clockwise = true; 

    public boolean drawBackground = true;
    public boolean backgroundAsLine = false; 
    public LuaTable bgColor;                 
    public boolean drawBorder = false;      
    public float borderWidth = 1f;           
    public LuaTable borderColor;            

    public ValueCircleBar(float x, float y, float radius) {
        super(x, y);
        this.radius = radius;
        
        this.anchorX = 0.5f;
        this.anchorY = 0.5f;

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

    private void drawHollowArc(ShapeRenderer sr, float cx, float cy, float rOut, float rIn, float startAng, float degrees, int segments) {
        if (degrees == 0) return;
        
        float startRad = startAng * MathUtils.degreesToRadians;
        float totalRad = degrees * MathUtils.degreesToRadians;
        float angleStep = totalRad / segments;

        for (int i = 0; i < segments; i++) {
            float alpha1 = startRad + i * angleStep;
            float alpha2 = startRad + (i + 1) * angleStep;

            float x1Out = cx + rOut * MathUtils.cos(alpha1);
            float y1Out = cy + rOut * MathUtils.sin(alpha1);
            float x2Out = cx + rOut * MathUtils.cos(alpha2);
            float y2Out = cy + rOut * MathUtils.sin(alpha2);

            float x1In = cx + rIn * MathUtils.cos(alpha1);
            float y1In = cy + rIn * MathUtils.sin(alpha1);
            float x2In = cx + rIn * MathUtils.cos(alpha2);
            float y2In = cy + rIn * MathUtils.sin(alpha2);

            sr.triangle(x1Out, y1Out, x2Out, y2Out, x1In, y1In);
            sr.triangle(x2Out, y2Out, x2In, y2In, x1In, y1In);
        }
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        float centerX = radius - (radius * 2 * this.anchorX);
        float centerY = radius - (radius * 2 * this.anchorY);

        int segments = Math.max(24, (int)(radius * 0.8f));
        float clampedInnerRadius = Math.max(0f, Math.min(innerRadius, radius - 0.1f));

        if (drawBackground) {
            ShapeRenderer.ShapeType bgType = backgroundAsLine ? ShapeRenderer.ShapeType.Line : ShapeRenderer.ShapeType.Filled;
            nrs.beginShape(bgType);
            shapeRenderer.setColor(
                this.bgColor.get("r").tofloat(), 
                this.bgColor.get("g").tofloat(),
                this.bgColor.get("b").tofloat(), 
                this.bgColor.get("a").tofloat()
            );

            if (backgroundAsLine) {
                shapeRenderer.circle(centerX, centerY, radius, segments);
                if (clampedInnerRadius > 0) {
                    shapeRenderer.circle(centerX, centerY, clampedInnerRadius, segments);
                }
            } else {
                if (clampedInnerRadius > 0) {
                    drawHollowArc(shapeRenderer, centerX, centerY, radius, clampedInnerRadius, 0f, 360f, segments);
                } else {
                    shapeRenderer.arc(centerX, centerY, radius, 0f, 360f, segments);
                }
            }
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

            float degrees = 360f * percentage;
            float actualDegrees = clockwise ? -degrees : degrees;

            if (clampedInnerRadius > 0) {
                drawHollowArc(shapeRenderer, centerX, centerY, radius, clampedInnerRadius, startAngle, actualDegrees, segments);
            } else {
                shapeRenderer.arc(centerX, centerY, radius, startAngle, actualDegrees, segments);
            }
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
            shapeRenderer.circle(centerX, centerY, radius, segments);
            if (clampedInnerRadius > 0) {
                shapeRenderer.circle(centerX, centerY, clampedInnerRadius, segments);
            }
            com.badlogic.gdx.Gdx.gl.glLineWidth(1f);
        }
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        float centerX = radius - (radius * 2 * this.anchorX);
        float centerY = radius - (radius * 2 * this.anchorY);
        
        float dx = localX - centerX;
        float dy = localY - centerY;
        float distanceSq = dx * dx + dy * dy;
        
        float rOutSq = radius * radius;
        float rInSq = innerRadius * innerRadius;
        
        return distanceSq <= rOutSq && distanceSq >= rInSq;
    }
}
