package de.codekenner.roadtrip.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class SnappingScrollView extends HorizontalScrollView {
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;

    private GestureDetector mGestureDetector;
    private int currentPage = 0;

    public SnappingScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SnappingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnappingScrollView(Context context) {
        super(context);
        init();
    }

    private void init() {

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // If the user swipes
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollX = getScrollX();
                    int featureWidth = v.getMeasuredWidth();
                    currentPage = ((scrollX + (featureWidth / 2)) / featureWidth);
                    smoothScrollToPage(currentPage);
                    return true;
                } else {
                    return false;
                }
            }
        });
        mGestureDetector = new GestureDetector(getContext(),
                new SwipeGestureDetector(new MySwipeHandler()));

    }

    private int getChildWidth() {
        final int childCount = getChildCount();
        if (childCount == 1) {
            return getChildAt(0).getMeasuredWidth();
        }

        return getMeasuredWidth();
    }

    private class MySwipeHandler implements SwipeGestureDetector.SwipeHandler {

        @Override
        public void onSwipe(SwipeGestureDetector.SwipeDirection direction) {
            try {
                switch (direction) {
                    case RIGHT:
                        if ((currentPage + 1) * getMeasuredWidth() < getChildWidth()) {
                            currentPage++;
                        }
                        break;
                    case LEFT:
                        currentPage = Math.max(0, currentPage - 1);
                        break;
                    default:
                        return;
                }
                smoothScrollToPage(SnappingScrollView.this.currentPage);
            } catch (Exception e) {
                Log.e("Fling", "There was an error processing the Fling event:"
                        + e.getMessage());
            }
        }
    }

    private void smoothScrollToPage(int page) {
        smoothScrollTo(page * getMeasuredWidth(), 0);
    }

    private enum SwipeDirection {
        LEFT, RIGHT, NONE;
    }
}
