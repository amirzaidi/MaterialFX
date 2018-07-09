package com.android.musicfx.material.effects;

import android.content.Context;

public class ReverbEffect {
    private final Context mContext;
    private final String mCallingPackageName;
    private final int mAudioSession;

    public ReverbEffect(Context context, String callingPackageName, int audioSession) {
        mContext = context;
        mCallingPackageName = callingPackageName;
        mAudioSession = audioSession;
    }
}
