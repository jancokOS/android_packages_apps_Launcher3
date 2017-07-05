/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.shortcuts;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;

public class DeepShortcutView extends FrameLayout {

    private static final Point sTempPoint = new Point();

    private BubbleTextView mBubbleText;
    private ShortcutInfoCompat mDetail;
    private View mIconView;
    private ShortcutInfo mInfo;
    private final Rect mPillRect;

    public DeepShortcutView(Context context) {
        this(context, null, 0);
    }

    public DeepShortcutView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DeepShortcutView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);

        mPillRect = new Rect();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBubbleText = (BubbleTextView) findViewById(R.id.bubble_text);
        mIconView = findViewById(R.id.icon);
    }

    public BubbleTextView getBubbleText() {
        return mBubbleText;
    }

    public void setWillDrawIcon(boolean state) {
        mIconView.setVisibility(state ? VISIBLE : INVISIBLE);
    }

    public Point getIconCenter() {
        Point point = sTempPoint;
        int measuredHeight = getMeasuredHeight() / 2;
        sTempPoint.x = measuredHeight;
        point.y = measuredHeight;
        if (Utilities.isRtl(getResources())) {
            sTempPoint.x = getMeasuredWidth() - sTempPoint.x;
        }
        return sTempPoint;
    }

    @Override
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        mPillRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    public void applyShortcutInfo(ShortcutInfo shortcutInfo, ShortcutInfoCompat shortcutInfoCompat, ShortcutsItemView shortcutsItemView) {
        Object obj = null;
        mInfo = shortcutInfo;
        mDetail = shortcutInfoCompat;
        mBubbleText.applyFromShortcutInfo(shortcutInfo);
        mIconView.setBackground(mBubbleText.getIcon());
        CharSequence longLabel = mDetail.getLongLabel();
        int width = (mBubbleText.getWidth() - mBubbleText.getTotalPaddingLeft()) - mBubbleText.getTotalPaddingRight();
        if (!TextUtils.isEmpty(longLabel) && mBubbleText.getPaint().measureText(longLabel.toString()) <= ((float) width)) {
            obj = 1;
        }
        mBubbleText.setText(obj != null ? longLabel : mDetail.getShortLabel());
        mBubbleText.setOnClickListener(Launcher.getLauncher(getContext()));
        mBubbleText.setOnLongClickListener(shortcutsItemView);
        mBubbleText.setOnTouchListener(shortcutsItemView);
    }

    public ShortcutInfo getFinalInfo() {
        ShortcutInfo shortcutInfo = new ShortcutInfo(mInfo);
        Launcher.getLauncher(getContext()).getModel().updateAndBindShortcutInfo(shortcutInfo, mDetail);
        return shortcutInfo;
    }

    public View getIconView() {
        return mIconView;
    }
}
