package com.android.launcher3.widget;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.widget.RemoteViews;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
import com.android.launcher3.HolographicOutlineHelper;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.PendingAddItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.graphics.DragPreviewProvider;
import com.android.launcher3.graphics.LauncherIcons;

public class PendingItemDragHelper extends DragPreviewProvider {
    private final PendingAddItemInfo mAddInfo;
    private RemoteViews mPreview;
    private Bitmap mPreviewBitmap;

    public PendingItemDragHelper(View view) {
        super(view);
        mAddInfo = (PendingAddItemInfo) view.getTag();
    }

    public void setPreview(RemoteViews remoteViews) {
        mPreview = remoteViews;
    }

    public void startDrag(Rect rect, int i, int i2, Point point, DragSource dragSource, DragOptions dragOptions) {
        int min;
        int i3;
        float width;
        Point point2;
        Rect rect2;
        Bitmap bitmap;
        Launcher launcher = Launcher.getLauncher(mView.getContext());
        LauncherAppState instance = LauncherAppState.getInstance();
        Bitmap bmap;
        if (mAddInfo instanceof PendingAddWidgetInfo) {
            PendingAddWidgetInfo pendingAddWidgetInfo = (PendingAddWidgetInfo) this.mAddInfo;
            min = Math.min((int) (((float) i) * 1.25f), launcher.getWorkspace().estimateItemSize(pendingAddWidgetInfo, true)[0]);
            int[] iArr = new int[1];
            bmap = instance.getWidgetCache().generateWidgetPreview(launcher, pendingAddWidgetInfo.info, min, null, iArr);
            if (iArr[0] < i) {
                i3 = (i - iArr[0]) / 2;
                if (i > i2) {
                    i3 = (i3 * i2) / i;
                }
                rect.left += i3;
                rect.right -= i3;
            }
            width = ((float) rect.width()) / ((float) bmap.getWidth());
            launcher.getDragController().addDragListener(new WidgetHostViewLoader(launcher, mView));
            point2 = null;
            rect2 = null;
            bitmap = bmap;
        } else {
            bmap = LauncherIcons.createScaledBitmapWithoutShadow(((PendingAddShortcutInfo) mAddInfo).activityInfo.getFullResIcon(instance.getIconCache()), launcher, 26);
            PendingAddItemInfo pendingAddItemInfo = this.mAddInfo;
            this.mAddInfo.spanY = 1;
            pendingAddItemInfo.spanX = 1;
            width = ((float) launcher.getDeviceProfile().iconSizePx) / ((float) bmap.getWidth());
            point2 = new Point(previewPadding / 2, previewPadding / 2);
            int[] estimateItemSize = launcher.getWorkspace().estimateItemSize(mAddInfo, false);
            DeviceProfile deviceProfile = launcher.getDeviceProfile();
            int i4 = deviceProfile.iconSizePx;
            int dimensionPixelSize = launcher.getResources().getDimensionPixelSize(R.dimen.widget_preview_shortcut_padding);
            rect.left += dimensionPixelSize;
            rect.top = dimensionPixelSize + rect.top;
            rect2 = new Rect();
            rect2.left = (estimateItemSize[0] - i4) / 2;
            rect2.right = rect2.left + i4;
            rect2.top = (((estimateItemSize[1] - i4) - deviceProfile.iconTextSizePx) - deviceProfile.iconDrawablePaddingPx) / 2;
            rect2.bottom = rect2.top + i4;
            bitmap = bmap;
        }
        launcher.getWorkspace().prepareDragWithProvider(this);
        i3 = ((int) (((((float) bitmap.getWidth()) * width) - ((float) bitmap.getWidth())) / 2.0f)) + (point.x + rect.left);
        min = ((int) (((((float) bitmap.getHeight()) * width) - ((float) bitmap.getHeight())) / 2.0f)) + (point.y + rect.top);
        mPreviewBitmap = bitmap;
        launcher.getDragController().startDrag(bitmap, i3, min, dragSource, mAddInfo, point2, rect2, width, dragOptions);
    }

    @Override
    public Bitmap createDragOutline(Canvas canvas) {
        int i;
        Rect rect2;
        if (mAddInfo instanceof PendingAddShortcutInfo) {
            Bitmap createBitmap = Bitmap.createBitmap(mPreviewBitmap.getWidth() + this.blurSizeOutline, mPreviewBitmap.getHeight() + this.blurSizeOutline, Config.ALPHA_8);
            canvas.setBitmap(createBitmap);
            i = Launcher.getLauncher(mView.getContext()).getDeviceProfile().iconSizePx;
            Rect rect = new Rect(0, 0, mPreviewBitmap.getWidth(), mPreviewBitmap.getHeight());
            rect2 = new Rect(0, 0, i, i);
            rect2.offset(this.blurSizeOutline / 2, this.blurSizeOutline / 2);
            canvas.drawBitmap(mPreviewBitmap, rect, rect2, new Paint(2));
            HolographicOutlineHelper.obtain(mView.getContext()).applyExpensiveOutlineWithBlur(createBitmap, canvas);
            canvas.setBitmap(null);
            return createBitmap;
        }
        int[] estimateItemSize = Launcher.getLauncher(mView.getContext()).getWorkspace().estimateItemSize(mAddInfo, false);
        i = estimateItemSize[0];
        int i2 = estimateItemSize[1];
        Bitmap createBitmap2 = Bitmap.createBitmap(i, i2, Config.ALPHA_8);
        canvas.setBitmap(createBitmap2);
        rect2 = new Rect(0, 0, mPreviewBitmap.getWidth(), mPreviewBitmap.getHeight());
        float min = Math.min(((float) (i - this.blurSizeOutline)) / ((float) mPreviewBitmap.getWidth()), ((float) (i2 - this.blurSizeOutline)) / ((float) mPreviewBitmap.getHeight()));
        int width = (int) (((float) mPreviewBitmap.getWidth()) * min);
        int height = (int) (min * ((float) this.mPreviewBitmap.getHeight()));
        Rect rect3 = new Rect(0, 0, width, height);
        rect3.offset((i - width) / 2, (i2 - height) / 2);
        canvas.drawBitmap(mPreviewBitmap, rect2, rect3, null);
        HolographicOutlineHelper.obtain(mView.getContext()).applyExpensiveOutlineWithBlur(createBitmap2, canvas);
        canvas.setBitmap(null);
        return createBitmap2;
    }
}