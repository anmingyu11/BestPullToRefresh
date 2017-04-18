package com.amy.inertia.widget;

import android.view.MotionEvent;
import android.view.View;

import com.amy.inertia.util.LogUtil;
import com.amy.inertia.util.ScrollerUtil;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static com.amy.inertia.widget.TouchHelper.FOOTER_REFRESHING;
import static com.amy.inertia.widget.TouchHelper.HEADER_REFRESHING;
import static com.amy.inertia.widget.TouchHelper.IDLE;
import static com.amy.inertia.widget.TouchHelper.OVER_FLING_FOOTER;
import static com.amy.inertia.widget.TouchHelper.OVER_FLING_HEADER;
import static com.amy.inertia.widget.TouchHelper.OVER_SCROLL_FOOTER;
import static com.amy.inertia.widget.TouchHelper.OVER_SCROLL_HEADER;

final class AViewTouchEventHandler {
    private _IBaseAView mAView;
    private AScrollerController mScrollerController;
    private AViewParams mParams;
    private TouchHelper mTouchHelper;
    private OverScrollHelper mOverScrollHelper;

    AViewTouchEventHandler(_IBaseAView AView,
                           AScrollerController aScrollerController,
                           AViewParams aViewParams,
                           TouchHelper touchHelper,
                           OverScrollHelper overScrollHelper) {
        mAView = AView;
        mScrollerController = aScrollerController;
        mParams = aViewParams;
        mTouchHelper = touchHelper;
        mOverScrollHelper = overScrollHelper;
    }

    boolean handleDispatchTouchEvent(MotionEvent ev) {
        final MotionEvent e = ev;
        switch (e.getActionMasked()) {
            case ACTION_DOWN: {
                mTouchHelper.setTouchLastXY(e);
                break;
            }
            case ACTION_POINTER_DOWN: {
                mTouchHelper.setTouchDXY(e);
                break;
            }
            case ACTION_MOVE: {
                //LogUtil.d("last action : " + mTouchHelper.getLastAction());
                if (mTouchHelper.getLastAction() == ACTION_POINTER_UP) {
                    mTouchHelper.setTouchLastXY(e);
                } else {
                    mTouchHelper.setTouchDXY(e);
                }
                break;
            }
            case ACTION_POINTER_UP: {
                int indexOfUpPointer = e.getActionIndex();
                if (indexOfUpPointer == 0) {
                    mTouchHelper.setTouchLastXY(e);
                }
                break;
            }
            case ACTION_UP: {
                mTouchHelper.resetTouch();
                break;
            }
        }
        return mAView.processDispatchTouchEvent(e);
    }

    boolean handleOnInterceptTouchEvent(MotionEvent e) {
        //Todo : scrolling return true;
        //LogUtil.d("is Finished : " + mScrollerController.isFinished());
        //If scroller still running abort this and handle touchEvent.
        if (!mScrollerController.isFinished()) {
            mScrollerController.abort();
            return true;
        }

        //Todo : refreshing return false
        return mAView.processInterceptTouchEvent(e);
    }

    boolean handleOnTouchEvent(MotionEvent ev) {
        //LogUtil.i("action : " + ev.getActionMasked());
        final MotionEvent e = ev;
        mTouchHelper.storeMotionEvent(e);
        return handleTouchEvent(e);
    }

    private boolean handleTouchEvent(MotionEvent e) {

        final int action = e.getActionMasked();
        final int actionIndex = e.getActionIndex();
        final int pointerId = e.getPointerId(actionIndex);

        switch (action) {
            default: {
                return mAView.processTouchEvent(e);
            }
            case ACTION_DOWN: {
                return onTouchDown(e, 0);
            }
            case ACTION_POINTER_DOWN: {
                return onTouchPointerDown(e, 0);
            }
            case ACTION_MOVE: {
                return onTouchMove(e, pointerId);
            }
            case ACTION_POINTER_UP: {
                return onTouchPointerUp(e, pointerId);
            }
            case ACTION_UP: {
                return onTouchUp(e);
            }
            case ACTION_CANCEL: {
                return onTouchCancel(e);
            }
        }
    }


    private boolean onTouchDown(MotionEvent e, int pointerId) {
        final int touchMode = mTouchHelper.CurrentTouchMode;

        //First abort all Anim
        mScrollerController.abort();

        //Todo Change Header or footer visibility

        switch (touchMode) {
            default: {
                return mAView.processTouchEvent(e);
            }
            case OVER_FLING_FOOTER: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_FOOTER);
                return true;
            }
            case OVER_FLING_HEADER: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_HEADER);
                return true;
            }
            case FOOTER_REFRESHING: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_FOOTER);
                return true;
            }
            case HEADER_REFRESHING: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_HEADER);
                return true;
            }
            case OVER_SCROLL_HEADER:
            case OVER_SCROLL_FOOTER: {
                throw new RuntimeException("this cannot be happend");
            }
        }
    }

    private boolean onTouchPointerDown(MotionEvent e, int pointerId) {
        return mAView.processTouchEvent(e);
    }

    private boolean onTouchMove(MotionEvent e, int pointerId) {
        final int mode = mTouchHelper.CurrentTouchMode;

        boolean needOverScroll = false;

        View v = mAView.getView();

        switch (mode) {
            default: {
                break;
            }
            case IDLE: {
                //To process when the view is first load, or in the origin position, top or bottom.
                if (mTouchHelper.touchDY < 0 && ScrollerUtil.isChildScrollToBottom(v)) {
                    //This is finger scroll up
                    mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_FOOTER);
                } else if (mTouchHelper.touchDY > 0 && ScrollerUtil.isChildScrollToTop(v)) {
                    //This is finger scroll down
                    mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_HEADER);
                }
                break;
            }
            case OVER_FLING_FOOTER: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_FOOTER);
                break;
            }
            case OVER_FLING_HEADER: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_HEADER);
                break;
            }
            case OVER_SCROLL_FOOTER: {
                needOverScroll = true;
                break;
            }
            case OVER_SCROLL_HEADER: {
                needOverScroll = true;
                break;
            }
            case FOOTER_REFRESHING: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_FOOTER);
                break;
            }
            case HEADER_REFRESHING: {
                mTouchHelper.notifyTouchModeChanged(OVER_SCROLL_HEADER);
                break;
            }
        }

        if (needOverScroll) {
            final int deltaY = mTouchHelper.touchDY;
            //final int deltaY = dY;
            LogUtil.d("deltaY : " + deltaY + " maxScroll : " + mParams.maxOverScrollDistance);
            mOverScrollHelper.overScroll(
                    mAView.canViewScrollHorizontal(), mAView.canViewScrollVertical(),
                    0, deltaY,//deltaY
                    0, mAView.getViewTranslationY(),//translation
                    0, 0,//range
                    0, mParams.maxOverScrollDistance,
                    true
            );
            return true;
        } else {
            return mAView.processTouchEvent(e);
        }

    }

    private boolean onTouchPointerUp(MotionEvent e, int pointerId) {
        return mAView.processTouchEvent(e);
    }

    private boolean onTouchUp(MotionEvent e) {
        //Todo
        int mode = mTouchHelper.CurrentTouchMode;

        mTouchHelper.resetTouch();//ResetTouch.

        mScrollerController.abort();//Abort all anim.

        switch (mode) {
            default: {
                return mAView.processTouchEvent(e);
            }
            case OVER_SCROLL_FOOTER: {
                //Todo anim back
                //Todo anim to
                final int transY = mAView.getViewTranslationY();
                LogUtil.d("transY : " + transY);
                boolean springBack = mScrollerController.springBack(0, transY, 0, 0, 0, 0);
                mTouchHelper.notifyTouchModeChanged(OVER_FLING_FOOTER);
                mAView.getView().invalidate();
                LogUtil.d("spring back : " + springBack);
                break;
            }
            case OVER_SCROLL_HEADER: {
                //Todo anim back
                //Todo anim to
                final int transY = mAView.getViewTranslationY();
                LogUtil.d("transY : " + transY);
                boolean springBack = mScrollerController.springBack(0, transY, 0, 0, 0, 0);
                mTouchHelper.notifyTouchModeChanged(OVER_FLING_HEADER);
                mAView.getView().invalidate();
                LogUtil.d("spring back : " + springBack);
                break;
            }
        }
        return false;
    }

    private boolean onTouchCancel(MotionEvent e) {
        final int transY = mAView.getViewTranslationY();
        mTouchHelper.resetTouch();

        //Todo abort all anim
        mScrollerController.abort();
        //Todo to anim 0
        //mScrollerController.springBack(0, transY, 0, 0, 0, 0);
        mAView.setViewTranslationY(0);
        //Todo set touch mode to idle
        mTouchHelper.notifyTouchModeChanged(IDLE);
        return mAView.processTouchEvent(e);
    }
}
