/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.hzy.libp7zip.P7ZipApi;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * @author weishu
 * @date 2021/10/22.
 */

public class RomUtil {

    private static final String TAG = "RomUtil";

    public static class RomInfo {
        public String author = "unknown";
        public String version = "unknown";
        public String md5 = "";
        public long code = 0;

        @Override
        public String toString() {
            return "RomInfo{" +
                    "author='" + author + '\'' +
                    ", version='" + version + '\'' +
                    ", md5='" + md5 + '\'' +
                    ", code=" + code +
                    '}';
        }
    }

    public static RomInfo DEFAULT_ROM_INFO = new RomInfo();

    public static boolean romExist(Context context) {
        File initFile = new File(getRootfsDir(context), "init");
        return initFile.exists();
    }

    public static boolean needsUpgrade(Context context) {
        RomInfo currentRomInfo = getCurrentRomInfo(context);
        Log.i(TAG, "current rom: " + currentRomInfo);
        if (currentRomInfo.equals(DEFAULT_ROM_INFO)) {
            return true;
        }

        RomInfo romInfoFromAssets = getRomInfoFromAssets(context);
        Log.i(TAG, "asset rom: " + romInfoFromAssets);
        return romInfoFromAssets.code > currentRomInfo.code;
    }

    public static RomInfo getCurrentRomInfo(Context context) {
        File infoFile = new File(getRootfsDir(context), "rom.ini");
        try (FileInputStream inputStream = new FileInputStream(infoFile)) {
            return getRomInfo(inputStream);
        } catch (Throwable e) {
            return DEFAULT_ROM_INFO;
        }
    }

    public static RomInfo getRomInfo(File rom) {
        try (SevenZFile zFile = new SevenZFile(rom)) {

            SevenZArchiveEntry entry;

            while ((entry = zFile.getNextEntry()) != null) {
                if (entry.getName().equals("rootfs/rom.ini")) {
                    byte[] content = new byte[(int) entry.getSize()];
                    zFile.read(content, 0, content.length);
                    ByteArrayInputStream bais = new ByteArrayInputStream(content);
                    return getRomInfo(bais);
                }
            }
        } catch (IOException ignored) {
        }
        return DEFAULT_ROM_INFO;
    }

    public static RomInfo getRomInfoFromAssets(Context context) {
        AssetManager assets = context.getAssets();
        try (InputStream open = assets.open("rom.ini")) {
            return getRomInfo(open);
        } catch (Throwable ignored) {
        }
        return DEFAULT_ROM_INFO;
    }

    public static void extractRootfs(Context context) {

        // force remove system dir to avoiding wired issues
        removeSystemPartition(context);

        // read assets
        long t1 = SystemClock.elapsedRealtime();
        File rootfs7z = context.getFileStreamPath("rootfs.7z");
        try (InputStream inputStream = new BufferedInputStream(context.getAssets().open("rootfs.7z"));
             OutputStream os = new BufferedOutputStream(new FileOutputStream(rootfs7z))) {
            byte[] buffer = new byte[10240];
            int count;
            while ((count = inputStream.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long t2 = SystemClock.elapsedRealtime();

        int ret = P7ZipApi.executeCommand(String.format(Locale.US, "7z x -mmt=%d -aoa '%s' '-o%s'", Runtime.getRuntime().availableProcessors(), rootfs7z, context.getDataDir()));

        long t3 = SystemClock.elapsedRealtime();

        System.out.println("extract rootfs, read assets: " + (t2 - t1) + " un7z: " + (t3 - t2) + "ret: " + ret);
    }

    public static File getRootfsDir(Context context) {
        return new File(context.getDataDir(), "rootfs");
    }

    public static File getRomSdcardDir(Context context) {
        return new File(getRootfsDir(context), "sdcard");
    }

    public static boolean isAndroid12() {
        return Build.VERSION.PREVIEW_SDK_INT + Build.VERSION.SDK_INT == Build.VERSION_CODES.S;
    }

    private static void removeSystemPartition(Context context) {
        File rootfsDir = getRootfsDir(context);
        File systemDir = new File(rootfsDir, "system");

        IOUtils.deleteDirectory(systemDir);
    }

    private static RomInfo getRomInfo(InputStream in) {
        Properties prop = new Properties();
        try {
            prop.load(in);

            RomInfo info = new RomInfo();
            info.author = prop.getProperty("author");
            info.code = Long.parseLong(prop.getProperty("code"));
            info.version = prop.getProperty("version");
            info.md5 = prop.getProperty("md5");
            return info;
        } catch (Throwable e) {
            Log.e(TAG, "read rom info err", e);
            return DEFAULT_ROM_INFO;
        }
    }


}
