package com.android.musicfx.material;

import android.content.Context;
import android.media.audiofx.AudioEffect;

import com.android.musicfx.ControlPanelEffect;
import com.android.musicfx.material.effects.BassBoostEffect;
import com.android.musicfx.material.effects.EqualizerEffect;
import com.android.musicfx.material.effects.ReverbEffect;
import com.android.musicfx.material.effects.VirtualizerEffect;

import java.util.UUID;

public class AudioHandler {
    private static final String TAG = "AudioHandler";

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
    }

    public boolean isEnabled(AudioPortUpdater.Mode mode) {
        return Utilities.prefs(mContext).getBoolean(mode.toString(), false);
    }

    public void setEnabled(AudioPortUpdater.Mode mode, boolean enabled) {
        Utilities.prefs(mContext).edit().putBoolean(mode.toString(), enabled).apply();
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
}
