package com.kakos.override.engine.Core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.kakos.override.engine.Core.Tween.TweenManager;
import com.kakos.override.engine.luaAPI.*;
import com.kakos.override.engine.luaAPI.Output.Transcriptor;
import com.kotcrab.vis.ui.widget.file.FileUtils;

public class API {

    public static Globals globals = JsePlatform.standardGlobals();

    public static void genAPI() {
        LuaValue packageTable = globals.get("package");
        String luaPath = Project.projectRootPath + File.separator + "?.lua;" + Project.projectRootPath + File.separator
                + "?" + File.separator + "init.lua";
        packageTable.set("path", LuaValue.valueOf(luaPath));

        Map<String, Class<?>> apiMap = new HashMap<>();

        apiMap.put("display", Display.class);
        apiMap.put("sceneManager", SceneManager.class);
        apiMap.put("mouse", Mouse.class);
        apiMap.put("mathUtils", LuaMath.class);
        apiMap.put("fileUtils", FileTool.class);
        apiMap.put("timer", TimerManager.class);
        apiMap.put("window", com.kakos.override.engine.luaAPI.Window.class);
        apiMap.put("sound", com.kakos.override.engine.luaAPI.Sound.class);
        apiMap.put("xmlParser", com.kakos.override.engine.luaAPI.SpriteSheet.TextureParser.class);
        apiMap.put("physics", Physics.class);
        globals.set("print", new Transcriptor());

        globals.set("tween", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public org.luaj.vm2.Varargs invoke(org.luaj.vm2.Varargs args) {
                LuaValue target = args.arg(1);
                String key = args.arg(2).tojstring();
                float duration = args.arg(3).tofloat();
                float endValue = args.arg(4).tofloat();
                String easeType = args.arg(5).tojstring();
                LuaValue onComplete = args.arg(6);

                TweenManager.to(target, key, duration, endValue, easeType, onComplete);
                return LuaValue.NIL;
            }
        });

        for (Map.Entry<String, Class<?>> entry : apiMap.entrySet()) {
            String luaName = entry.getKey();
            Class<?> javaClass = entry.getValue();

            globals.set(luaName, CoerceJavaToLua.coerce(javaClass));
        }
    }
}
