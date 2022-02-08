/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;

/**
 * @author weishu
 * @date 2022/1/28.
 */

public class NavUtils {

    public interface OnNavigationStateListener {
        void onNavigationState(boolean shown, int height);
    }

    public static void isNavigationBarExist(Activity activity, final OnNavigationStateListener onNavigationStateListener) {
        if (activity == null) {
            return;
        }
        final int height = getNavigationHeight(activity);

        activity.getWindow().getDecorView().setOnApplyWindowInsetsListener((v, windowInsets) -> {
            boolean isShowing = false;
            int b = 0;
            if (windowInsets != null) {
                b = windowInsets.getSystemWindowInsetBottom();
                isShowing = (b == height);
            }
            if (onNavigationStateListener != null && b <= height) {
                onNavigationStateListener.onNavigationState(isShowing, b);
            }
            return windowInsets;
        });
    }

    public static int getNavigationHeight(Context activity) {
        if (activity == null) {
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        int height = 0;
        if (resourceId > 0) {
            //获取NavigationBar的高度
            height = resources.getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static void hideNavigation(Window window) {
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
