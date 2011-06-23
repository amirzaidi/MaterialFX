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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 *
 */
public class ActivityFirstUse extends Activity {

    private final static String TAG = "MusicFXActivityFirstUse";

    /**
     * Dialog IDS
     */
    static final int DIALOG_FIRST_USE = 1;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialog(DIALOG_FIRST_USE);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        AlertDialog alertDialog;
        switch (id) {
        case DIALOG_FIRST_USE: {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.first_use_dialog_message));
            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    finish();
                }
            });
            alertDialog = builder.create();
            break;
        }
        default:
            Log.e(TAG, "onCreateDialog invalid Dialog id: " + id);
            alertDialog = null;
            break;
        }
        return alertDialog;
    }

    static void checkFirstUse(final Context context, final String packageName) {
        final SharedPreferences prefs = context.getSharedPreferences(packageName, MODE_PRIVATE);
        final String prefsKey = context.getString(R.string.first_use_dailog_shown_key);
        if (!prefs.getBoolean(prefsKey, false)) {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(context, ActivityFirstUse.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(prefsKey, true);
            editor.commit();
        }
    }
}
