package com.kakos.override.engine.luaAPI;

import java.io.File;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.kakos.override.editor.Console;
import com.kakos.override.engine.Core.API;
import com.kakos.override.engine.Core.DisplayManager;
import com.kakos.override.engine.Core.Physics;
import com.kakos.override.engine.Core.Project;
import com.kakos.override.engine.Core.ResourceManager;
import com.kakos.override.engine.Core.Tween.TweenManager;

public class SceneManager {
    public static String currentScene = "main.lua";
    public static LuaTable aviableScenes;

    public static LuaTable globals = new LuaTable();

    public static void reloadState() {

        TweenManager.clear();

        Physics.world.removeAllBodies();
        DisplayManager.gameObjects.clear();
        //API.globals = JsePlatform.standardGlobals();
        //API.genAPI();
    }

    public static void loadScene(String path) {
        String localPath = Project.projectRootPath + File.separator + path;

        File sceneFile = new File(localPath);
        if (sceneFile.exists()) {
            reloadState();
            
            Console.log(Console.Modifier.OVERRIDE, "Scene loaded: " + path);
            Project.start(path);
        } else {
            Console.logError(Console.Modifier.FILESYSTEM, "No scene: " + localPath);
        }
    }
}
