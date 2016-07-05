/*
 * Copyright (c) 2013-2014,2016, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *       * Redistributions in binary form must reproduce the above
 *         copyright notice, this list of conditions and the following
 *         disclaimer in the documentation and/or other materials provided
 *         with the distribution.
 *       * Neither the name of The Linux Foundation nor the names of its
 *         contributors may be used to endorse or promote products derived
 *         from this software without specific prior written permission.
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
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.musicfx.R;

public class Gallery extends android.widget.Gallery {
    public interface OnItemSelectedListener {
        public void onItemSelected(int position);
    }

    private boolean mEnabled = false;

    private int mHighlightColor;
    private int mLowlightColor;
    private int mDisabledColor;

    private TextView mLastView = null;
    private OnItemSelectedListener mOnItemSelectedListener = null;

    public Gallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources res = getResources();
        mHighlightColor = res.getColor(R.color.highlight);
        mLowlightColor = res.getColor(R.color.grey);
        mDisabledColor = res.getColor(R.color.disabled_gallery);

        setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                if (tv != null) {
                    tv.setTextColor(mEnabled ? mHighlightColor : mDisabledColor);
                }
                if (mLastView != null && mLastView != tv) {
                    mLastView.setTextColor(mEnabled ? mLowlightColor : mDisabledColor);
                }
                mLastView = tv;
                if (mEnabled && mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(position);
                }
                int mGalleryTextSize =
                        getResources().getDimensionPixelSize(R.dimen.gallery_text_size);
                if (mLastView != null) {
                    mLastView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mGalleryTextSize);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public Gallery(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Gallery(Context context) {
        this(context, null);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        final int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            final View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(enabled ? mLowlightColor : mDisabledColor);
            }
        }

        if (enabled) {
            final TextView tv = (TextView) getSelectedView();
            if (tv != null) {
                tv.setTextColor(mHighlightColor);
            }
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return mEnabled ? super.onDown(e) : false;
    }
}
