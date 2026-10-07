package com.kakos.override.engine.luaAPI.Data;

import org.luaj.vm2.LuaTable;

public class LuaVector3 {
    public float x;
    public float y;
    public float z;

    public LuaTable get(){
        LuaTable data = new LuaTable();
        data.set("x", x);
        data.set("y",y);
        data.set("z",z);

        return data;
    }
}
