package com.kakos.override.engine.Core.GameObjects.Objs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextField;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import com.kakos.override.engine.Core.*;

public class InputField extends GameObject {
    public float width;
    public float height;
    
    private VisTextField textField;
    private static Stage dummyStage;

    public InputField(float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;

        if (dummyStage == null) {
            dummyStage = new Stage();
        }

        if (!VisUI.isLoaded()) {
            VisUI.load();
        }

        textField = new VisTextField("");
        textField.setSize(width, height);
        
        TextField.TextFieldStyle style = textField.getStyle();
        if (style != null) {
            style.cursor.setMinWidth(2f);
        }
        
        dummyStage.addActor(textField);

        textField.setTextFieldListener((field, c) -> {
            LuaTable changeData = new LuaTable();
            changeData.set("text", LuaValue.valueOf(field.getText()));
            emit("changed", changeData);
        });

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
                    dummyStage.setKeyboardFocus(textField);
                    textField.setCursorPosition(textField.getText().length());
                    Gdx.input.setOnscreenKeyboardVisible(true);
                }
                return LuaValue.NIL;
            }
        });
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void setHint(String hint) {
        textField.setMessageText(hint);
    }

    public void setPasswordMode(boolean isPassword) {
        textField.setPasswordMode(isPassword);
        textField.setPasswordCharacter('*');
    }

    public static Stage getStageInstance() {
        if (dummyStage == null) {
            dummyStage = new Stage();
        }
        return dummyStage;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        nrs.beginSprite();
        
        float offsetX = -this.width * this.anchorX;
        float offsetY = -this.height * this.anchorY;

        com.badlogic.gdx.math.Matrix4 globalTransform = getGlobalTransform();
        com.badlogic.gdx.math.Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        
        batch.setTransformMatrix(globalTransform);
        
        textField.setPosition(offsetX, offsetY);
        textField.setSize(this.width, this.height);
        
        if (dummyStage.getKeyboardFocus() == textField) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                dummyStage.setKeyboardFocus(null);
                Gdx.input.setOnscreenKeyboardVisible(false);
            }
        }
        
        textField.draw(batch, 1.0f);
        
        batch.setTransformMatrix(oldTransform);
    }

    @Override
    public boolean containsPoint(float localX, float localY) {
        return MathShape.rectangleContainsPoint(this.width, this.height, this.anchorX, this.anchorY, localX, localY);
    }

    @Override
    public void remove() {
        super.remove();
        if (textField != null) {
            textField.remove();
        }
    }
}
