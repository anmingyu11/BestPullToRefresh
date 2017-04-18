package com.amy.inertia.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.amy.inertia.util.LogUtil;

import static com.amy.inertia.widget.TouchHelper.IDLE;
import static com.amy.inertia.widget.TouchHelper.SETTLING_IN_CONTENT;

public class ARecyclerView extends RecyclerView implements
        _IBaseAView {

    private Context mContext;

    private boolean isInTouching = false;

    PullToRefreshContainer mPullToRefreshContainer;

    //Inside helpers.
    AScrollerController mScrollerController;
    OverScrollHelper mOverScrollHelper;
    TouchHelper mTouchHelper;
    AViewTouchEventHandler mTouchEventHandler;
    HeaderFooterController mHeaderFooterController;
    //Public params.
    AViewParams mParams;

    boolean isPullingHeader;
    boolean isPullingFooter;

    public ARecyclerView(Context context) {
        this(context, null, 0);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        //Calculate overFling distance here.
        final int overScrollDistance = b - t;
        final int overFlingDistance = overScrollDistance / 2;

        mParams.maxOverScrollDistance = overScrollDistance;
        mParams.maxOverFlingDistance = overFlingDistance;

        LogUtil.d("overScrollDistance : " + overScrollDistance
                + ("\noverFlingDistance : " + overFlingDistance));

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Todo : what
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        LogUtil.d("vY : " + velocityY);

        boolean r = super.fling(velocityX, velocityY);
        //Our scroller will fling while view-inside scroller fling.
        if (Math.abs(velocityY) > mParams.minVelocity) {
            mScrollerController.fling(0, 0, 0, velocityY, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return r;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //Todo may be Need Optimized

        boolean isInOverFling = mTouchHelper.isInOverFling();

        if (mScrollerController.isFinished()) {
            //LogUtil.d("scroller is finished");
            return;
        }

        //Compute offset and if is in over fling state, do translation y, then post this to next invalidate.
        boolean computeResult = mScrollerController.computeOffset();

        //LogUtil.d("scroller controller compute result : " + computeResult + " isInOverFling : " + isInOverFling);

//        LogUtil.d("state : " + AScrollerController.ScrollStates[mScrollerController.ScrollState]
//                + " computeResult : " + computeResult
//                + " isInOverFling : " + isInOverFling);
        if (computeResult && isInOverFling) {
            if (mScrollerController.ScrollState == AScrollerController.SPRING_BACK) {
                final int currY = mScrollerController.getCurrY();
                //LogUtil.d("spring back : " + currY);
                setViewTranslationY(currY);
            } else if (mScrollerController.ScrollState == AScrollerController.OVER_FLING) {
                //do overScroll
                final int transY = getViewTranslationY();
                final int currY = mScrollerController.getCurrY();
                final int deltaY = currY - transY;

                //LogUtil.d("over fling : " + "deltaY : " + deltaY + " currY : " + mScrollerController.getCurrY() + " transY : " + transY);
                mOverScrollHelper.overScroll(
                        canViewScrollHorizontal(), canViewScrollVertical(),
                        0, deltaY,
                        0, transY,
                        0, 0,
                        0, mParams.maxOverFlingDistance,
                        false);
            }
            invalidate();
        }
    }

    //--------------------Handle Touch Event--------------------
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mTouchEventHandler.handleDispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return mTouchEventHandler.handleOnInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        setInTouching(e);
        return mTouchEventHandler.handleOnTouchEvent(e);
    }

    private void setInTouching(MotionEvent e) {
        boolean changed = false;
        int action = e.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (isInTouching) {
                    changed = true;
                }
                isInTouching = false;
                break;
            }
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                if (!isInTouching) {
                    changed = true;
                }
                isInTouching = true;
                break;
            }
        }
       /*
        if (changed) {
            mTouchHelper.onIsTouchingChanged();
        }
        */
    }

    //--------------------Internal API--------------------
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void attachToParent(PullToRefreshContainer container) {
        mPullToRefreshContainer = container;
        //Attaching params
        mParams = container.mParams;
        mTouchEventHandler = container.mTouchEventHandler;
        mOverScrollHelper = container.mOverScrollHelper;
        mScrollerController = container.mScrollerController;
        mTouchHelper = container.mTouchHelper;
        mHeaderFooterController = container.mHeaderFooterController;
    }

    @Override
    public boolean canViewScrollVertical() {
        return computeVerticalScrollRange() > computeVerticalScrollExtent();
    }

    @Override
    public boolean canViewScrollHorizontal() {
        return computeHorizontalScrollRange() > computeHorizontalScrollExtent();
    }

    @Override
    public void realSetTranslationY(int transY) {
        setTranslationY(transY);
    }

    @Override
    public void setViewTranslationY(int translationY) {
        LogUtil.d("translationY : " + translationY);
        int transY = 0;
        if (translationY > 0 && getViewTranslationY() < 0) {
            transY = 0;
        } else if (translationY < 0 && getViewTranslationY() > 0) {
            transY = 0;
        } else if (Float.isNaN(translationY)) {
            transY = 0;
        } else {
            transY = translationY;
        }

        //LogUtil.d("last mode : " + TOUCH_MODES[mTouchHelper.LastTouchMode]
        //        + " curr mode : " + TOUCH_MODES[mTouchHelper.CurrentTouchMode]);
        final int finalTransY = transY;
        if (transY == 0 && mTouchHelper.LastTouchMode != SETTLING_IN_CONTENT) {//Todo this need to be optimized.
            mScrollerController.abort();
            post(new Runnable() {
                @Override
                public void run() {
                    setTranslationY(finalTransY);
                }
            });
            mTouchHelper.notifyTouchModeChanged(IDLE);
        } else {
            pulling(finalTransY);
        }
    }

    private void pulling(int transY) {
        if (transY > 0) {
            mHeaderFooterController.pullingHeader(transY);
        } else if (transY < 0) {
            mHeaderFooterController.pullingFooter(transY);
            setTranslationY(transY);
        }
    }

    @Override
    public int getViewTranslationY() {
        return (int) getTranslationY();
    }

    @Override
    public boolean processDispatchTouchEvent(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean processInterceptTouchEvent(MotionEvent e) {
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean processTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }

    @Override
    public boolean isInTouching() {
        return isInTouching;
    }

}
