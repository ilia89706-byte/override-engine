package com.kakos.override.engine.Core.GameObjects.Primitives;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.kakos.override.engine.Core.Events;
import com.kakos.override.engine.Core.GameObject;
import com.kakos.override.engine.Core.Project;
import com.kakos.override.engine.Core.nrs;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;

import java.io.File;
import java.io.FileNotFoundException;

public class Video extends GameObject {
    public String videoPath;
    public boolean playing = true;
    public boolean cancelled = false;
    public VideoPlayer videoPlayer;
    public Texture currentTexture;
    public float width = 0.0f;
    public float height = 0.0f;


    public Video(String pathToFile, float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.videoPath = pathToFile;

        this.videoPlayer = VideoPlayerCreator.createVideoPlayer();

        initVideo(pathToFile);
    }

    public void initVideo(String path) {
        FileHandle file = Gdx.files.absolute(Project.projectRootPath + File.separator + path);
        try {
            videoPlayer.load(file);
            videoPlayer.play();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Video thisVideo = this;
        videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
            @Override
            public void onCompletionListener(FileHandle file) {
                Events.callEventToObject(thisVideo,"onComplete",new LuaTable());
            }
        });
    }

    public void updateVideo() {
        if (videoPlayer != null) {
            videoPlayer.update();
        }
    }
    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch){
        this.updateVideo();
        if (this.videoPlayer != null && this.playing) {
            this.currentTexture = this.videoPlayer.getTexture();
            nrs.beginSprite();
            spriteBatch.setColor(this.color.get("r").tofloat(), this.color.get("g").tofloat(),
                    this.color.get("b").tofloat(), this.color.get("a").tofloat());
            float localX = -this.width * this.anchorX;
            float localY = -this.height * this.anchorY;
            
            spriteBatch.draw(
                this.currentTexture, 
                localX, localY, 
                this.width, this.height
            );
        }
    }

    public void stop() {
        if (videoPlayer != null) videoPlayer.stop();
        playing = false;
    }

    public void pause() {
        if (videoPlayer != null) videoPlayer.pause();
        playing = false;
    }

    public void resume() {
        if (videoPlayer != null) videoPlayer.resume();
        playing = true;
    }

    public void setVolume(float val) {
        if (videoPlayer != null) videoPlayer.setVolume(val);
    }

    public float getVolume(){
        return videoPlayer.getVolume();
    }

    public int getCurrentTimestamp(){
        return videoPlayer.getCurrentTimestamp();
    }

}
