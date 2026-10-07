package com.kakos.override.engine.luaAPI;

import com.badlogic.gdx.Gdx;
import com.kakos.override.engine.Core.Project;

public class Mouse {

    public static float getX(){
        return Gdx.input.getX()-Project.windowWidth/2;
    }

    public static float getY(){
        return  -Gdx.input.getY() +Project.windowHeight/2;
    }

    public static boolean isButtonPressed(int button){
        return Gdx.input.isButtonPressed(button);
    }

    public static boolean isTouched(){
        return Gdx.input.isTouched();
    }

    public static float getDeltaX(){
        return Gdx.input.getDeltaX();
    }

    public static float getDeltaY(){
        return Gdx.input.getDeltaY();
    }

    public static void setPosition(int x,int y){
        Gdx.input.setCursorPosition(x,y);
    }

    public static void setCatched(boolean setter){
        Gdx.input.setCursorCatched(setter);
    }
}
