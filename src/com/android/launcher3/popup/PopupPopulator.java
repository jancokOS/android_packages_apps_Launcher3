package com.android.launcher3.popup;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.notification.NotificationInfo;
import com.android.launcher3.notification.NotificationItemView;
import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.DeepShortcutView;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.util.PackageUserKey;

public class PopupPopulator {
    static final int NUM_DYNAMIC = 2;
    private static final Comparator SHORTCUT_RANK_COMPARATOR = new ShortcutComparator();

    static final class ShortcutComparator implements Comparator<ShortcutInfoCompat> {
        ShortcutComparator() {
        }

        @Override
        public int compare(ShortcutInfoCompat shortcutInfoCompat, ShortcutInfoCompat shortcutInfoCompat2) {
            if (shortcutInfoCompat.isDeclaredInManifest() && !shortcutInfoCompat2.isDeclaredInManifest()) {
                return -1;
            }
            if (shortcutInfoCompat.isDeclaredInManifest() || !shortcutInfoCompat2.isDeclaredInManifest()) {
                return Integer.compare(shortcutInfoCompat.getRank(), shortcutInfoCompat2.getRank());
            }
            return 1;
        }
    }

    static final class ShortcutRunnable implements Runnable {
        final ComponentName val$activity;
        final PopupContainerWithArrow val$container;
        final Launcher val$launcher;
        final List val$notificationKeys;
        final NotificationItemView val$notificationView;
        final ItemInfo val$originalInfo;
        final List val$shortcutIds;
        final List val$shortcutViews;
        final List val$systemShortcutViews;
        final List val$systemShortcuts;
        final Handler val$uiHandler;
        final UserHandle val$user;

        ShortcutRunnable(NotificationItemView notificationItemView, Launcher launcher, List list, Handler handler, ComponentName componentName, List list2, UserHandle userHandle, List list3, PopupContainerWithArrow popupContainerWithArrow, List list4, List list5, ItemInfo itemInfo) {
            val$notificationView = notificationItemView;
            val$launcher = launcher;
            val$notificationKeys = list;
            val$uiHandler = handler;
            val$activity = componentName;
            val$shortcutIds = list2;
            val$user = userHandle;
            val$shortcutViews = list3;
            val$container = popupContainerWithArrow;
            val$systemShortcuts = list4;
            val$systemShortcutViews = list5;
            val$originalInfo = itemInfo;
        }

        @Override
        public void run() {
            List statusBarNotificationsForKeys;
            String str;
            if (val$notificationView != null) {
                statusBarNotificationsForKeys = val$launcher.getPopupDataProvider().getStatusBarNotificationsForKeys(val$notificationKeys);
                List<NotificationInfo> arrayList = new ArrayList<>(statusBarNotificationsForKeys.size());
                for (int i = 0; i < statusBarNotificationsForKeys.size(); i++) {
                    arrayList.add(new NotificationInfo(val$launcher, (StatusBarNotification) statusBarNotificationsForKeys.get(i)));
                }
                val$uiHandler.post(new UpdateNotificationChild(val$notificationView, arrayList));
            }
            List queryForShortcutsContainer = DeepShortcutManager.getInstance(val$launcher).queryForShortcutsContainer(val$activity, val$shortcutIds, val$user);
            if (val$notificationKeys.isEmpty()) {
                str = null;
            } else {
                str = ((NotificationKeyData) val$notificationKeys.get(0)).shortcutId;
            }
            statusBarNotificationsForKeys = PopupPopulator.sortAndFilterShortcuts(queryForShortcutsContainer, str);
            int i2 = 0;
            while (i2 < statusBarNotificationsForKeys.size() && i2 < val$shortcutViews.size()) {
                ShortcutInfoCompat shortcutInfoCompat = (ShortcutInfoCompat) statusBarNotificationsForKeys.get(i2);
                ShortcutInfo shortcutInfo = new ShortcutInfo(shortcutInfoCompat, val$launcher);
                shortcutInfo.iconBitmap = LauncherIcons.createShortcutIcon(shortcutInfoCompat, val$launcher, false);
                shortcutInfo.rank = i2;
                val$uiHandler.post(new UpdateShortcutChild(val$container, (DeepShortcutView) val$shortcutViews.get(i2), shortcutInfo, shortcutInfoCompat));
                i2++;
            }
            for (int i3 = 0; i3 < val$systemShortcuts.size(); i3++) {
                val$uiHandler.post(new UpdateSystemShortcutChild(val$container, (View) val$systemShortcutViews.get(i3), (SystemShortcut) val$systemShortcuts.get(i3), val$launcher, val$originalInfo));
            }
            Handler handler = val$uiHandler;
            final Launcher launcher = val$launcher;
            final ItemInfo itemInfo = val$originalInfo;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    launcher.refreshAndBindWidgetsForPackageUser(PackageUserKey.fromItemInfo(itemInfo));
                }
            });
        }
    }

    public enum Item {
        SHORTCUT(R.layout.deep_shortcut, true),
        NOTIFICATION(R.layout.notification, false),
        SYSTEM_SHORTCUT(R.layout.system_shortcut, true),
        SYSTEM_SHORTCUT_ICON(R.layout.system_shortcut_icon_only, true);

        public final boolean isShortcut;
        public final int layoutId;

        private Item(int i, boolean z) {
            layoutId = i;
            isShortcut = z;
        }
    }

    static class UpdateNotificationChild implements Runnable {
        private List<NotificationInfo> mNotificationInfos;
        private NotificationItemView mNotificationView;

        public UpdateNotificationChild(NotificationItemView notificationItemView, List<NotificationInfo> list) {
            mNotificationView = notificationItemView;
            mNotificationInfos = list;
        }

        @Override
        public void run() {
            mNotificationView.applyNotificationInfos(mNotificationInfos);
        }
    }

    static class UpdateShortcutChild implements Runnable {
        private final PopupContainerWithArrow mContainer;
        private final ShortcutInfoCompat mDetail;
        private final DeepShortcutView mShortcutChild;
        private final ShortcutInfo mShortcutChildInfo;

        public UpdateShortcutChild(PopupContainerWithArrow popupContainerWithArrow, DeepShortcutView deepShortcutView, ShortcutInfo shortcutInfo, ShortcutInfoCompat shortcutInfoCompat) {
            mContainer = popupContainerWithArrow;
            mShortcutChild = deepShortcutView;
            mShortcutChildInfo = shortcutInfo;
            mDetail = shortcutInfoCompat;
        }

        @Override
        public void run() {
            mShortcutChild.applyShortcutInfo(mShortcutChildInfo, mDetail, mContainer.mShortcutsItemView);
        }
    }

    static class UpdateSystemShortcutChild implements Runnable {
        private final PopupContainerWithArrow mContainer;
        private final ItemInfo mItemInfo;
        private final Launcher mLauncher;
        private final View mSystemShortcutChild;
        private final SystemShortcut mSystemShortcutInfo;

        public UpdateSystemShortcutChild(PopupContainerWithArrow popupContainerWithArrow, View view, SystemShortcut systemShortcut, Launcher launcher, ItemInfo itemInfo) {
            mContainer = popupContainerWithArrow;
            mSystemShortcutChild = view;
            mSystemShortcutInfo = systemShortcut;
            mLauncher = launcher;
            mItemInfo = itemInfo;
        }

        @Override
        public void run() {
            PopupPopulator.initializeSystemShortcut(mSystemShortcutChild.getContext(), mSystemShortcutChild, mSystemShortcutInfo);
            mSystemShortcutChild.setOnClickListener(mSystemShortcutInfo.getOnClickListener(mLauncher, mItemInfo));
        }
    }

    public static Item[] getItemsToPopulate(List list, List list2, List list3) {
        int i = 2;
        int i2 = 1;
        int i3 = 0;
        int size = list.size();
        boolean i4 = list2.size() > 0;
        if (!i4) {
            i2 = 0;
        }
        if (!i4 || size <= 2) {
            i = size;
        }
        i = list3.size() + Math.min(4, i2 + i);
        Item[] itemArr = new Item[i];
        for (i2 = 0; i2 < i; i2++) {
            itemArr[i2] = Item.SHORTCUT;
        }
        if (i4) {
            itemArr[0] = Item.NOTIFICATION;
        }
        i4 = !list.isEmpty();
        while (i3 < list3.size()) {
            itemArr[(i - 1) - i3] = i4 ? Item.SYSTEM_SHORTCUT_ICON : Item.SYSTEM_SHORTCUT;
            i3++;
        }
        return itemArr;
    }

    public static Item[] reverseItems(Item[] itemArr) {
        if (itemArr == null) {
            return null;
        }
        int length = itemArr.length;
        Item[] itemArr2 = new Item[length];
        for (int i = 0; i < length; i++) {
            itemArr2[i] = itemArr[(length - i) - 1];
        }
        return itemArr2;
    }

    public static List sortAndFilterShortcuts(List list, String str) {
        int i = 0;
        if (str != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                if (((ShortcutInfoCompat) it.next()).getId().equals(str)) {
                    it.remove();
                    break;
                }
            }
        }
        Collections.sort(list, SHORTCUT_RANK_COMPARATOR);
        if (list.size() <= 4) {
            return list;
        }
        List arrayList = new ArrayList(4);
        int size = list.size();
        for (int i2 = 0; i2 < size; i2++) {
            ShortcutInfoCompat shortcutInfoCompat = (ShortcutInfoCompat) list.get(i2);
            int size2 = arrayList.size();
            if (size2 < 4) {
                int i3;
                arrayList.add(shortcutInfoCompat);
                if (shortcutInfoCompat.isDynamic()) {
                    i3 = i + 1;
                } else {
                    i3 = i;
                }
                i = i3;
            } else if (shortcutInfoCompat.isDynamic() && i < 2) {
                i++;
                arrayList.remove(size2 - i);
                arrayList.add(shortcutInfoCompat);
            }
        }
        return arrayList;
    }

    public static Runnable createUpdateRunnable(Launcher launcher, ItemInfo itemInfo, Handler handler, PopupContainerWithArrow popupContainerWithArrow, List list, List list2, List list3, NotificationItemView notificationItemView, List list4, List list5) {
        return new ShortcutRunnable(notificationItemView, launcher, list3, handler, itemInfo.getTargetComponent(), list, itemInfo.user, list2, popupContainerWithArrow, list4, list5, itemInfo);
    }

    public static void initializeSystemShortcut(Context context, View view, SystemShortcut systemShortcut) {
        if (view instanceof DeepShortcutView) {
            DeepShortcutView deepShortcutView = (DeepShortcutView) view;
            deepShortcutView.getIconView().setBackground(systemShortcut.getIcon(context, 16843282));
            deepShortcutView.getBubbleText().setText(systemShortcut.getLabel(context));
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(systemShortcut.getIcon(context, 16842906));
            imageView.setContentDescription(systemShortcut.getLabel(context));
        }
        view.setTag(systemShortcut);
    }
}