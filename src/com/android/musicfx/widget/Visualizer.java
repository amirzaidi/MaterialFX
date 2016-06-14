/*
 * Copyright (c) 2013-2014,2016, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.android.musicfx.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.android.musicfx.R;
import com.android.musicfx.seekbar.SeekBar;

public class Visualizer extends LinearLayout {
    public interface OnSeekBarChangeListener {
        public void onProgressChanged(Visualizer visualizer, int progress, boolean fromUser);
        public void onStartTrackingTouch(Visualizer visualizer);
        public void onStopTrackingTouch(Visualizer visualizer);
    }

    private final int MAX_TILES = 17;
    private final float SPACE_RATIO = 1.0f;
    private final float HORIZONTAL_PADDING = 0.138f;
    private final int VERTICAL_PADDING = 20;
    private final static String TAG = "Visualizer-JAVA";

    private final TextView mTV;
    private final SeekBar mSB;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    private boolean mEnabled;
    private boolean mShowSeekBar = false;
    private int mMax;
    private int mProgress;

    private int mWidth;
    private float mHeight;
    private float mTileWidth;
    private float mBaseHeight;
    private final float mTVHeight;
    private final float mTextBottom;
    private final float mTileBottom;
    private final float mLeftMargin;

    private int mHighlightColor;
    private int mLowlightColor;
    private int mDisabledColor;
    private final Paint mPaint;

    public Visualizer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater li = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.visualizer, this, true);

        Resources res = getResources();
        mHighlightColor = res.getColor(R.color.highlight);
        mLowlightColor = res.getColor(R.color.lowlight);
        mDisabledColor = res.getColor(R.color.disabled_knob);
        mTVHeight = res.getDimension(R.dimen.eq_text_height);
        mTextBottom = res.getDimensionPixelOffset(R.dimen.visualizer_text_bottom);
        mTileBottom = res.getDimensionPixelOffset(R.dimen.visualizer_tile_bottom);
        mTileWidth = res.getDimensionPixelOffset(R.dimen.tile_width);
        mLeftMargin = res.getDimensionPixelOffset(R.dimen.visualizer_margin_left);

        mTV = (TextView) findViewById(R.id.EQBandTextView);
        mSB = (SeekBar) findViewById(R.id.EQBandSeekBar);
        final Visualizer v = this;
        mSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekbar, final int progress,
                final boolean fromUser) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(v, progress, fromUser);
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekbar) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStartTrackingTouch(v);
                }
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekbar) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStopTrackingTouch(v);
                }
            }
        });

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);
    }

    public Visualizer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Visualizer(Context context) {
        this(context, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d(TAG, "onSizeChanged w="+w+" oldW="+oldW);
        Log.d(TAG, "onSizeChanged h="+h+" oldH="+oldH);
        mWidth = w;
        mHeight = h;
        // Calculate the height of each tile
        mBaseHeight = (mHeight - mTileBottom - mTVHeight - mTextBottom)
                / (MAX_TILES * SPACE_RATIO + MAX_TILES - 1);
        Log.d(TAG, "onSizeChanged mBaseHeight="+mBaseHeight+" mTVHeight="+mTVHeight);
        final LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, (int) (mHeight - mTVHeight - mTextBottom));
        mSB.setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShowSeekBar) {
            return;
        }
        boolean topDrawn = false;
        final RectF rf = new RectF();

        // Count of visible tiles
        int tiles = (int) ((float) mProgress * MAX_TILES / mMax + 0.5);
        for (int i = MAX_TILES - tiles; i < MAX_TILES; ++i) {
            if (mEnabled) {
                if (!topDrawn) {
                    topDrawn = true;
                    mPaint.setColor(mHighlightColor);
                } else {
                    mPaint.setColor(mLowlightColor);
                }
            } else {
                mPaint.setColor(mDisabledColor);
            }
            // Draw a tile
            rf.set(mLeftMargin,
                    i * (SPACE_RATIO + 1) * mBaseHeight,
                    mTileWidth + mLeftMargin,
                    ((i + 1) * SPACE_RATIO + i) * mBaseHeight);
            canvas.drawRect(rf, mPaint);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        if (!enabled) {
            setShowSeekBar(false);
        }
        invalidate();
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    public void setText(final String text) {
        mTV.setText(text);
    }

    public void setMax(int max) {
        mMax = max;
        mSB.setMax(max);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    public void setProgress(int progress) {
        Log.d(TAG, "setProgress progress="+progress);
        mProgress = progress;
        mSB.setProgress(progress);
        invalidate();
    }

    public void setShowSeekBar(boolean show) {
        Log.d(TAG, "setShowSeekBar mEnabled="+mEnabled+" show="+show);
        if (mEnabled || !show) {
            mShowSeekBar = show;
            mSB.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            invalidate();
        }
    }
}
