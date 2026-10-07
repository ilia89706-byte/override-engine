package com.kakos.override.engine.Core;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;

public class GameObject3D {
        public float x;
    public float y;
    public float z;

        public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float scaleZ = 1.0f;

        public float anchorX = 0.5f;
    public float anchorY = 0.5f;
    public float anchorZ = 0.5f;

    public Quaternion rotation = new Quaternion();

    public int layer = 1;

    public ArrayList<GameObject3D> childs = new ArrayList<>();

    public ModelInstance modelInstance;
    public ShaderProgram shader;
    public LuaTable data = new LuaTable();
    public LuaTable shaderUniforms = null;
    public LuaTable color;

    public boolean isVisible = true;

    public ArrayMap<String, LuaFunction> events = new ArrayMap<>();

    public GameObject3D() {

        color = new LuaTable();
        color.set("r", 1.0f);
        color.set("g", 1.0f);
        color.set("b", 1.0f);
        color.set("a", 1.0f);

    }

}
