package com.example.viewsystem.commons.util.recyclerview

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

@Suppress("unused")
class SmoothScrollingLinearLayoutManager(
    context: Context,
    reverseLayout: Boolean,
) : LinearLayoutManager(context, RecyclerView.VERTICAL, reverseLayout) {

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

