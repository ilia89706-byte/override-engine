package com.kakos.override.engine.Core;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MathShape {

    
    private static final Vector3 tmpLocalPoint = new Vector3();

    
    public static Vector3 getLocalPoint(GameObject obj, float worldX, float worldY) {
        Matrix4 globalTransform = obj.getGlobalTransform();
        if (globalTransform == null) {
            return tmpLocalPoint.set(worldX, worldY, 0);
        }
        
        
        Matrix4 invTransform = globalTransform.cpy().inv();
        
        
        tmpLocalPoint.set(worldX, worldY, 0).mul(invTransform);
        return tmpLocalPoint;
    }

    
    public static boolean rectangleContainsPoint(float width, float height, float anchorX, float anchorY, float localX, float localY) {
        
        float left = -width * anchorX;
        float right = left + width;
        float bottom = -height * anchorY;
        float top = bottom + height;

        return localX >= left && localX <= right && localY >= bottom && localY <= top;
    }

    
    public static boolean circleContainsPoint(float radius, float anchorX, float anchorY, float localX, float localY) {
        
        
        float diameter = radius * 2;
        float centerX = radius - (diameter * anchorX);
        float centerY = radius - (diameter * anchorY);
        
        float dx = localX - centerX;
        float dy = localY - centerY;
        
        return (dx * dx + dy * dy) <= (radius * radius);
    }
}
