package com.android.launcher3.notification;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.List;

import com.android.launcher3.R;
import com.android.launcher3.anim.PillHeightRevealOutlineProvider;
import com.android.launcher3.graphics.IconPalette;
import com.android.launcher3.popup.PopupItemView;

public class NotificationItemView extends PopupItemView {
    private static final Rect sTempRect = new Rect();
    private boolean mAnimatingNextIcon;
    private NotificationFooterLayout mFooter;
    private TextView mHeaderCount;
    private NotificationMainView mMainView;
    private int mNotificationHeaderTextColor;
    private SwipeHelper mSwipeHelper;

    final class IconAnimation implements NotificationFooterLayout.IconAnimationEndListener {
        IconAnimation() {
        }

        @Override
        public void onIconAnimationEnd(NotificationInfo notificationInfo) {
            if (notificationInfo != null) {
                NotificationItemView.this.mMainView.applyNotificationInfo(notificationInfo, NotificationItemView.this.mIconView, true);
                NotificationItemView.this.mMainView.setVisibility(VISIBLE);
            }
            NotificationItemView.this.mAnimatingNextIcon = false;
        }
    }

    public NotificationItemView(Context context) {
        this(context, null, 0);
    }

    public NotificationItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationItemView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mNotificationHeaderTextColor = 0;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderCount = (TextView) findViewById(R.id.notification_count);
        mMainView = (NotificationMainView) findViewById(R.id.main_view);
        mFooter = (NotificationFooterLayout) findViewById(R.id.footer);
        mSwipeHelper = new SwipeHelper(0, mMainView, getContext());
        mSwipeHelper.setDisableHardwareLayers(true);
    }

    public NotificationMainView getMainView() {
        return mMainView;
    }

    public int getHeightMinusFooter() {
        return getHeight() - (mFooter.getParent() == null ? 0 : mFooter.getHeight());
    }

    public Animator animateHeightRemoval(int i) {
        return new PillHeightRevealOutlineProvider(mPillRect, getBackgroundRadius(), getHeight() - i).createRevealAnimator(this, true);
    }

    public void updateHeader(int i, IconPalette iconPalette) {
        mHeaderCount.setText(i <= 1 ? "" : String.valueOf(i));
        if (iconPalette != null) {
            if (mNotificationHeaderTextColor == 0) {
                mNotificationHeaderTextColor = IconPalette.resolveContrastColor(getContext(), iconPalette.dominantColor, getResources().getColor(R.color.popup_header_background_color));
            }
            mHeaderCount.setTextColor(mNotificationHeaderTextColor);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (mMainView.getNotificationInfo() == null) {
            return false;
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        return mSwipeHelper.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mMainView.getNotificationInfo() == null) {
            return false;
        }
        return !mSwipeHelper.onTouchEvent(motionEvent) ? super.onTouchEvent(motionEvent) : true;
    }

    public void applyNotificationInfos(List list) {
        if (!list.isEmpty()) {
            mMainView.applyNotificationInfo((NotificationInfo) list.get(0), mIconView);
            for (int i = 1; i < list.size(); i++) {
                mFooter.addNotificationInfo((NotificationInfo) list.get(i));
            }
            mFooter.commitNotificationInfos();
        }
    }

    public void trimNotifications(List list) {
        if (list.contains(mMainView.getNotificationInfo().notificationKey) || mAnimatingNextIcon) {
            mFooter.trimNotifications(list);
            return;
        }
        mAnimatingNextIcon = true;
        mMainView.setVisibility(INVISIBLE);
        mMainView.setTranslationX(0.0f);
        mIconView.getGlobalVisibleRect(sTempRect);
        mFooter.animateFirstNotificationTo(sTempRect, new IconAnimation());
    }

    @Override
    public int getArrowColor(boolean z) {
        int i;
        Context context = getContext();
        if (z) {
            i = R.color.popup_background_color;
        } else {
            i = R.color.popup_header_background_color;
        }
        return ContextCompat.getColor(context, i);
    }
}