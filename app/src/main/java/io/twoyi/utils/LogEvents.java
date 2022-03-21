/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
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

    public static void trackError(Throwable e) {
        Crashes.trackError(e);
    }
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

    public static File getKmsgFile(Context context) {
        return new File(context.getDataDir(), "log.txt");
    }

    public static File getLastKmsgFile(Context context) {
        return new File(context.getDataDir(), "last_kmsg.txt");
    }

    private static class ReportItem {
        File file;
        String entry;

        public static ReportItem create(File file, String entry) {
            ReportItem item = new ReportItem();
            item.file = file;
            item.entry = entry;
            return item;
        }

        public static ReportItem create(File file) {
            return create(file, file.getName());
        }
    }

    public static byte[] getBugreport(Context context) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(baos);

        // file, entry
        List<ReportItem> reportItems = new ArrayList<>();

        // current kmsg log
        File initLogFile = getKmsgFile(context);
        reportItems.add(ReportItem.create(initLogFile, "kmsg.txt"));

        // last kmsg log
        File lastKmsgFile = getLastKmsgFile(context);
        reportItems.add(ReportItem.create(lastKmsgFile));

        // logcat
        File logcatFile = getLogcatFile(context);
        ProcessBuilder logcat = new ProcessBuilder("logcat", "-d");
        logcat.redirectOutput(logcatFile);
        try {
            Process process = logcat.start();
            process.waitFor();
        } catch (Throwable ignored) {}

        reportItems.add(ReportItem.create(logcatFile));

        // tombstones
        File rootfsDir = RomManager.getRootfsDir(context);
        File romDataDir = new File(rootfsDir, "data");
        File tombstoneDir = new File(romDataDir, "tombstones");
        File[] tombstones = tombstoneDir.listFiles();
        if (tombstones != null) {
            for (File tombstone : tombstones) {
                reportItems.add(ReportItem.create(tombstone, "tombstones/" + tombstone.getName()));
            }
        }

        // dropboxs
        File dataSystemDir = new File(romDataDir, "system");
        File dropboxDir = new File(dataSystemDir, "dropbox");
        File[] dropboxs = dropboxDir.listFiles();
        if (dropboxs != null) {
            for (File dropbox : dropboxs) {
                reportItems.add(ReportItem.create(dropbox, "dropbox/" + dropbox.getName()));
            }
        }

        // proc info
        File procInfo = new File(context.getCacheDir(), "proc.txt");
        ProcessBuilder pb = new ProcessBuilder("ps", "-ef");
        pb.redirectOutput(procInfo);
        try {
            Process process = pb.start();
            process.waitFor();
            reportItems.add(ReportItem.create(procInfo));
        } catch (Throwable ignored) {
        }

        // build.prop
        File buildInfo = new File(context.getCacheDir(), "basic.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(buildInfo))) {
            pw.println("BRAND: " + Build.BRAND);
            pw.println("MODEL: " + Build.MODEL);
            pw.println("PRODUCT: " + Build.PRODUCT);
            pw.println("MANUFACTURER: " + Build.MANUFACTURER);
            pw.println("SDK: " + Build.VERSION.SDK_INT);
            pw.println("PREVIEW_SDK: " + Build.VERSION.PREVIEW_SDK_INT);
            pw.println("FINGERPRINT: " + Build.FINGERPRINT);
            pw.println("DEVICE: " + Build.DEVICE);

            RomManager.RomInfo romInfo = RomManager.getCurrentRomInfo(context);
            pw.println("ROM: " + romInfo.version);
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            pw.println("PACKAGE: " + packageInfo.packageName);
            pw.println("VERSION: " + packageInfo.versionName);
        } catch (Throwable ignored) {}

        reportItems.add(ReportItem.create(buildInfo));

        for (ReportItem item : reportItems) {
            try {
                ZipEntry ze = new ZipEntry(item.entry);
                zout.putNextEntry(ze);

                byte[] bytes = Files.readAllBytes(item.file.toPath());
                zout.write(bytes, 0, bytes.length);

                zout.closeEntry();
            } catch (IOException ignored) {
            }
        }
        IOUtils.closeSilently(zout);

        return baos.toByteArray();
    }
}
