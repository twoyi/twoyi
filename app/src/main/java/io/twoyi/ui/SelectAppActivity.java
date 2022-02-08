/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.CompoundButtonCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.twoyi.R;
import io.twoyi.utils.AppKV;
import io.twoyi.utils.CacheManager;
import io.twoyi.utils.IOUtils;
import io.twoyi.utils.Installer;
import io.twoyi.utils.UIHelper;
import io.twoyi.utils.image.GlideModule;

/**
 * @author weishu
 * @date 2018/7/21.
 */
public class SelectAppActivity extends AppCompatActivity {

    private static final String TAG = "SelectAppActivity";

    private static final int REQUEST_GET_FILE = 1;

    private static int TAG_KEY = R.id.create_app_list;

    private ListAppAdapter mAdapter;
    private final List<AppItem> mDisplayItems = new ArrayList<>();
    private final List<AppItem> mAllApps = new ArrayList<>();
    private AppItem mSelectItem;
    private TextView mEmptyView;

    private final Set<String> specifiedPackages = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createapp);

        ListView mListView = findViewById(R.id.create_app_list);
        mAdapter = new ListAppAdapter();
        mListView.setAdapter(mAdapter);
        mEmptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(mEmptyView);

        FloatingActionButton mFloatButton = findViewById(R.id.create_app_from_external);
        mFloatButton.setColorNormalResId(R.color.colorPrimary);
        mFloatButton.setColorPressedResId(R.color.colorPrimaryOpacity);

        mFloatButton.setOnClickListener((v) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("application/vnd.android.package-archive"); // apk file
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(intent, REQUEST_GET_FILE);
            } catch (Throwable ignored) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });


        TextView createApp = findViewById(R.id.create_app_btn);
        createApp.setBackgroundResource(R.color.colorPrimary);
        createApp.setText(R.string.select_app_button);
        createApp.setOnClickListener((v) -> {

            Set<AppItem> selectedApps = new HashSet<>();
            for (AppItem displayItem : mDisplayItems) {
                if (displayItem.selected) {
                    selectedApps.add(displayItem);
                }
            }
            if (selectedApps.isEmpty()) {
                Toast.makeText(this, R.string.select_app_tips, Toast.LENGTH_SHORT).show();
                return;
            }

            selectComplete(selectedApps);
        });

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.colorPrimary));
            actionBar.setTitle(R.string.create_app_activity);
        }

        Intent intent = getIntent();
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null && TextUtils.equals(data.getScheme(), "package")) {
                String schemeSpecificPart = data.getSchemeSpecificPart();
                if (schemeSpecificPart != null) {
                    String[] split = schemeSpecificPart.split("\\|");
                    specifiedPackages.clear();
                    specifiedPackages.addAll(Arrays.asList(split));
                }
            }
        }

        if (true) {
            int size = specifiedPackages.size();
            if (size > 1) {
                specifiedPackages.clear();
            }
        }

        loadAsync();

        // UpdateUtil.checkForceUpdate(this);
    }

    private void selectComplete(Set<AppItem> pkgs) {

        if (pkgs.size() != 1) {

            // TODO: support install mutilpe apps together
            Toast.makeText(getApplicationContext(), R.string.please_install_one_by_one, Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = UIHelper.getProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.show();

        for (AppItem pkg : pkgs) {
            List<File> apks = new ArrayList<>();
            ApplicationInfo applicationInfo = pkg.applicationInfo;

            // main apk
            String sourceDir = applicationInfo.sourceDir;
            apks.add(new File(sourceDir));

            String[] splitSourceDirs = applicationInfo.splitSourceDirs;
            if (splitSourceDirs != null) {
                for (String splitSourceDir : splitSourceDirs) {
                    apks.add(new File(splitSourceDir));
                }
            }

            startInstall(apks, progressDialog, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        // 当SearchView获得焦点时弹出软键盘的类型，就是设置输入类型
        searchView.setIconified(false);
        searchView.onActionViewExpanded();

        searchView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        // 设置回车键表示查询操作
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        // 为searchView添加事件
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // 输入后点击回车改变文本
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 随着输入改变文本
            @Override
            public boolean onQueryTextChange(String newText) {
                filterListByText(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            mDisplayItems.clear();
            mDisplayItems.addAll(mAllApps);
            notifyDataSetChangedWithSort();
            return false;
        });

        MenuItem menuItem = setFilterMenuItem(menu, R.id.menu_not_show_system, AppKV.ADD_APP_NOT_SHOW_SYSTEM, false);

        return super.onCreateOptionsMenu(menu);
    }

    private MenuItem setFilterMenuItem(Menu menu, int id, String key, boolean defalutValue) {
        MenuItem menuItem = menu.findItem(id);
        menuItem.setChecked(AppKV.getBooleanConfig(getApplicationContext(), key, defalutValue));

        menuItem.setOnMenuItemClickListener(item -> {
            boolean checked = !item.isChecked();

            item.setChecked(checked);

            AppKV.setBooleanConfig(getApplicationContext(), key, checked);

            // 重新加载所有配置
            // loadAsync 的时候会检查这个标记
            loadAsync();

            return true;
        });
        return menuItem;
    }

    private void filterListByText(String query) {
        if (TextUtils.isEmpty(query)) {
            mDisplayItems.clear();
            mDisplayItems.addAll(mAllApps);
            notifyDataSetChangedWithSort();
            return;
        }

        List<AppItem> newApps = new ArrayList<>();
        Set<CharSequence> pkgs = new HashSet<>();
        for (AppItem mAllApp : mAllApps) {
            pkgs.add(mAllApp.pkg);
        }

        for (AppItem appItem : mAllApps) {
            String name = appItem.name.toString().toLowerCase();
            String pkg = appItem.pkg.toString().toLowerCase();
            String queryLower = query.toLowerCase();
            if (name.contains(queryLower) || pkg.contains(queryLower)) {
                newApps.add(appItem);
            }
            if (appItem.selected && !pkgs.contains(appItem.pkg)) {
                newApps.add(appItem);
            }
        }
        mDisplayItems.clear();
        mDisplayItems.addAll(newApps);
        notifyDataSetChangedWithSort();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(requestCode == REQUEST_GET_FILE && resultCode == Activity.RESULT_OK)) {
            return;
        }

        if (data == null) {
            return;
        }

        ClipData clipData = data.getClipData();
        List<Uri> fileUris = new ArrayList<>();
        if (clipData == null) {
            // single file
            fileUris.add(data.getData());
        } else {
            // multiple file
            int itemCount = clipData.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                fileUris.add(uri);
            }
        }

        if (fileUris.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.select_app_app_not_found, Toast.LENGTH_SHORT).show();
            return;

        }

        ProgressDialog dialog = UIHelper.getProgressDialog(this);
        dialog.setCancelable(false);
        dialog.show();

        // start copy and install
        UIHelper.defer().when(() -> {
            List<File> files = copyFilesFromUri(fileUris);

            Log.i(TAG, "files copied: " + files);

            boolean allValid = Installer.checkFile(getApplicationContext(), files);
            if (!allValid) {
                IOUtils.deleteAll(files);
                throw new RuntimeException("invalid apk file!");
            }

            return files;
        }).done(result -> startInstall(result, dialog, false))
                .fail(result -> runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.install_failed_reason, result.getMessage()), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                }));

    }

    private List<File> copyFilesFromUri(List<Uri> fileUris) {
        List<File> files = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        for (Uri uri : fileUris) {
            String lastPathSegment = uri.getLastPathSegment();
            long now = System.currentTimeMillis();
            File tmpFile = new File(getCacheDir(), now + lastPathSegment + ".apk");
            Log.i(TAG, "tmpFile: " + tmpFile);
            try (InputStream inputStream = contentResolver.openInputStream(uri); OutputStream os = new FileOutputStream(tmpFile)) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = inputStream.read(buffer)) > 0) {
                    os.write(buffer, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            files.add(tmpFile);
        }
        return files;
    }

    private void startInstall(List<File> result, ProgressDialog dialog, boolean cleanFile) {
        Installer.installAsync(getApplicationContext(), result, new Installer.InstallResult() {
            @Override
            public void onSuccess(List<File> files) {
                if (cleanFile) {
                    IOUtils.deleteAll(files);
                }

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), R.string.install_success, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                });
            }

            @Override
            public void onFail(List<File> files, String msg) {
                if (cleanFile) {
                    IOUtils.deleteAll(files);
                }

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.install_failed_reason, msg), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                });
            }
        });
    }

    private void loadAsync() {

        mEmptyView.setText(null);

        mDisplayItems.clear();

        MaterialDialog progressDialog = UIHelper.getNumberProgressDialog(this);
        UIHelper.show(progressDialog);

        // 是否从别的地方过来的，这种情况下不做任何过滤。
        boolean specified = specifiedPackages.size() > 0;
        AtomicBoolean specifiedFound = new AtomicBoolean(false);

        long start = SystemClock.elapsedRealtime();
        UIHelper.defer().when(() -> {
            PackageManager packageManager = getPackageManager();
            if (packageManager == null) {
                return Collections.<AppItem>emptyList();
            }
            List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

            runOnUiThread(() -> progressDialog.setMaxProgress(apps.size()));

            List<AppItem> appItems = new ArrayList<>();
            int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            PackageInfo sys = packageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES);

            boolean directlyAdd = true;

            boolean noSystemApps = AppKV.getBooleanConfig(getApplicationContext(), AppKV.ADD_APP_NOT_SHOW_SYSTEM, false);
            Set<String> allAppPackages = new HashSet<>();

            for (ApplicationInfo app : apps) {
                // 自己忽略
                runOnUiThread(() -> progressDialog.incrementProgress(1));

                if (TextUtils.equals(getPackageName(), app.packageName)) {
                    continue;
                }

                // 检查系统应用
                boolean isSystemApp = (app.flags & mask) != 0 || isSystemApp(packageManager, app.packageName, sys);
                if (isSystemApp) {
                    // 如果是系统应用
                    if (!directlyAdd) {
                        // 非 magisk 模式直接跳过，因为不可能支持
                        continue;
                    }
                    if (noSystemApps && !specified) {
                        // magisk 模式如果设置了 "不显示系统" 也忽略
                        // 如果是从别的地方跳转过来的，那么忽略
                        continue;
                    }
                }

                AppItem appItem = new AppItem();

                appItem.applicationInfo = app;
                appItem.name = CacheManager.getLabel(getApplicationContext(), app, packageManager);
                appItem.pkg = app.packageName;
                appItem.selected = false;
                if (specifiedPackages.contains(app.packageName)) {
                    appItem.selected = true;
                    specifiedFound.set(true);
                }

                appItems.add(appItem);
            }

            if (apps.size() == 0) {
                // 压根没有应用列表，那么说明没有权限
                mEmptyView.setText(R.string.create_app_no_apps);
            } else if (appItems.size() == 0) {
                // 所有APP都被添加完毕了
                mEmptyView.setText(R.string.create_app_all_apps_added);
            }
            return appItems;

        }).done((v) -> {
            mAllApps.clear();
            mAllApps.addAll(v);

            mDisplayItems.clear();
            mDisplayItems.addAll(v);
            notifyDataSetChangedWithSort();

            if (specified && !specifiedFound.get()) {
                // 如果是跳转过来的，但是没有对应的包名对应，那么提示用户。
                Toast.makeText(getApplicationContext(), R.string.select_app_app_not_found, Toast.LENGTH_SHORT).show();
            }
        }).always((d, p, e) -> {
            UIHelper.dismiss(progressDialog);
//            Toast.makeText(getApplicationContext(), "load: +" + (SystemClock.elapsedRealtime() - start) + "ms",
//                    Toast.LENGTH_SHORT).show();
        });
    }

    private void notifyDataSetChangedWithSort() {
        Collections.sort(mDisplayItems, (o1, o2) -> {
            int w1 = o1.selected ? 1 : 0;
            int w2 = o2.selected ? 1 : 0;
            return w2 - w1;
        });

        mAdapter.notifyDataSetChanged();
    }

    private class ListAppAdapter extends BaseAdapter implements View.OnClickListener {

        ColorMatrixColorFilter colorFilter;

        ListAppAdapter() {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            colorFilter = new ColorMatrixColorFilter(matrix);
        }

        @Override
        public int getCount() {
            return mDisplayItems.size();
        }

        @Override
        public AppItem getItem(int position) {
            return mDisplayItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(SelectAppActivity.this, parent);
                convertView = holder.root;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppItem item = getItem(position);

            holder.icon.setColorFilter(colorFilter);

            int[][] states = {{android.R.attr.state_checked}, {}};
            int[] colors = {getResources().getColor(R.color.colorPrimary),
                    getResources().getColor(android.R.color.tab_indicator_text)};
            CompoundButtonCompat.setButtonTintList(holder.checkBox, new ColorStateList(states, colors));

            GlideModule.loadApplicationIcon(getApplicationContext(), item.applicationInfo, holder.icon);
            holder.label.setText(item.name);
            String pkg = String.format(Locale.US, "%s [ API %d %s]", item.pkg, item.applicationInfo.targetSdkVersion,
                    item.applicationInfo.splitPublicSourceDirs != null ? "S" : "");

            holder.pkg.setText(pkg);

            holder.checkBox.setChecked(item.selected);
            holder.checkBox.setTag(TAG_KEY, position);
            holder.root.setTag(TAG_KEY, position);
            holder.root.setOnClickListener(this);
            holder.checkBox.setOnClickListener(this);

            return convertView;
        }

        @Override
        public void onClick(View v) {
            Object tag = v.getTag(TAG_KEY);
            if (!(tag instanceof Integer)) {
                return;
            }
            int position = (int) tag;

            mSelectItem = mDisplayItems.get(position);

            boolean is64Bit = UIHelper.isApk64(mSelectItem.applicationInfo.sourceDir);

            if (!is64Bit) {

                Toast.makeText(getApplicationContext(), R.string.unsupported_for_32bit_app, Toast.LENGTH_SHORT).show();
                return;
            }
            mSelectItem.selected = !mSelectItem.selected;

            notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
        TextView pkg;
        CheckBox checkBox;

        View root;

        ViewHolder(Context context, ViewGroup parent) {
            root = LayoutInflater.from(context).inflate(R.layout.item_create_app, parent, false);
            icon = root.findViewById(R.id.item_app_icon);
            label = root.findViewById(R.id.item_app_name);
            pkg = root.findViewById(R.id.item_app_package);
            checkBox = root.findViewById(R.id.item_checkbox);
        }

    }

    private static class AppItem {
        private ApplicationInfo applicationInfo;
        private CharSequence name;
        private CharSequence pkg;
        private boolean selected;
    }

    /**
     * Match signature of application to identify that if it is signed by system
     * or not.
     *
     * @param packageName package of application. Can not be blank.
     * @return <code>true</code> if application is signed by system certificate,
     * otherwise <code>false</code>
     */
    public static boolean isSystemApp(PackageManager packageManager, String packageName, PackageInfo sys) {
        try {
            // Get packageinfo for target application
            PackageInfo targetPkgInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            // Get packageinfo for system package
            // Match both packageinfo for there signatures
            return (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0]
                    .equals(targetPkgInfo.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isSystemApp(Context context, String packageName) {

        if (context == null || packageName == null) {
            return false;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo sys = packageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES);
            return isSystemApp(packageManager, packageName, sys);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
