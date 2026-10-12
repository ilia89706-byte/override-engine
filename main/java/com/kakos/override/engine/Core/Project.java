package com.kakos.override.engine.Core;

import java.io.File;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.kakos.override.editor.Console;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class Project {
    public static String projectRootPath;
    public static boolean intialised = false;
    public static boolean isValid = false;

    public static String windowTitle = "Override Engine Game";
    public static int windowWidth = 800;
    public static int windowHeight = 600;
    public static int targetFps = 60;
    public static boolean vsync = true;
    public static String renderType = "fit";

    public static boolean fullscreen = false;
    public static boolean resizable = true;
    public static boolean decorated = true;

    public static boolean autoIconify = true;
    public static boolean maximized = false;
    public static boolean alwaysOnTop = false;

    public static int samples = 0;
    public static boolean useGL30 = false;
    public static int audioDeviceSimultaneousSources = 32;
    public static String iconPath = "";

    public static void start(String pathToFile) {
        if (intialised && isValid) {
            API.genAPI();
            FolderWatcher.start(null);
            ErrorHandler.processFile(projectRootPath + File.separator + pathToFile);
        }
    }

    public static void loadConf() {
        String localPath = projectRootPath + File.separator + "conf.lua";

        try {
            Globals globals = JsePlatform.standardGlobals();
            globals.loadfile(localPath).call();

            LuaValue configTable = globals.get("conf");
            if (!configTable.isnil()) {
                if (!configTable.get("title").isnil())
                    windowTitle = configTable.get("title").tojstring();
                if (!configTable.get("width").isnil())
                    windowWidth = configTable.get("width").toint();
                if (!configTable.get("height").isnil())
                    windowHeight = configTable.get("height").toint();
                if (!configTable.get("fps").isnil())
                    targetFps = configTable.get("fps").toint();
                if (!configTable.get("renderType").isnil())
                    renderType = configTable.get("renderType").tojstring();
                if (!configTable.get("samples").isnil())
                    samples = configTable.get("samples").toint();
                if (!configTable.get("audioDeviceSimultaneousSources").isnil())
                    audioDeviceSimultaneousSources = configTable.get("audioDeviceSimultaneousSources").toint();
                if (!configTable.get("iconPath").isnil())
                    iconPath = configTable.get("iconPath").tojstring();

                if (!configTable.get("vsync").isnil())
                    vsync = configTable.get("vsync").toboolean();
                if (!configTable.get("fullscreen").isnil())
                    fullscreen = configTable.get("fullscreen").toboolean();
                if (!configTable.get("resizable").isnil())
                    resizable = configTable.get("resizable").toboolean();
                if (!configTable.get("decorated").isnil())
                    decorated = configTable.get("decorated").toboolean();
                if (!configTable.get("autoIconify").isnil())
                    autoIconify = configTable.get("autoIconify").toboolean();
                if (!configTable.get("maximized").isnil())
                    maximized = configTable.get("maximized").toboolean();
                if (!configTable.get("alwaysOnTop").isnil())
                    alwaysOnTop = configTable.get("alwaysOnTop").toboolean();
                if (!configTable.get("useGL30").isnil())
                    useGL30 = configTable.get("useGL30").toboolean();
            }
        } catch (Exception e) {
            Console.logWarn(Console.Modifier.LUA, "Error on load conf.lua");
        }
    }

    public static void checkValid() {
        if (!intialised) {
            String localPath = projectRootPath + File.separator;
            File mainFile = new File(localPath + "main.lua");
            File confFile = new File(localPath + "conf.lua");

            if (mainFile.canRead() && confFile.canRead()) {
                isValid = true;
            } else {
                Console.logError(Console.Modifier.LUA, "main.lua or conf.lua not found");
            }
        }
    }

    public static void initGdx() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle(Project.windowTitle);
        config.useVsync(Project.vsync);
        config.setForegroundFPS(Project.targetFps);

        if (Project.fullscreen) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(Project.windowWidth, Project.windowHeight);
        }

        config.setResizable(Project.resizable);
        config.setDecorated(Project.decorated);
        config.setAutoIconify(Project.autoIconify);
        config.setMaximized(Project.maximized);

        if (Project.samples > 0) {
            config.setBackBufferConfig(8, 8, 8, 8, 16, 0, Project.samples);
        }

        if (Project.useGL30) {
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2);
        }

        config.setAudioConfig(Project.audioDeviceSimultaneousSources, 512, 9);

        if (Project.iconPath != null && !Project.iconPath.isEmpty()) {
            String fullIconPath = Project.projectRootPath + File.separator + Project.iconPath;
            File iconFile = new File(fullIconPath);
            if (iconFile.exists()) {
                config.setWindowIcon(fullIconPath);
            } else {
                Console.logWarn(Console.Modifier.LUA, "No icon file: " + iconPath);
            }
        }

        new Lwjgl3Application(new Window(), config);
    }

    public static void init(String projectPath) {
        if (!intialised) {
            projectRootPath = projectPath;
            checkValid();

            if (isValid) {
                intialised = true;
                loadConf();
                initGdx();
            }
        }
    }
}
