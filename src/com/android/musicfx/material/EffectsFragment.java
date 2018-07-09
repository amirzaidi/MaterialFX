package com.android.musicfx.material;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class EffectsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.prefs(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        Utilities.prefs(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.layout.effects_view);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        AudioHandler handler = getMainActivity().getAudioHandler();



    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
