package com.amy.inertia.widget;

import android.view.MotionEvent;
import android.view.View;

interface _IBaseAView {

    View getView();

    boolean canViewScrollVertical();

    boolean canViewScrollHorizontal();

    void setViewTranslationY(int transY);

    int getViewTranslationY();

    boolean processDispatchTouchEvent(MotionEvent e);

    boolean processInterceptTouchEvent(MotionEvent e);

    boolean processTouchEvent(MotionEvent e);

    boolean isInTouching();

}
