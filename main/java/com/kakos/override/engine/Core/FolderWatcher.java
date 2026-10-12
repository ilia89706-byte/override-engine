package com.kakos.override.engine.Core;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.luaj.vm2.lib.jse.JsePlatform;

import com.kakos.override.editor.Console;
import com.kakos.override.engine.luaAPI.SceneManager;

public class FolderWatcher {

    private static long lastEventTime = 0;
    private static final long DEBOUNCE_DELAY_MS = 150;

    private static Thread watcherThread = null;
    private static WatchService watchService = null;
    private static volatile boolean isRunning = false;
    private static final Object LOCK = new Object();

    public static volatile boolean needsReload = false;

    public static void start(String[] args) {
        synchronized (LOCK) {
            if (isRunning)
                return;
            isRunning = true;
        }

        Path dir = Path.of(Project.projectRootPath);

        watcherThread = new Thread(() -> {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (!isRunning)
                            break;

                        Path fileName = (Path) event.context();
                        if (fileName != null && fileName.toString().endsWith(".lua")) {
                            long currentTime = System.currentTimeMillis();

                            if ((currentTime - lastEventTime) > DEBOUNCE_DELAY_MS) {
                                Console.log(Console.Modifier.FILESYSTEM, "Detected file edit ->." + fileName);
                                
                                API.globals = null;
                                API.globals = JsePlatform.standardGlobals();
                                API.genAPI();
                                SceneManager.reloadState();
                                Project.start("main.lua");
                                needsReload = true;
                                lastEventTime = currentTime;
                            }
                        }
                    }

                    if (!key.reset())
                        break;
                }
            } catch (ClosedWatchServiceException | InterruptedException e) {

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isRunning = false;
            }
        }, "FolderWatcher-Thread");

        watcherThread.start();
    }

    public static void reset() {
        synchronized (LOCK) {
            if (!isRunning)
                return;
            isRunning = false;
        }

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
            watchService = null;
        }

        if (watcherThread != null) {
            watcherThread.interrupt();
            try {
                watcherThread.join(100);
            } catch (InterruptedException ignored) {
            }
            watcherThread = null;
        }

        needsReload = false;
        lastEventTime = 0;
        Console.log(Console.Modifier.FILESYSTEM, "Module has been fully reset to initial state.");
    }
}
