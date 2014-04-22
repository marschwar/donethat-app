package de.codekenner.roadtrip.view;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by markus on 22.07.13.
 */
public class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD_VELOCITY = 300;

    private final SwipeHandler handler;

    public SwipeGestureDetector(SwipeHandler handler) {
        this.handler = handler;
    }

    public enum SwipeDirection {
        LEFT, RIGHT, NONE;
    }

    public static interface SwipeHandler {

        void onSwipe(SwipeDirection direction);

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        if (handler != null) {
            try {
                final SwipeDirection swipeDirection = getSwipeDirection(e1, e2,
                        velocityX, velocityY);
                switch (swipeDirection) {
                    case RIGHT:
                    case LEFT:
                        handler.onSwipe(swipeDirection);
                        return true;
                    case NONE:
                        return false;
                }
            } catch (Exception e) {
                Log.e("Fling", "There was an error processing the Fling event:"
                        + e.getMessage());
            }
        }
        return false;
    }

    private SwipeDirection getSwipeDirection(MotionEvent e1,
                                             MotionEvent e2, float velocityX, float velocityY) {
        if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            return (velocityX < 0) ? SwipeDirection.RIGHT
                    : SwipeDirection.LEFT;
        }
        return SwipeDirection.NONE;
    }

}
