package com.example.viewsystem.commons.util

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.annotation.Px

object ViewUtil {

    fun getRoundedDrawable(@Px radius: Int, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                radius.toFloat(), radius.toFloat(), // top left
                radius.toFloat(), radius.toFloat(), // top right
                radius.toFloat(), radius.toFloat(), // bottom right
                radius.toFloat(), radius.toFloat()  // bottom left
            )
        }
    }

    fun getRoundedDrawable(
        @Px topLeft: Int,
        @Px topRight: Int,
        @Px bottomRight: Int,
        @Px bottomLeft: Int,
        color: Int,
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                topLeft.toFloat(), topLeft.toFloat(),           // top left
                topRight.toFloat(), topRight.toFloat(),         // top right
                bottomRight.toFloat(), bottomRight.toFloat(),   // bottom right
                bottomLeft.toFloat(), bottomLeft.toFloat()      // bottom left
            )
        }
    }

    fun animateIn(view: View, animation: Animation) {
        if (view.visibility == View.VISIBLE) return
        view.clearAnimation()
        animation.reset()
        animation.startTime = 0
        view.visibility = View.VISIBLE
        view.startAnimation(animation)
    }

    fun animateOut(
        view: View,
        animation: Animation,
        visibility: Int,
        future: (Boolean?) -> Unit = { _ -> },
    ) {
        if (view.visibility == visibility) {
            future(true)
        } else {
            view.clearAnimation()
            animation.reset()
            animation.startTime = 0
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    view.visibility = visibility
                    future(true)
                }
            })
            view.startAnimation(animation)
        }
        return future(true)
    }

    fun isLtr(view: View): Boolean {
        return isLtr(view.context)
    }

    fun isLtr(context: Context): Boolean {
        return context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR
    }

    fun isRtl(view: View): Boolean {
        return isRtl(view.context)
    }

    fun isRtl(context: Context): Boolean {
        return context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }


    fun updateLayoutParams(view: View, width: Int, height: Int) {
        view.layoutParams.width = width
        view.layoutParams.height = height
        view.requestLayout()
    }

    fun updateLayoutParamsIfNonNull(view: View?, width: Int, height: Int) {
        view?.let { updateLayoutParams(it, width, height) }
    }

    fun setVisibilityIfNonNull(view: View?, visibility: Int) {
        if (view != null) {
            view.visibility = visibility
        }
    }

    fun getLeftMargin(view: View): Int {
        return if (ViewUtil.isLtr(view)) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
        } else (view.layoutParams as ViewGroup.MarginLayoutParams).rightMargin
    }

    fun getRightMargin(view: View): Int {
        return if (ViewUtil.isLtr(view)) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).rightMargin
        } else (view.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
    }

    fun getTopMargin(view: View): Int {
        return (view.layoutParams as ViewGroup.MarginLayoutParams).topMargin
    }

    fun setLeftMargin(view: View, margin: Int) {
        if (ViewUtil.isLtr(view)) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = margin
        } else {
            (view.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = margin
        }
        view.forceLayout()
        view.requestLayout()
    }

    fun setRightMargin(view: View, margin: Int) {
        if (ViewUtil.isLtr(view)) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = margin
        } else {
            (view.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = margin
        }
        view.forceLayout()
        view.requestLayout()
    }

    fun setTopMargin(view: View, margin: Int) {
        (view.layoutParams as ViewGroup.MarginLayoutParams).topMargin = margin
        view.requestLayout()
    }

    fun setBottomMargin(view: View, margin: Int) {
        (view.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = margin
        view.requestLayout()
    }

    fun getWidth(view: View): Int {
        return view.layoutParams.width
    }

    fun setPaddingTop(view: View, padding: Int) {
        view.setPadding(view.paddingLeft, padding, view.paddingRight, view.paddingBottom)
    }

    fun setPaddingBottom(view: View, padding: Int) {
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, padding)
    }

    fun setPadding(view: View, padding: Int) {
        view.setPadding(padding, padding, padding, padding)
    }

    fun setPaddingStart(view: View, padding: Int) {
        if (ViewUtil.isLtr(view)) {
            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
        } else {
            view.setPadding(view.paddingLeft, view.paddingTop, padding, view.paddingBottom)
        }
    }

    fun setPaddingEnd(view: View, padding: Int) {
        if (ViewUtil.isLtr(view)) {
            view.setPadding(view.paddingLeft, view.paddingTop, padding, view.paddingBottom)
        } else {
            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
        }
    }

    fun isPointInsideView(view: View, x: Float, y: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]
        return x > viewX && x < viewX + view.width && y > viewY && y < viewY + view.height
    }

    fun isViewOverlapping(firstView: View, secondView: View): Boolean {
        val firstPosition = IntArray(2)
        val secondPosition = IntArray(2)
        firstView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        firstView.getLocationOnScreen(firstPosition)
        secondView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        secondView.getLocationOnScreen(secondPosition)
        return firstPosition[0] < secondPosition[0] + secondView.measuredWidth
                && firstPosition[0] + firstView.measuredWidth > secondPosition[0]
                && firstPosition[1] < secondPosition[1] + secondView.measuredHeight
                && firstPosition[1] + firstView.measuredHeight > secondPosition[1]
    }
}