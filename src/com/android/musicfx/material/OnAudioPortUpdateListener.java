package com.android.musicfx.material;

import android.media.AudioPatch;
import android.media.AudioPort;

public interface OnAudioPortUpdateListener {
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