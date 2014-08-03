/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.amachan.pinnedWidget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.amachan.pinned.CompositeCursorAdapter;
import com.amachan.list.PinnedHeaderListView;

/**
 * A subclass of {@link CompositeCursorAdapter} that manages pinned partition
 * headers.
 */
public abstract class PinnedHeaderListAdapter extends com.amachan.pinned.CompositeCursorAdapter
        implements PinnedHeaderListView.PinnedHeaderAdapter {

    public static final int PARTITION_HEADER_TYPE = 0;

    public boolean mHeaderBottomEnabled = true;
    public boolean mHeaderBottomKeepOne = true;
    public boolean mHeaderTopEnabled = true;
    public boolean mHeaderTopKeepOne = true;

    private boolean mPinnedPartitionHeadersEnabled;
    private boolean mHeaderVisibility[];
    private String TAG = this.getClass().getSimpleName();

    public PinnedHeaderListAdapter(Context context) {
        super(context);
    }

    public PinnedHeaderListAdapter(Context context, int initialCapacity) {
        super(context, initialCapacity);
    }

    public boolean getPinnedPartitionHeadersEnabled() {
        return mPinnedPartitionHeadersEnabled;
    }

    public void setPinnedPartitionHeadersEnabled(boolean flag) {
        this.mPinnedPartitionHeadersEnabled = flag;
    }

    public void setHeaderBottomEnabled(boolean flag) {
        this.mHeaderBottomEnabled = flag;
    }

    public boolean getHeaderBottomEnabled() {
        return mHeaderBottomEnabled;
    }

    public void setHeaderTopEnabled(boolean flag) {
        this.mHeaderTopEnabled = flag;
    }

    public boolean getHeaderTopEnabled() {
        return mHeaderTopEnabled;
    }

    public void setHeaderTopKeepOne(boolean flag) {
        this.mHeaderTopKeepOne = flag;
    }

    public boolean getHeaderTopKeepOne() {
        return mHeaderTopKeepOne;
    }

    public void setHeaderBottomKeepOne(boolean flag) {
        this.mHeaderBottomKeepOne = flag;
    }

    public boolean getHeaderBottomKeepOne() {
        return mHeaderBottomKeepOne;
    }

    @Override
    public int getPinnedHeaderCount() {
        if (mPinnedPartitionHeadersEnabled) {
            return getPartitionCount();
        } else {
            return 0;
        }
    }

    protected boolean isPinnedPartitionHeaderVisible(int partition) {
        return getPinnedPartitionHeadersEnabled() && hasHeader(partition)
                && !isPartitionEmpty(partition);
    }

    /**
     * The default implementation creates the same type of view as a normal
     * partition header.
     */
    @Override
    public View getPinnedHeaderView(int partition, View convertView, ViewGroup parent) {
        if (hasHeader(partition)) {
            View view = null;
            if (convertView != null) {
                Integer headerType = (Integer) convertView.getTag();
                if (headerType != null && headerType == PARTITION_HEADER_TYPE) {
                    view = convertView;
                }
            }
            if (view == null) {
                view = newHeaderView(getContext(), partition, null, parent);
                view.setTag(PARTITION_HEADER_TYPE);
                view.setFocusable(false);
                view.setEnabled(false);
            }
            bindHeaderView(view, partition, getCursor(partition));
            view.setLayoutDirection(parent.getLayoutDirection());
            return view;
        } else {
            return null;
        }
    }

    @Override
    public void configurePinnedHeaders(PinnedHeaderListView listView) {
        if (!getPinnedPartitionHeadersEnabled()) {
            return;
        }

        int size = getPartitionCount();

        // Cache visibility bits, because we will need them several times later
        // on
        if (mHeaderVisibility == null || mHeaderVisibility.length != size) {
            mHeaderVisibility = new boolean[size];
        }
        for (int i = 0; i < size; i++) {
            boolean visible = isPinnedPartitionHeaderVisible(i);
            mHeaderVisibility[i] = visible;
            if (!visible) {
                listView.setHeaderInvisible(i, true);
            }
        }

        int headerViewsCount = listView.getHeaderViewsCount();

        // Starting at the top, find and pin headers for partitions preceding
        // the visible one(s)
        int maxTopHeader = -1;
        int topHeaderHeight = 0;
        for (int i = 0; i < size; i++) {
            if (mHeaderVisibility[i]) {
                int position = listView.getPositionAt(topHeaderHeight) - headerViewsCount;
                int partition = getPartitionForPosition(position);
                if (i > partition) {
                    break;
                }
                Log.d(TAG, "________ i __________" + i);
                Log.d(TAG, "position " + position);
                Log.d(TAG, "Partition : " + partition);
                Log.d(TAG, "topHeaderHeight : " + topHeaderHeight);
                // Keep header top visible
                if (getHeaderTopEnabled()) {
                    // Keep only one header at the top
                    if (getHeaderTopKeepOne()) {
                        if (i == partition) {
                            listView.setHeaderPinnedAtTop(partition, topHeaderHeight, false);
                        } else {
                            listView.setHeaderInvisible(i, true);
                        }
                    } else {
                        listView.setHeaderPinnedAtTop(i, topHeaderHeight, false);
                    }
                } else {
                    listView.setHeaderInvisible(i, true);
                }

                if (!getHeaderTopKeepOne()) {
                    topHeaderHeight += listView.getPinnedHeaderHeight(i);
                }

                maxTopHeader = i;
            }
        }

        // Starting at the bottom, find and pin headers for partitions following
        // the visible one(s)
        int maxBottomHeader = size;
        int bottomHeaderHeight = 0;
        int listHeight = listView.getHeight();
        for (int i = size; --i > maxTopHeader;) {
            if (mHeaderVisibility[i]) {
                int position = listView.getPositionAt(listHeight - bottomHeaderHeight)
                        - headerViewsCount;
                if (position < 0) {
                    break;
                }

                int partition = getPartitionForPosition(position - 1);
                if (partition == -1 || i <= partition) {
                    break;
                }
                Log.d(TAG, "________ i __________" + i);
                Log.d(TAG, "position " + position);
                Log.d(TAG, "Partition : " + partition);
                Log.d(TAG, "bottomHeaderHeight : " + bottomHeaderHeight);
                int height = listView.getPinnedHeaderHeight(i);
                ;
                bottomHeaderHeight += height;

                if (getHeaderBottomEnabled()) {
                    if (getHeaderBottomKeepOne()) {

                        if (i == (partition + 1)) {
                            height = listView.getPinnedHeaderHeight(i);
                            listView.setHeaderPinnedAtBottom(i, listHeight -
                                    height, false);
                        } else {
                            listView.setHeaderInvisible(i, true);
                        }
                    } else {
                        listView.setHeaderPinnedAtBottom(i, listHeight -
                                bottomHeaderHeight, false);
                    }
                } else {
                    listView.setHeaderInvisible(i, true);
                }

                maxBottomHeader = i;
            }
        }

        // Headers in between the top-pinned and bottom-pinned should be hidden
        for (int i = maxTopHeader + 1; i < maxBottomHeader; i++) {
            if (mHeaderVisibility[i]) {
                listView.setHeaderInvisible(i, isPartitionEmpty(i));
            }
        }
    }

    @Override
    public int getScrollPositionForHeader(int viewIndex) {
        return getPositionForPartition(viewIndex);
    }
}
