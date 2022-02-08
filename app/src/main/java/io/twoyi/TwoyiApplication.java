/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author weishu
 * @date 2020/12/24.
 */

public class TwoyiApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // XCrash.init(this, new XCrash.InitParameters().setNativeRethrow(false));

        ensureDir(new File(base.getDataDir(), "rootfs/dev/input"));
        ensureDir(new File(base.getDataDir(), "rootfs/dev/socket"));
        ensureDir(new File(base.getDataDir(), "socket"));

        // detele boot_completed flag!
        new File(base.getDataDir(), "rootfs/boot_completed").delete();

//        Display display = getDisplay();
//        DisplayMetrics dm = new DisplayMetrics();
//        display.getRealMetrics(dm);
//
//
//        System.out.println("display, width: " + display.getWidth());
//        System.out.println("display, heigth: " + display.getHeight());
//        System.out.println("real, width: " + dm.widthPixels);
//        System.out.println("real, heigth: " + dm.heightPixels);
//        System.out.println("xdpi: " + dm.xdpi);
//        System.out.println("ydpi: " + dm.ydpi);
//        System.out.println("density: " + dm.density);
//        System.out.println("densityDpi: " + dm.densityDpi);
//        System.out.println("statusBar: " + px2dp(getStatusBarHeight(this)));

        TwoyiSocketServer.getInstance(base).start();
    }



    private static void ensureDir(File file) {
        if (file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();
    }

    static int statusBarHeight = -1;

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight != -1) {
            return statusBarHeight;
        }

        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resId);
        }

        if (statusBarHeight < 0) {
            int result = 0;
            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object obj = clazz.newInstance();
                Field field = clazz.getField("status_bar_height");
                int resourceId = Integer.parseInt(field.get(obj).toString());
                result = context.getResources().getDimensionPixelSize(resourceId);
            } catch (Exception e) {
            } finally {
                statusBarHeight = result;
            }
        }

        //Use 25dp if no status bar height found
        if (statusBarHeight < 0) {
            statusBarHeight = dip2px(context, 25);
        }
        return statusBarHeight;
    }

    private static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (dpValue * scale + 0.5f);
        return px;
    }

    public static float px2dp(float pxValue) {
        return (pxValue / Resources.getSystem().getDisplayMetrics().density);
    }
}
