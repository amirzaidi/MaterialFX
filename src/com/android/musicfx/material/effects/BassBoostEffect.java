package com.android.musicfx.material.effects;

import android.content.Context;

import com.android.musicfx.ControlPanelEffect;

public class BassBoostEffect {
    private final Context mContext;
    private final String mCallingPackageName;
    private final int mAudioSession;

    public BassBoostEffect(Context context, String callingPackageName, int audioSession) {
        mContext = context;
        mCallingPackageName = callingPackageName;
        mAudioSession = audioSession;
    }

    public boolean isEnabled() {
        return ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.bb_enabled);
    }

    public void setEnabled(boolean enabled) {
        ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.bb_enabled, enabled);
    }

    public int getStrength() {
        return ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.bb_strength) / 100;
    }

    public void setStrength(int value) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.bb_strength, value * 100);
    }
}
