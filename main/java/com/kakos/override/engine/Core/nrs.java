package com.kakos.override.engine.Core;  

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; 
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; 
import com.badlogic.gdx.math.Matrix4; 
import com.badlogic.gdx.math.Rectangle; 
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack; 
import com.badlogic.gdx.graphics.OrthographicCamera; 
import com.badlogic.gdx.utils.ScreenUtils; 
import com.kakos.override.engine.Core.GameObjects.Primitives.Image;  

import java.util.Comparator;  

public class nrs {     
    public static ShapeRenderer shapeRenderer = new ShapeRenderer();     
    public static SpriteBatch spriteBatch = new SpriteBatch();     
    public static OrthographicCamera camera;      

    private enum RenderMode { NONE, SHAPE, SPRITE }     
    private static RenderMode currentMode = RenderMode.NONE;      

    private static Rectangle activeScissors = new Rectangle();     
    private static boolean isScissorsPushed = false;      

    public static boolean debugEnabled = false;      

    public static void init() {         
        camera = new OrthographicCamera();         
        camera.setToOrtho(false, Project.windowWidth, Project.windowHeight);         
        camera.position.set(0, 0, 0);         
        camera.update();     
    }      

    public static void render() {             
        float r = com.kakos.override.engine.luaAPI.Display.color.get("r").tofloat();     
        float g = com.kakos.override.engine.luaAPI.Display.color.get("g").tofloat();     
        float b = com.kakos.override.engine.luaAPI.Display.color.get("b").tofloat();     
        float a = com.kakos.override.engine.luaAPI.Display.color.get("a").tofloat();      

        ScreenUtils.clear(r, g, b, a, true);          

        render3DScene();

        camera.update();          

        spriteBatch.setProjectionMatrix(camera.combined);         
        shapeRenderer.setProjectionMatrix(camera.combined);                  

        Matrix4 identity = new Matrix4();         
        spriteBatch.setTransformMatrix(identity);         
        shapeRenderer.setTransformMatrix(identity);          

        DisplayManager.gameObjects.sort(Comparator.comparingInt(GameObject::getLayer));         
        currentMode = RenderMode.NONE;          

        for (GameObject obj : DisplayManager.gameObjects) {             
            renderObject(obj, identity, false);         
        }          

        if (debugEnabled) {             
            for (GameObject obj : DisplayManager.gameObjects) {                 
                renderObject(obj, identity, true);             
            }         
        }          

        endCurrentMode();     
    }      

    private static void render3DScene() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        Matrix4 identity = new Matrix4();

        for (GameObject3D obj : DisplayManager.gameObjects3D) {
            if (obj.isVisible) {
                obj.draw3D(camera, identity);
            }
        }

        if (debugEnabled) {
            for (GameObject3D obj : DisplayManager.gameObjects3D) {
                if (obj.isVisible) {
                    obj.drawDebug3D(shapeRenderer, camera, identity);
                }
            }
        }

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    private static void renderObject(GameObject obj, Matrix4 parentTransform, boolean isDebugPass) {         
        if (!obj.isVisible) return;          

        Matrix4 localTransform = new Matrix4();         
        localTransform.translate(obj.x, obj.y, 0);         
        if (obj.angle != 0) localTransform.rotate(0, 0, 1, obj.angle);                           

        float finalScaleX = obj.scaleX * obj.scale;         
        float finalScaleY = obj.scaleY * obj.scale;                  

        if (finalScaleX != 1.0f || finalScaleY != 1.0f) {             
            localTransform.scale(finalScaleX, finalScaleY, 1.0f);         
        }          

        Matrix4 myGlobalTransform = parentTransform.cpy().mul(localTransform);         
        applyTransformMatrix(myGlobalTransform);          

        if (!isDebugPass) {             
            if (obj.clipBounds != null) {                 
                endCurrentMode();                 
                ScissorStack.calculateScissors(camera, spriteBatch.getTransformMatrix(), obj.clipBounds, activeScissors);                 
                isScissorsPushed = ScissorStack.pushScissors(activeScissors);             
            }              

            obj.draw(shapeRenderer, spriteBatch);              

            if (isScissorsPushed) {                 
                endCurrentMode();                  
                ScissorStack.popScissors();                 
                isScissorsPushed = false;             
            }         
        } else {             
            //.drawDebug(shapeRenderer);         
        }          

        if (!obj.childs.isEmpty()) {             
            @SuppressWarnings("unchecked")             
            java.util.ArrayList<GameObject> sortedChilds = new java.util.ArrayList<>(obj.childs);             
            sortedChilds.sort(Comparator.comparingInt(GameObject::getLayer));                          

            for (GameObject child : sortedChilds) {                 
                renderObject(child, myGlobalTransform, isDebugPass);             
            }                                                       

            applyTransformMatrix(myGlobalTransform);         
        }     
    }      

    private static void applyTransformMatrix(Matrix4 matrix) {         
        if (shapeRenderer.isDrawing()) shapeRenderer.flush();         
        shapeRenderer.setTransformMatrix(matrix);          

        if (spriteBatch.isDrawing()) spriteBatch.flush();         
        spriteBatch.setTransformMatrix(matrix);     
    }      

    public static void beginShape(ShapeRenderer.ShapeType type) {         
        if (currentMode == RenderMode.SHAPE) {             
            if (shapeRenderer.getCurrentType() == type) return;         
        }         
        endCurrentMode();         
        shapeRenderer.begin(type);         
        currentMode = RenderMode.SHAPE;     
    }      

    public static void beginSprite() {         
        if (currentMode == RenderMode.SPRITE) return;         
        endCurrentMode();         
        spriteBatch.begin();         
        currentMode = RenderMode.SPRITE;     
    }      

    private static void endCurrentMode() {         
        if (currentMode == RenderMode.SHAPE) {             
            shapeRenderer.end();         
        } else if (currentMode == RenderMode.SPRITE) {             
            spriteBatch.end();         
        }         
        currentMode = RenderMode.NONE;     
    }      

    public static void dispose() {         
        endCurrentMode();         
        shapeRenderer.dispose();         
        spriteBatch.dispose();     
    } 
}
