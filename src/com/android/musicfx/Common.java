/*
 * Copyright (C) 2010-2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.musicfx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

class Common {

    final static int DIALOG_ABOUT = 1000;

    static String mVersionName;

    static public void handleMenuItem(final Activity context, final MenuItem item) {
        if (item.getTitle().equals(context.getString(R.string.menu_about))) {
            context.showDialog(DIALOG_ABOUT);
        }

        if (item.getTitle().equals(context.getString(R.string.menu_help))) {
            final Intent browserIntent = new Intent(null, Uri.parse(context
                    .getString(R.string.url_help)));
            browserIntent.setClass(context, ActivityBrowser.class);
            context.startActivity(browserIntent);
        }

        if (item.getTitle().equals(context.getString(R.string.menu_feedback))) {
            PackageInfo packageInfo;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                        0);
                mVersionName = packageInfo.versionName;
            } catch (final NameNotFoundException e) {
                mVersionName = context.getString(R.string.about_version_error);
            }
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[] { context.getString(R.string.feedback_email_address) });
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    context.getString(R.string.feedback_email_subject) + " " + mVersionName);
            context.startActivity(Intent.createChooser(emailIntent,
                    context.getString(R.string.feedback_email_title)));
        }

        if (item.getTitle().equals(context.getString(R.string.menu_developers))) {
            final Intent browserIntent = new Intent(null, Uri.parse(context
                    .getString(R.string.url_developers)));
            browserIntent.setClass(context, ActivityBrowser.class);
            context.startActivity(browserIntent);
        }
    }

    static public AlertDialog.Builder createDialog(final Activity activity) {
        PackageInfo packageInfo;
        try {
            packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            mVersionName = packageInfo.versionName;
        } catch (final NameNotFoundException e) {
            mVersionName = activity.getString(R.string.about_version_error);
        }

        final LayoutInflater factory = LayoutInflater.from(activity);
        final View aboutView = factory.inflate(R.layout.about, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_menu_info_details).setTitle(R.string.about_title)
                .setView(aboutView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        /* User clicked OK so do some stuff */
                    }
                });

        final TextView versionTextView = (TextView) aboutView
                .findViewById(R.id.TextViewAboutVersion);
        versionTextView.setText(activity.getString(R.string.about_version) + " " + mVersionName);

        return builder;
    }
}
