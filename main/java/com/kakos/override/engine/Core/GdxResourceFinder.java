package com.kakos.override.engine.Core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.luaj.vm2.lib.ResourceFinder;
import java.io.InputStream;
import java.io.File;

public class GdxResourceFinder implements ResourceFinder {
    @Override
    public InputStream findResource(String filename) {
        if (filename == null) return null;

                String cleanPath = filename.replace('.', '/');
        if (cleanPath.startsWith("@")) {
            cleanPath = cleanPath.substring(1);
        }

                String root = Project.projectRootPath;
        if (root == null) {
            root = "";
        }
        
                if (!root.isEmpty() && !root.endsWith(File.separator)) {
            root += File.separator;
        }

        String absolutePath = root + cleanPath;

                FileHandle file = Gdx.files.absolute(absolutePath);
        
                if (!file.exists()) {
            file = Gdx.files.absolute(absolutePath + ".lua");
        }

                if (file.exists()) {
            return file.read();
        }

                return null;
    }
}
