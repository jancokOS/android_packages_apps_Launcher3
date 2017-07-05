package com.android.launcher3.anim;

import android.graphics.Rect;

import com.android.launcher3.util.PillRevealOutlineProvider;

public class PillHeightRevealOutlineProvider extends PillRevealOutlineProvider {
    private final int mNewHeight;

    public PillHeightRevealOutlineProvider(Rect rect, float f, int i) {
        super(0, 0, rect, f);
        mOutline.set(rect);
        mNewHeight = i;
    }

    @Override
    public void setProgress(float f) {
        mOutline.top = 0;
        int height = mPillRect.height() - mNewHeight;
        mOutline.bottom = (int) (((float) mPillRect.bottom) - (((float) height) * (1.0f - f)));
    }
}