
package com.amachan.list;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class PullRefreshListView extends PinnedHeaderListView {

    private static final int MIN_DISTANCE_TO_TRIGGER_SYNC = 150; // dp
    private static final int MAX_DISTANCE_TO_TRIGGER_SYNC = 300; // dp
    private static final int DISTANCE_TO_IGNORE = 15; // dp
    private static final int DISTANCE_TO_TRIGGER_CANCEL = 10; // dp
    // Whether to ignore events in {#dispatchTouchEvent}.
    private boolean mIgnoreTouchEvents = false;

    private boolean mTrackingScrollMovement = false;
    // Y coordinate of where scroll started
    private float mTrackingScrollStartY;
    // Max Y coordinate reached since starting scroll, this is used to know
    // whether
    // user moved back up which should cancel the current tracking state and
    // hide the
    // sync trigger bar.
    private float mTrackingScrollMaxY;

    private float mDensity;

    // Minimum vertical distance (in dips) of swipe to trigger a sync.
    // This value can be different based on the device.
    private float mDistanceToTriggerSyncDp = MIN_DISTANCE_TO_TRIGGER_SYNC;

    private String TAG = this.getClass().getSimpleName();

    public PullRefreshListView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public PullRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);

    }

    public PullRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mDensity = displayMetrics.density;

        // Calculate distance threshold for triggering a sync based on
        // screen height. Apply a min and max cutoff.
        float threshold = (displayMetrics.heightPixels) / mDensity / 2.5f;
        mDistanceToTriggerSyncDp = Math.max(
                Math.min(threshold, MAX_DISTANCE_TO_TRIGGER_SYNC),
                MIN_DISTANCE_TO_TRIGGER_SYNC);
    }

    // Pull down sync notification
    // Modified from com.android.mail.ui.ConversationListView
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // First check for any events that can trigger end of a swipe, so we can
        // reset
        // mIgnoreTouchEvents back to false (it can only be set to true at
        // beginning of swipe)
        // via {#onBeginSwipe()} callback.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIgnoreTouchEvents = false;
        }

        if (mIgnoreTouchEvents) {
            return super.dispatchTouchEvent(event);
        }

        float y = event.getY(0);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Only if we have reached the top of the list, any further
                // scrolling
                // can potentially trigger a sync.
                Log.d(TAG, "Action Down - " + this.getChildCount() + " , "
                        + this.getChildAt(0).getTop());
                if (this.getChildCount() == 0 || this.getChildAt(0).getTop() == 0) {
                    startMovementTracking(y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "Action Move - " + mTrackingScrollMovement);
                if (mTrackingScrollMovement) {

                    // Sync is triggered when tap and drag distance goes over a
                    // certain threshold
                    float verticalDistancePx = y - mTrackingScrollStartY;
                    float verticalDistanceDp = verticalDistancePx / mDensity;
                    Log.d(TAG, "Action Move - verticalDistanceDp " + verticalDistanceDp);
                    Log.d(TAG, "Action Move - mDistanceToTriggerSyncDp " + mDistanceToTriggerSyncDp);
                    if (verticalDistanceDp > mDistanceToTriggerSyncDp) {
                        Log.i(TAG, "Sync triggered from distance");
                        triggerSync();
                        break;
                    }

                    // Moving back up vertically should be handled the same as
                    // CANCEL / UP:
                    float verticalDistanceFromMaxPx = mTrackingScrollMaxY - y;
                    float verticalDistanceFromMaxDp = verticalDistanceFromMaxPx / mDensity;
                    if (verticalDistanceFromMaxDp > DISTANCE_TO_TRIGGER_CANCEL) {
                        cancelMovementTracking();
                        break;
                    }

                    // Otherwise hint how much further user needs to drag to
                    // trigger sync by
                    // expanding the sync status bar proportional to how far
                    // they have dragged.
                    if (verticalDistanceDp < DISTANCE_TO_IGNORE) {
                        // Ignore small movements such as tap
                        verticalDistanceDp = 0;
                    }

                    if (y > mTrackingScrollMaxY) {
                        mTrackingScrollMaxY = y;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mTrackingScrollMovement) {
                    cancelMovementTracking();
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    private void startMovementTracking(float y) {
        Log.d(TAG, "Start swipe to refresh tracking");
        mTrackingScrollMovement = true;
        mTrackingScrollStartY = y;
        mTrackingScrollMaxY = mTrackingScrollStartY;
    }

    private void cancelMovementTracking() {
        if (mTrackingScrollMovement) {
            mTrackingScrollMovement = false;
        }
    }

    private void triggerSync() {
        Log.d(TAG, "triggerSync");
        // Any continued dragging after this should have no effect
        mTrackingScrollMovement = false;
        Toast.makeText(getContext(), "Refresh is called", Toast.LENGTH_LONG).show();
    }

}
