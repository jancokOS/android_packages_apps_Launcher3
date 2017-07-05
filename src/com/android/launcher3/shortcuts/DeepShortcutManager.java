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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Utilities;
import android.os.UserHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeepShortcutManager {

    // TODO: Replace this with platform constants when the new sdk is available.
    public static final int FLAG_MATCH_DYNAMIC = 1 << 0;
    public static final int FLAG_MATCH_MANIFEST = 1 << 3;
    public static final int FLAG_MATCH_PINNED = 1 << 1;

    private static DeepShortcutManager sInstance;
    private static final Object sInstanceLock = new Object();
    private final LauncherApps mLauncherApps;
    private boolean mWasLastCallSuccess;

    public static DeepShortcutManager getInstance(Context context) {
        DeepShortcutManager deepShortcutManager;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new DeepShortcutManager(context.getApplicationContext());
            }
            deepShortcutManager = sInstance;
        }
        return deepShortcutManager;
    }

    public DeepShortcutManager(Context context) {
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    public static boolean supportsShortcuts(ItemInfo itemInfo) {
        if (itemInfo.itemType == 0) {
            return !itemInfo.isDisabled();
        }
        return false;
    }

    public boolean wasLastCallSuccess() {
        return mWasLastCallSuccess;
    }

    public void onShortcutsChanged(List list) {
        // mShortcutCache.removeShortcuts(shortcuts);
    }

    public List queryForFullDetails(String str, List list, UserHandle userHandle) {
        return query(11, str, null, list, userHandle);
    }

    public List queryForShortcutsContainer(ComponentName componentName, List list, UserHandle userHandle) {
        return query(9, componentName.getPackageName(), componentName, list, userHandle);
    }

    public void unpinShortcut(ShortcutKey shortcutKey) {
        if (Utilities.isNycOrAbove()) {
            String packageName = shortcutKey.componentName.getPackageName();
            String id = shortcutKey.getId();
            UserHandle userHandle = shortcutKey.user;
            List extractIds = extractIds(queryForPinnedShortcuts(packageName, userHandle));
            extractIds.remove(id);
            try {
                mLauncherApps.pinShortcuts(packageName, extractIds, userHandle);
                mWasLastCallSuccess = true;
            } catch (Throwable e) {
                Log.w("DeepShortcutManager", "Failed to unpin shortcut", e);
                mWasLastCallSuccess = false;
            }
        }
    }

    public void pinShortcut(ShortcutKey shortcutKey) {
        if (Utilities.isNycMR1OrAbove()) {
            String packageName = shortcutKey.componentName.getPackageName();
            String id = shortcutKey.getId();
            UserHandle userHandle = shortcutKey.user;
            List extractIds = extractIds(queryForPinnedShortcuts(packageName, userHandle));
            extractIds.add(id);
            try {
                mLauncherApps.pinShortcuts(packageName, extractIds, userHandle);
                mWasLastCallSuccess = true;
            } catch (SecurityException|IllegalStateException e) {
                Log.w("DeepShortcutManager", "Failed to pin shortcut", e);
                mWasLastCallSuccess = false;
            }
        }
    }

    public void startShortcut(String str, String str2, Rect rect, Bundle bundle, UserHandle userHandle) {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                mLauncherApps.startShortcut(str, str2, rect, bundle, userHandle);
                mWasLastCallSuccess = true;
            } catch (Throwable e) {
                Log.e("DeepShortcutManager", "Failed to start shortcut", e);
                mWasLastCallSuccess = false;
            }
        }
    }

    public Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfoCompat, int i) {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                Drawable shortcutIconDrawable = this.mLauncherApps.getShortcutIconDrawable(shortcutInfoCompat.getShortcutInfo(), i);
                mWasLastCallSuccess = true;
                return shortcutIconDrawable;
            } catch (Throwable e) {
                Log.e("DeepShortcutManager", "Failed to get shortcut icon", e);
                mWasLastCallSuccess = false;
            }
        }
        return null;
    }

    public List queryForPinnedShortcuts(String str, UserHandle userHandle) {
        return query(2, str, null, null, userHandle);
    }

    public List queryForAllShortcuts(UserHandle userHandle) {
        return query(11, null, null, null, userHandle);
    }

    private List extractIds(List<ShortcutInfoCompat> list) {
        List arrayList = new ArrayList(list.size());
        for (ShortcutInfoCompat id : list) {
            arrayList.add(id.getId());
        }
        return arrayList;
    }

    private List query(int i, String str, ComponentName componentName, List list, UserHandle userHandle) {
        List<ShortcutInfo> iterable = null;
        if (!Utilities.isNycMR1OrAbove()) {
            return Collections.EMPTY_LIST;
        }
        ShortcutQuery shortcutQuery = new ShortcutQuery();
        shortcutQuery.setQueryFlags(i);
        if (str != null) {
            shortcutQuery.setPackage(str);
            shortcutQuery.setActivity(componentName);
            shortcutQuery.setShortcutIds(list);
        }
        try {
            iterable = this.mLauncherApps.getShortcuts(shortcutQuery, userHandle);
            mWasLastCallSuccess = true;
        } catch (Throwable e) {
            Log.e("DeepShortcutManager", "Failed to query for shortcuts", e);
            mWasLastCallSuccess = false;
        }
        if (iterable == null) {
            return Collections.EMPTY_LIST;
        }
        List arrayList = new ArrayList(iterable.size());
        for (ShortcutInfo shortcutInfoCompat : iterable) {
            arrayList.add(new ShortcutInfoCompat(shortcutInfoCompat));
        }
        return arrayList;
    }

    @TargetApi(25)
    public boolean hasHostPermission() {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                return mLauncherApps.hasShortcutHostPermission();
            } catch (Throwable e) {
                Log.e("DeepShortcutManager", "Failed to make shortcut manager call", e);
            }
        }
        return false;
    }
}
