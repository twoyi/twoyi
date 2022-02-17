/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.content.Context;

import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author weishu
 * @date 2022/2/16.
 */

public class LogEvents {

    private static final RuntimeException BOOT_FAILURE = new RuntimeException("BootFailureException");

    public static void trackError(Throwable e, Map<String, String> properties, Iterable<ErrorAttachmentLog> attachments) {
        Crashes.trackError(e, properties, attachments);
    }

    public static void trackBootFailure(Context context) {

        Map<String, String> properties = new HashMap<>();
        RomManager.RomInfo info = RomManager.getCurrentRomInfo(context);

        properties.put("rom_ver", String.valueOf(info.code));
        properties.put("rom_author", info.author);
        properties.put("rom_md5", info.md5);

        List<ErrorAttachmentLog> errors = new ArrayList<>();

        errors.add(ErrorAttachmentLog.attachmentWithBinary(getBugreport(context), "bugreport.zip", "application/zip"));

        trackError(BOOT_FAILURE, properties, errors);
    }

    public static File getLogcatFile(Context context) {
        return new File(context.getCacheDir(), "logcat.txt");
    }

    public static byte[] getBugreport(Context context) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(baos);

        List<File> reportFiles = new ArrayList<>();

        // init log
        File initLogFile = new File(context.getDataDir(), "log.txt");
        reportFiles.add(initLogFile);

        // logcat
        File logcatFile = getLogcatFile(context);
        reportFiles.add(logcatFile);

        // tombstones
        File rootfsDir = RomManager.getRootfsDir(context);
        File romDataDir = new File(rootfsDir, "data");
        File tombstoneDir = new File(romDataDir, "tombstones");
        File[] tombstones = tombstoneDir.listFiles();
        if (tombstones != null) {
            reportFiles.addAll(Arrays.asList(tombstones));
        }

        // dropboxs
        File dataSystemDir = new File(romDataDir, "system");
        File dropboxDir = new File(dataSystemDir, "dropbox");
        File[] dropboxs = dropboxDir.listFiles();
        if (dropboxs != null) {
            reportFiles.addAll(Arrays.asList(dropboxs));
        }

        for (File f : reportFiles) {
            try {
                ZipEntry ze = new ZipEntry(f.getName());
                zout.putNextEntry(ze);

                byte[] bytes = Files.readAllBytes(f.toPath());
                zout.write(bytes, 0, bytes.length);

                zout.closeEntry();
            } catch (IOException ignored) {
            }
        }
        IOUtils.closeSilently(zout);

        return baos.toByteArray();
    }
}
