package com.amy.inertia.widget;

import android.content.Context;
import android.view.ViewConfiguration;

final class AViewParams {

    private Context mContext;

    int maxOverFlingDistance;
    int maxOverScrollDistance;
    int maxVelocity;
    int minVelocity;

    //OverFling switch
    boolean isEnableOverFling = true;
    boolean isEnableOverFlingHeaderShow = false;
    boolean isEnableOverFlingFooterShow = false;

    //OverFlingParams
    int overFlingDuration = 100;
    int overFlingMaxVY = 300;

    //OverScroll
    boolean isEnableOverScroll = true;
    boolean isEnableHeaderOverScroll = true;
    boolean isEnableFooterOverScroll = true;
    boolean isEnableOverScrollHeaderShow = true;
    boolean isEnableOverScrollFooterShow = true;

    //OverScrollParams
    float mOverScrollDamp = 0.5f;

    //Refresh
    boolean isEnableHeaderPullToRefresh = true;
    boolean isEnableFooterPullToRefresh = true;
    boolean isHeaderRefreshing = false;
    boolean isFooterRefreshing = false;

    //ScrollBackParams
    int scrollBackAnimMinDuration = 600;
    int scrollBackAnimMaxDuration = 1200;
    float scrollBackDamp = 7f / 10f;

    //ScrollToParams
    int scrollToAnimMinDuration = 300;
    int scrollToAnimMaxDuration = 600;

    //Trigger and Max params
    int headerPullMaxHeight = 2240;
    int footerPullMaxHeight = 2240;
    int headerTriggerRefreshHeight = 500;
    int footerTriggerRefreshHeight = 500;

    public AViewParams(Context context) {
        mContext = context;
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
    }
}
