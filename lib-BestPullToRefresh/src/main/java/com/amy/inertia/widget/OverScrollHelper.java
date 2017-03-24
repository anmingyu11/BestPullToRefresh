package com.amy.inertia.widget;

final class OverScrollHelper {
    interface OverScrollListener {
        void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY);
    }

    private final OverScrollListener mOverScrollListener;

    OverScrollHelper(OverScrollListener overScrollListener) {
        mOverScrollListener = overScrollListener;
    }

    /**
     * see this {@link android.view.View#overScrollBy(int, int, int, int, int, int, int, int, boolean)}
     *
     * @param canScrollHorizontal
     * @param canScrollVertical
     * @param deltaX
     * @param deltaY
     * @param scrollX
     * @param scrollY
     * @param scrollRangeX
     * @param scrollRangeY
     * @param maxOverScrollX
     * @param maxOverScrollY
     * @param isTouchEvent
     * @return
     */
    boolean overScroll(boolean canScrollHorizontal, boolean canScrollVertical,
                       int deltaX, int deltaY,
                       int scrollX, int scrollY,
                       int scrollRangeX, int scrollRangeY,
                       int maxOverScrollX, int maxOverScrollY,
                       boolean isTouchEvent) {
        //_IBaseAView v = mTargetView;
        //final boolean canViewScrollHorizontal =
        //        v._computeHorizontalScrollRange() > v._computeHorizontalScrollExtent();
        //final boolean canViewScrollVertical =
        //        v._computeVerticalScrollRange() > v._computeVerticalScrollExtent();

        int newScrollX = scrollX + deltaX;
        if (isTouchEvent && ((scrollX >= scrollRangeX && deltaX >= 0) || (scrollX <= 0 && deltaX <= 0))) {
            int overScrollX = scrollX > 0 ? scrollX - scrollRangeX : -scrollX;
            float caliDelta = 0;

            if (overScrollX < 0.5 * maxOverScrollX) {
                caliDelta = (float) maxOverScrollX / (overScrollX * 12 + maxOverScrollX) * deltaX;
            } else if (overScrollX < 0.67 * maxOverScrollX) {
                caliDelta = (float) maxOverScrollX / (overScrollX * 20 + maxOverScrollX) * deltaX;
            } else {
                if (Math.abs(deltaX) > 20) {
                    caliDelta = deltaX > 0 ? 1 : -1;
                } else {
                    caliDelta = 0;
                }
            }

            newScrollX = scrollX + (int) caliDelta;
        }

        int newScrollY = scrollY + deltaY;
        if (isTouchEvent && ((scrollY >= scrollRangeY && deltaY >= 0) || (scrollY <= 0 && deltaY <= 0))) {
            int overScrollY = scrollY > 0 ? scrollY - scrollRangeY : -scrollY;
            float caliDelta = 0;

            if (overScrollY < 0.5 * maxOverScrollY) {
                caliDelta = (float) maxOverScrollY / (overScrollY * 12 + maxOverScrollY) * deltaY;
            } else if (overScrollY < 0.67 * maxOverScrollY) {
                caliDelta = (float) maxOverScrollY / (overScrollY * 20 + maxOverScrollY) * deltaY;
            } else {
                if (Math.abs(deltaY) > 20) {
                    caliDelta = deltaY > 0 ? 1 : -1;
                } else {
                    caliDelta = 0;
                }
            }

            newScrollY = scrollY + (int) caliDelta;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        // Does not clamp the scroll if it is already in spring back
        if (newScrollX > right && newScrollX > scrollX) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left && newScrollX < scrollX) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        // Does not clamp the scroll if it is already in spring back
        if (newScrollY > bottom && newScrollY > scrollY) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top && newScrollY < scrollY) {
            newScrollY = top;
            clampedY = true;
        }

        mOverScrollListener.onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return clampedX || clampedY;
    }


}
