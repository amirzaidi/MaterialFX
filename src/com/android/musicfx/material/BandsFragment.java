package com.android.musicfx.material;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.musicfx.material.effects.EqualizerEffect;

public class BandsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bands_view, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();

        final EqualizerEffect eq = getMainActivity().getAudioHandler().getEqualizer();
        if (eq == null) return;

        LinearLayoutCompat root = (LinearLayoutCompat) getView();
        root.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Update and show the active N-Band Equalizer bands.
        for (int band = 0; band < eq.getNumberEqualizerBands(); band++) {
            float level = eq.getBandLevel(band) / 100f;

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.band, root, false);

            final TextView name = layout.findViewById(R.id.band_name);
            name.setText(eq.getBandFreq(band));

            final VerticalSeekBar slider = layout.findViewById(R.id.band_slider);
            slider.init(eq.minBandLevel / 100, eq.maxBandLevel / 100, 2);
            slider.set(level);

            final TextView value = layout.findViewById(R.id.band_value);
            value.setText(slider.get() + "dB");

            final int bandInt = band;
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    eq.setBandLevel(bandInt, (int)(slider.get() * 100));
                    value.setText(slider.get() + "dB");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            root.addView(layout);
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
