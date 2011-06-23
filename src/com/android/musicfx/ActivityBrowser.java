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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ActivityBrowser extends Activity {

    private final static String TAG = "MusicFXActivityBrowser";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String urlString = intent.getDataString();
        try {
            final WebView webView = new WebView(this);
            final URL url = new URL(urlString);
            new LoadURLTask(webView).execute(url);
        } catch (final MalformedURLException e) {
            Log.e(TAG, "MalformedURLException " + urlString);
        }
    }

    /**
     * ASync Task that checks if the site is reachable and then loads the URL.
     */
    private class LoadURLTask extends AsyncTask<URL, Void, Boolean> {
        final WebView mWebView;
        String mURLString;

        public LoadURLTask(final WebView webView) {
            mWebView = webView;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Boolean doInBackground(final URL... urls) {

            final URL url = urls[0];
            mURLString = url.toString();

            boolean isSiteReachable = false;
            final HttpURLConnection httpURLConnection;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                final int response = httpURLConnection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    isSiteReachable = true;
                }
            } catch (final IOException e) {
                Log.e(TAG, "Error connecting to " + mURLString);
            }
            return isSiteReachable;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(final Boolean isReachable) {
            if (isReachable) {
                mWebView.setBackgroundColor(Color.BLACK);
                setContentView(mWebView);
                mWebView.loadUrl(mURLString);
            } else {
                createAndShowNetworkDialog();
            }
        }
    }

    /**
     * Shows the network dialog alerting the user that the net is down.
     */
    private void createAndShowNetworkDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.browser_error_dialog_title)
                .setMessage(R.string.browser_error_dialog_text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        finish();
                    }
                }).show();
    }
}
