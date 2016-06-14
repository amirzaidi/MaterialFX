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

import com.android.audiofx.OpenSLESConstants;
import com.android.musicfx.widget.Gallery;
import com.android.musicfx.widget.InterceptableLinearLayout;
import com.android.musicfx.widget.Knob;
import com.android.musicfx.widget.Knob.OnKnobChangeListener;
import com.android.musicfx.widget.Visualizer;
import com.android.musicfx.widget.Visualizer.OnSeekBarChangeListener;

import android.app.ActionBar;
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
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioPort;
import android.media.AudioPatch;
import android.media.AudioManager.OnAudioPortUpdateListener;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AudioEffect.Descriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.util.DisplayMetrics;

import java.util.Formatter;
import java.util.Locale;
import java.util.UUID;

/**
 *
 */
public class ActivityMusic extends Activity {
    private final static String TAG = "MusicFXActivityMusic";

    /**
     * Max number of EQ bands supported
     */
    private final static int EQUALIZER_MAX_BANDS = 32;

    /**
     * Max levels per EQ band in millibels (1 dB = 100 mB)
     */
    private final static int EQUALIZER_MAX_LEVEL = 1000;

    /**
     * Min levels per EQ band in millibels (1 dB = 100 mB)
     */
    private final static int EQUALIZER_MIN_LEVEL = -1000;

    /**
     * Indicates if Virtualizer effect is supported.
     */
    private boolean mVirtualizerSupported;
    private boolean mVirtualizerIsHeadphoneOnly;
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
    private final Visualizer[] mEqualizerVisualizer = new Visualizer[EQUALIZER_MAX_BANDS];
    private int mNumberEqualizerBands;
    private int mEqualizerMinBandLevel;
    private int mEQPresetUserPos = 1;
    private int mEQPreset;
    private int[] mEQPresetUserBandLevelsPrev;
    private String[] mEQPresetNames;
    private String[] mReverbPresetNames;

    private int mPRPreset;
    private int mPRPresetPrevious;

    private boolean mIsHeadsetOn = false;
    private boolean mIsSpeakerOn = false;
    private boolean mIsComboDevice = false;
    private ToggleButton mToggleSwitch;
    private TextView toggleSwithText;
    private StringBuilder mFormatBuilder = new StringBuilder();
    private Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    // Preset Reverb fields
    /**
     * Array containing RSid of preset reverb names.
     */
    private static final int[] mReverbPresetRSids = {
        R.string.none, R.string.smallroom, R.string.mediumroom, R.string.largeroom,
        R.string.mediumhall, R.string.largehall, R.string.plate
    };

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

    /**
     * AudioPortUpdateListener to handle UI update on device change
     */
    private MyOnAudioPortUpdateListener mAudioPortUpdateListener = null;

    private class MyOnAudioPortUpdateListener implements OnAudioPortUpdateListener {
        /**
         * Callback method called upon audio port list update.
         */
        @Override
        public void onAudioPortListUpdate(AudioPort[] portList) {
            final boolean isHeadsetOnPrev = mIsHeadsetOn;
            final boolean isSpeakerOnPrev = mIsSpeakerOn;
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            mIsHeadsetOn = false;
            mIsSpeakerOn = false;
            mIsComboDevice = false;

            int device = am.getDevicesForStream(AudioManager.STREAM_MUSIC);
            if (device == AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES ||
                device == AudioManager.DEVICE_OUT_BLUETOOTH_A2DP ||
                device == AudioManager.DEVICE_OUT_WIRED_HEADPHONE ||
                device == AudioManager.DEVICE_OUT_WIRED_HEADSET) {
                mIsHeadsetOn = true;
             } else if (device == AudioManager.DEVICE_OUT_SPEAKER) {
                mIsSpeakerOn = true;
             } else if (device == (AudioManager.DEVICE_OUT_SPEAKER |
                                   AudioManager.DEVICE_OUT_WIRED_HEADPHONE)) {
               mIsComboDevice = true;
             }

             Log.v(TAG, "onAudioPortListUpdate: device=" + device);
             if (isHeadsetOnPrev != mIsHeadsetOn ||
                 isSpeakerOnPrev != mIsSpeakerOn) {
                 updateUIHeadset(false);
             }
        }

        /**
         * Callback method called upon audio patch list update.
         */
        @Override
        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
            // Ingore audio port update
        }

        /**
         * Callback method called when the mediaserver dies
         */
        @Override
        public void onServiceDied() {
            // Nothing to Do
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
                if (effect.uuid.equals(UUID.fromString("1d4033c0-8557-11df-9f2d-0002a5d5c51b"))
                    || effect.uuid.equals(UUID.fromString("e6c98a16-22a3-11e2-b87b-f23c91aec05e"))
                    || effect.uuid.equals(UUID.fromString("d3467faa-acc7-4d34-acaf-0002a5d5c51b"))) {
                    mVirtualizerIsHeadphoneOnly = true;
                }
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_BASS_BOOST)) {
                mBassBoostSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_EQUALIZER)) {
                mEqualizerSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_PRESET_REVERB)) {
                mPresetReverbSupported = true;
            }
        }

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.music_main);
        final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.contentSoundEffects);

        // Fill array with presets from AudioEffects call.
        // allocate a space for 2 extra strings (CI Extreme & User)
        final int numPresets = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_presets);
        mEQPresetNames = new String[numPresets + 2];
        for (short i = 0; i < numPresets; i++) {
            final String eqPresetName = ControlPanelEffect.getParameterString(mContext,
                    mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_preset_name, i);
            mEQPresetNames[i] = localizePresetName(eqPresetName);
        }
        mEQPresetNames[numPresets] = getString(R.string.ci_extreme);
        mEQPresetNames[numPresets + 1] = getString(R.string.user);
        mEQPresetUserPos = numPresets + 1;

        // Load string resource of reverb presets
        mReverbPresetNames = new String[mReverbPresetRSids.length];
        for (short i = 0; i < mReverbPresetRSids.length; ++i) {
            mReverbPresetNames[i] = getString(mReverbPresetRSids[i]);
        }

        // Watch for button clicks and initialization.
        if ((mVirtualizerSupported) || (mBassBoostSupported) || (mEqualizerSupported)
                || (mPresetReverbSupported)) {
            // Set the listener for the main enhancements toggle button.
            // Depending on the state enable the supported effects if they were
            // checked in the setup tab.
            toggleSwithText = (TextView)findViewById(R.id.switchstatus);
            mToggleSwitch = (ToggleButton)findViewById(R.id.togglebutton);
            mToggleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView,
                        final boolean isChecked) {
                    toggleSwithText.setText(isChecked? R.string.toggle_button_on : R.string.toggle_button_off);
                    // set parameter and state
                    ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                            mAudioSession, ControlPanelEffect.Key.global_enabled, isChecked);
                    // Enable Linear layout (in scroll layout) view with all
                    // effect contents depending on checked state
                    setEnabledAllChildren(viewGroup, isChecked);
                    // update UI according to headset state
                    updateUIHeadset(false);
                    setInterception(isChecked);
                }
            });
            // Init device info
            MyOnAudioPortUpdateListener al = new MyOnAudioPortUpdateListener();
            al.onAudioPortListUpdate(null);

            // Initialize the Virtualizer elements.
            // Set the SeekBar listener.
            if (mVirtualizerSupported) {
                final Knob knob = (Knob) findViewById(R.id.vIStrengthKnob);
                knob.setMax(OpenSLESConstants.VIRTUALIZER_MAX_STRENGTH -
                        OpenSLESConstants.VIRTUALIZER_MIN_STRENGTH);
                knob.setOnKnobChangeListener(new OnKnobChangeListener() {
                    // Update the parameters while Knob changes and set the
                    // effect parameter.
                    @Override
                    public void onValueChanged(final Knob knob, final int value,
                        final boolean fromUser) {
                        // set parameter and state
                        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.virt_strength, value);
                    }

                    @Override
                    public boolean onSwitchChanged(final Knob knob, boolean on) {
                        if (on && !mIsHeadsetOn) {
                            if (mIsComboDevice) {
                                showHeadsetMsg(getString(R.string.combo_device));
                            } else {
                                showHeadsetMsg(getString(R.string.headset_plug));
                            }
                            return false;
                        }
                        ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.virt_enabled, on);
                        return true;
                    }
                });
            }

            // Initialize the Bass Boost elements.
            // Set the SeekBar listener.
            if (mBassBoostSupported) {
                final Knob knob = (Knob) findViewById(R.id.bBStrengthKnob);
                knob.setMax(OpenSLESConstants.BASSBOOST_MAX_STRENGTH
                        - OpenSLESConstants.BASSBOOST_MIN_STRENGTH);
                knob.setOnKnobChangeListener(new OnKnobChangeListener() {
                    // Update the parameters while SeekBar changes and set the
                    // effect parameter.

                    @Override
                    public void onValueChanged(final Knob knob, final int value,
                            final boolean fromUser) {
                        // set parameter and state
                        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.bb_strength, value);
                    }

                    @Override
                    public boolean onSwitchChanged(final Knob knob,boolean on) {
                        if (on && !mIsHeadsetOn  && !mIsSpeakerOn) {
                            if (mIsComboDevice) {
                                showHeadsetMsg(getString(R.string.combo_device));
                            } else {
                                showHeadsetMsg(getString(R.string.headset_plug));
                            }
                            return false;
                        }
                        ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.bb_enabled, on);
                        return true;
                    }
                });
            }

            // Initialize the Equalizer elements.
            if (mEqualizerSupported) {
                mEQPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.eq_current_preset);
                if (mEQPreset >= mEQPresetNames.length) {
                    mEQPreset = 0;
                }
                equalizerPresetsInit((Gallery)findViewById(R.id.eqPresets));
                equalizerBandsInit((LinearLayout)findViewById(R.id.eqcontainer));
            }

            // Initialize the Preset Reverb elements.
            // Set Spinner listeners.
            if (mPresetReverbSupported) {
                mPRPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.pr_current_preset);
                mPRPresetPrevious = mPRPreset;
                reverbSpinnerInit((Spinner)findViewById(R.id.prSpinner));
            }

        } else {
            viewGroup.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.noEffectsTextView)).setVisibility(View.VISIBLE);
        }

        ActionBar ab = getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_HOME_AS_UP);
    }

    private final String localizePresetName(final String name) {
        final String[] names = {
            "Normal", "Classical", "Dance", "Flat", "Folk",
            "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock"
        };
        final int[] ids = {
            R.string.normal, R.string.classical, R.string.dance, R.string.flat, R.string.folk,
            R.string.heavy_metal, R.string.hip_hop, R.string.jazz, R.string.pop, R.string.rock
        };

        for (int i = names.length - 1; i >= 0; --i) {
            if (names[i].equals(name)) {
                return getString(ids[i]);
            }
        }
        return name;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            // Register for AudioPortUpdateListener that might affect the onscreen UI.
            if (mAudioPortUpdateListener == null) {
                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                mAudioPortUpdateListener = new MyOnAudioPortUpdateListener();
                am.registerAudioPortUpdateListener(mAudioPortUpdateListener);
            }

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

        // Unregister AudioPortUpdateListener. (These affect the visible UI,
        // so we only care about them while we're in the foreground.)
        if (mAudioPortUpdateListener != null) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.unregisterAudioPortUpdateListener(mAudioPortUpdateListener);
            mAudioPortUpdateListener = null;
        }

    }

    private void reverbSpinnerInit(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, mReverbPresetNames);
        adapter.setDropDownViewResource(R.layout.spinner_detail_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != mPRPresetPrevious) {
                    presetReverbSetPreset(position);
                }
                mPRPresetPrevious = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(mPRPreset);
    }

    private void equalizerPresetsInit(Gallery gallery) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.equalizer_presets,
                mEQPresetNames);

        gallery.setAdapter(adapter);
        gallery.setOnItemSelectedListener(new Gallery.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                mEQPreset = position;
                showSeekBar(position == mEQPresetUserPos);
                equalizerSetPreset(position);
            }
        });
        gallery.setSelection(mEQPreset);
    }


    /**
     * En/disables all children for a given view. For linear and relative layout children do this
     * recursively
     *
     * @param viewGroup
     * @param enabled
     */
    private void setEnabledAllChildren(final ViewGroup viewGroup, final boolean enabled) {
        final int count = viewGroup.getChildCount();
        final View bb = findViewById(R.id.bBStrengthKnob);
        final View virt = findViewById(R.id.vIStrengthKnob);
        final View eq = findViewById(R.id.eqcontainer);
        boolean on = true;

        for (int i = 0; i < count; i++) {
            final View view = viewGroup.getChildAt(i);
            if ((view instanceof LinearLayout) || (view instanceof RelativeLayout)) {
                final ViewGroup vg = (ViewGroup) view;
                setEnabledAllChildren(vg, enabled);
            }

            if (enabled && view == virt) {
                on = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.virt_enabled);
                view.setEnabled(on);
            } else if (enabled && view == bb) {
                on = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.bb_enabled);
                view.setEnabled(on);
            } else if (enabled && view == eq) {
                showSeekBar(mEQPreset == mEQPresetUserPos);
                view.setEnabled(true);
            } else {
                view.setEnabled(enabled);
            }
        }
    }

    /**
     * Updates UI (checkbox, seekbars, enabled states) according to the current stored preferences.
     */
    private void updateUI() {
        final boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
        mToggleSwitch.setChecked(isEnabled);
        toggleSwithText.setText(isEnabled? R.string.toggle_button_on : R.string.toggle_button_off);
        setEnabledAllChildren((ViewGroup) findViewById(R.id.contentSoundEffects), isEnabled);
        updateUIHeadset(false);

        if (mVirtualizerSupported) {
            Knob knob = (Knob) findViewById(R.id.vIStrengthKnob);
            int strength = ControlPanelEffect
                    .getParameterInt(mContext, mCallingPackageName, mAudioSession,
                            ControlPanelEffect.Key.virt_strength);
            knob.setValue(strength);
            boolean hasStrength = ControlPanelEffect.getParameterBoolean(mContext,
                    mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.virt_strength_supported);
            if (!hasStrength) {
                knob.setVisibility(View.GONE);
            }
        }
        if (mBassBoostSupported) {
            ((Knob) findViewById(R.id.bBStrengthKnob)).setValue(ControlPanelEffect
                    .getParameterInt(mContext, mCallingPackageName, mAudioSession,
                            ControlPanelEffect.Key.bb_strength));
        }
        if (mEqualizerSupported) {
            equalizerUpdateDisplay();
        }
        if (mPresetReverbSupported) {
            int reverb = ControlPanelEffect.getParameterInt(
                                    mContext, mCallingPackageName, mAudioSession,
                                    ControlPanelEffect.Key.pr_current_preset);
            ((Spinner)findViewById(R.id.prSpinner)).setSelection(reverb);
        }

        setInterception(isEnabled);
    }

    private void setInterception(boolean isEnabled) {
        final InterceptableLinearLayout ill =
            (InterceptableLinearLayout) findViewById(R.id.contentSoundEffects);
        ill.setInterception(!isEnabled);
        if (isEnabled) {
            ill.setOnClickListener(null);
        } else {
            ill.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Toast toast = Toast.makeText(mContext,
                        getString(R.string.power_on_prompt), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
        }
    }

    /**
     * Updates UI for headset mode. En/disable VI and BB controls depending on
     * headset state (on/off) if effects are on. Do the inverse for their
     * layouts so they can take over control/events.
     */
    private void updateUIHeadset(boolean force) {
        boolean enabled = mToggleSwitch.isChecked() && mIsHeadsetOn;
        final Knob bBKnob = (Knob) findViewById(R.id.bBStrengthKnob);
        bBKnob.setBinary(mIsSpeakerOn);
        bBKnob.setEnabled(mToggleSwitch.isChecked()
                && (mIsHeadsetOn || mIsSpeakerOn));
        final Knob vIKnob = (Knob) findViewById(R.id.vIStrengthKnob);
        vIKnob.setEnabled(enabled || !mVirtualizerIsHeadphoneOnly);

        Log.v(TAG, "updateUIHeadset: mIsHeadsetOn: " + mIsHeadsetOn);
        Log.v(TAG, "updateUIHeadset: mIsSpeakerOn: " + mIsSpeakerOn);
        if (!force) {
            boolean on = ControlPanelEffect.getParameterBoolean(mContext,
                    mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.bb_enabled);
            bBKnob.setOn(mToggleSwitch.isChecked()
                    && (mIsHeadsetOn || mIsSpeakerOn) && on);
            on = ControlPanelEffect.getParameterBoolean(mContext,
                    mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.virt_enabled);
            vIKnob.setOn((enabled && on) || !mVirtualizerIsHeadphoneOnly);
        }
    }

    /**
     * Initializes the equalizer elements. Set the SeekBars and Spinner listeners.
     */
    private void equalizerBandsInit(LinearLayout eqcontainer) {
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
        mEqualizerMinBandLevel = (int) Math.min(EQUALIZER_MIN_LEVEL, bandLevelRange[0]);
        final int mEqualizerMaxBandLevel = (int) Math.max(EQUALIZER_MAX_LEVEL, bandLevelRange[1]);
        final OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final Visualizer v, final int progress,
                    final boolean fromUser) {
                for (short band = 0; band < mNumberEqualizerBands; ++band) {
                    if (mEqualizerVisualizer[band] == v) {
                        final short level = (short) (progress + mEqualizerMinBandLevel);
                        if (fromUser) {
                            equalizerBandUpdate(band, level);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(final Visualizer v) {
            }

            @Override
            public void onStopTrackingTouch(final Visualizer v) {
                equalizerUpdateDisplay();
            }
        };

        final OnTouchListener tl = new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mEQPreset != mEQPresetUserPos) {
                            final Toast toast = Toast.makeText(mContext,
                                    getString(R.string.eq_custom), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0,
                                    toast.getYOffset() * 2);
                            toast.show();
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            }
        };

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int pixels = getResources().getDimensionPixelOffset(R.dimen.each_visualizer_width);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                pixels, ViewGroup.LayoutParams.MATCH_PARENT);
        for (int band = 0; band < mNumberEqualizerBands; band++) {
            // Unit conversion from mHz to Hz and use k prefix if necessary to display
            final int centerFreq = centerFreqs[band] / 1000;
            float centerFreqHz = centerFreq;
            String unitPrefix = "";
            if (centerFreqHz >= 1000) {
                centerFreqHz = centerFreqHz / 1000;
                unitPrefix = "k";
            }

            final Visualizer v = new Visualizer(mContext);
            v.setText(format("%.0f", centerFreqHz) + unitPrefix);
            v.setMax(mEqualizerMaxBandLevel - mEqualizerMinBandLevel);
            v.setOnSeekBarChangeListener(listener);
            v.setOnTouchListener(tl);
            eqcontainer.addView(v, lp);
            mEqualizerVisualizer[band] = v;
        }

        TextView tv = (TextView) findViewById(R.id.maxLevelText);
        tv.setText(String.format("+%d dB", (int) Math.ceil(mEqualizerMaxBandLevel / 100)));
        tv = (TextView) findViewById(R.id.centerLevelText);
        tv.setText("0 dB");
        tv = (TextView) findViewById(R.id.minLevelText);
        tv.setText(String.format("%d dB", (int) Math.floor(mEqualizerMinBandLevel / 100)));
        equalizerUpdateDisplay();
    }

    private String format(String format, Object... args) {
        mFormatBuilder.setLength(0);
        mFormatter.format(format, args);
        return mFormatBuilder.toString();
    }

    private void showSeekBar(boolean show) {
        for (int i = 0; i < mNumberEqualizerBands; ++i) {
            mEqualizerVisualizer[i].setShowSeekBar(show);
        }
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
            final int progress = level - mEqualizerMinBandLevel;
            mEqualizerVisualizer[band].setProgress(progress);
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
    private void equalizerBandUpdate(final int band, final int level) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_band_level, level, band);
    }

    /**
     * Sets the given EQ preset.
     *
     * @param preset
     *            EQ preset id.
     */
    private void equalizerSetPreset(final int preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_current_preset, preset);
        equalizerUpdateDisplay();
    }

    /**
     * Sets the given PR preset.
     *
     * @param preset
     *            PR preset id.
     */
    private void presetReverbSetPreset(final int preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.pr_current_preset, preset);
    }

    /**
     * Show msg that headset needs to be plugged.
     */
    private void showHeadsetMsg(String message) {
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        final Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
    }
}
