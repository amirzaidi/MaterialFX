package com.android.musicfx.material;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

public class ExtendedSeekBar extends AppCompatSeekBar {
    private int mMin;
    private int mMax;
    private int mStep;

    public ExtendedSeekBar(Context context) {
        this(context, null);
    }

    public ExtendedSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.verticalSeekbarStyle);
    }

    public void init(int min, int max, int step) {
        mMin = min;
        mMax = max;
        mStep = step;

        setMax((max - min) * step);
        set((max + min) / 2f);
    }

    public void set(float progress) {
        setProgress((int)((progress - mMin) * mStep));
    }

    public float get() {
        return (float) getProgress() / mStep + mMin;
    }
}
