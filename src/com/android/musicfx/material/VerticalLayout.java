package com.android.musicfx.material;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;

public class VerticalLayout extends LinearLayoutCompat {
    public VerticalLayout(Context context) {
        super(context);
    }

    public VerticalLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);

        setRotation(270);
        setTranslationX((widthMeasureSpec - heightMeasureSpec) / 2);
        setTranslationY((heightMeasureSpec - widthMeasureSpec) / 2);
    }
}
