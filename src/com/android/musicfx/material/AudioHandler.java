package com.android.musicfx.material;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioSystem;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Virtualizer;

import com.android.musicfx.ActivityMusic;
import com.android.musicfx.ControlPanelEffect;
import com.android.musicfx.material.effects.BassBoostEffect;
import com.android.musicfx.material.effects.EqualizerEffect;
import com.android.musicfx.material.effects.ReverbEffect;
import com.android.musicfx.material.effects.VirtualizerEffect;

import java.util.UUID;

public class AudioHandler {
    private static final String TAG = "AudioHandler";

    public enum Mode {
        Speaker,
        Aux,
        Bluetooth,
        Unknown
    }

    private final Context mContext;
    private final String mCallingPackage;
    private final int mAudioSession;

    private final boolean mEqSupport;
    private final boolean mVirtualizerSupport;
    private final boolean mBassSupport;
    private final boolean mReverbSupport;

    private final EqualizerEffect mEq;
    private final VirtualizerEffect mVirtualizer;
    private final ReverbEffect mReverb;
    private final BassBoostEffect mBass;

    private OnAudioPortUpdateListener mUpdateListener;

    private Mode mOut = Mode.Unknown;

    public AudioHandler(Context context, String callingPackage, int audioSession) {
        mContext = context;
        mCallingPackage = callingPackage == null ? "" : callingPackage;
        mAudioSession = audioSession;

        ControlPanelEffect.initEffectsPreferences(mContext, mCallingPackage, mAudioSession);

        boolean eqSupport = false;
        boolean virtualizerSupport = false;
        boolean bassSupport = false;
        boolean reverbSupport = false;

        for (AudioEffect.Descriptor effect : AudioEffect.queryEffects()) {
            UUID type = effect.type;
            if (type.equals(AudioEffect.EFFECT_TYPE_EQUALIZER)) {
                eqSupport = true;
            } else if (type.equals(AudioEffect.EFFECT_TYPE_VIRTUALIZER)) {
                virtualizerSupport = true;
            } else if (type.equals(AudioEffect.EFFECT_TYPE_BASS_BOOST)) {
                bassSupport = true;
            } else if (type.equals(AudioEffect.EFFECT_TYPE_PRESET_REVERB)) {
                reverbSupport = true;
            }
        }

        mEqSupport = eqSupport;
        mVirtualizerSupport = virtualizerSupport;
        mBassSupport = bassSupport;
        mReverbSupport = reverbSupport;

        mEq = mEqSupport ? new EqualizerEffect(mContext, mCallingPackage, mAudioSession) : null;
        mVirtualizer = mVirtualizerSupport ? new VirtualizerEffect(mContext, mCallingPackage, mAudioSession) : null;
        mBass = mBassSupport ? new BassBoostEffect(mContext, mCallingPackage, mAudioSession) : null;
        mReverb = mReverbSupport ? new ReverbEffect(mContext, mCallingPackage, mAudioSession) : null;

        // Init device info
        MyOnAudioPortUpdateListener al = new MyOnAudioPortUpdateListener();
        al.onAudioPortListUpdate(null);
    }

    public boolean isEnabled() {
        return isEnabled(mOut);
    }

    public boolean isEnabled(Mode mode) {
        return Utilities.prefs(mContext).getBoolean(mode.toString(), false);
    }

    public void setEnabled(Mode mode, boolean enabled) {
        Utilities.prefs(mContext).edit().putBoolean(mode.toString(), enabled).apply();

        ControlPanelEffect.setParameterBoolean(mContext, mCallingPackage,
                mAudioSession, ControlPanelEffect.Key.global_enabled, enabled);
    }

    public EqualizerEffect getEqualizer() {
        return mEq;
    }

    public VirtualizerEffect getVirtualizer() {
        return mVirtualizer;
    }

    public BassBoostEffect getBass() {
        return mBass;
    }

    public ReverbEffect getReverb() {
        return mReverb;
    }

    public void attach() {
        if (mUpdateListener == null) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mUpdateListener = new MyOnAudioPortUpdateListener();

            try {
                AudioManager.class
                        .getDeclaredMethod("registerAudioPortUpdateListener", ActivityMusic.OnAudioPortUpdateListener.class)
                        .invoke(am, mUpdateListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void detach() {
        if (mUpdateListener != null) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            try {
                AudioManager.class
                        .getDeclaredMethod("unregisterAudioPortUpdateListener", ActivityMusic.OnAudioPortUpdateListener.class)
                        .invoke(am, mUpdateListener);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mUpdateListener = null;
        }
    }

    private interface OnAudioPortUpdateListener {
        /**
         * Callback method called upon audio port list update.
         * @param portList the updated list of audio ports
         */
        public void onAudioPortListUpdate(AudioPort[] portList);

        /**
         * Callback method called upon audio patch list update.
         * @param patchList the updated list of audio patches
         */
        public void onAudioPatchListUpdate(AudioPatch[] patchList);

        /**
         * Callback method called when the mediaserver dies
         */
        public void onServiceDied();
    }

    private class MyOnAudioPortUpdateListener implements OnAudioPortUpdateListener {
        @SuppressWarnings("JavaReflectionMemberAccess")
        @SuppressLint("PrivateApi")
        @Override
        public void onAudioPortListUpdate(AudioPort[] portList) {
            mOut = Mode.Unknown;

            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            int device = 0;
            try {
                device = (int) AudioManager.class
                        .getDeclaredMethod("getDevicesForStream", int.class)
                        .invoke(am, AudioManager.STREAM_MUSIC);
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
        }

        @Override
        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
        }

        @Override
        public void onServiceDied() {
        }
    };
}
