package com.amy.inertia.widget;

import android.content.Context;

import com.amy.inertia.util.LogUtil;

import static com.amy.inertia.widget.TouchHelper.IDLE;
import static com.amy.inertia.widget.TouchHelper.SETTLING_IN_CONTENT;

final class AScrollerController {
    int ScrollState;
    final static int FLING = 0;
    final static int OVER_FLING = 1;
    final static int SPRING_BACK = 2;
    //final static int OVER_SCROLL = 3;
    final static String[] ScrollStates = new String[]{
            "FLING", "OVER_FLING", "SPRING_BACK", "OVER_SCROLL"
    };

    private Context mContext;
    private AViewParams mParams;

    private final AScroller mScroller;
    private final TouchHelper mTouchHelper;
    private final _IBaseAView mAView;

    AScrollerController(Context context,
                        TouchHelper touchHelper,
                        _IBaseAView aView,
                        AViewParams params) {
        mScroller = new AScroller(context, null, false);
        mContext = context;
        mTouchHelper = touchHelper;
        mAView = aView;
        mParams = params;
    }

    void setScrollState(int state) {
        ScrollState = state;
    }

    void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, minY, minY, maxY);
        setScrollState(FLING);
    }

    boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        boolean springBackResult = mScroller.springBack(startX, startY, minX, maxX, minY, maxY);
        if (springBackResult) {
            setScrollState(SPRING_BACK);
        }
        return springBackResult;
    }

    void abort() {
        mScroller.abortAnimation();
    }

    boolean isFinished() {
        return mScroller.isFinished();
    }

    int timePassed() {
        return mScroller.timePassed();
    }

    int getCurrY() {
        return mScroller.getCurrY();
    }

    int getCurrVelocity() {
        return (int) mScroller.getCurrVelocity();
    }

    boolean computeOffset() {
        LogUtil.v("vel : " + mScroller.getCurrVelocity());
        LogUtil.v("y : " + mScroller.getCurrY());

        //boolean computeScrollOffset = mScroller.computeScrollOffset();

        final int state = ScrollState;
        boolean time = mScroller.getDuration() > mScroller.timePassed();
        boolean computeScrollResult = mScroller.computeScrollOffset();
        LogUtil.d(" computeScrollOffset : " + computeScrollResult +
                " time : " + time +
                " isFinished : " + isFinished() +
                " state : " + ScrollStates[state]);
        //Todo this is not correct in fling.
        //Give a idle state when this finished
        if (computeScrollResult) {
            if (state == SPRING_BACK && !time) {
                mScroller.abortAnimation();
                mAView.notifyHeaderRefreshing();
                return false;
            }
            return true;
        } else {
            mScroller.abortAnimation();
            if (state == OVER_FLING) {
                //Not Fling
                mAView.setViewTranslationY(0);
                mTouchHelper.notifyTouchModeChanged(IDLE);
            } else if (state == FLING) {
                //If is fling
                mTouchHelper.notifyTouchModeChanged(IDLE);
            }
            return false;
        }
    }

    private int getOverFlingDistance() {
        final int vel = (int) mScroller.getCurrVelocity();
        final int overDistance = (int) (vel / (float) mParams.maxVelocity * mParams.maxOverFlingDistance);
        return overDistance;
    }

    void notifyTopEdgeReached() {
        if (mTouchHelper.LastTouchMode != SETTLING_IN_CONTENT) {
            LogUtil.e("not from settling to over fling");
            return;
        }
        //final int transY = mAView.getViewTranslationY();Todo : what?
        final int overFlingDistance = getOverFlingDistance();
        LogUtil.e("topEdgeReached : " + " overFling distance " + overFlingDistance);
        mScroller.notifyVerticalEdgeReached(0, 0, overFlingDistance);

        setScrollState(OVER_FLING);
    }

    void notifyBottomEdgeReached() {
        if (mTouchHelper.LastTouchMode != SETTLING_IN_CONTENT) {
            LogUtil.e("not from settling to over fling");
            return;
        }
        //final int transY = mAView.getViewTranslationY();Todo : what?
        final int overFlingDistance = getOverFlingDistance();
        LogUtil.e("bottomEdgeReached : " + " overFling distance " + overFlingDistance);
        mScroller.notifyVerticalEdgeReached(0, 0, overFlingDistance);

        setScrollState(OVER_FLING);
    }

}
