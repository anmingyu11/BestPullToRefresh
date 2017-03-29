package com.amy.inertia.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.amy.inertia.interfaces.OnTouchModeChangeListener;
import com.amy.inertia.util.LogUtil;

import static com.amy.inertia.widget.TouchHelper.IDLE;
import static com.amy.inertia.widget.TouchHelper.OVER_FLING_FOOTER;
import static com.amy.inertia.widget.TouchHelper.OVER_FLING_HEADER;
import static com.amy.inertia.widget.TouchHelper.SETTLING_IN_CONTENT;

public class ARecyclerView extends RecyclerView implements
        _IBaseAView {

    private Context mContext;

    private boolean isInTouching = false;

    //Inside helpers.
    private final AScrollerController mScrollerController;
    private final OverScrollHelper mOverScrollHelper;
    private final TouchHelper mTouchHelper;
    private final AViewTouchEventHandler mTouchEventHandler;
    //Public params.
    private final AViewParams mParams;

    public ARecyclerView(Context context) {
        this(context, null, 0);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        mParams = new AViewParams(mContext);
        mOverScrollHelper = new OverScrollHelper(new OverScrollHelper.OverScrollListener() {
            @Override
            public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
                if (mTouchHelper.isInOverScroll()) {
                    setViewTranslationY(scrollY);
                } else if (mTouchHelper.isInOverFling()) {
                    setViewTranslationY(-scrollY);
                }
            }
        });

        mTouchHelper = new TouchHelper(this);
        mScrollerController = new AScrollerController(mContext, mTouchHelper, this, mParams);
        mTouchEventHandler = new AViewTouchEventHandler(this, mScrollerController, mParams, mTouchHelper, mOverScrollHelper);

        mTouchHelper.addScrollDetectorListener(new OnTouchModeChangeListener() {
            @Override
            public void onTouchModeChanged(int touchMode) {
                LogUtil.d("Touch mode : " + TouchHelper.TOUCH_MODES[touchMode]);
                switch (touchMode) {
                    case OVER_FLING_FOOTER: {
                        mScrollerController.notifyBottomEdgeReached();//Todo error will be
                        break;
                    }
                    case OVER_FLING_HEADER: {
                        mScrollerController.notifyTopEdgeReached();//Todo error will be
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        //Calculate overFling distance here.
        final int overScrollDistance = b - t;
        final int overFlingDistance = overScrollDistance / 2;

        mParams.maxOverScrollDistance = overScrollDistance;
        mParams.maxOverFlingDistance = overFlingDistance;
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
        if (changed) {
            mTouchHelper.onIsTouchingChanged();
        }
    }

    //--------------------Internal API--------------------
    @Override
    public View getView() {
        return this;
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
    public void setViewTranslationY(int translationY) {
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
        if (transY == 0 && mTouchHelper.LastTouchMode != SETTLING_IN_CONTENT) {//Todo this need to be optimized.
            mScrollerController.abort();
            setTranslationY(transY);
            mTouchHelper.notifyTouchModeChanged(IDLE);
        } else {
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

    @Override
    public int getViewScrollState() {
        return getScrollState();
    }

}
