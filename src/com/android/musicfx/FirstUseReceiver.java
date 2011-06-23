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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.util.Log;

/**
 *
 */
public class FirstUseReceiver extends BroadcastReceiver {

    private final static String TAG = "MusicFXFirstUseReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if ((context == null) || (intent == null)) {
            Log.w(TAG, "Context or intent is null. Do nothing.");
            return;
        }

        final String action = intent.getAction();

        if (action.equals(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)) {
            final String packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME);
            ActivityFirstUse.checkFirstUse(context, packageName);
        }
    }
}
