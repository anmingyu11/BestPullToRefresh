package com.amy.inertia.widget;

import android.widget.FrameLayout;

import com.amy.inertia.interfaces.IFooterView;
import com.amy.inertia.interfaces.IHeaderView;
import com.amy.inertia.interfaces.IPullToRefreshListener;
import com.amy.inertia.util.LogUtil;

import static com.amy.inertia.widget.TouchHelper.OVER_FLING_HEADER;

public final class HeaderFooterController {
    boolean isHeaderRefreshing = false;

    _IBaseAView mAView;

    AViewParams mParams;

    FrameLayout mHeaderContainer;
    FrameLayout mFooterContainer;

    IHeaderView mHeaderView;
    IFooterView mFooterView;

    TouchHelper mTouchHelper;
    AScrollerController mScrollerController;

    PullToRefreshContainer mPullToRefreshContainer;

    HeaderFooterController(PullToRefreshContainer container, FrameLayout headerContainer, FrameLayout footerContainer,
                           IHeaderView headerView, IFooterView footerView,
                           AViewParams aViewParams, _IBaseAView aView,
                           TouchHelper touchHelper, AScrollerController aScrollerController) {
        mHeaderContainer = headerContainer;
        mFooterContainer = footerContainer;
        mHeaderView = headerView;
        mFooterView = footerView;
        mParams = aViewParams;
        mAView = aView;

        mPullToRefreshContainer = container;
        mTouchHelper = touchHelper;
        mScrollerController = aScrollerController;
    }

    void finishHeaderRefresh() {
        LogUtil.d("finishHeaderRefresh");
        isHeaderRefreshing = false;
        for (IPullToRefreshListener pullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            pullToRefreshListener.onFinishHeaderRefresh();
        }
        if (mAView != null) {
            boolean springBack = mScrollerController.springBack(0, mAView.getViewTranslationY(), 0, 0, 0, 0);
            mTouchHelper.notifyTouchModeChanged(OVER_FLING_HEADER);
            mAView.getView().invalidate();
        }
    }

    void finishFooterRefresh() {
    }

    void changeHeaderOrFooterVisibility(boolean headerShow, boolean footerShow) {
        //LogUtil.d("headerShow : " + headerShow + " footerShow : " + footerShow);
        if (mHeaderView != null) {
            mHeaderView.setVisible(headerShow);
        }

        if (mFooterView != null) {
            mFooterView.setVisible(footerShow);
        }
    }

    void pullingHeader(final float currentHeight) {
        if (mHeaderView == null) {
            return;
        }

        float fraction = currentHeight / mParams.headerTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mHeaderContainer.getLayoutParams().height = (int) currentHeight;
        LogUtil.e("requestLayout");
        mHeaderContainer.requestLayout();

        mHeaderView.onPulling(fraction);

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingHeader(fraction, currentHeight);
        }
    }

    void pullingFooter(float currentHeight) {
        if (mFooterView == null) {
            return;
        }

        float fraction = currentHeight / mParams.footerTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mFooterContainer.getLayoutParams().height = (int) currentHeight;
        mFooterContainer.requestLayout();

        mFooterView.onPulling(fraction);

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingFooter(fraction, currentHeight);
        }
    }

    void headerReleasing(float currentHeight) {
        float fraction = currentHeight / mParams.headerTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mHeaderContainer.getLayoutParams().height = (int) currentHeight;
        mHeaderContainer.requestLayout();

        if (mHeaderView != null) {
            mHeaderView.onPulling(fraction);
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingHeader(fraction, currentHeight);
        }
    }

    void footerReleasing(float currentHeight) {
        float fraction = currentHeight / mParams.footerTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mFooterContainer.getLayoutParams().height = (int) currentHeight;
        mFooterContainer.requestLayout();

        if (mFooterView != null) {
            mFooterView.onPulling(fraction);
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingFooter(fraction, currentHeight);
        }
    }

    void headerRefresh() {
        if (mHeaderView != null) {
            mHeaderView.onRefresh(mParams.headerPullMaxHeight, mAView.getViewTranslationY());
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            iPullToRefreshListener.onHeaderRefresh();
        }
    }

    void footerRefresh() {
        if (mFooterView != null) {
            mFooterView.onRefresh(mParams.footerPullMaxHeight, mAView.getViewTranslationY());
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshContainer.mPullToRefreshListeners) {
            iPullToRefreshListener.onFooterRefresh();
        }
    }

}