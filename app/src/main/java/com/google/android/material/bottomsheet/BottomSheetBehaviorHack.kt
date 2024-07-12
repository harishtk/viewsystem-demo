package com.google.android.material.bottomsheet

import android.view.View
import android.widget.FrameLayout
import java.lang.ref.WeakReference

object BottomSheetBehaviorHack {
    fun setNestedScrollingChild(behavior: BottomSheetBehavior<FrameLayout>, view: View) {
        behavior.nestedScrollingChildRef = WeakReference(view)
    }
}