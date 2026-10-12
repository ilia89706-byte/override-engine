package com.kakos.override.engine.Core.gameLoader;

import com.kakos.override.engine.Core.Project;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

public class Crypter {

    public static void crypt(String srcProjectPath, String destBuildPath, String gameName, Frame frame){
        File srcDir = new File(srcProjectPath);
        File destDir = new File(destBuildPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        String rawPassword = UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
        char[] secretPassword = rawPassword.toCharArray();
        File finalZip = new File(destDir, "game_build.ovr");
        File keyFile = new File(destDir, "config.dat");
        try {
            byte[] passwordBytes = rawPassword.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < passwordBytes.length; i++) {
                passwordBytes[i] ^= 0x5A;
            }
            Files.write(keyFile.toPath(), passwordBytes);
            ZipFile zipFile = new ZipFile(finalZip, secretPassword);
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            if (srcDir.exists() && srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();
                if (files != null) {
                    String destCanonicalPath = destDir.getCanonicalPath();
                    String finalZipCanonicalPath = finalZip.getCanonicalPath();
                    for (File file : files) {
                        String fileCanonicalPath = file.getCanonicalPath();
                        if (fileCanonicalPath.equals(destCanonicalPath)
                                || fileCanonicalPath.equals(finalZipCanonicalPath)) {
                            System.out.println("[Ovr Build] pass: " + file.getName());
                            continue;
                        }
                        if (file.isDirectory()) {
                            zipFile.addFolder(file, zipParameters);
                        } else {
                            zipFile.addFile(file, zipParameters);
                        }
                    }
                }
            } else {
                throw new FileNotFoundException("Project folder not found: " + srcProjectPath);
            }
            JOptionPane.showMessageDialog(frame, "Great!", "", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error:\n" + e.getMessage(), "Error build", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(secretPassword, '0');
        }
    }

    public static void encrypt(){
        try {
            char[] engineKey = loadEngineKey();

            File ovrFile = new File("game_build.ovr");

            System.out.println("OVR size: " + ovrFile.length() + " byte");
            System.out.println("Key length: " + engineKey.length);

            File cacheDir = new File(System.getProperty("java.io.tmpdir"),
                    "override_cache_" + System.currentTimeMillis());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            cacheDir.deleteOnExit();

            ZipFile zipFile = new ZipFile(ovrFile, engineKey);

            try {
                zipFile.extractAll(cacheDir.getAbsolutePath());
            } catch (net.lingala.zip4j.exception.ZipException ze) {
                throw new IOException(
                        "Не удалось распаковать .ovr архив. Возможная причина: неверный ключ (config.dat) или файл поврежден.",
                        ze);
            }

            Arrays.fill(engineKey, '0');


            Project.init(cacheDir.getAbsolutePath());
            Project.start(cacheDir.getAbsolutePath());
            System.out.println("Great, start project: " + Project.projectRootPath);



        } catch (Exception e) {
            System.err.println("[Ovr] Runtime error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static char[] loadEngineKey() throws IOException {
        File keyFile = new File("config.dat");
        if (!keyFile.exists()) {
            throw new java.io.FileNotFoundException("[Ovr] file config.dat not found");
        }

        byte[] passwordBytes = Files.readAllBytes(keyFile.toPath());
        for (int i = 0; i < passwordBytes.length; i++) {
            passwordBytes[i] = (byte) (passwordBytes[i] ^ 0x5A);
        }

        String decryptedPassword = new String(passwordBytes, StandardCharsets.UTF_8);
        return decryptedPassword.toCharArray();
    }
}
