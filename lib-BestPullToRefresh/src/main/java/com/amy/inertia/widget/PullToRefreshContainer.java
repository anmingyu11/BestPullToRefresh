package com.amy.inertia.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amy.inertia.interfaces.IFooterView;
import com.amy.inertia.interfaces.IHeaderView;
import com.amy.inertia.interfaces.IPullToRefreshListener;
import com.amy.inertia.interfaces.OnTouchModeChangeListener;
import com.amy.inertia.util.LogUtil;

import static com.amy.inertia.widget.TouchHelper.OVER_FLING_FOOTER;
import static com.amy.inertia.widget.TouchHelper.OVER_FLING_HEADER;

public class PullToRefreshContainer extends FrameLayout {

    private Context mContext;

    private FrameLayout mHeaderContainer;
    private FrameLayout mFooterContainer;

    //Header and Footer view.
    private IHeaderView mHeaderView;
    private IFooterView mFooterView;

    //ChildView
    private _IBaseAView mAView;

    AViewParams mParams;
    AScrollerController mScrollerController;
    OverScrollHelper mOverScrollHelper;
    TouchHelper mTouchHelper;
    AViewTouchEventHandler mTouchEventHandler;

    HeaderFooterController mHeaderFooterController;

    public PullToRefreshContainer(Context context) {
        this(context, null, 0);
    }

    public PullToRefreshContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshContainer(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            return;
        }

        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        init();
    }

    private void init() {

        initChildView();

        initFooterAndHeader();

        initParams();

    }

    private void initParams() {
        mParams = new AViewParams(mContext);
        mOverScrollHelper = new OverScrollHelper(new OverScrollHelper.OverScrollListener() {
            @Override
            public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
                LogUtil.e("scrollY : " + scrollY);
                if (mTouchHelper.isInOverScroll()) {
                    mAView.setViewTranslationY(scrollY);
                } else if (mTouchHelper.isInOverFling()) {
                    mAView.setViewTranslationY(-scrollY);
                }
            }
        });
        mTouchHelper = new TouchHelper(mAView);
        mScrollerController = new AScrollerController(mContext, mTouchHelper, mAView, mParams);
        mTouchEventHandler = new AViewTouchEventHandler(mAView, mScrollerController, mParams, mTouchHelper, mOverScrollHelper);
        mHeaderFooterController = new HeaderFooterController(mHeaderContainer, mFooterContainer, mHeaderView, mFooterView, mParams, mAView);

        mAView.attachToParent(this);
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

    private void initChildView() {
        if (getChildCount() != 1) {
            throw new RuntimeException("Child count : " + getChildCount());
        } else {
            if (getChildAt(0) instanceof _IBaseAView) {
                mAView = (_IBaseAView) getChildAt(0);
            } else {
                throw new IllegalArgumentException("Child view must be AView ");
            }
        }

        if (mAView == null) {
            throw new NullPointerException("ChildView cannot be null.");
        }
    }

    private void initFooterAndHeader() {
        //Init Header container
        mHeaderContainer = new FrameLayout(getContext());
        LayoutParams headerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        headerLayoutParams.gravity = Gravity.TOP;
        mHeaderContainer.setLayoutParams(headerLayoutParams);
        addView(mHeaderContainer, 0);

        //Init Footer container
        mFooterContainer = new FrameLayout(getContext());
        LayoutParams footerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        footerLayoutParams.gravity = Gravity.BOTTOM;
        mFooterContainer.setLayoutParams(footerLayoutParams);
        addView(mFooterContainer, 0);
    }

    public void setHeaderView(final IHeaderView iHeaderView) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeaderContainer.removeAllViewsInLayout();
                mHeaderContainer.addView(mHeaderView.getView());
            }
        });
        mHeaderView = iHeaderView;
    }

    public void setFooterView(final IFooterView iFooterView) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeaderContainer.removeAllViewsInLayout();
                mHeaderContainer.addView(iFooterView.getView());
            }
        });
        mFooterView = iFooterView;
    }

    public boolean addIPullListener(IPullToRefreshListener iPullToRefreshListener) {
        return mHeaderFooterController.mPullToRefreshListeners.add(iPullToRefreshListener);
    }

    public boolean removeIPullListener(IPullToRefreshListener iPullToRefreshListener) {
        return mHeaderFooterController.mPullToRefreshListeners.remove(iPullToRefreshListener);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void updateHeader(){
       mAView.realSetTranslationY(mHeaderContainer.getLayoutParams().height);
    }
}