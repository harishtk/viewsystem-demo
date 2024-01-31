package com.example.viewsystem.view.animation.transitions

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

class ResizeTransition : Transition() {
    companion object {
        private const val WIDTH = "ResizeTransition.WIDTH"
        private const val HEIGHT = "ResizeTransition.HEIGHT"
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val targetView = endValues.view
        val startWidth = startValues.values[WIDTH] as Int
        val startHeight = startValues.values[HEIGHT] as Int
        val endWidth = endValues.values[WIDTH] as Int
        val endHeight = endValues.values[HEIGHT] as Int

        val widthAnimator = ValueAnimator.ofInt(startWidth, endWidth)
            .apply {
                addUpdateListener { animator ->
                    val animatedValue = animator.animatedValue as Int
                    targetView.layoutParams.width = animatedValue
                    targetView.requestLayout()
                }
            }

        val heightAnimator = ValueAnimator.ofInt(startHeight, endHeight)
            .apply {
                addUpdateListener { animator ->
                    val animatedValue = animator.animatedValue as Int
                    targetView.layoutParams.height = animatedValue
                    targetView.requestLayout()
                }
            }

        return AnimatorSet().apply {
            playTogether(widthAnimator, heightAnimator)
        }
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        transitionValues.values[WIDTH] = view.width
        transitionValues.values[HEIGHT] = view.height
    }
}