package com.android.musicfx.material;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioSystem;
import android.os.IBinder;

import com.android.musicfx.ControlPanelEffect;

import java.util.Map;

@SuppressWarnings("JavaReflectionMemberAccess")
@SuppressLint("PrivateApi")
public class AudioPortUpdater extends Service
        implements android.media.AudioManager.OnAudioPortUpdateListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "AudioPortUpdater";

    public enum Mode {
        Speaker,
        Aux,
        Bluetooth,
        Unknown
    }

    // Current output routing mode
    private Mode mOut = Mode.Unknown;

    // Handler to update global on/off state
    private AudioHandler mAudioHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.v(TAG, "onCreate");

        mAudioHandler = new AudioHandler(this, getPackageName(), 0);
        onAudioPortListUpdate(null);

        try {
            AudioManager.class
                    .getDeclaredMethod("registerAudioPortUpdateListener", android.media.AudioManager.OnAudioPortUpdateListener.class)
                    .invoke(getAudioManager(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utilities.prefs(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Utilities.prefs(this).unregisterOnSharedPreferenceChangeListener(this);

        try {
            AudioManager.class
                    .getDeclaredMethod("unregisterAudioPortUpdateListener", android.media.AudioManager.OnAudioPortUpdateListener.class)
                    .invoke(getAudioManager(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAudioHandler = null;

        super.onDestroy();
        android.util.Log.v(TAG, "onDestroy");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (Mode mode : Mode.values()) {
            if (key.equals(mode.toString())) {
                update();
                return;
            }
        }
    }

    @Override
    public void onAudioPortListUpdate(AudioPort[] portList) {
        mOut = Mode.Unknown;

        int device = 0;
        try {
            device = (int) AudioManager.class
                    .getDeclaredMethod("getDevicesForStream", int.class)
                    .invoke(getAudioManager(), AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (device == AudioSystem.DEVICE_OUT_SPEAKER) {
            mOut = Mode.Speaker;
        } else if (device == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES ||
                device == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP) {
            mOut = Mode.Bluetooth;
        } else if (device == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE ||
                device == AudioSystem.DEVICE_OUT_WIRED_HEADSET) {
            mOut = Mode.Aux;
        }

        update();
    }

    @Override
    public void onAudioPatchListUpdate(AudioPatch[] patchList) {
    }

    @Override
    public void onServiceDied() {
        stopSelf();
    }

    public void update() {
        boolean enabled = mAudioHandler.isEnabled(mOut);
        android.util.Log.v(TAG, "Setting enabled to " + enabled);
        for (Map.Entry<String, Integer> entry : ControlPanelEffect.getSessions().entrySet()) {
            android.util.Log.v(TAG, "Setting for " + entry.getKey() + " " + entry.getValue());
            ControlPanelEffect.setParameterBoolean(AudioPortUpdater.this, entry.getKey(),
                    entry.getValue(), ControlPanelEffect.Key.global_enabled, enabled);
        }
    }

    private AudioManager getAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
}
