package com.kakos.override.engine.luaAPI;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import com.badlogic.gdx.Gdx;
import com.kakos.override.engine.Core.Project;

public class Window {
    public static int width = 0;
    public static int height = 0;

    public static void init(int w, int h) {
        width = w;
        height = h;
    }

    public static int getWidth() {
        return Project.windowWidth;
    }

    public static int getHeight() {
        return Project.windowHeight;
    }

    public static void setSize(int width, int height) {
        Gdx.graphics.setWindowedMode(width, height);
        Project.windowWidth = width; 
        Project.windowHeight = height;
    }

    public static void setUndecorated(boolean flag) {
        Gdx.graphics.setUndecorated(flag);
    }

    public static String openColorChooser(String title, String defaultHex) {
        java.awt.Color defaultColor = java.awt.Color.WHITE;
        if (defaultHex != null && !defaultHex.isEmpty()) {
            try {
                defaultColor = java.awt.Color.decode(defaultHex);
            } catch (NumberFormatException ignored) {}
        }

        java.awt.Color selectedColor = JColorChooser.showDialog(null, title, defaultColor);

        if (selectedColor != null) {
            return String.format("#%02X%02X%02X", selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
        }
        return "";
    }

    public static boolean openConfirmDialog(String title, String message) {
        int response = JOptionPane.showConfirmDialog(
            null, 
            message, 
            title, 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public static String openInputDialog(String title, String message, String defaultValue) {
        String input = JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
        if (input != null) {
            return input;
        }
        return "";
    }
}
