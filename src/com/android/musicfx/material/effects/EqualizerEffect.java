package com.android.musicfx.material.effects;

import android.content.Context;

import com.android.musicfx.ControlPanelEffect;
import com.android.musicfx.material.Utilities;

public class EqualizerEffect {
    public class Preset {
        public final String name;
        private final int[] mLevels;

        private Preset(final String eqName, final short[] levels) {
            name = eqName;
            mLevels = new int[levels.length];
            for (int i = 0; i < levels.length; i++) {
                mLevels[i] = levels[i];
            }
        }

        public int getLevel(int band) {
            return mLevels[band];
        }
    }

    public final static int PRESET_CUSTOM = -1;

    // Max levels per EQ band in millibels (1 dB = 100 mB)
    private final static int EQUALIZER_MAX_LEVEL = 1000;

    // Min levels per EQ band in millibels (1 dB = 100 mB)
    private final static int EQUALIZER_MIN_LEVEL = -1000;

    private final Context mContext;
    private final String mCallingPackageName;
    private final int mAudioSession;

    public final int numberEqualizerBands;

    private final Preset[] mPresets;

    //private int[] mEQPresetUserBandLevelsPrev;

    private int[] mBandFreqs;
    private int[] mBandLevels;

    public final int minBandLevel;
    public final int maxBandLevel;

    public EqualizerEffect(Context context, String callingPackageName, int audioSession) {
        mContext = context;
        mCallingPackageName = callingPackageName;
        mAudioSession = audioSession;

        numberEqualizerBands = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_bands);

        mPresets = new Preset[ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_presets)];

        for (int i = 0; i < mPresets.length; i++) {
            mPresets[i] = new Preset(ControlPanelEffect.getPresetName(i),
                    ControlPanelEffect.getPreset(i));
        }

        /*mEQPresetUserBandLevelsPrev = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_preset_user_band_level);*/

        final int[] bandLevelRange = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_level_range);

        minBandLevel = Math.min(EQUALIZER_MIN_LEVEL, bandLevelRange[0]);
        maxBandLevel = Math.max(EQUALIZER_MAX_LEVEL, bandLevelRange[1]);

        mBandFreqs = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_center_freq);

        mBandLevels = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_band_level);
    }

    public String getBandFreq(int band) {
        float centerFreqHz = mBandFreqs[band] / 1000;
        String unitPrefix = "";
        if (centerFreqHz >= 1000) {
            centerFreqHz = centerFreqHz / 1000;
            unitPrefix = "k";
        }
        return Utilities.floatToString(centerFreqHz) + unitPrefix + "Hz";
    }

    public int getBandLevel(int band) {
        return mBandLevels[band];
    }

    public void setBandLevel(int band, int level) {
        mBandLevels[band] = level;
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_band_level, level, band);
    }

    public int getPresetCount() {
        return mPresets.length;
    }

    public Preset getPreset(int preset) {
        return mPresets[preset];
    }

    public void applyPreset(int preset) {
        Preset presetBands = getPreset(preset);
        for (int i = 0; i < numberEqualizerBands; i++) {
            setBandLevel(i, presetBands.getLevel(i));
        }
    }

    public int currentPreset() {
        for (int i = 0; i < getPresetCount(); i++) {
            if (isPresetApplied(getPreset(i))) {
                return i;
            }
        }
        return PRESET_CUSTOM;
    }

    private boolean isPresetApplied(Preset preset) {
        for (int i = 0; i < numberEqualizerBands; i++) {
            if (preset.getLevel(i) != getBandLevel(i)) {
                return false;
            }
        }
        return true;
    }
}
