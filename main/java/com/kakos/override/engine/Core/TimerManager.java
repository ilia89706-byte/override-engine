package com.kakos.override.engine.Core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

class Timer {
    public float currentTime = 0.0f;
    public float endTime = 0.0f;
    public LuaValue listener;
    public boolean isLoop = false;
    public boolean paused = false;
    public boolean closed = false;

    Timer(float endTime, boolean isLoop, LuaValue listener) {
        this.endTime = endTime;
        this.isLoop = isLoop;
        this.listener = listener;
    }

    public void callListener() {
        if (this.listener.isfunction()) {
            listener.call(CoerceJavaToLua.coerce(this));
        }
    }

    public void pause() { this.paused = true; }
    public void resume() { this.paused = false; }
    public void stop() { this.closed = true; }

    public void update(float dt) {
        if (closed || paused) return; 

        if (this.currentTime < this.endTime) {
            currentTime += dt;
        } else {
            if (isLoop) {
                callListener();
                currentTime = Math.max(0.0f, currentTime + dt - endTime); 
            } else {
                callListener();
                currentTime = 0.0f;
                closed = true;
            }
        }
    }
}

public class TimerManager {
    public static final List<Timer> timers = new CopyOnWriteArrayList<>();

    public static Timer create(float endTime, boolean isLoop, LuaValue listener) {
        Timer timer = new Timer(endTime, isLoop, listener);
        timers.add(timer);
        return timer;
    }

    public static void updateAll(float dt) {
        if (timers.isEmpty()) return;

        for (Timer timer : timers) {
            if (timer.closed) {
                timers.remove(timer);
                continue;
            }
            timer.update(dt);
        }
    }

    public static void clearAll() {
        timers.clear();
    }
}
