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
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AudioEffect.Descriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.audiofx.OpenSLESConstants;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ActivityMusic extends Activity implements OnSeekBarChangeListener {
    private final static String TAG = "MusicFXActivityMusic";

    /**
     * Max number of EQ bands supported
     */
    private final static int EQUALIZER_MAX_BANDS = 32;

    /**
     * Dialog IDS
     */
    static final int DIALOG_EQUALIZER = 0;
    static final int DIALOG_PRESET_REVERB = 1;
    static final int DIALOG_RESET_DEFAULTS = 2;

    /**
     * Indicates if Virtualizer effect is supported.
     */
    private boolean mVirtualizerSupported;
    /**
     * Indicates if BassBoost effect is supported.
     */
    private boolean mBassBoostSupported;
    /**
     * Indicates if Equalizer effect is supported.
     */
    private boolean mEqualizerSupported;
    /**
     * Indicates if Preset Reverb effect is supported.
     */
    private boolean mPresetReverbSupported;

    // Equalizer fields
    private final SeekBar[] mEqualizerSeekBar = new SeekBar[EQUALIZER_MAX_BANDS];
    private final TextView[] mEqualizerValueText = new TextView[EQUALIZER_MAX_BANDS];
    private int mNumberEqualizerBands;
    private int mEqualizerMinBandLevel;
    private int mEQPresetUserPos = 1;
    private View mEqualizerView;
    private int mEQPreset;
    private int mEQPresetPrevious;
    private int[] mEQPresetUserBandLevelsPrev;

    private int mPRPreset;

    private boolean mIsHeadsetOn = false;

    /**
     * Mapping for the EQ widget ids per band
     */
    private static final int[][] EQViewElementIds = {
            { R.id.EQBand0TextView, R.id.EQBand0SeekBar, R.id.EQBand0Value },
            { R.id.EQBand1TextView, R.id.EQBand1SeekBar, R.id.EQBand1Value },
            { R.id.EQBand2TextView, R.id.EQBand2SeekBar, R.id.EQBand2Value },
            { R.id.EQBand3TextView, R.id.EQBand3SeekBar, R.id.EQBand3Value },
            { R.id.EQBand4TextView, R.id.EQBand4SeekBar, R.id.EQBand4Value },
            { R.id.EQBand5TextView, R.id.EQBand5SeekBar, R.id.EQBand5Value },
            { R.id.EQBand6TextView, R.id.EQBand6SeekBar, R.id.EQBand6Value },
            { R.id.EQBand7TextView, R.id.EQBand7SeekBar, R.id.EQBand7Value },
            { R.id.EQBand8TextView, R.id.EQBand8SeekBar, R.id.EQBand8Value },
            { R.id.EQBand9TextView, R.id.EQBand9SeekBar, R.id.EQBand9Value },
            { R.id.EQBand10TextView, R.id.EQBand10SeekBar, R.id.EQBand10Value },
            { R.id.EQBand11TextView, R.id.EQBand11SeekBar, R.id.EQBand11Value },
            { R.id.EQBand12TextView, R.id.EQBand12SeekBar, R.id.EQBand12Value },
            { R.id.EQBand13TextView, R.id.EQBand13SeekBar, R.id.EQBand13Value },
            { R.id.EQBand14TextView, R.id.EQBand14SeekBar, R.id.EQBand14Value },
            { R.id.EQBand15TextView, R.id.EQBand15SeekBar, R.id.EQBand15Value },
            { R.id.EQBand16TextView, R.id.EQBand16SeekBar, R.id.EQBand16Value },
            { R.id.EQBand17TextView, R.id.EQBand17SeekBar, R.id.EQBand17Value },
            { R.id.EQBand18TextView, R.id.EQBand18SeekBar, R.id.EQBand18Value },
            { R.id.EQBand19TextView, R.id.EQBand19SeekBar, R.id.EQBand19Value },
            { R.id.EQBand20TextView, R.id.EQBand20SeekBar, R.id.EQBand20Value },
            { R.id.EQBand21TextView, R.id.EQBand21SeekBar, R.id.EQBand21Value },
            { R.id.EQBand22TextView, R.id.EQBand22SeekBar, R.id.EQBand22Value },
            { R.id.EQBand23TextView, R.id.EQBand23SeekBar, R.id.EQBand23Value },
            { R.id.EQBand24TextView, R.id.EQBand24SeekBar, R.id.EQBand24Value },
            { R.id.EQBand25TextView, R.id.EQBand25SeekBar, R.id.EQBand25Value },
            { R.id.EQBand26TextView, R.id.EQBand26SeekBar, R.id.EQBand26Value },
            { R.id.EQBand27TextView, R.id.EQBand27SeekBar, R.id.EQBand27Value },
            { R.id.EQBand28TextView, R.id.EQBand28SeekBar, R.id.EQBand28Value },
            { R.id.EQBand29TextView, R.id.EQBand29SeekBar, R.id.EQBand29Value },
            { R.id.EQBand30TextView, R.id.EQBand30SeekBar, R.id.EQBand30Value },
            { R.id.EQBand31TextView, R.id.EQBand31SeekBar, R.id.EQBand31Value } };

    // Preset Reverb fields
    /**
     * Array containing the PR preset names.
     */
    private static final String[] PRESETREVERBPRESETSTRINGS = { "None", "SmallRoom", "MediumRoom",
            "LargeRoom", "MediumHall", "LargeHall", "Plate" };

    /**
     * Context field
     */
    private Context mContext;

    /**
     * Calling package name field
     */
    private String mCallingPackageName = "empty";

    /**
     * Audio session field
     */
    private int mAudioSession = AudioEffect.ERROR_BAD_VALUE;

    // Broadcast receiver to handle wired and Bluetooth A2dp headset events
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            final boolean isHeadsetOnPrev = mIsHeadsetOn;
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                mIsHeadsetOn = (intent.getIntExtra("state", 0) == 1)
                        || audioManager.isBluetoothA2dpOn();
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                final int deviceClass = ((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getBluetoothClass()
                        .getDeviceClass();
                if ((deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
                        || (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)) {
                    mIsHeadsetOn = true;
                }
            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                mIsHeadsetOn = audioManager.isBluetoothA2dpOn() || audioManager.isWiredHeadsetOn();
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                final int deviceClass = ((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getBluetoothClass()
                        .getDeviceClass();
                if ((deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
                        || (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)) {
                    mIsHeadsetOn = audioManager.isWiredHeadsetOn();
                }
            }
            if (isHeadsetOnPrev != mIsHeadsetOn) {
                updateUIHeadset();
            }
        }
    };

    /*
     * Declares and initializes all objects and widgets in the layouts and the CheckBox and SeekBar
     * onchange methods on creation.
     *
     * (non-Javadoc)
     *
     * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init context to be used in listeners
        mContext = this;

        // Receive intent
        // get calling intent
        final Intent intent = getIntent();
        mAudioSession = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                AudioEffect.ERROR_BAD_VALUE);
        Log.v(TAG, "audio session: " + mAudioSession);

        mCallingPackageName = getCallingPackage();

        // check for errors
        if (mCallingPackageName == null) {
            Log.e(TAG, "Package name is null");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setResult(RESULT_OK);

        Log.v(TAG, mCallingPackageName + " (" + mAudioSession + ")");

        ControlPanelEffect.initEffectsPreferences(mContext, mCallingPackageName, mAudioSession);

        // query available effects
        final Descriptor[] effects = AudioEffect.queryEffects();

        // Determine available/supported effects
        Log.v(TAG, "Available effects:");
        for (final Descriptor effect : effects) {
            Log.v(TAG, effect.name.toString() + ", type: " + effect.type.toString());

            if (effect.type.equals(AudioEffect.EFFECT_TYPE_VIRTUALIZER)) {
                mVirtualizerSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_BASS_BOOST)) {
                mBassBoostSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_EQUALIZER)) {
                mEqualizerSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_PRESET_REVERB)) {
                mPresetReverbSupported = true;
            }
        }

        setContentView(R.layout.music_main);
        final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.contentSoundEffects);
        final View mainToggleView = findViewById(R.id.mainToggleEffectsLayout);

        // Watch for button clicks and initialization.
        if ((mVirtualizerSupported) || (mBassBoostSupported) || (mEqualizerSupported)
                || (mPresetReverbSupported)) {
            // Set the listener for the main enhancements toggle button.
            // Depending on the state enable the supported effects if they were
            // checked in the setup tab.
            final CheckBox toggleEffects = (CheckBox) findViewById(R.id.mainToggleEffectsCheckBox);
            toggleEffects.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView,
                        final boolean isChecked) {

                    // set parameter and state
                    ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                            mAudioSession, ControlPanelEffect.Key.global_enabled, isChecked);
                    // Enable Linear layout (in scroll layout) view with all
                    // effect contents depending on checked state
                    setEnabledAllChilds(viewGroup, isChecked);
                    // update UI according to headset state
                    updateUIHeadset();
                }
            });

            mainToggleView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    toggleEffects.toggle();
                }
            });
            ((LinearLayout) findViewById(R.id.mainToggleEffectsLayout)).setVisibility(View.VISIBLE);

            // Initialize the Virtualizer elements.
            // Set the SeekBar listener.
            if (mVirtualizerSupported) {
                // Show msg when disabled slider (layout) is touched
                findViewById(R.id.vILayout).setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(final View v, final MotionEvent event) {
                        showHeadsetMsg();
                        return false;
                    }
                });

                final SeekBar seekbar = (SeekBar) findViewById(R.id.vIStrengthSeekBar);
                seekbar.setMax(OpenSLESConstants.VIRTUALIZER_MAX_STRENGTH
                        - OpenSLESConstants.VIRTUALIZER_MIN_STRENGTH);

                seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    // Update the parameters while SeekBar changes and set the
                    // effect parameter.

                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress,
                            final boolean fromUser) {
                        // set parameter and state
                        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.virt_strength, progress);
                    }

                    // If slider pos was 0 when starting re-enable effect
                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {
                        if (seekBar.getProgress() == 0) {
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.virt_enabled, true);
                        }
                    }

                    // If slider pos = 0 when stopping disable effect
                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {
                        if (seekBar.getProgress() == 0) {
                            // disable
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.virt_enabled, false);
                        }
                    }
                });
            }

            // Initialize the Bass Boost elements.
            // Set the SeekBar listener.
            if (mBassBoostSupported) {
                // Show msg when disabled slider (layout) is touched
                findViewById(R.id.bBLayout).setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(final View v, final MotionEvent event) {
                        showHeadsetMsg();
                        return false;
                    }
                });

                final SeekBar seekbar = (SeekBar) findViewById(R.id.bBStrengthSeekBar);
                seekbar.setMax(OpenSLESConstants.BASSBOOST_MAX_STRENGTH
                        - OpenSLESConstants.BASSBOOST_MIN_STRENGTH);

                seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    // Update the parameters while SeekBar changes and set the
                    // effect parameter.

                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress,
                            final boolean fromUser) {
                        // set parameter and state
                        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.bb_strength, progress);
                    }

                    // If slider pos was 0 when starting re-enable effect
                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {
                        if (seekBar.getProgress() == 0) {
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.bb_enabled, true);
                        }
                    }

                    // If slider pos = 0 when stopping disable effect
                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {
                        if (seekBar.getProgress() == 0) {
                            // disable
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.bb_enabled, false);
                        }

                    }
                });
            }

            // Initialize the Equalizer elements.
            if (mEqualizerSupported) {
                final View view = findViewById(R.id.eqLayout);
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        showDialog(DIALOG_EQUALIZER);
                    }
                });
            }

            // Initialize the Preset Reverb elements.
            // Set Spinner listeners.
            if (mPresetReverbSupported) {
                final View view = findViewById(R.id.eRLayout);
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        showDialog(DIALOG_PRESET_REVERB);
                    }
                });
            }

            // init reset defaults
            final View view = findViewById(R.id.resetDefaultsLayout);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    showDialog(DIALOG_RESET_DEFAULTS);
                }
            });
        } else {
            viewGroup.setVisibility(View.GONE);
            mainToggleView.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.noEffectsTextView)).setVisibility(View.VISIBLE);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        if ((mVirtualizerSupported) || (mBassBoostSupported) || (mEqualizerSupported)
                || (mPresetReverbSupported)) {
            // Listen for broadcast intents that might affect the onscreen UI for headset.
            final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(mReceiver, intentFilter);

            // Check if wired or Bluetooth headset is connected/on
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mIsHeadsetOn = (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn());
            Log.v(TAG, "onResume: mIsHeadsetOn : " + mIsHeadsetOn);

            // Update UI
            updateUI();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister for broadcast intents. (These affect the visible UI,
        // so we only care about them while we're in the foreground.)
        unregisterReceiver(mReceiver);
    }

    /*
     * Create dialogs for about, EQ preset control, PR and reset to default (alert) dialogs
     *
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog alertDialog;
        switch (id) {
        case Common.DIALOG_ABOUT: {
            return Common.createDialog(this).create();
        }
        case DIALOG_EQUALIZER: {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.eq_dialog_title);
            builder.setSingleChoiceItems(getEQPresetStrings(), -1,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int item) {
                            if (item != mEQPresetPrevious) {
                                equalizerSetPreset(item);
                                final ListView listView = ((AlertDialog) dialog).getListView();
                                // For the user preset, where EQ sliders need to be shown at the
                                // bottom of the list when selected, the footer view of a list is
                                // used to display them in.
                                // This footer view will then be added or removed from the list
                                // depending on whether user preset is selected or not.
                                // Using transcript mode to scroll to bottom when in user
                                if (!isEqualizerUserPreset(item)) {
                                    listView.removeFooterView(mEqualizerView);
                                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                                } else {
                                    listView.addFooterView(mEqualizerView);
                                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                }
                            }
                            mEQPresetPrevious = item;
                        }
                    });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int whichButton) {
                    final ListView listView = ((AlertDialog) dialog).getListView();
                    final int newPreset = listView.getCheckedItemPosition();
                    equalizerSetPreset(newPreset);
                    ((TextView) findViewById(R.id.eqPresetsTitleTextView))
                            .setText(getString(R.string.eq_title) + " "
                                    + listView.getItemAtPosition(newPreset).toString());
                }
            });
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int whichButton) {
                            dialog.cancel();
                        }
                    });
            builder.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface dialog) {
                    if (!isEqualizerUserPreset(mEQPreset)) {
                        final ListView listView = ((AlertDialog) dialog).getListView();
                        listView.removeFooterView(mEqualizerView);
                    }
                    equalizerSetPreset(mEQPreset);
                    final int[] presetUserBandLevels = ControlPanelEffect.getParameterIntArray(
                            mContext, mCallingPackageName, mAudioSession,
                            ControlPanelEffect.Key.eq_preset_user_band_level);
                    short band = 0;
                    for (final int bandLevel : mEQPresetUserBandLevelsPrev) {
                        if (bandLevel != presetUserBandLevels[band]) {
                            if (!isEqualizerUserPreset(mEQPreset)) {
                                ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                        mAudioSession,
                                        ControlPanelEffect.Key.eq_preset_user_band_level,
                                        bandLevel, band);
                            } else {
                                equalizerBandUpdate(band, (short) bandLevel);
                            }
                        }
                        band++;
                    }
                }
            });

            alertDialog = builder.create();
            final LayoutInflater factory = LayoutInflater.from(this);
            mEqualizerView = factory.inflate(R.layout.music_eq, null);
            equalizerInit();
            final ListView listView = alertDialog.getListView();
            // Add empty footer view
            listView.addFooterView(mEqualizerView);

            mEQPresetUserPos = getEQPresetStrings().length - 1;
            break;
        }
        case DIALOG_PRESET_REVERB: {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.pr_dialog_title);
            builder.setSingleChoiceItems(PRESETREVERBPRESETSTRINGS, -1,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int item) {
                            presetReverbSetPreset(item);
                        }
                    });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int whichButton) {
                    final ListView listView = ((AlertDialog) dialog).getListView();
                    final int newPreset = listView.getCheckedItemPosition();
                    presetReverbSetPreset(newPreset);
                    ((TextView) findViewById(R.id.eRPresetsTitleTextView))
                            .setText(getString(R.string.pr_title) + " "
                                    + listView.getItemAtPosition(newPreset).toString());
                }
            });
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int whichButton) {
                            dialog.cancel();
                        }
                    });
            builder.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface dialog) {
                    presetReverbSetPreset(mPRPreset);
                }
            });

            alertDialog = builder.create();
            break;
        }
        case DIALOG_RESET_DEFAULTS: {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.reset_defaults_dialog_title);
            builder.setMessage(getString(R.string.reset_defaults_dialog_message));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int whichButton) {
                    ControlPanelEffect.setEffectDefaults(mContext, mCallingPackageName,
                            mAudioSession);
                    updateUI();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
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

    /*
     * Updates dialog (selections) before they are shown if necessary
     *
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog, android.os.Bundle)
     */
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
        switch (id) {
        case Common.DIALOG_ABOUT: {
            break;
        }
        case DIALOG_EQUALIZER: {
            mEQPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                    mAudioSession, ControlPanelEffect.Key.eq_current_preset);
            mEQPresetPrevious = mEQPreset;
            mEQPresetUserBandLevelsPrev = ControlPanelEffect.getParameterIntArray(mContext,
                    mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.eq_preset_user_band_level);
            final ListView listView = ((AlertDialog) dialog).getListView();
            listView.setItemChecked(mEQPreset, true);
            listView.setSelection(mEQPreset);

            if (isEqualizerUserPreset(mEQPreset)) {
                if (listView.getFooterViewsCount() == 0) {
                    listView.addFooterView(mEqualizerView);
                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                }
                equalizerUpdateDisplay();
            } else {
                // FIXME: because of a probable bug in Android removeFooterView, we need to catch
                // NPE which is sometimes thrown from inside removeFooterView (encountered only in
                // Honeycomb).
                // Should ideally be be avoided otherwise.
                try {
                    listView.removeFooterView(mEqualizerView);
                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                } catch (final NullPointerException e) {
                    Log.w(TAG, "onPrepareDialog: DIALOG_EQUALIZER: removeFooterView: " + e);
                }
            }

            break;
        }
        case DIALOG_PRESET_REVERB: {
            mPRPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                    mAudioSession, ControlPanelEffect.Key.pr_current_preset);
            final ListView listView = ((AlertDialog) dialog).getListView();
            listView.setItemChecked(mPRPreset, true);
            listView.setSelection(mPRPreset);
            break;
        }
        case DIALOG_RESET_DEFAULTS: {
            break;
        }
        default:
            Log.e(TAG, "onPrepareDialog invalid Dialog id: " + id);
            break;
        }
    }

    /**
     * En/disables all childs for a given view. For linear and relative layout childs do this
     * recursively
     *
     * @param view
     * @param enabled
     */
    private void setEnabledAllChilds(final ViewGroup viewGroup, final boolean enabled) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = viewGroup.getChildAt(i);
            if ((view instanceof LinearLayout) || (view instanceof RelativeLayout)) {
                final ViewGroup vg = (ViewGroup) view;
                setEnabledAllChilds(vg, enabled);
            }
            view.setEnabled(enabled);
        }
    }

    /**
     * Updates UI (checkbox, seekbars, enabled states) according to the current stored preferences.
     */
    private void updateUI() {
        final boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
        ((CheckBox) findViewById(R.id.mainToggleEffectsCheckBox)).setChecked(isEnabled);
        setEnabledAllChilds((ViewGroup) findViewById(R.id.contentSoundEffects), isEnabled);
        updateUIHeadset();

        if (mVirtualizerSupported) {
            ((SeekBar) findViewById(R.id.vIStrengthSeekBar)).setProgress(ControlPanelEffect
                    .getParameterInt(mContext, mCallingPackageName, mAudioSession,
                            ControlPanelEffect.Key.virt_strength));
        }
        if (mBassBoostSupported) {
            ((SeekBar) findViewById(R.id.bBStrengthSeekBar)).setProgress(ControlPanelEffect
                    .getParameterInt(mContext, mCallingPackageName, mAudioSession,
                            ControlPanelEffect.Key.bb_strength));
        }
        if (mEqualizerSupported) {
            ((TextView) findViewById(R.id.eqPresetsTitleTextView))
                    .setText(getString(R.string.eq_title)
                            + " "
                            + getEQPresetStrings()[ControlPanelEffect.getParameterInt(mContext,
                                    mCallingPackageName, mAudioSession,
                                    ControlPanelEffect.Key.eq_current_preset)]);
        }
        if (mPresetReverbSupported) {
            ((TextView) findViewById(R.id.eRPresetsTitleTextView))
                    .setText(getString(R.string.pr_title)
                            + " "
                            + PRESETREVERBPRESETSTRINGS[ControlPanelEffect.getParameterInt(
                                    mContext, mCallingPackageName, mAudioSession,
                                    ControlPanelEffect.Key.pr_current_preset)]);
        }
    }

    /**
     * Updates UI for headset mode. En/disable VI and BB controls depending on headset state
     * (on/off) if effects are on. Do the inverse for their layouts so they can take over
     * control/events.
     */
    private void updateUIHeadset() {
        if (((CheckBox) findViewById(R.id.mainToggleEffectsCheckBox)).isChecked()) {
            ((TextView) findViewById(R.id.vIStrengthText)).setEnabled(mIsHeadsetOn);
            ((SeekBar) findViewById(R.id.vIStrengthSeekBar)).setEnabled(mIsHeadsetOn);
            findViewById(R.id.vILayout).setEnabled(!mIsHeadsetOn);
            ((TextView) findViewById(R.id.bBStrengthText)).setEnabled(mIsHeadsetOn);
            ((SeekBar) findViewById(R.id.bBStrengthSeekBar)).setEnabled(mIsHeadsetOn);
            findViewById(R.id.bBLayout).setEnabled(!mIsHeadsetOn);
        }
    }

    /**
     * Initializes the equalizer elements. Set the SeekBars and Spinner listeners.
     */
    private void equalizerInit() {
        // Initialize the N-Band Equalizer elements.
        mNumberEqualizerBands = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_bands);
        mEQPresetUserBandLevelsPrev = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_preset_user_band_level);
        final int[] centerFreqs = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_center_freq);
        final int[] bandLevelRange = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_level_range);
        mEqualizerMinBandLevel = bandLevelRange[0];
        final int mEqualizerMaxBandLevel = bandLevelRange[1];

        for (int band = 0; band < mNumberEqualizerBands; band++) {
            // Unit conversion from mHz to Hz and use k prefix if necessary to display
            final int centerFreq = centerFreqs[band] / 1000;
            float centerFreqHz = centerFreq;
            String unitPrefix = "";
            if (centerFreqHz >= 1000) {
                centerFreqHz = centerFreqHz / 1000;
                unitPrefix = "k";
            }
            ((TextView) mEqualizerView.findViewById(EQViewElementIds[band][0])).setText(String
                    .format("%.0f ", centerFreqHz) + unitPrefix + "Hz");
            mEqualizerSeekBar[band] = (SeekBar) mEqualizerView
                    .findViewById(EQViewElementIds[band][1]);
            mEqualizerSeekBar[band].setMax(mEqualizerMaxBandLevel - mEqualizerMinBandLevel);
            mEqualizerValueText[band] = (TextView) mEqualizerView
                    .findViewById(EQViewElementIds[band][2]);
            mEqualizerSeekBar[band].setOnSeekBarChangeListener(this);
        }

        // Hide the inactive Equalizer bands.
        for (int band = mNumberEqualizerBands; band < EQUALIZER_MAX_BANDS; band++) {
            // CenterFreq text
            mEqualizerView.findViewById(EQViewElementIds[band][0]).setVisibility(View.GONE);
            // SeekBar
            mEqualizerView.findViewById(EQViewElementIds[band][1]).setVisibility(View.GONE);
            // Value text
            mEqualizerView.findViewById(EQViewElementIds[band][2]).setVisibility(View.GONE);
        }
    }

    /*
     * For the EQ Band SeekBars
     *
     * (non-Javadoc)
     *
     * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android
     * .widget.SeekBar, int, boolean)
     */

    @Override
    public void onProgressChanged(final SeekBar seekbar, final int progress, final boolean fromUser) {
        final int id = seekbar.getId();

        for (short band = 0; band < mNumberEqualizerBands; band++) {
            if (id == EQViewElementIds[band][1]) {
                final short level = (short) (progress + mEqualizerMinBandLevel);
                equalizerBandUpdateDisplay(band, level);
                if (fromUser) {
                    equalizerBandUpdate(band, level);
                }
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android
     * .widget.SeekBar)
     */

    @Override
    public void onStartTrackingTouch(final SeekBar seekbar) {
        // Do nothing
    }

    /*
     * Updates the EQ display when the user stops changing.
     *
     * (non-Javadoc)
     *
     * @see android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android
     * .widget.SeekBar)
     */

    @Override
    public void onStopTrackingTouch(final SeekBar seekbar) {
        equalizerUpdateDisplay();
    }

    /**
     * Updates the EQ by getting the parameters.
     */
    private void equalizerUpdateDisplay() {
        // Update and show the active N-Band Equalizer bands.
        final int[] bandLevels = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_band_level);
        for (short band = 0; band < mNumberEqualizerBands; band++) {
            final int level = bandLevels[band];
            equalizerBandUpdateDisplay(band, (short) level);
            final int progress = level - mEqualizerMinBandLevel;
            mEqualizerSeekBar[band].setProgress(progress);
        }
    }

    /**
     * Updates/sets a given EQ band level.
     *
     * @param band
     *            Band id
     * @param level
     *            EQ band level
     */
    private void equalizerBandUpdate(final short band, final short level) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_band_level, level, band);
    }

    /**
     * Updates the EQ band level display.
     *
     * @param band
     *            Band id
     * @param level
     *            EQ band level
     */
    private void equalizerBandUpdateDisplay(final short band, final short level) {
        final int dBValue = level / 100;
        mEqualizerValueText[band].setText(String.format("%d dB", dBValue));
    }

    /**
     * Sets the given EQ preset.
     *
     * @param preset
     *            EQ preset id.
     */
    private void equalizerSetPreset(final short preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_current_preset, preset);
        if (isEqualizerUserPreset(preset)) {
            equalizerUpdateDisplay();
        }
    }

    /**
     * Sets the given EQ preset.
     *
     * @param preset
     *            EQ preset id.
     */
    private void equalizerSetPreset(final int preset) {
        equalizerSetPreset((short) preset);
    }

    /**
     * Checks if an User EQ preset is set.
     */
    private boolean isEqualizerUserPreset(final int preset) {
        return (preset == mEQPresetUserPos);
    }

    /**
     * Gets the EQ preset names
     *
     * @return
     */
    private final String[] getEQPresetStrings() {
        final int numPresets = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_presets);
        // Fill array with presets from AudioEffects call.
        // allocate a space for 2 extra strings (CI Extreme & User)
        final String[] eQViewPresetStrings = new String[numPresets + 2];
        for (short i = 0; i < numPresets; i++) {
            eQViewPresetStrings[i] = ControlPanelEffect.getParameterString(mContext,
                    mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_preset_name, i);
        }

        eQViewPresetStrings[numPresets] = getString(R.string.ci_extreme);
        eQViewPresetStrings[numPresets + 1] = getString(R.string.user);
        return eQViewPresetStrings;
    }

    /**
     * Sets the given PR preset.
     *
     * @param preset
     *            PR preset id.
     */
    private void presetReverbSetPreset(final short preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.pr_current_preset, preset);
    }

    /**
     * Sets the given PR preset.
     *
     * @param preset
     *            PR preset id.
     */
    private void presetReverbSetPreset(final int preset) {
        presetReverbSetPreset((short) preset);
    }

    /**
     * Show msg that headset needs to be plugged.
     */
    private void showHeadsetMsg() {
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        final Toast toast = Toast.makeText(context, getString(R.string.headset_plug), duration);
        toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
    }

    /*
     * Creates the options menu
     *
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the currently selected menu XML resource.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Common.handleMenuItem(this, item);
        return true;
    }
}
