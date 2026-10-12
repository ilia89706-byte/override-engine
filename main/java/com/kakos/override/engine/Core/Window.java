package com.kakos.override.engine.Core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kakos.override.editor.*;
import com.kakos.override.engine.Core.Tween.TweenManager;
import com.kakos.override.engine.luaAPI.Display;
import com.kotcrab.vis.ui.VisUI;

import java.io.File;

import org.luaj.vm2.LuaTable;

public class Window extends ApplicationAdapter {
    

    @Override
    public void create() {
        Events engineEvents = new Events();
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();

        
        multiplexer.addProcessor(com.kakos.override.engine.Core.GameObjects.Objs.InputField.getStageInstance());

        
        multiplexer.addProcessor(com.kakos.override.engine.Core.GameObjects.Objs.ScrollPanel.getStageInstance());

        
        multiplexer.addProcessor(engineEvents);

        Gdx.input.setInputProcessor(multiplexer);

        nrs.init();
        Display.init();
        Project.start("main.lua");
    }

    @Override
    public void render() {
        LuaTable dt = new LuaTable();
        TweenManager.update(Gdx.graphics.getDeltaTime());
        TimerManager.updateAll(Gdx.graphics.getDeltaTime());
        dt.set("dt", Gdx.graphics.getDeltaTime());
        Events.callEvent("update", dt);
        dt = null;
        Physics.updatePhys(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClearColor(Display.color.get("r").tofloat(), Display.color.get("g").tofloat(),
                Display.color.get("b").tofloat(), Display.color.get("a").tofloat());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        nrs.render();

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {
        nrs.dispose();
        VisUI.dispose();
    }
}
