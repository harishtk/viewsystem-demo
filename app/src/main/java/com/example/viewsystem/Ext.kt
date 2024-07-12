package com.example.viewsystem

import android.graphics.Rect
import android.view.View
import androidx.core.widget.NestedScrollView

internal fun NestedScrollView.isChildVisible(view: View): Boolean {
    val scrollBounds = Rect()
    getDrawingRect(scrollBounds)
    val top = view.y
    val bottom = view.height + top
    return scrollBounds.bottom > bottom
}