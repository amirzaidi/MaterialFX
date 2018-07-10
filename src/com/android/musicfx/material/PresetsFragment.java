package com.android.musicfx.material;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.musicfx.material.effects.EqualizerEffect;

public class PresetsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.presets_view, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart() {
        super.onStart();

        final EqualizerEffect eq = getMainActivity().getAudioHandler().getEqualizer();
        if (eq == null) return;

        LinearLayoutCompat root = getView().findViewById(R.id.presets);
        root.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        for (int i = 0; i < eq.getPresetCount(); i++) {
            EqualizerEffect.Preset presetEq = eq.getPreset(i);

            LinearLayoutCompat preset = (LinearLayoutCompat) inflater.inflate(R.layout.preset, root, false);
            preset.setTag("Preset" + i);

            final int presetNum = i;
            preset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eq.applyPreset(presetNum);
                    setPresetText();
                }
            });

            TextView title = preset.findViewById(R.id.preset_title);
            title.setText(presetEq.name);

            final int offset = 200;
            int minValue = presetEq.getLevel(0);
            int maxValue = presetEq.getLevel(0);

            for (int band = 1; band < eq.numberEqualizerBands; band++) {
                int value = presetEq.getLevel(band);
                minValue = Math.min(minValue, value);
                maxValue = Math.max(maxValue, value);
            }

            LinearLayoutCompat bands = preset.findViewById(R.id.preset_bands);
            for (int band = 0; band < eq.numberEqualizerBands; band++) {
                ExtendedSeekBar bar = new VerticalSeekBar(getActivity());
                bar.init(minValue - offset, maxValue + offset, 1);
                bar.set(presetEq.getLevel(band));
                bands.addView(bar);
            }

            TextView apply = preset.findViewById(R.id.preset_apply);
            apply.setTypeface(getMainActivity().getGSans());

            root.addView(preset);
        }

        setPresetText();
    }

    private void setPresetText() {
        final EqualizerEffect eq = getMainActivity().getAudioHandler().getEqualizer();
        for (int i = 0; i < eq.getPresetCount(); i++) {
            LinearLayoutCompat preset = getView().findViewWithTag("Preset" + i);
            TextView apply = preset.findViewById(R.id.preset_apply);
            apply.setText(eq.currentPreset() == i ? R.string.applied : R.string.apply);
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
