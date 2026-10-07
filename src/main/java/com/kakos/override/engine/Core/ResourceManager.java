package com.kakos.override.engine.Core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    public static String projectRootPath = Project.projectRootPath;

    private static final Map<String, Texture> textureCache = new HashMap<>();
    private static final Map<String, Sound> soundCache = new HashMap<>();
    private static final Map<String, Music> musicCache = new HashMap<>();
    private static final Map<String, Music> videoCache = new HashMap<>();
    private static final Map<String, ShaderProgram> shaderCache = new HashMap<>();
    

    private static final Map<String, FreeTypeFontGenerator> generatorCache = new HashMap<>();
    private static final Map<String, BitmapFont> fontCache = new HashMap<>();

    public static FileHandle resolveFile(String relativePath) {
        if (projectRootPath != null && !projectRootPath.isEmpty()) {
            String fullPath = projectRootPath + File.separator + relativePath;
            FileHandle absoluteFile = Gdx.files.absolute(fullPath);
            if (absoluteFile.exists()) {
                return absoluteFile;
            }
        }

        return Gdx.files.internal(relativePath);
    }

    public static Texture getTexture(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }
        
        FileHandle file = resolveFile(path);
        if (!file.exists()) {
            Gdx.app.error("ResourceManager", "Текстура не найдена: " + path);
            return null;
        }
        
        Texture texture = new Texture(file);
        textureCache.put(path, texture);
        return texture;
    }

    
    public static BitmapFont getFont(String path) {

        if (fontCache.containsKey(path)) {
            return fontCache.get(path);
        }

        FileHandle file = resolveFile(path);
        if (!file.exists()) {
            Gdx.app.error("ResourceManager", "Файл шрифта не найден: " + path);
            return null;
        }


        FreeTypeFontGenerator generator = generatorCache.get(path);
        if (generator == null) {
            generator = new FreeTypeFontGenerator(file);
            generatorCache.put(path, generator);
        }


        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 24; 
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS 
                             + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" 
                             + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

        BitmapFont font = generator.generateFont(parameter);
        

        font.getRegion().getTexture().setFilter(
            Texture.TextureFilter.Linear, 
            Texture.TextureFilter.Linear
        );

        fontCache.put(path, font);
        return font;
    }

    public static Sound getSound(String path) {
        if (soundCache.containsKey(path)) {
            return soundCache.get(path);
        }
        
        FileHandle file = resolveFile(path);
        if (!file.exists()) {
            Gdx.app.error("ResourceManager", "Звук не найден: " + path);
            return null;
        }
        
        Sound sound = Gdx.audio.newSound(file);
        soundCache.put(path, sound);
        return sound;
    }

    public static Music getMusic(String path) {
        if (musicCache.containsKey(path)) {
            return musicCache.get(path);
        }
        
        FileHandle file = resolveFile(path);
        if (!file.exists()) {
            Gdx.app.error("ResourceManager", "Музыка не найдена: " + path);
            return null;
        }
        
        Music music = Gdx.audio.newMusic(file);
        musicCache.put(path, music);
        return music;
    }

    public static ShaderProgram getShader(String vertexPath, String fragmentPath) {
        String cacheKey = vertexPath + "|" + fragmentPath;
        if (shaderCache.containsKey(cacheKey)) {
            return shaderCache.get(cacheKey);
        }

        FileHandle vertFile = resolveFile(vertexPath);
        FileHandle fragFile = resolveFile(fragmentPath);

        if (!vertFile.exists() || !fragFile.exists()) {
            Gdx.app.error("ResourceManager", "Файлы шейдера не найдены: " + vertexPath + " или " + fragmentPath);
            return null;
        }

        ShaderProgram shader = new ShaderProgram(vertFile, fragFile);
        
        if (!shader.isCompiled()) {
            Gdx.app.error("ResourceManager", "Ошибка компиляции шейдера: " + shader.getLog());
            shader.dispose();
            return null;
        }

        shaderCache.put(cacheKey, shader);
        return shader;
    }

    public static void disposeAll() {
        for (Texture t : textureCache.values()) t.dispose();
        for (Sound s : soundCache.values()) s.dispose();
        for (Music m : musicCache.values()) m.dispose();
        for (ShaderProgram sh : shaderCache.values()) sh.dispose();
        

        for (BitmapFont f : fontCache.values()) f.dispose();
        for (FreeTypeFontGenerator g : generatorCache.values()) g.dispose();

        textureCache.clear();
        soundCache.clear();
        musicCache.clear();
        shaderCache.clear();
        

        fontCache.clear();
        generatorCache.clear();
        
        Gdx.app.log("ResourceManager", "Все ресурсы успешно выгружены из памяти GPU/RAM.");
    }
}
