package com.kakos.override.engine.Core;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import java.io.File;

import com.kakos.override.editor.Console;

public class ErrorHandler {
    
    public static Varargs processCall(LuaValue lf, LuaValue ... args) {
        if (lf == null || lf.isnil()) {
            Console.logError(Console.Modifier.LUA, " Attempted to call a null or nil function.");
            return LuaValue.NIL;
        }

        try {
            return lf.invoke(args);
        } catch (LuaError e) {
            Console.logError(Console.Modifier.LUA, " Safe Lua Call (" + lf.tojstring() + ") failed: " + e.getMessage());
            return LuaValue.NIL;
        } catch (Exception e) {
            Console.logError(Console.Modifier.LUA, " Critical Java error during Lua call: " + e.getMessage());
            e.printStackTrace();
            return LuaValue.NIL;
        }
    }

    public static Varargs processFile(String pathToFile) {
        if (Project.projectRootPath == null || pathToFile == null) {
            Console.logError(Console.Modifier.LUA, " Cannot load file: project root path or file path is null.");
            return LuaValue.NIL;
        }

        String fullPath = pathToFile;
        File file = new File(fullPath);

        if (!file.exists() || !file.canRead()) {
            Console.logError(Console.Modifier.LUA, " Lua file not found or unreadable: " + fullPath);
            return LuaValue.NIL;
        }

        try {
            return API.globals.loadfile(fullPath).call();
        } catch (LuaError e) {
            Console.logError(Console.Modifier.LUA, " Failed to execute Lua file (" + pathToFile + "): " + e.getMessage());
            return LuaValue.NIL;
        } catch (Exception e) {
            Console.logError(Console.Modifier.LUA, " Critical Java error during Lua file execution: " + e.getMessage());
            e.printStackTrace();
            return LuaValue.NIL;
        }
    }
}
