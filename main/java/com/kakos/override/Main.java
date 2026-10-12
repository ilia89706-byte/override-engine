package com.kakos.override;
import com.kakos.override.engine.Menu;
import com.kakos.override.editor.Console;
import com.kakos.override.engine.Core.Joke;
import com.kakos.override.engine.Core.Project;
import com.kakos.override.engine.Core.gameLoader.Crypter;

public class Main {

    public static boolean isBuilded = false;
    public static boolean debug = false;
    public static String version = "1.0.1";

    public static void main(String[] args) {
        if (!debug) {
            if (!isBuilded) {
                Console.init();
                Console.log(Console.Modifier.OVERRIDE, "Current version: " + version);
                Joke.printJoke();
                Menu m = new Menu();
                m.showMenu();
            } else {
                Crypter.encrypt();
            }
        } else {
            Console.init();
            
            Project.init("/home/ilia/PROJECTS[lx]/OVERRIDE_PROJECT");
            Project.start("/home/ilia/PROJECTS[lx]/OVERRIDE_PROJECT");
        }

    }
}
