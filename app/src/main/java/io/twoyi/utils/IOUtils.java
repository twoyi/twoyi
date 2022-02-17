/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.text.TextUtils;

import androidx.annotation.Keep;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * @author weishu
 * @date 2018/8/28.
 */
@Keep
public class IOUtils {

    public static void ensureCreated(File file) {
        if (!file.exists()) {
            boolean ret = file.mkdirs();
            if (!ret) {
                throw new RuntimeException("create dir: " + file + " failed");
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        boolean success = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean ret = deleteDir(new File(dir, file));
                if (!ret) {
                    success = false;
                }
            }
            if (success) {
                // if all subdirectory are deleted, delete the dir itself.
                return dir.delete();
            }
        }
        return dir.delete();
    }

    public static void deleteAll(List<File> files) {
        if (files.isEmpty()) {
            return;
        }

        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);
            FileChannel iChannel = inputStream.getChannel();
            FileChannel oChannel = outputStream.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                int r = iChannel.read(buffer);
                if (r == -1)
                    break;
                buffer.limit(buffer.position());
                buffer.position(0);
                oChannel.write(buffer);
            }
        } finally {
            closeSilently(inputStream);
            closeSilently(outputStream);
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public static void setPermissions(String path, int mode, int uid, int gid) {
        try {
            Class<?> fileUtilsClass = Class.forName("android.os.FileUtils");
            Method setPermissions = fileUtilsClass.getDeclaredMethod("setPermissions", String.class, int.class, int.class, int.class);
            setPermissions.setAccessible(true);
            setPermissions.invoke(null, path, mode, uid, gid);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void writeContent(File file, String content) {
        if (file == null || TextUtils.isEmpty(content)) {
            return;
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
        } catch (Throwable ignored) {
        } finally {
            IOUtils.closeSilently(fileWriter);
        }
    }

    public static String readContent(File file) {
        if (file == null) {
            return null;
        }
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString().trim();
        } catch (Throwable ignored) {
            return null;
        } finally {
            IOUtils.closeSilently(fileReader);
        }
    }

    public static boolean deleteDirectory(File directory) {
        try {
            Files.walk(directory.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
