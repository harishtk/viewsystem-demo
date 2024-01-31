package com.example.viewsystem.commons.util.recyclerview

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

@Suppress("unused")
class SmoothScrollingGridLayoutManager(
    context: Context,
    spanCount: Int,
    reverseLayout: Boolean,
) : GridLayoutManager(context, spanCount, RecyclerView.VERTICAL, reverseLayout) {

    override fun supportsPredictiveItemAnimations(): Boolean = false

    fun smoothScrollToPosition(
        context: Context,
        position: Int,
        millisPerInch: Float,
    ) {
        val scroller = object : LinearSmoothScroller(context) {

            override fun getVerticalSnapPreference(): Int = SNAP_TO_END

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return millisPerInch / displayMetrics!!.densityDpi
            }
        }

        scroller.targetPosition = position
        startSmoothScroll(scroller)
    }
}