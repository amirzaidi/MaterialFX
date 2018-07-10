package com.android.musicfx.material;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PresetsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.presets_view, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        LinearLayoutCompat top = getView().findViewById(R.id.presets);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
