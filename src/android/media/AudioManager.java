package android.media;

public class AudioManager {
    /** Used to identify the volume of audio streams for music playback */
    public static final int STREAM_MUSIC = 3;

    /**
     * Listener registered by client to be notified upon new audio port connections,
     * disconnections or attributes update.
     * @hide
     */
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
}
