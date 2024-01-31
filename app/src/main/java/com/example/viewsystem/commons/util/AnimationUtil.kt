package com.example.viewsystem.commons.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.transition.Transition
import androidx.transition.Transition.TransitionListener

object AnimationUtil {

    fun scaleAnimation(
        fromX: Float = 0.0F,
        toX: Float = 1F,
        fromY: Float = 0F,
        toY: Float = 1F,
        pivotX: Float = 0.5F,
        pivotY: Float = 0.5F,
        onAnimationEnd: () -> Unit = {},
    ): Animation {
        return ScaleAnimation(
            fromX, toX, fromY, toY,
            ScaleAnimation.RELATIVE_TO_SELF,
            pivotX,
            ScaleAnimation.RELATIVE_TO_SELF,
            pivotY
        ).apply {
            animationListener(
                onAnimationEnd = onAnimationEnd
            )
        }
    }

    fun View.scaleAnimator(
        fromX: Float = 0.0F,
        toX: Float = 1F,
        fromY: Float = 0F,
        toY: Float = 1F,
        pivotX: Float = 0.5F,
        pivotY: Float = 0.5F,
        onAnimationEnd: () -> Unit = {},
    ): Animator {
        val target = this
        val (lastPivotX, lastPivotY) = pivotX to pivotY
        // target.pivotX = 100.times(pivotX); target.pivotY = 100.times(pivotY);
        // target.pivotX = pivotX; target.pivotY = pivotY
        target.scaleX = fromX
        target.scaleY = fromY
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(target, View.SCALE_X, fromX, toX),
                ObjectAnimator.ofFloat(target, View.SCALE_Y, fromY, toY),
            )
            this.addListener(
                animatorListener(
                    onAnimationStart = {
                        target.isVisible = true
                    },
                    onAnimationEnd = {
                        // target.pivotX = lastPivotX; target.pivotY = lastPivotY
                        onAnimationEnd()
                    },
                )
            )
        }
    }

    fun View.shakeNow(onAnimationEnd: () -> Unit) {
        val shake = TranslateAnimation(0F, 15F, 0F, 0F)
        shake.duration = SHAKE_ANIMATION_DURATION.toLong()
        shake.interpolator = CycleInterpolator(4F)
        shake.animationListener(
            onAnimationEnd = onAnimationEnd
        )
        startAnimation(shake)
    }

    fun View.shakeNow() {
        val shake = TranslateAnimation(0F, 15F, 0F, 0F)
        shake.duration = SHAKE_ANIMATION_DURATION.toLong()
        shake.interpolator = CycleInterpolator(4F)
        startAnimation(shake)
    }

    fun View.touchInteractFeedback(scaleMultiplier: Float = DEFAULT_SCALE_MULTIPLIER) {
        val scale = ScaleAnimation(
            scaleMultiplier, 1.0f, scaleMultiplier, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = QUICK_ANIMATION_DURATION
        scale.interpolator = LinearInterpolator()
        startAnimation(scale)
    }

    fun View.quickScale(scaleMultiplier: Float = DEFAULT_SCALE_MULTIPLIER) {
        val scale = ScaleAnimation(
            scaleMultiplier, 1.0f, scaleMultiplier, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = QUICK_ANIMATION_DURATION
        scale.interpolator = LinearInterpolator()
        startAnimation(scale)
    }

    fun flip(
        v: View,
        flipDirection: FlipDirection = FlipDirection.HORIZONTAL,
        show: Boolean = true,
    ) {
        val axis = if (flipDirection == FlipDirection.VERTICAL) {
            View.ROTATION_X
        } else {
            View.ROTATION_Y
        }
        ObjectAnimator.ofFloat(v, axis, 90F, 0F).apply {
            duration = QUICK_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            doOnEnd { v.rotation = 0F }
            start()
        }
    }

    fun View.rotate(durationMillis: Long = DEFAULT_ANIMATION_DURATION) {
        val rotateAnimation = RotateAnimation(
            0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            interpolator = DecelerateInterpolator()
            duration = durationMillis
        }
        startAnimation(rotateAnimation)
    }

    fun View.fadeIn(fromAlpha: Float = 0F, toAlpha: Float = 1F, durationMillis: Long = QUICK_ANIMATION_DURATION) {
        val fadeInAnimation = AlphaAnimation(fromAlpha, toAlpha).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = durationMillis
        }
        startAnimation(fadeInAnimation)
    }

    fun View.fadeOut(durationMillis: Long = QUICK_ANIMATION_DURATION) {
        val fadeInAnimation = AlphaAnimation(1F, 0F).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = durationMillis
        }
        startAnimation(fadeInAnimation)
    }

    /* Syntatic Sugar for [Animation#setAnimationListener] */
    inline fun animationListener(
        crossinline onAnimationStart: () -> Unit = {},
        crossinline onAnimationEnd: () -> Unit = {},
        crossinline onAnimationRepeat: () -> Unit = {},
    ) = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            onAnimationStart()
        }

        override fun onAnimationEnd(animation: Animation?) {
            onAnimationEnd()
        }

        override fun onAnimationRepeat(animation: Animation?) {
            onAnimationRepeat()
        }
    }

    inline fun animatorListener(
        crossinline onAnimationStart: () -> Unit = {},
        crossinline onAnimationEnd: () -> Unit = {},
        crossinline onAnimationRepeat: () -> Unit = {},
        crossinline onAnimationCancel: () -> Unit = {},
    ) = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            onAnimationStart()
        }

        override fun onAnimationEnd(animation: Animator) {
            onAnimationEnd()
        }

        override fun onAnimationRepeat(animation: Animator) {
            onAnimationRepeat()
        }

        override fun onAnimationCancel(animation: Animator) {
            onAnimationCancel()
        }
    }

    /* Syntatic Sugar for [Animation#setAnimationListener] */
    inline fun Animation.animationListener(
        crossinline onAnimationStart: () -> Unit = {},
        crossinline onAnimationEnd: () -> Unit = {},
        crossinline onAnimationRepeat: () -> Unit = {},
    ) = setAnimationListener(object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            onAnimationStart()
        }

        override fun onAnimationEnd(animation: Animation?) {
            onAnimationEnd()
        }

        override fun onAnimationRepeat(animation: Animation?) {
            onAnimationRepeat()
        }
    })

    /* Syntatic Sugar for [Transition.TransitionListener] */
    inline fun Transition.transitionListener(
        crossinline onTransitionStart: () -> Unit = {},
        crossinline onTransitionEnd: () -> Unit = {},
        crossinline onTransitionCancel: () -> Unit = {},
        crossinline onTransitionPause: () -> Unit = {},
        crossinline onTransitionResume: () -> Unit = {},
    ) = addListener(object : TransitionListener {
        override fun onTransitionStart(transition: Transition) {
            onTransitionStart()
        }

        override fun onTransitionEnd(transition: Transition) {
            onTransitionEnd()
        }

        override fun onTransitionCancel(transition: Transition) {
            onTransitionCancel()
        }

        override fun onTransitionPause(transition: Transition) {
            onTransitionPause()
        }

        override fun onTransitionResume(transition: Transition) {
            onTransitionResume()
        }
    })

    private const val SHAKE_ANIMATION_DURATION = 500
    private const val DEFAULT_ANIMATION_DURATION = 500L
    private const val QUICK_ANIMATION_DURATION = 200L

    private const val DEFAULT_SCALE_MULTIPLIER = 1.2F
}

enum class FlipDirection { VERTICAL, HORIZONTAL }