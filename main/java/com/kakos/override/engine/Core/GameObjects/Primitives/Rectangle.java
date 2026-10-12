package com.kakos.override.engine.Core.GameObjects.Primitives;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.kakos.override.engine.Core.*;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Shape;

public class Rectangle extends GameObject{
    public float width;
    public float height;

    public Rectangle(float x, float y,float width,float height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

 @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        nrs.beginShape(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(
            this.color.get("r").tofloat(), 
            this.color.get("g").tofloat(),
            this.color.get("b").tofloat(), 
            this.color.get("a").tofloat()
        );

        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;

        shapeRenderer.rect(offsetX, offsetY, this.width, this.height);
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        return MathShape.rectangleContainsPoint(this.width, this.height, this.anchorX, this.anchorY, localX, localY);
    }



}
