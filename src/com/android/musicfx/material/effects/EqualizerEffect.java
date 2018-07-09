package com.android.musicfx.material.effects;

import android.content.Context;

import com.android.musicfx.ControlPanelEffect;
import com.android.musicfx.material.Utilities;

public class EqualizerEffect {
    // Max levels per EQ band in millibels (1 dB = 100 mB)
    private final static int EQUALIZER_MAX_LEVEL = 1000;

    // Min levels per EQ band in millibels (1 dB = 100 mB)
    private final static int EQUALIZER_MIN_LEVEL = -1000;

    private final Context mContext;
    private final String mCallingPackageName;
    private final int mAudioSession;

    private int mNumberEqualizerBands;
    //private int[] mEQPresetUserBandLevelsPrev;

    private int[] mBandFreqs;
    private int[] mBandLevels;

    public final int minBandLevel;
    public final int maxBandLevel;

    public EqualizerEffect(Context context, String callingPackageName, int audioSession) {
        mContext = context;
        mCallingPackageName = callingPackageName;
        mAudioSession = audioSession;

        mNumberEqualizerBands = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_bands);

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

    public int getNumberEqualizerBands() {
        return mNumberEqualizerBands;
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
}
