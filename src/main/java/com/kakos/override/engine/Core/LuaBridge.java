package com.kakos.override.engine.Core;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaBridge {

    public interface FieldChangeListener {
        void onFieldChanged(Object javaObject, String fieldName, LuaValue newValue);
    }

    public static LuaValue wrap(Object javaObject, FieldChangeListener listener) {
        LuaValue rawUserdata = CoerceJavaToLua.coerce(javaObject);
        LuaTable wrapper = new LuaTable();
        LuaTable metatable = new LuaTable();

        metatable.set("__index", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue table, LuaValue key) {
                if (key.tojstring().equals("__raw_java_object")) {
                    return rawUserdata;
                }

                LuaValue val = rawUserdata.get(key);
                if (val.isfunction()) {
                    return new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                            int narg = args.narg();
                            LuaValue[] remappedArgs;
                            int srcStart = 0;

                            if (narg > 0 && args.arg(1).istable()
                                    && args.arg(1).get("__raw_java_object").isuserdata()) {
                                remappedArgs = new LuaValue[narg];
                                remappedArgs[0] = rawUserdata;
                                srcStart = 1;
                            } else {
                                remappedArgs = new LuaValue[narg + 1];
                                remappedArgs[0] = rawUserdata;
                                srcStart = 0;
                            }

                            for (int i = srcStart; i < narg; i++) {
                                LuaValue currentArg = args.arg(i + 1);
                                int targetIndex = i + (srcStart == 0 ? 1 : 0);

                                if (currentArg.istable()) {
                                    LuaValue rawChild = currentArg.get("__raw_java_object");
                                    if (rawChild.isuserdata()) {
                                        remappedArgs[targetIndex] = rawChild;
                                    } else {
                                        remappedArgs[targetIndex] = currentArg;
                                    }
                                } else {
                                    remappedArgs[targetIndex] = currentArg;
                                }
                            }

                            return val.invoke(LuaValue.varargsOf(remappedArgs));
                        }
                    };
                }
                return val;
            }
        });

        metatable.set("__newindex", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue table, LuaValue key, LuaValue value) {
                String fieldName = key.tojstring();
                rawUserdata.set(key, value);
                if (listener != null) {
                    listener.onFieldChanged(javaObject, fieldName, value);
                }
                return NONE;
            }
        });

        wrapper.setmetatable(metatable);
        return wrapper;
    }
}
