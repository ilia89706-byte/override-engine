package com.kakos.override.engine.Core;

import java.util.ArrayList;

public class DisplayManager {
    public static ArrayList<GameObject> gameObjects = new ArrayList<>();
    public static ArrayList<GameObject3D> gameObjects3D = new ArrayList<>();

    public static void addToStack(GameObject obj){
        gameObjects.add(obj);
    }

    public static void removeFromStack(GameObject obj){
        gameObjects.remove(obj);
    }
}
