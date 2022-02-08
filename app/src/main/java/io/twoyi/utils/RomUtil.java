/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import com.hzy.libp7zip.P7ZipApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * @author weishu
 * @date 2021/10/22.
 */

public class RomUtil {

    public static boolean romExist(Context context) {
        File initFile = new File(new File(context.getDataDir(), "rootfs"), "init");
        return initFile.exists();
    }

    public static void extractRootfs(Context context) {

        if (romExist(context)) {
            return;
        }

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

        P7ZipApi.executeCommand(String.format(Locale.US, "7z x -mmt=%d '%s' '-o%s'", Runtime.getRuntime().availableProcessors(), rootfs7z, context.getDataDir()));

        long t3 = SystemClock.elapsedRealtime();

        System.out.println("extract rootfs, read assets: " + (t2 - t1) + " un7z: " + (t3 - t2));
    }

    public static boolean isAndroid12() {
        return Build.VERSION.PREVIEW_SDK_INT + Build.VERSION.SDK_INT == Build.VERSION_CODES.S;
    }
}
