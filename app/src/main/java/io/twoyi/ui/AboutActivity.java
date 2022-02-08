/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD2ClauseLicense;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense3;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import io.twoyi.R;
import io.twoyi.utils.UIHelper;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * @author weishu
 * @date 2018/9/14.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutPage mPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addItem(getCopyRightsElement())
                .addItem(getAuthorElement())
                .addItem(getVersionElement())
                .addItem(getCheckUpdateElement())
                .addItem(getWeChatPublicNumberElement())
                .addItem(getWebsiteElement())
                .addItem(getFaqElement())
                .addItem(getFeedbackEmailElement())
                .addItem(getFeedbackTelegramElement())
                .addItem(getLicenseElement())
                .addItem(getPrivacyElement());

        mPage.setDescription(getResources().getString(R.string.app_name));

        View aboutView = mPage.create();
        setContentView(aboutView);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.colorPrimary));
            actionBar.setTitle(R.string.about_btn);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setGravity(Gravity.START);
        return copyRightsElement;
    }

    Element getVersionElement() {
        Element version = new Element();
        String versionName = "unknown";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        version.setTitle(versionName);

        return version;
    }

    Element getFaqElement() {
        Element element = new Element();
        element.setTitle(getResources().getString(R.string.help_text));
        element.setOnClickListener(v -> UIHelper.showFAQ(AboutActivity.this));
        return element;
    }

    Element getCheckUpdateElement() {
        Element checkUpdate = new Element();
        checkUpdate.setTitle(getResources().getString(R.string.check_update));
        checkUpdate.setOnClickListener(v -> {
            // TODO: checkUpdate
            // UpdateUtil.checkUpdateImmediately(getApplicationContext(), true);
        });
        return checkUpdate;
    }

    Element getWeChatPublicNumberElement() {
        Element element = new Element();
        element.setTitle(getResources().getString(R.string.about_follow_gongzhonghao));
        element.setOnClickListener(v -> {
            UIHelper.openWeiXin(AboutActivity.this, "虚拟框架");
        });
        return element;
    }

    Element getAuthorElement() {
        Element author = new Element();
        author.setTitle(getResources().getString(R.string.about_author));
        author.setOnClickListener(v -> {
            String[] contacts = new String[]{
                    getResources().getString(R.string.about_author_github),
                    getResources().getString(R.string.about_author_zhihu),
                    getResources().getString(R.string.about_author_xda),
                    getResources().getString(R.string.about_author_coolapk),
                    getResources().getString(R.string.about_author_email),
            };

            String[] urls = new String[]{"https://github.com/tiann",
                    "https://www.zhihu.com/people/tian-weishu",
                    "https://forum.xda-developers.com/member.php?u=8994560",
                    "http://www.coolapk.com/u/1257513",
                    "mailto:twsxtd@gmail.com",
            };

            AlertDialog alertDialog = UIHelper.getDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.about_author_contact)
                    .setItems(contacts, (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(urls[which]));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Throwable ignored) {
                        }
                    })
                    .create();
            UIHelper.show(alertDialog);
        });
        return author;
    }

    Element getFeedbackTelegramElement() {
        Element telegramGroup = new Element();
        telegramGroup.setTitle(getResources().getString(R.string.about_telegram_group));
        telegramGroup.setOnClickListener(v -> {
            UIHelper.goTelegram(this);
        });
        return telegramGroup;
    }

    Element getWebsiteElement() {
        Element website = new Element();
        website.setTitle(getResources().getString(R.string.about_page_website));
        website.setOnClickListener(v -> UIHelper.goWebsite(this));
        return website;
    }

    Element getPrivacyElement() {
        Element website = new Element();
        website.setTitle(getResources().getString(R.string.privacy));
        website.setOnClickListener(v -> UIHelper.showPrivacy(this));
        return website;
    }

    Element getFeedbackEmailElement() {
        Element emailElement = new Element();
        final String email = "twsxtd@gmail.com";
        String title = getResources().getString(R.string.about_feedback_title);
        emailElement.setTitle(title);

        emailElement.setOnClickListener(v -> {
            AlertDialog alertDialog = UIHelper.getDialogBuilder(AboutActivity.this)
                    .setTitle(R.string.about_feedback_title)
                    .setMessage(R.string.feedback_confirm)
                    .setPositiveButton(R.string.feedback_ok, (dialog, which) -> {
                        Uri uri = Uri.parse("mailto:" + email);
                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                        intent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题
                    }).setNegativeButton(R.string.read_faq_text, (dialog, which) -> {
                        UIHelper.showFAQ(this);
                    })
                    .create();
            UIHelper.show(alertDialog);
        });
        return emailElement;
    }


    Element getLicenseElement() {
        Element license = new Element();
        license.setTitle(getResources().getString(R.string.notices_title));
        Notices notices = new Notices();
        notices.addNotice(new Notice("AndroidP7Zip", "https://github.com/hzy3774/AndroidP7zip",
                "7-Zip Copyright (C) 1999-2020 Igor Pavlov.",new GnuLesserGeneralPublicLicense3()));
        notices.addNotice(new Notice("termux-adb-fastboot", "https://github.com/rendiix/termux-adb-fastboot",
                "Copyright (c) 2022 rendiix", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("libsu", "https://github.com/topjohnwu/libsu", "topjohnwu", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("qemu", "https://android.googlesource.com/platform/external/qemu", "qemu", new GnuGeneralPublicLicense20()));
        notices.addNotice(new Notice("LicenseDialog", "https://github.com/PSDev/LicensesDialog",
                "Copyright 2013 Philip Schiffer", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("material-dialogs", "https://github.com/afollestad/material-dialogs",
                "Aidan Follestad (@afollestad)", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("FloatingActionButton", "https://github.com/Clans/FloatingActionButton",
                "Clans", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("android-about-page", "https://github.com/medyo/android-about-page",
                "Copyright (c) 2016 Mehdi Sakout", new MITLicense()));
        notices.addNotice(new Notice("AlipayZeroSdk", "https://github.com/fython/AlipayZeroSdk",
                "Copyright 2016 Fung Go (fython)", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Glide", "https://github.com/bumptech/glide",
                "Copyright 2014 Google, Inc.", new BSD2ClauseLicense()));
        notices.addNotice(new Notice("Once", "https://github.com/jonfinerty/Once",
                "Copyright 2018 Jon Finerty", new ApacheSoftwareLicense20()));

        license.setOnClickListener(v -> {
            LicensesDialog licensesDialog = new LicensesDialog.Builder(AboutActivity.this)
                    .setThemeResourceId(R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                    .setNotices(notices)
                    .build();
            try {
                licensesDialog.show();
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }

        });
        return license;
    }


}
