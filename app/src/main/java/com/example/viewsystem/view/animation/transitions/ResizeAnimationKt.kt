package com.example.viewsystem.view.animation.transitions

import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import com.example.viewsystem.tag
import timber.log.Timber
import kotlin.properties.Delegates

class ResizeAnimationKt(
    private val target: View,
    private val targetWidthPx: Int,
    private val targetHeightPx: Int,
) : Animation() {
    private var startWidth by Delegates.notNull<Int>()
    private var startHeight by Delegates.notNull<Int>()

    init {
        Log.d(TAG, "ResizeAnimationKt: target=$target targetWidthPx=$targetWidthPx targetHeightPx=$targetHeightPx")
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        val newWidth: Int = ((startWidth + (targetWidthPx - startWidth) * interpolatedTime).toInt())
        val newHeight: Int = ((startHeight + (targetHeightPx - startHeight) * interpolatedTime).toInt())

        val params = target.layoutParams
        params.width = newWidth
        params.height = newHeight

        target.layoutParams = params
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        Log.d(
            TAG,
            "initialize() called with: width = $width, height = $height, parentWidth = $parentWidth, parentHeight = $parentHeight"
        )

        this.startWidth = width
        this.startHeight = 0
    }

    override fun willChangeBounds(): Boolean { return true; }

    companion object {
        private val TAG = tag(ResizeTransition::class.java)
    }
}