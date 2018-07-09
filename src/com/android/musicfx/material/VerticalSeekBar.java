package com.android.musicfx.material;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

public class VerticalSeekBar extends AppCompatSeekBar {
    private int mMin;
    private int mMax;
    private int mStep;

    public VerticalSeekBar(Context context) {
        this(context, null);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
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
