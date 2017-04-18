package com.amy.inertia.widget;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.amy.inertia.interfaces.OnTouchModeChangeListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.amy.inertia.util.ScrollerUtil.isChildScrollInContent;
import static com.amy.inertia.util.ScrollerUtil.isChildScrollToBottom;
import static com.amy.inertia.util.ScrollerUtil.isChildScrollToTop;

final class TouchHelper {

    private _IBaseAView mView;
    boolean isScrollToTop;
    boolean isScrollToBottom;
    boolean isScrollInContent;

    TouchHelper(_IBaseAView iBaseAView) {
        mView = iBaseAView;
        registerOnScrollChangedListener();
    }

    void onIsTouchingChanged() {
        /*
        if (!mView.isInTouching()) {
            final long l = mScrollChecker.lastScrollTime;
            mScrollChecker.checkIsStopScroll(l);
        }
        */
    }

    private final ScrollChecker mScrollChecker = new ScrollChecker();

    private final class ScrollChecker {
        //Check if is scrolling.
        private long lastScrollTime = 0;
        private static final int CHECK_SCROLL_STOP_DELAY_MILLS = 75;// human eyes can receive this fps = 30
        private static final int MSG_SCROLL = 1;

        boolean isInTouching;

        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SCROLL) {
                    boolean notify = !(isInOverFling() || isInOverScroll());
                    if (notify) {
                        notifyTouchModeChanged(IDLE);
                    }
                }
            }
        };

        private void checkIsStopScroll(long l) {
            mHandler.removeMessages(MSG_SCROLL);
            Message msg = new Message();
            msg.what = MSG_SCROLL;
            msg.obj = l;
            //mHandler.sendEmptyMessageDelayed(MSG_SCROLL, CHECK_SCROLL_STOP_DELAY_MILLS);
            mHandler.sendMessageDelayed(msg, CHECK_SCROLL_STOP_DELAY_MILLS);
        }

    }

    //--------------------------ScrollChangedListener--------------------------
    private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {

        private void scrollPositionDetect() {
            final View v = mView.getView();
            isScrollToTop = isChildScrollToTop(v);
            isScrollToBottom = isChildScrollToBottom(v);
            isScrollInContent = isChildScrollInContent(v);
        }

        /**
         * This will be called when the first time view was displayed in window.
         * So set a state as UNUSED.
         */

        @Override
        public void onScrollChanged() {
            //Detect position you scroll.
            scrollPositionDetect();
            //use is inTouching or not to determine the touch mode.
            final boolean isInTouching = mScrollChecker.isInTouching = mView.isInTouching();
            if (isScrollToTop) {
                if (isInTouching) {
                    notifyTouchModeChanged(OVER_SCROLL_HEADER);
                } else {
                    notifyTouchModeChanged(OVER_FLING_HEADER);
                }
            } else if (isScrollToBottom) {
                if (isInTouching) {
                    notifyTouchModeChanged(OVER_SCROLL_FOOTER);
                } else {
                    notifyTouchModeChanged(OVER_FLING_FOOTER);
                }
            } else if (isScrollInContent) {
                if (isInTouching) {
                    notifyTouchModeChanged(DRAGGING_IN_CONTENT);
                } else {
                    notifyTouchModeChanged(SETTLING_IN_CONTENT);
                }
            }
           /*
            if (!isInTouching) {
                mScrollChecker.lastScrollTime = System.currentTimeMillis();
                final long l = mScrollChecker.lastScrollTime;
                mScrollChecker.checkIsStopScroll(l);
            }
            */
        }

    };

    private void registerOnScrollChangedListener() {
        final ViewTreeObserver observer = mView.getView().getViewTreeObserver();
        observer.removeOnScrollChangedListener(mOnScrollChangedListener);
        observer.addOnScrollChangedListener(mOnScrollChangedListener);
    }

    //--------------------------TouchModeChangeListener--------------------------
    private final List<OnTouchModeChangeListener> mOnTouchModeChangeListeners = new ArrayList<OnTouchModeChangeListener>();

    boolean isInOverFling() {
        final int mode = CurrentTouchMode;
        if (mode == OVER_FLING_FOOTER || mode == OVER_FLING_HEADER) {
            return true;
        } else {
            return false;
        }
    }

    boolean isInOverScroll() {
        final int mode = CurrentTouchMode;
        if (mode == OVER_SCROLL_FOOTER || mode == OVER_SCROLL_HEADER) {
            return true;
        } else {
            return false;
        }
    }

    boolean addScrollDetectorListener(OnTouchModeChangeListener onTouchModeChangeListener) {
        if (mOnTouchModeChangeListeners.contains(onTouchModeChangeListener)) {
            return false;
        } else {
            return mOnTouchModeChangeListeners.add(onTouchModeChangeListener);
        }
    }

    void clearScrollDetectorListener() {
        mOnTouchModeChangeListeners.clear();
    }

    boolean removeScrollDetectorListener(OnTouchModeChangeListener onTouchModeChangeListener) {
        return mOnTouchModeChangeListeners.remove(onTouchModeChangeListener);
    }

    //--------------------------TouchMode--------------------------
    int LastTouchMode = UNUSED;
    int CurrentTouchMode = UNUSED;

    static final String[] TOUCH_MODES = new String[]{
            "IDLE",//0
            "DRAGGING_IN_CONTENT",//1
            "SETTLING_IN_CONTENT",//2
            "OVER_SCROLL_HEADER",//3
            "OVER_SCROLL_FOOTER",//4
            "OVER_FLING_HEADER",//5
            "OVER_FLING_FOOTER",//6
            "HEADER_REFRESHING",//7
            "FOOTER_REFRESHING",//8,
            "UNUSED"//9
    };

    static final int UNUSED = 9;
    static final int IDLE = 0;
    static final int DRAGGING_IN_CONTENT = 1;
    static final int SETTLING_IN_CONTENT = 2;
    static final int OVER_SCROLL_HEADER = 3;
    static final int OVER_SCROLL_FOOTER = 4;
    static final int OVER_FLING_HEADER = 5;
    static final int OVER_FLING_FOOTER = 6;
    static final int HEADER_REFRESHING = 7;
    static final int FOOTER_REFRESHING = 8;

    void setTouchMode(int touchMode) {
        LastTouchMode = CurrentTouchMode;
        CurrentTouchMode = touchMode;
    }

    boolean notifyTouchModeChanged(int newTouchMode) {
        LogUtil.w("newTouchMode : " + TOUCH_MODES[newTouchMode]);
        //LogUtil.printTraceStack("");
        //First time .
        if (CurrentTouchMode == UNUSED) {
            setTouchMode(IDLE);
            //LogUtil.printTraceStack("where");
            /*
            LogUtil.e("----------------");
            LogUtil.d("CurrentTouchMode : " + TOUCH_MODES[CurrentTouchMode]);
            LogUtil.i("LastTouchMode: " + TOUCH_MODES[LastTouchMode]);
            LogUtil.e("----------------");
            */
            for (OnTouchModeChangeListener onTouchModeChangeListener : mOnTouchModeChangeListeners) {
                onTouchModeChangeListener.onTouchModeChanged(IDLE);
            }
            return true;
        } else if (CurrentTouchMode != newTouchMode) {
            setTouchMode(newTouchMode);
            //LogUtil.printTraceStack("where");
            /*
            LogUtil.e("----------------");
            LogUtil.d("CurrentTouchMode : " + TOUCH_MODES[CurrentTouchMode]);
            LogUtil.i("LastTouchMode: " + TOUCH_MODES[LastTouchMode]);
            LogUtil.e("----------------");
            */
            for (OnTouchModeChangeListener onTouchModeChangeListener : mOnTouchModeChangeListeners) {
                onTouchModeChangeListener.onTouchModeChanged(newTouchMode);
            }
            return true;
        } else {
            return false;
        }
    }

    //--------------------------MotionEvents--------------------------
    private final static int EVENT_BUFFER_SIZE = 3;
    final Queue<MotionEvent> MotionEvents = new LinkedList<MotionEvent>();
    private int lastMotionEventAction;

    void storeMotionEvent(MotionEvent e) {
        //LogUtil.d("sto : " + MotionEvents.size());
        if (MotionEvents.size() == EVENT_BUFFER_SIZE) {
            MotionEvents.poll();
            MotionEvents.offer(e);
        } else {
            MotionEvents.offer(e);
        }
        lastMotionEventAction = e.getActionMasked();
    }

    MotionEvent[] getMotionEvents() {
        MotionEvent[] motionEvents = new MotionEvent[EVENT_BUFFER_SIZE];
        for (int i = 0; i < EVENT_BUFFER_SIZE; i++) {
            motionEvents[i] = MotionEvents.peek();
            //LogUtil.i(" " + i + " : " + motionEvents[i].getActionMasked());
        }
        return motionEvents;
    }

    int getLastAction() {
        return lastMotionEventAction;
    }

    //--------------------------TouchParams--------------------------
    int touchLastX;
    int touchLastY;
    int touchDX;
    int touchDY;

    void resetTouch() {
        touchLastX = (int) 0f;
        touchLastY = (int) 0f;
        touchDX = (int) 0f;
        touchDY = (int) 0f;
    }

    void setTouchLastXY(MotionEvent e) {
        touchLastX = (int) (e.getRawX() + 0.5f);
        touchLastY = (int) (e.getRawY() + 0.5f);
    }

  /*
    void setTouchLastXY(MotionEvent e) {
        touchLastX = (int) e.getX();
        touchLastY = (int) e.getY();
    }
    */

  /*
    void setTouchLastXY(MotionEvent e, int pointerIndex) {
        touchLastX = (int) e.getX(pointerIndex);
        touchLastY = (int) e.getY(pointerIndex);
    }
    */

    void setTouchLastXY(float X, float Y) {
        touchLastX = (int) (X + 0.5f);
        touchLastY = (int) (Y + 0.5f);
    }

    /*
        void setTouchDXY(MotionEvent e) {
            float X = e.getX();
            float Y = e.getY();
            touchDX = (int) (X - touchLastX);
            touchDY = (int) (Y - touchLastY);
            setTouchLastXY(X, Y);
        }
    */
    void setTouchDXY(MotionEvent e) {
        float X = e.getRawX();
        float Y = e.getRawY();
        touchDX = (int) (X - touchLastX);
        touchDY = (int) (Y - touchLastY);
        //setTouchLastXY(e);
        setTouchLastXY(X, Y);
    }
}
