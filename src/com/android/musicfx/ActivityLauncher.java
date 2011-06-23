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
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActivityLauncher extends Activity {

    Boolean isStopping;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the currently selected menu XML resource.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        isStopping = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // only show the menu if not stopping
                if (!isStopping) {
                    openOptionsMenu();
                }
            }
        }, 500);
    }

    @Override
    public void onStop() {
        isStopping = true;
        super.onStop();
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder builder = Common.createDialog(this);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {

                /* User clicked OK so do some stuff */
                openOptionsMenu();
            }
        });

        return builder.create();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Common.handleMenuItem(this, item);

        return true;
    }
}
