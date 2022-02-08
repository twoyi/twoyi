/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * @author weishu
 * @date 2019/4/17.
 */
public class CacheManager {

    private static final byte[] LABEL_LOCK = new byte[0];

    private static ACache sLabelCache;

    public static ACache getLabelCache(Context context) {
        synchronized (LABEL_LOCK) {
            if (sLabelCache != null) {
                return sLabelCache;
            }
            Context appContext = context.getApplicationContext();
            if (appContext == null) {
                appContext = context;
            }

            sLabelCache = ACache.get(appContext, "labelCache");
            return sLabelCache;
        }
    }

    public static String getLabel(Context context, ApplicationInfo info, PackageManager pm) {
        PackageManager packageManager;
        if (pm != null) {
            packageManager = pm;
        } else {
            packageManager = context.getPackageManager();
        }

        if (info == null) {
            return null;
        }

        String key = info.packageName;
        ACache labelCache = getLabelCache(context);
        synchronized (LABEL_LOCK) {
            String label = labelCache.getAsString(key);
            if (label == null) {
                // 缓存没有，那么直接读
                String label1 = info.loadLabel(packageManager).toString();
                labelCache.put(key, label1);
                return label1;
            }
            return label;
        }
    }
}
