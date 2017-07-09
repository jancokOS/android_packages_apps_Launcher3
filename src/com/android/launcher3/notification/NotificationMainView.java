package com.android.launcher3.notification;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.util.Themes;

public class NotificationMainView extends FrameLayout implements SwipeHelper.Callback {
    private int mBackgroundColor;
    private NotificationInfo mNotificationInfo;
    private ViewGroup mTextAndBackground;
    private TextView mTextView;
    private TextView mTitleView;

    public NotificationMainView(Context context) {
        this(context, null, 0);
    }

    public NotificationMainView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationMainView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextAndBackground = (ViewGroup) findViewById(R.id.text_and_background);
        ColorDrawable colorDrawable = (ColorDrawable) mTextAndBackground.getBackground();
        mBackgroundColor = colorDrawable.getColor();
        mTextAndBackground.setBackground(new RippleDrawable(ColorStateList.valueOf(Themes.getAttrColor(getContext(), 16843820)), colorDrawable, null));
        mTitleView = (TextView) mTextAndBackground.findViewById(R.id.title);
        mTextView = (TextView) mTextAndBackground.findViewById(R.id.text);
    }

    public void applyNotificationInfo(NotificationInfo notificationInfo, View view) {
        applyNotificationInfo(notificationInfo, view, false);
    }

    public void applyNotificationInfo(NotificationInfo notificationInfo, View view, boolean z) {
        this.mNotificationInfo = notificationInfo;
        CharSequence charSequence = mNotificationInfo.title;
        CharSequence charSequence2 = mNotificationInfo.text;
        if (TextUtils.isEmpty(charSequence) || TextUtils.isEmpty(charSequence2)) {
            mTitleView.setMaxLines(2);
            TextView textView = mTitleView;
            if (!TextUtils.isEmpty(charSequence)) {
                charSequence2 = charSequence;
            }
            textView.setText(charSequence2);
            mTextView.setVisibility(GONE);
        } else {
            mTitleView.setText(charSequence);
            mTextView.setText(charSequence2);
        }
        view.setBackground(mNotificationInfo.getIconForBackground(getContext(), mBackgroundColor));
        if (this.mNotificationInfo.intent != null) {
            setOnClickListener(mNotificationInfo);
        }
        setTranslationX(0.0f);
        setTag(new ItemInfo());
        if (z) {
            ObjectAnimator.ofFloat(mTextAndBackground, ALPHA, new float[]{0.0f, 1.0f}).setDuration(150).start();
        }
    }

    public NotificationInfo getNotificationInfo() {
        return mNotificationInfo;
    }

    @Override
    public View getChildAtPosition(MotionEvent motionEvent) {
        return this;
    }

    @Override
    public boolean canChildBeDismissed(View view) {
        return mNotificationInfo != null && mNotificationInfo.dismissable;
    }

    @Override
    public boolean isAntiFalsingNeeded() {
        return false;
    }

    @Override
    public void onBeginDrag(View view) {
    }

    @Override
    public void onChildDismissed(View view) {
        Launcher launcher = Launcher.getLauncher(getContext());
        launcher.getPopupDataProvider().cancelNotification(mNotificationInfo.notificationKey);
    }

    @Override
    public void onDragCancelled(View view) {
    }

    @Override
    public void onChildSnappedBack(View view, float f) {
    }

    @Override
    public boolean updateSwipeProgress(View view, boolean z, float f) {
        return true;
    }

    @Override
    public float getFalsingThresholdFactor() {
        return 1.0f;
    }
}