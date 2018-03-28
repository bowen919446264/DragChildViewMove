package com.bowen.dragchildview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

/**
 * drag child view move any position
 * <p>
 * Note: need child view setClickable(true)
 *
 * @author liuwenbo
 */
public class DragChildMoveView extends RelativeLayout {

    private boolean isChildCouldDragAble = true;

    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private float mLastMotionX, mLastMotionY;
    private float mInitialMotionX, mInitialMotionY;

    private View dragView;

    private boolean isAnimRunning;
    private int mWidth;
    private int mHeight;

    public DragChildMoveView(Context context) {
        this(context, null);
    }

    public DragChildMoveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragChildMoveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mWidth = r - l;
        mHeight = b - t;
    }

    @Override
    public final boolean onInterceptTouchEvent(MotionEvent event) {

        if (!isChildCouldDragAble) {
            return false;
        }

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isReadyForDrag()) {
                    final float y = event.getY(), x = event.getX();
                    final float xDiff, yDiff;

                    xDiff = x - mLastMotionX;
                    yDiff = y - mLastMotionY;
                    if (Math.abs(xDiff) > mTouchSlop || Math.abs(yDiff) > mTouchSlop) {//移动
                        mIsBeingDragged = true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                dragView = getPointView((int) event.getX(), (int) event.getY());
                if (isReadyForDrag()) {
                    mLastMotionY = mInitialMotionY = event.getY();
                    mLastMotionX = mInitialMotionX = event.getX();
                    mIsBeingDragged = false;
                }
                break;
            }
        }

        return mIsBeingDragged;
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        if (!isChildCouldDragAble) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }
        if (isAnimRunning) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                if (mIsBeingDragged) {
                    mLastMotionY = event.getY();
                    mLastMotionX = event.getX();
                    onDragMove();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                dragView = getPointView((int) event.getX(), (int) event.getY());
                if (isReadyForDrag()) {
                    mLastMotionY = mInitialMotionY = event.getY();
                    mLastMotionX = mInitialMotionX = event.getX();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onDragEnd();
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void onDragEnd() {
        if (dragView == null && mWidth != 0) {
            return;
        }
        boolean isLeft = dragView.getX() < mWidth / 2;
        float leftD = dragView.getLeft();
        float rightD = mWidth - dragView.getRight();
        float xDistance = Math.min(leftD, rightD);
        float toX = isLeft ? xDistance : mWidth - xDistance - dragView.getWidth();

        float topD = dragView.getTop();
        float bottomD = mHeight - dragView.getBottom();
        float yDistance = Math.min(topD, bottomD);

        boolean isNeedTransY = dragView.getY() < yDistance || dragView.getY() > mHeight - yDistance - dragView.getHeight();
        float toY = dragView.getY();
        if (isNeedTransY) {
            if (dragView.getY() < yDistance) {
                toY = yDistance;
            } else {
                toY = mHeight - yDistance - dragView.getHeight();
            }
        }

        AnimatorSet translateSet = new AnimatorSet();
        ObjectAnimator transX = ObjectAnimator.ofFloat(
                dragView,
                "X",
                dragView.getX(),
                toX
        );

        ObjectAnimator transY = isNeedTransY ? ObjectAnimator.ofFloat(
                dragView,
                "Y",
                dragView.getY(),
                toY
        ) : null;
        translateSet.setDuration(300);
        translateSet.setInterpolator(new FastOutSlowInInterpolator());
        isAnimRunning = true;
        translateSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dragView = null;
                isAnimRunning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                dragView = null;
                isAnimRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (transY == null) {
            translateSet.play(transX);
        } else {
            translateSet.playTogether(transX, transY);
        }
        translateSet.start();
    }

    private View getPointView(int x, int y) {
        if (dragView != null) {
            return dragView;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (isPointViewContains(child, x, y, false)) {
                return child;
            }
        }
        return null;
    }

    private void onDragMove() {
        if (dragView == null) {
            return;
        }
        float toX = mLastMotionX - dragView.getWidth() / 2.0f;
        float toY = mLastMotionY - dragView.getHeight() / 2.0f;
        dragView.setX(toX);
        dragView.setY(toY);
    }

    private boolean isReadyForDrag() {
        return dragView != null;
    }

    public void setChildCouldDragAble(boolean childCouldDragAble) {
        isChildCouldDragAble = childCouldDragAble;
    }

    public static boolean isPointViewContains(View view, int x, int y, boolean useLocOnScreen) {

        int viewX;
        int viewY;

        if (useLocOnScreen) {
            int[] loc = new int[2];
            view.getLocationOnScreen(loc);
            viewX = loc[0];
            viewY = loc[1];
        } else {
            viewX = (int) view.getX();
            viewY = (int) view.getY();
        }

        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        boolean is = !(x < viewX || x > viewX + viewWidth || y < viewY || y > viewY + viewHeight);

        return is;
    }
}
