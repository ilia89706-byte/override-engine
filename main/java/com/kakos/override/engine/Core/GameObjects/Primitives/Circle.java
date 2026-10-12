package com.kakos.override.engine.Core.GameObjects.Primitives;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kakos.override.engine.Core.GameObject;
import com.kakos.override.engine.Core.MathShape;
import com.kakos.override.engine.Core.Physics;
import com.kakos.override.engine.Core.nrs;

public class Circle extends GameObject{
    public float radius;

    public Circle(float x, float y,float radius) {
        super(x, y);

        this.radius = radius;
    }

    @Override
    public boolean containsPoint(float pointX, float pointY) {
        return MathShape.circleContainsPoint(this.x, this.y, this.radius, pointX, pointY);
    }



    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch){
        nrs.beginShape(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(this.color.get("r").tofloat(), this.color.get("g").tofloat(),
                this.color.get("b").tofloat(), this.color.get("a").tofloat());
        shapeRenderer.circle(0,0, this.radius);
    }
    
}
