package com.android.musicfx.material.effects;

import android.content.Context;

import com.android.musicfx.ControlPanelEffect;

public class ReverbEffect {
    private final Context mContext;
    private final String mCallingPackageName;
    private final int mAudioSession;

    public ReverbEffect(Context context, String callingPackageName, int audioSession) {
        mContext = context;
        mCallingPackageName = callingPackageName;
        mAudioSession = audioSession;
    }

    public int getPreset() {
        return ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.pr_current_preset);
    }

    public void setPreset(int preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.pr_current_preset, preset);
    }
}
