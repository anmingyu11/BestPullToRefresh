package com.amy.inertia.widget;

import android.content.Context;
import android.view.ViewConfiguration;

final class AViewParams {

    private Context mContext;

    int maxOverFlingDistance;
    int maxOverScrollDistance;
    int maxVelocity;
    int minVelocity;

    public AViewParams(Context context) {
        mContext = context;
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
    }
}
