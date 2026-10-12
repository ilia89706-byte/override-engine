package com.kakos.override.engine.Core.GameObjects.Primitives;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.kakos.override.engine.Core.GameObject;
import com.kakos.override.engine.Core.Project;
import com.kakos.override.engine.Core.ResourceManager;
import org.luaj.vm2.LuaTable;

import java.io.File;

public class SpriteSheet extends GameObject {

    public float width;
    public float height;
    public Sprite sprite;
    public LuaTable offsets = new LuaTable();

    public Pixmap pixmap;

    public Texture texture;

    public void updateTexture(String texturePath) {
        File tex = new File(Project.projectRootPath + File.separator + texturePath);
        if (tex.exists()) {
            texture = ResourceManager.getTexture(tex.getPath());
            sprite = new Sprite(texture);

            if (pixmap != null) pixmap.dispose();

            if (!texture.getTextureData().isPrepared()) {
                texture.getTextureData().prepare();
            }
            pixmap = texture.getTextureData().consumePixmap();
        } else {
            System.err.println("Texture not found: " + tex.getPath());
        }
    }

    public SpriteSheet(String path, LuaTable offsets,float x,float y,float width,float height){
        super(x,y);
        this.width = width;
        this.height = height;
        this.offsets = offsets;

        updateTexture(path);
    }
}
