package com.amy.inertia.widget;

import android.view.MotionEvent;
import android.view.View;

interface _IBaseAView {

    View getView();

    void attachToParent(PullToRefreshContainer container);

    void notifyHeaderRefreshing();

    boolean canViewScrollVertical();

    boolean canViewScrollHorizontal();

    void setViewTranslationY(int transY);

    void realSetTranslationY(int transY);

    int getViewTranslationY();

    boolean processDispatchTouchEvent(MotionEvent e);

    boolean processInterceptTouchEvent(MotionEvent e);

    boolean processTouchEvent(MotionEvent e);

    boolean isInTouching();

}
