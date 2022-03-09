/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.android.AndroidDeferredManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.twoyi.R;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * @author weishu
 * @date 2018/7/21.
 */
public class UIHelper {
    private static final AndroidDeferredManager gDM = new AndroidDeferredManager();

    public static ExecutorService GLOBAL_EXECUTOR = Executors.newCachedThreadPool();

    public static AndroidDeferredManager defer() {
        return gDM;
    }

    public static void dismiss(Dialog dialog) {
        if (dialog == null) {
            return;
        }

        try {
            dialog.dismiss();
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    public static void openWeiXin(Context context, String weixin) {
        try {
            // 获取剪贴板管理服务
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null) {
                return;
            }
            cm.setText(weixin);

            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);

            context.startActivity(intent);
            Toast.makeText(context, R.string.wechat_public_account_tips, Toast.LENGTH_LONG).show();
        } catch (Throwable e) {
            Toast.makeText(context, "WeChat is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void show(Dialog dialog) {
        if (dialog == null) {
            return;
        }

        try {
            dialog.show();
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    public static AlertDialog.Builder getDialogBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
        builder.setIcon(R.mipmap.ic_launcher);
        return builder;
    }

    public static AlertDialog.Builder getWebViewBuilder(Context context, String title, String url) {
        AlertDialog.Builder dialogBuilder = getDialogBuilder(context);
        if (!TextUtils.isEmpty(title)) {
            dialogBuilder.setTitle(title);
        }
        WebView webView = new WebView(context);
        webView.loadUrl(url);
        dialogBuilder.setView(webView);
        dialogBuilder.setPositiveButton(R.string.i_know_it, null);
        return dialogBuilder;
    }

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(R.string.progress_dialog_title);
        return dialog;
    }

    public static MaterialDialog getNumberProgressDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.progress_dialog_title)
                .iconRes(R.mipmap.ic_launcher)
                .progress(false, 0, true)
                .build();
    }

    public static void showDonateDialog(Activity activity) {
        if (activity == null) {
            return;
        }

        final String alipay = activity.getResources().getString(R.string.donate_alipay);
        final String donateOthers = activity.getResources().getString(R.string.donate_others);
        final String[] items = {alipay, "Paypal", donateOthers};

        AlertDialog chooseDialog = getDialogBuilder(activity)
                .setTitle(R.string.donate_choose_title)
                .setItems(items, (dialog1, which1) -> {
                    dialog1.dismiss();

                    if (which1 == 0) {
                        if (!AlipayZeroSdk.hasInstalledAlipayClient(activity)) {
                            Toast.makeText(activity.getApplicationContext(), R.string.prompt_alipay_not_found, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AlipayZeroSdk.startAlipayClient(activity, "FKX016770URBZGZSR37U37");
                    } else if (which1 == 1) {
                        try {
                            Intent t1 = new Intent(Intent.ACTION_VIEW);
                            t1.setData(Uri.parse("https://paypal.me/virtualxposed"));
                            activity.startActivity(t1);
                        } catch (Throwable ignored) {
                            ignored.printStackTrace();
                        }
                    } else if (which1 == 2) {
                        try {
                            Intent t = new Intent(Intent.ACTION_VIEW);
                            t.setData(Uri.parse("https://twoyi.app/guide/sponsor.html"));
                            activity.startActivity(t);
                        } catch (Throwable ignored) {
                        }
                    }
                })
                .create();

        show(chooseDialog);
    }

    public static void showPrivacy(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setData(Uri.parse("https://twoyi.app/privacy"));
            context.startActivity(intent);
        } catch (Throwable ignored) {
        }
    }

    public static void showFAQ(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setData(Uri.parse("https://twoyi.app/guide"));
            context.startActivity(intent);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    public static void goWebsite(Context context) {
        visitSite(context, "https://twoyi.app");
    }

    public static void goTelegram(Context context) {
        visitSite(context, "https://t.me/twoyi");
    }

    public static void visitSite(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Throwable ignored) {
        }
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static List<ApplicationInfo> getInstalledApplications(PackageManager packageManager) {
        if (packageManager == null) {
            return Collections.emptyList();
        }

        @SuppressLint("WrongConstant")
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
                | PackageManager.GET_DISABLED_COMPONENTS);
        int userApp = 0;
        for (ApplicationInfo installedApplication : installedApplications) {
            if ((installedApplication.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                if (userApp++ > 3) {
                    return installedApplications;
                }
            }
        }

        List<ApplicationInfo> applicationInfos = new ArrayList<>();
        for (int uid = 0; uid <= Process.LAST_APPLICATION_UID; uid++) {
            String[] packagesForUid = packageManager.getPackagesForUid(uid);
            if (packagesForUid == null || packagesForUid.length == 0) {
                continue;
            }
            for (String pkg : packagesForUid) {
                try {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkg, 0);
                    applicationInfos.add(applicationInfo);
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }
        }

        return applicationInfos;
    }

    public static String toModuleScope(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int size = scopes.size();

        int i = 0;
        for (String scope : scopes) {
            sb.append(scope);

            if (i++ < size - 1) {
                sb.append(',');
            }

        }
        return sb.toString();
    }

    public static void shareText(Context context, @StringRes int shareTitle, String extraText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(shareTitle));
        intent.putExtra(Intent.EXTRA_TEXT, extraText);//extraText为文本的内容
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为Activity新建一个任务栈
        context.startActivity(
                Intent.createChooser(intent, context.getString(shareTitle)));
    }

    public static String paste(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) {
            return null;
        }
        boolean hasPrimaryClip = manager.hasPrimaryClip();
        if (!hasPrimaryClip) {
            return null;
        }
        ClipData primaryClip = manager.getPrimaryClip();
        if (primaryClip == null) {
            return null;
        }
        if (primaryClip.getItemCount() <= 0) {
            return null;
        }
        CharSequence addedText = primaryClip.getItemAt(0).getText();
        return String.valueOf(addedText);
    }

    public static void startActivity(Context context, Class<?> clazz) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent(context, clazz);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            context.startActivity(intent);
        } catch (Throwable ignored) {
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isVM64(Set<String> supportedABIs) {
        if (Build.SUPPORTED_64_BIT_ABIS.length == 0) {
            return false;
        }

        if (supportedABIs == null || supportedABIs.isEmpty()) {
            return true;
        }

        for (String supportedAbi : supportedABIs) {
            if ("arm64-v8a".endsWith(supportedAbi) || "x86_64".equals(supportedAbi) || "mips64".equals(supportedAbi)) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> getABIsFromApk(String apk) {
        try (ZipFile apkFile = new ZipFile(apk)) {
            Enumeration<? extends ZipEntry> entries = apkFile.entries();
            Set<String> supportedABIs = new HashSet<String>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("../")) {
                    continue;
                }
                if (name.startsWith("lib/") && !entry.isDirectory() && name.endsWith(".so")) {
                    String supportedAbi = name.substring(name.indexOf("/") + 1, name.lastIndexOf("/"));
                    supportedABIs.add(supportedAbi);
                }
            }
            return supportedABIs;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isApk64(String apk) {
        long start = SystemClock.elapsedRealtime();
        Set<String> abIsFromApk = getABIsFromApk(apk);
        return isVM64(abIsFromApk);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    public static boolean isAppSupport64bit(ApplicationInfo info) {
        try {
            // fast path, the isApk64 is too heavy!
            Field primaryCpuAbiField = ApplicationInfo.class.getDeclaredField("primaryCpuAbi");
            String primaryCpuAbi = (String) primaryCpuAbiField.get(info);
            if (primaryCpuAbi == null) {
                // no native libs, support!
                return true;
            }

            return Arrays.asList("arm64-v8a", "x86_64").contains(primaryCpuAbi.toLowerCase());
        } catch (Throwable e) {
            return isApk64(info.sourceDir);
        }
    }
}
