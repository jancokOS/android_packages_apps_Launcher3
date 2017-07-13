/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.WidgetPreviewLoader;
import com.android.launcher3.compat.AlphabeticIndexCompat;
import com.android.launcher3.model.PackageItemInfo;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.util.LabelComparator;
import com.android.launcher3.util.MultiHashMap;
import com.android.launcher3.util.PackageUserKey;

public class WidgetsListAdapter extends RecyclerView.Adapter<WidgetsRowViewHolder> {
    private final ArrayList<WidgetListRowEntry> mEntries = new ArrayList<>();
    private final OnClickListener mIconClickListener;
    private final OnLongClickListener mIconLongClickListener;
    private final int mIndent;
    private final AlphabeticIndexCompat mIndexer;
    private final LayoutInflater mLayoutInflater;
    private final WidgetPreviewLoader mWidgetPreviewLoader;

    public class WidgetListRowEntryComparator implements Comparator<WidgetListRowEntry> {
        private final LabelComparator mComparator = new LabelComparator();

        @Override
        public int compare(WidgetListRowEntry widgetListRowEntry, WidgetListRowEntry widgetListRowEntry2) {
            return this.mComparator.compare(widgetListRowEntry.pkgItem.title.toString(), widgetListRowEntry2.pkgItem.title.toString());
        }
    }

    public WidgetsListAdapter(OnClickListener onClickListener, OnLongClickListener onLongClickListener, Context context) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();
        this.mIndexer = new AlphabeticIndexCompat(context);
        this.mIconClickListener = onClickListener;
        this.mIconLongClickListener = onLongClickListener;
        this.mIndent = context.getResources().getDimensionPixelSize(R.dimen.widget_section_indent);
    }

    public void setWidgets(MultiHashMap<PackageItemInfo, ArrayList<WidgetListRowEntry>> multiHashMap) {
        this.mEntries.clear();
        Comparator<WidgetItem> widgetItemComparator = new WidgetItemComparator();
        for (Entry<PackageItemInfo, ArrayList<ArrayList<WidgetListRowEntry>>> entry : multiHashMap.entrySet()) {
            WidgetListRowEntry widgetListRowEntry = new WidgetListRowEntry(entry.getKey(), entry.getValue());
            widgetListRowEntry.titleSectionName = this.mIndexer.computeSectionName(widgetListRowEntry.pkgItem.title);
            Collections.sort(widgetListRowEntry.widgets, widgetItemComparator);
            this.mEntries.add(widgetListRowEntry);
        }
        Collections.sort(this.mEntries, new WidgetListRowEntryComparator());
    }

    @Override
    public int getItemCount() {
        return this.mEntries.size();
    }

    public String getSectionName(int i) {
        return ((WidgetListRowEntry) this.mEntries.get(i)).titleSectionName;
    }

    public List copyWidgetsForPackageUser(PackageUserKey packageUserKey) {
        for (WidgetListRowEntry widgetListRowEntry : this.mEntries) {
            if (widgetListRowEntry.pkgItem.packageName.equals(packageUserKey.mPackageName)) {
                ArrayList arrayList = new ArrayList(widgetListRowEntry.widgets);
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    if (!((WidgetItem) it.next()).user.equals(packageUserKey.mUser)) {
                        it.remove();
                    }
                }
                if (!arrayList.isEmpty()) {
                    return arrayList;
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(WidgetsRowViewHolder widgetsRowViewHolder, int i) {
        WidgetListRowEntry widgetListRowEntry = (WidgetListRowEntry) this.mEntries.get(i);
        List list = widgetListRowEntry.widgets;
        ViewGroup viewGroup = widgetsRowViewHolder.cellContainer;
        int max = Math.max(0, list.size() - 1) + list.size();
        int childCount = viewGroup.getChildCount();
        if (max > childCount) {
            while (childCount < max) {
                if ((childCount & 1) == 1) {
                    this.mLayoutInflater.inflate(R.layout.widget_list_divider, viewGroup);
                } else {
                    WidgetCell widgetCell = (WidgetCell) this.mLayoutInflater.inflate(R.layout.widget_cell, viewGroup, false);
                    widgetCell.setOnClickListener(this.mIconClickListener);
                    widgetCell.setOnLongClickListener(this.mIconLongClickListener);
                    viewGroup.addView(widgetCell);
                }
                childCount++;
            }
        } else if (max < childCount) {
            for (int i2 = max; i2 < childCount; i2++) {
                viewGroup.getChildAt(i2).setVisibility(View.GONE);
            }
        }
        widgetsRowViewHolder.title.applyFromPackageItemInfo(widgetListRowEntry.pkgItem);
        for (max = 0; max < list.size(); max++) {
            WidgetCell widgetCell2 = (WidgetCell) viewGroup.getChildAt(max * 2);
            widgetCell2.applyFromCellItem((WidgetItem) list.get(max), this.mWidgetPreviewLoader);
            widgetCell2.ensurePreview();
            widgetCell2.setVisibility(View.VISIBLE);
            if (max > 0) {
                viewGroup.getChildAt((max * 2) - 1).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public WidgetsRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        ViewGroup viewGroup2 = (ViewGroup) this.mLayoutInflater.inflate(R.layout.widgets_list_row_view, viewGroup, false);
        viewGroup2.findViewById(R.id.widgets_cell_list).setPaddingRelative(this.mIndent, 0, 1, 0);
        return new WidgetsRowViewHolder(viewGroup2);
    }

    @Override
    public void onViewRecycled(WidgetsRowViewHolder widgetsRowViewHolder) {
        int childCount = widgetsRowViewHolder.cellContainer.getChildCount();
        for (int i = 0; i < childCount; i += 2) {
            ((WidgetCell) widgetsRowViewHolder.cellContainer.getChildAt(i)).clear();
        }
    }

    @Override
    public boolean onFailedToRecycleView(WidgetsRowViewHolder widgetsRowViewHolder) {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return (long) i;
    }
}
