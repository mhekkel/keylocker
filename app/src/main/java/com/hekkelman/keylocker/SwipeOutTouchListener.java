package com.hekkelman.keylocker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maarten on 25-11-15.
 */
public class SwipeOutTouchListener implements RecyclerView.OnItemTouchListener {
    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private long animationTime;

    private RecyclerView recyclerView;
    private SwipeOutListener swipeOutListener;
    private int viewWidth;

    // Transient properties
    private List<PendingSwipedOutData> pendingSwipedOutData = new ArrayList<>();
    private int swipedOutAnimationRefCount = 0;
    private float downViewAlpha;
    private float downX;
    private float downY;
    private boolean swiping;
    private boolean swipedOutRight;
    private VelocityTracker velocityTracker;
    private int downPosition;
    private int animatingPosition = ListView.INVALID_POSITION;
    private View downView;
    private boolean enabled;
    private boolean disallowed;

    public SwipeOutTouchListener(RecyclerView recyclerView, SwipeOutListener listener) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());

        this.slop = vc.getScaledTouchSlop() * 2;
        this.minFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        this.maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        this.animationTime = recyclerView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        this.recyclerView = recyclerView;
        this.swipeOutListener = listener;
        this.disallowed = false;
        this.enabled = true;

        /**
         * This will ensure that this SwipeableRecyclerViewTouchListener is paused during list view scrolling.
         * If a scroll listener is already assigned, the caller should still pass scroll changes through
         * to this listener.
         */
        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        return handleTouchEvent(motionEvent);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        this.disallowed = true;
    }

    private boolean handleTouchEvent(MotionEvent motionEvent) {

        // check to see if we need to handle this at all
        if (this.disallowed || this.enabled == false) {
            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
                this.disallowed = false;
            return false;
        }

        if (viewWidth < 2) {
            viewWidth = recyclerView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = recyclerView.getChildCount();
                int[] listViewCoords = new int[2];
                recyclerView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = recyclerView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        downView = child;
                        break;
                    }
                }

                if (downView != null && animatingPosition != recyclerView.getChildAdapterPosition(downView)) {
                    downViewAlpha = downView.getAlpha();
                    downX = motionEvent.getRawX();
                    downY = motionEvent.getRawY();
                    downPosition = recyclerView.getChildAdapterPosition(downView);
                    if (swipeOutListener.canSwipe(downPosition)) {
                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(motionEvent);
                    } else {
                        downView = null;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker != null) {
                    velocityTracker.addMovement(motionEvent);
                    float deltaX = motionEvent.getRawX() - downX;
                    float deltaY = motionEvent.getRawY() - downY;

                    if (swiping == false)
                        swiping = Math.abs(deltaX) > slop && Math.abs(deltaY) < Math.abs(deltaX) / 2;

                    if (swiping) {
                        downView.setTranslationX(deltaX);

                        float alpha = downViewAlpha * (1f - Math.abs(deltaX) / viewWidth);
                        if (alpha < 0)
                            alpha = 0;
                        downView.setAlpha(alpha);

                        return true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (velocityTracker != null) {
                    if (downView != null && swiping) {
                        // cancel
                        downView.animate()
                                .translationX(0)
                                .alpha(downViewAlpha)
                                .setDuration(animationTime)
                                .setListener(null);
                    }
                    velocityTracker.recycle();
                    velocityTracker = null;
                    downX = 0;
                    downY = 0;
                    downView = null;
                    downPosition = ListView.INVALID_POSITION;
                    swiping = false;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (velocityTracker != null) {
                    velocityTracker.addMovement(motionEvent);
                    velocityTracker.computeCurrentVelocity(1000);

                    float finalDelta = motionEvent.getRawX() - downX;
                    float velocityX = velocityTracker.getXVelocity();
                    float absVelocityX = Math.abs(velocityX);
                    float absVelocityY = Math.abs(velocityTracker.getYVelocity());
                    boolean swipeOut = false;
                    swipedOutRight = false;

                    if (swiping && Math.abs(finalDelta) > viewWidth / 2) {
                        swipeOut = true;
                        swipedOutRight = finalDelta > 0;
                    } else if (minFlingVelocity <= absVelocityX && absVelocityX <= maxFlingVelocity
                            && absVelocityY < absVelocityX && swiping) {
                        // swipeOut only if flinging in the same direction as dragging
                        swipeOut = (velocityX < 0) == (finalDelta < 0);
                        swipedOutRight = velocityTracker.getXVelocity() > 0;
                    }

                    if (swipeOut && downPosition != animatingPosition && downPosition != ListView.INVALID_POSITION) {
                        // swipeOut
                        final View downView = this.downView; // mDownView gets null'd before animation ends
                        final int downPosition = this.downPosition;
                        ++swipedOutAnimationRefCount;
                        animatingPosition = downPosition;
                        downView.animate()
                                .translationX(swipedOutRight ? viewWidth : -viewWidth)
                                .alpha(0)
                                .setDuration(animationTime)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        performSwipeOut(downView, downPosition);
                                    }
                                });
                    } else {
                        // cancel
                        downView.animate()
                                .translationX(0)
                                .alpha(downViewAlpha)
                                .setDuration(animationTime)
                                .setListener(null);
                    }

                    velocityTracker.recycle();
                    velocityTracker = null;
                    downX = 0;
                    downY = 0;
                    downView = null;
                    downPosition = ListView.INVALID_POSITION;
                    swiping = false;
                }
                break;
            }
        }

        return false;
    }

    private void performSwipeOut(final View swipedOutView, final int swipeOutPosition) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        final ViewGroup.LayoutParams lp = swipedOutView.getLayoutParams();
        final int originalLayoutParamsHeight = lp.height;
        final int originalHeight = swipedOutView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --swipedOutAnimationRefCount;
                if (swipedOutAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(pendingSwipedOutData);

                    int[] swipedOutPositions = new int[pendingSwipedOutData.size()];
                    for (int i = pendingSwipedOutData.size() - 1; i >= 0; i--) {
                        swipedOutPositions[i] = pendingSwipedOutData.get(i).position;
                    }

                    if (swipedOutRight) {
                        swipeOutListener.onSwipeOutRight(recyclerView, swipedOutPositions);
                    } else {
                        swipeOutListener.onSwipeOutLeft(recyclerView, swipedOutPositions);
                    }

                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                    // animation with a stale position
                    downPosition = ListView.INVALID_POSITION;

                    ViewGroup.LayoutParams lp;
                    for (PendingSwipedOutData pendingSwipeOut : pendingSwipedOutData) {
                        // Reset view presentation
                        pendingSwipeOut.view.setAlpha(downViewAlpha);
                        pendingSwipeOut.view.setTranslationX(0);

                        lp = pendingSwipeOut.view.getLayoutParams();
                        lp.height = originalLayoutParamsHeight;

                        pendingSwipeOut.view.setLayoutParams(lp);
                    }

                    // Send a cancel event
                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    recyclerView.dispatchTouchEvent(cancelEvent);

                    pendingSwipedOutData.clear();
                    animatingPosition = ListView.INVALID_POSITION;
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                swipedOutView.setLayoutParams(lp);
            }
        });

        pendingSwipedOutData.add(new PendingSwipedOutData(swipeOutPosition, swipedOutView));
        animator.start();
    }

    public interface SwipeOutListener {
        boolean canSwipe(int position);
        void onSwipeOutLeft(RecyclerView recyclerView, int[] reverseSortedPositions);
        void onSwipeOutRight(RecyclerView recyclerView, int[] reverseSortedPositions);
    }

    class PendingSwipedOutData implements Comparable<PendingSwipedOutData> {
        public int position;
        public View view;

        public PendingSwipedOutData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(@NonNull PendingSwipedOutData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

}
