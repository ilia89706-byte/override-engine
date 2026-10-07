package com.kakos.override.engine.luaAPI;

import com.badlogic.gdx.Gdx;
import com.kakos.override.editor.Console;
import com.kakos.override.engine.Core.Project;
import java.io.File;

public class Sound {
    public static class SoundObj {
        public boolean playing = false;
        public boolean loaded = false;
        public long ID = -1;
        public com.badlogic.gdx.audio.Sound sound;

        public SoundObj(String pathToFile) {
            String absolutePath = Project.projectRootPath + File.separator + pathToFile;
            File soundFile = new File(absolutePath);

            if (soundFile.exists()) {
                try {
                    this.sound = Gdx.audio.newSound(Gdx.files.absolute(absolutePath));
                    this.loaded = true;
                } catch (Exception e) {
                    Console.log(Console.Modifier.FILESYSTEM, e);
                    this.loaded = false;
                }
            } else {
                this.loaded = false;
            }
        }

        public void play(float volume) {
            if (!loaded || sound == null) return;

            ID = sound.play(volume);
            sound.setVolume(ID,1);
            playing = true;
        }

        public void stop() {
            if (!loaded || sound == null) return;

            if (ID != -1) {
                sound.stop(ID);
                ID = -1;
            }
            playing = false;
        }

        public void setVolume(float val) {
            if (!loaded || sound == null || ID == -1) return;
            sound.setVolume(ID, val);
        }
        public void dispose() {
            if (loaded && sound != null) {
                sound.dispose();
                loaded = false;
                playing = false;
            }
        }
    }

    public static SoundObj create(String path){
        return new SoundObj(path);
    }
}
