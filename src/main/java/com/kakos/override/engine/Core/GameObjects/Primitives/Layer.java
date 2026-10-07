package com.kakos.override.engine.Core.GameObjects.Primitives;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.kakos.override.engine.Core.*;

public class Layer extends GameObject {

    public Layer(float x, float y) {
        super(x, y);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
    }

    @Override
    public boolean containsPoint(float pointX, float pointY) {
        Matrix4 transform = new Matrix4();
        transform.translate(this.x, this.y, 0);
        if (this.angle != 0) transform.rotate(0, 0, 1, this.angle);
        if (this.scaleX != 1.0f || this.scaleY != 1.0f) transform.scale(this.scaleX, this.scaleY, 1.0f);
        
        Matrix4 invTransform = transform.inv();
        Vector3 localPoint = new Vector3(pointX, pointY, 0).mul(invTransform);
        
        for (Object childObj : childs) {
            if (childObj instanceof GameObject) {
                GameObject obj = (GameObject) childObj;
                if (obj.containsPoint(localPoint.x, localPoint.y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
