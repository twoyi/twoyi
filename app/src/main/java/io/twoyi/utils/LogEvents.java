/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.content.Context;

import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, String> properties = new HashMap<String, String>();
        RomUtil.RomInfo info = RomUtil.getCurrentRomInfo(context);

        properties.put("rom_ver", String.valueOf(info.code));
        properties.put("rom_author", info.author);
        properties.put("rom_md5", info.md5);


        List<ErrorAttachmentLog> errors = new ArrayList<>();

        File initLogFile = new File(context.getFilesDir().getParentFile(), "log.txt");
        errors.add(ErrorAttachmentLog.attachmentWithText(IOUtils.readContent(initLogFile), "dmesg.txt"));
        File logcatFile = getLogcatFile(context);
        errors.add(ErrorAttachmentLog.attachmentWithText(IOUtils.readContent(logcatFile), "logcat.txt"));

        trackError(BOOT_FAILURE, properties, errors);
    }

    public static File getLogcatFile(Context context) {
        return new File(context.getFilesDir().getParentFile(), "logcat.txt");
    }

}
