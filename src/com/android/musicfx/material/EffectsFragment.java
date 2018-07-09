package com.android.musicfx.material;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

public class EffectsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {
    private final static String PREF_VIRT = "virtualization";
    private final static String PREF_VIRT_STR = "virtualization_strength";
    private final static String PREF_BASS = "bass";
    private final static String PREF_BASS_STR = "bass_strength";
    private final static String PREF_REVERB = "reverb";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioHandler handler = getMainActivity().getAudioHandler();

        SwitchPreferenceCompat prefVirt = (SwitchPreferenceCompat) findPreference(PREF_VIRT);
        prefVirt.setChecked(handler.getVirtualizer().isEnabled());
        prefVirt.setOnPreferenceChangeListener(this);

        SyncedSeekBar prefVirtStr = (SyncedSeekBar) findPreference(PREF_VIRT_STR);
        prefVirtStr.setValue(handler.getVirtualizer().getStrength());
        prefVirtStr.setOnPreferenceChangeListener(this);

        SwitchPreferenceCompat prefBass = (SwitchPreferenceCompat) findPreference(PREF_BASS);
        prefBass.setChecked(handler.getBass().isEnabled());
        prefBass.setOnPreferenceChangeListener(this);

        SyncedSeekBar prefBassStr = (SyncedSeekBar) findPreference(PREF_BASS_STR);
        prefBassStr.setValue(handler.getBass().getStrength());
        prefBassStr.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.effects_view);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        AudioHandler handler = getMainActivity().getAudioHandler();
        switch (preference.getKey()) {
            case PREF_VIRT:
                handler.getVirtualizer().setEnabled((boolean) newValue);
                break;
            case PREF_VIRT_STR:
                handler.getVirtualizer().setStrength((int) newValue);
                break;
            case PREF_BASS:
                handler.getBass().setEnabled((boolean) newValue);
                break;
            case PREF_BASS_STR:
                handler.getBass().setStrength((int) newValue);
                break;
            case PREF_REVERB:
                break;
        }
        return true;
    }
}
