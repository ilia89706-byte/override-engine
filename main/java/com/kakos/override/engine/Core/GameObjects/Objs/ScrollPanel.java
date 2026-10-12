package com.kakos.override.engine.Core.GameObjects.Objs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import com.kakos.override.engine.Core.*;

public class ScrollPanel extends GameObject {
    public float width;
    public float height;
    
    public float scrollY = 0f;
    public float minScrollY = 0f;
    public float maxScrollY = 0f;
    
    private float scrollVelocity = 0f;
    private final float friction = 0.9f;

    private final Rectangle scissorBounds = new Rectangle();
    private final Rectangle clipBoundsTmp = new Rectangle();

    private static Stage dummyStage;


    public ScrollPanel(float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;

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
            private float lastTouchY = 0f;

            @Override
            public LuaValue call(LuaValue luaSelf, LuaValue arg2) {
                if (arg2 == null || arg2.isnil()) return LuaValue.NIL;

                LuaTable data = (LuaTable) arg2;
                String phase = data.get("phase").toString();
                float currentWorldY = data.get("y").tofloat();

                if (phase.equals("began")) {
                    lastTouchY = currentWorldY;
                    scrollVelocity = 0f;
                } else if (phase.equals("moved")) {
                    float deltaY = currentWorldY - lastTouchY;
                    scrollVelocity = deltaY * 0.5f;
                    
                    scrollY += deltaY;
                    clampScroll();
                    
                    lastTouchY = currentWorldY;
                }
                return LuaValue.NIL;
            }
        });
    }

    private void clampScroll() {
        calculateScrollLimits();
        if (scrollY < minScrollY) {
            scrollY = minScrollY;
            scrollVelocity = 0f;
        }
        if (scrollY > maxScrollY) {
            scrollY = maxScrollY;
            scrollVelocity = 0f;
        }
    }

    private void calculateScrollLimits() {
        float minChildY = 0f;
        for (Object item : childs) {
            if (item instanceof GameObject) {
                GameObject child = (GameObject) item;
                if (child.y < minChildY) {
                    minChildY = child.y;
                }
            }
        }
        maxScrollY = Math.max(0f, Math.abs(minChildY) - this.height + 50f);
        minScrollY = 0f;
    }

    public void addElement(GameObject obj) {
        this.insert(obj);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        if (!isVisible) return;

        if (Math.abs(scrollVelocity) > 0.1f) {
            scrollY += scrollVelocity;
            scrollVelocity *= friction;
            clampScroll();
        }

        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;

        nrs.beginShape(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(
            this.color.get("r").tofloat(),
            this.color.get("g").tofloat(),
            this.color.get("b").tofloat(),
            this.color.get("a").tofloat()
        );
        shapeRenderer.rect(offsetX, offsetY, this.width, this.height);

        nrs.beginSprite();

        clipBoundsTmp.set(offsetX, offsetY, this.width, this.height);
        
        ScissorStack.calculateScissors(
            nrs.camera, 
            batch.getTransformMatrix(), 
            clipBoundsTmp, 
            scissorBounds
        );
        
        boolean pushed = ScissorStack.pushScissors(scissorBounds);

        com.badlogic.gdx.math.Matrix4 globalTransform = getGlobalTransform();
        
        for (Object item : childs) {
            if (item instanceof GameObject) {
                GameObject child = (GameObject) item;
                if (!child.isVisible) continue;

                com.badlogic.gdx.math.Matrix4 childTransform = globalTransform.cpy();
                childTransform.translate(child.x, child.y + scrollY, 0);

                if (child.angle != 0) childTransform.rotate(0, 0, 1, child.angle);

                float finalScaleX = child.scaleX * child.scale;
                float finalScaleY = child.scaleY * child.scale;
                if (finalScaleX != 1.0f || finalScaleY != 1.0f) {
                    childTransform.scale(finalScaleX, finalScaleY, 1.0f);
                }

                if (shapeRenderer.isDrawing()) shapeRenderer.flush();
                shapeRenderer.setTransformMatrix(childTransform);

                if (batch.isDrawing()) batch.flush();
                batch.setTransformMatrix(childTransform);

                child.draw(shapeRenderer, batch);
            }
        }

        if (shapeRenderer.isDrawing()) shapeRenderer.flush();
        shapeRenderer.setTransformMatrix(globalTransform);

        if (batch.isDrawing()) batch.flush();
        batch.setTransformMatrix(globalTransform);

        if (pushed) {
            ScissorStack.popScissors();
        }
    }

        public static Stage getStageInstance() {
        if (dummyStage == null) {
            dummyStage = new Stage();
        }
        return dummyStage;
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        return MathShape.rectangleContainsPoint(this.width, this.height, this.anchorX, this.anchorY, localX, localY);
    }
}
