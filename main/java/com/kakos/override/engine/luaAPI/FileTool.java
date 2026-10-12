package com.kakos.override.engine.luaAPI;

import com.kakos.override.engine.Core.Project;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.kakos.override.editor.Console;

public class FileTool {

    public static String getDir(String dirType) {
        try {
            Path targetPath = switch (dirType.toLowerCase().trim()) {
                case "project" -> Path.of(Project.projectRootPath);

                case "temporary", "temponary" -> {
                    String winTmp = System.getenv("TEMP");
                    yield (winTmp != null) ? Path.of(winTmp) : Path.of(System.getProperty("java.io.tmpdir"));
                }

                case "sandbox" -> {
                    String projectDirName = Project.windowTitle.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
                    if (projectDirName.isEmpty()) {
                        projectDirName = "DefaultProject";
                    }

                    String appData = System.getenv("APPDATA");
                    if (appData != null) {
                        yield Path.of(appData, projectDirName, "sandbox");
                    } else {
                        yield Path.of(System.getProperty("user.home"), "." + projectDirName.toLowerCase(), "sandbox");
                    }
                }

                default -> throw new IllegalArgumentException("Unknown directory type: " + dirType);
            };

            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
                Console.log(Console.Modifier.FILESYSTEM, "Created system directory: " + targetPath.toAbsolutePath());
            }

            String pathStr = targetPath.toAbsolutePath().toString();
            if (!pathStr.endsWith(File.separator)) {
                pathStr += File.separator;
            }

            return pathStr;
        } catch (Exception e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to resolve directory '" + dirType + "': " + e.getMessage());
            return "";
        }
    }

    public static String read(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to read file: " + filePath + " | " + e.getMessage());
            return "";
        }
    }

    public static void write(String filePath, String content) {
        try {
            Files.writeString(Path.of(filePath), content);
            Console.log(Console.Modifier.FILESYSTEM, "File written successfully: " + filePath);
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to write file: " + filePath + " | " + e.getMessage());
        }
    }

    public static boolean exists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    public static boolean delete(String filePath) {
        try {
            boolean deleted = Files.deleteIfExists(Path.of(filePath));
            if (deleted) {
                Console.logWarn(Console.Modifier.FILESYSTEM, "File deleted: " + filePath);
            }
            return deleted;
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to delete file: " + filePath + " | " + e.getMessage());
            return false;
        }
    }

    public static void createDir(String dirPath) {
        try {
            Files.createDirectories(Path.of(dirPath));
            Console.log(Console.Modifier.FILESYSTEM, "Directory created: " + dirPath);
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to create directory: " + dirPath + " | " + e.getMessage());
        }
    }

    public static boolean deleteRecursive(String path) {
        Path target = Path.of(path);
        if (!Files.exists(target)) {
            return false;
        }
        try (Stream<Path> walk = Files.walk(target)) {
            Console.logWarn(Console.Modifier.FILESYSTEM, "Starting recursive deletion of: " + path);
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            Console.logWarn(Console.Modifier.FILESYSTEM, "Recursive deletion finished for: " + path);
            return true;
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed recursive deletion on: " + path + " | " + e.getMessage());
            return false;
        }
    }

    public static LuaTable list(String dirPath) {
        LuaTable table = LuaValue.tableOf();
        try (Stream<Path> stream = Files.list(Path.of(dirPath))) {
            Path[] paths = stream.toArray(Path[]::new);
            for (int i = 0; i < paths.length; i++) {
                table.set(i + 1, LuaValue.valueOf(paths[i].toString()));
            }
            return table;
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to list directory contents: " + dirPath + " | " + e.getMessage());
            return table;
        }
    }

    public static boolean isDirectory(String path) {
        return Files.isDirectory(Path.of(path));
    }

    public static long getFileSize(String filePath) {
        try {
            return Files.size(Path.of(filePath));
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to get file size for: " + filePath + " | " + e.getMessage());
            return -1;
        }
    }

    public static LuaTable getMetadata(String path) {
        LuaTable meta = LuaValue.tableOf();
        try {
            BasicFileAttributes attrs = Files.readAttributes(Path.of(path), BasicFileAttributes.class);
            meta.set("size", LuaValue.valueOf(attrs.size()));
            meta.set("creationTime", LuaValue.valueOf(attrs.creationTime().toMillis()));
            meta.set("lastModifiedTime", LuaValue.valueOf(attrs.lastModifiedTime().toMillis()));
            meta.set("isDirectory", LuaValue.valueOf(attrs.isDirectory()));
            meta.set("isRegularFile", LuaValue.valueOf(attrs.isRegularFile()));
            return meta;
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to read metadata for: " + path + " | " + e.getMessage());
            return meta;
        }
    }

    public static void copy(String source, String target) {
        try {
            Files.copy(Path.of(source), Path.of(target), StandardCopyOption.REPLACE_EXISTING);
            Console.log(Console.Modifier.FILESYSTEM, "File copied from '" + source + "' to '" + target + "'");
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to copy file from '" + source + "' to '" + target + "' | " + e.getMessage());
        }
    }

    public static void move(String source, String target) {
        try {
            Files.move(Path.of(source), Path.of(target), StandardCopyOption.REPLACE_EXISTING);
            Console.log(Console.Modifier.FILESYSTEM, "File moved from '" + source + "' to '" + target + "'");
        } catch (IOException e) {
            Console.logError(Console.Modifier.FILESYSTEM, "Failed to move file from '" + source + "' to '" + target + "' | " + e.getMessage());
        }
    }


    public static String openFileDialog(String title, String defaultPath, String filterDescription, String extensions) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        
        if (defaultPath != null && !defaultPath.isEmpty()) {
            chooser.setCurrentDirectory(new File(defaultPath));
        } else {
            chooser.setCurrentDirectory(new File("."));
        }

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (extensions != null && !extensions.isEmpty()) {
            String cleanExts = extensions.replace("*.", "").replace(" ", "");
            String[] extArray = cleanExts.split(",");
            
            FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDescription, extArray);
            chooser.setFileFilter(filter);
        }

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return ""; 
    }

    public static String openFolderDialog(String title, String defaultPath) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        
        if (defaultPath != null && !defaultPath.isEmpty()) {
            chooser.setCurrentDirectory(new File(defaultPath));
        } else {
            chooser.setCurrentDirectory(new File("."));
        }

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
return ""; 
}
}