@file:Suppress("DEPRECATION")

package com.example.viewsystem.view.animation.helper

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IntDef
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import com.example.viewsystem.commons.util.AnimationUtil.animationListener
import com.example.viewsystem.commons.util.AnimationUtil.animatorListener
import com.example.viewsystem.commons.util.AnimationUtil.scaleAnimator
import com.example.viewsystem.core.di.AppDependencies
import com.example.viewsystem.extensions.ifDebug
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.FROM_BOTTOM
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.FROM_LEFT
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.FROM_RIGHT
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.FROM_TOP
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.HORIZONTAL
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.TO_BOTTOM
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.TO_LEFT
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.TO_RIGHT
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.TO_TOP
import com.example.viewsystem.view.animation.helper.ChainedAnimation.Companion.VERTICAL
import timber.log.Timber


fun View.beginChainedAnimations(): ChainedAnimation {
    return ChainedAnimation(this)
}

/**
 * @deprecated use [beginChainedAnimations]
 */
class ChainedAnimation @Deprecated("use function") constructor(
    private val root: View,
) {
    private val animatableViews: MutableList<AnimatableView> = mutableListOf()

    fun with(
        target: View,
        vararg animators: Animator,
        offsetMultiplier: Float = 1f,
    ): ChainedAnimation {
        animatableViews.add(
            AnimatableView(
                target,
                animators.toList(),
                offsetMultiplier
            )
        )
        return this
    }

    /*fun fadeIn(target: View, from: Float = 0f, to: Float = 1f, offsetMultiplier: Float = 1f): ChainedAnimation {
        val previousTargetVisibility = target.visibility
        target.isVisible = false
        val animator = ValueAnimator.ofFloat(from, to).apply {
            addUpdateListener { animator ->
                val animatedValue = animator.animatedValue as Float
                target.alpha = animatedValue
            }
            doOnStart {
                target.isVisible = true
            }
            doOnEnd {
                ifDebug { Timber.d("Animation finished for ${target.id}") }
                target.visibility = previousTargetVisibility
            }
        }
        this.animatableViews.add(
            AnimatableView(
                target,
                listOf(animator),
                offsetMultiplier
            )
        )
        return this
    }*/

    fun scaleIn(target: View, offsetMultiplier: Float = 1F): ChainedAnimation {
        val previousTargetVisibility = target.visibility
        val animator = target.scaleAnimator(
            fromX = 0.8F,
            toX = 1F,
            fromY = 0.8F,
            toY = 1F,
            pivotX = 0.5F,
            pivotY = 0F
        ).apply {
            animationListener(
                onAnimationStart = { target.isVisible = true },
                onAnimationEnd = { /*target.visibility = previousTargetVisibility;*/ }
            )
        }
        this.animatableViews.add(
            AnimatableView(
                target,
                listOf(animator),
                offsetMultiplier
            )
        )
        return this
    }

    fun startTransitions(
        duration: Long = 500L,
        startOffset: Long = 0,
        onAnimationEnd: () -> Unit = {},
    ) {
        Timber.d("Starting chained animations.. ${animatableViews.size} views")
        AnimatorSet().apply {
            playTogether(
                animatableViews.map { animatable ->
                    val startOffset = (startOffset * animatable.offsetMultiplier).toLong()
                    animatable.animators.onEach { animator ->
                        animator.apply { this.startDelay = startOffset; }
                    }
                }
                    .flatten()
            )
            this.duration = duration
            interpolator = DecelerateInterpolator()
            this.addListener(
                animatorListener(
                    onAnimationEnd = {
                        onAnimationEnd()
                        Timber.d("Chained animations finished.")
                    }
                )
            )
        }
            .start()
    }

    companion object {
        const val VERTICAL = 1
        const val HORIZONTAL = 0

        const val FROM_TOP = 0
        const val FROM_RIGHT = 1
        const val FROM_BOTTOM = 2
        const val FROM_LEFT = 4

        const val TO_TOP = 5
        const val TO_RIGHT = 6
        const val TO_BOTTOM = 7
        const val TO_LEFT = 8

        fun fade(
            target: View,
            from: Float = 0f,
            to: Float = 1f,
            fillBefore: Boolean = true,
            resetAfter: Boolean = true,
        ): Animator {
            val previousTargetVisibility = target.visibility
            target.visibility = if (fillBefore) {
                View.INVISIBLE
            } else {
                View.GONE
            }
            target.alpha = from
            return ObjectAnimator.ofFloat(target, View.ALPHA, from, to).apply {
                doOnStart {
                    target.isVisible = true
                }
                doOnEnd {
                    ifDebug { Timber.d("Animation finished for ${target.id}") }
                    /*target.visibility = previousTargetVisibility*/
                    if (resetAfter) {
                        target.alpha = 1F
                    }
                }
            }
        }

        fun scale(
            target: View,
            fromX: Float = 0F,
            toX: Float = 1F,
            fromY: Float = 0F,
            toY: Float = 1F,
            pivotX: Float = 0.5F,
            pivotY: Float = 0.5F,
            onAnimationEnd: () -> Unit = {},
        ): Animator {
            val previousTargetVisibility = target.visibility
            return target.scaleAnimator(
                fromX, toX, fromY, toY, pivotX, pivotY, onAnimationEnd
            )
        }

        fun slide(
            target: View,
            @Orientation orientation: Int,
            @AnimDirection direction: Int,
        ): Animator {
            val offset: Float =
                run {
                    if (orientation == VERTICAL) {
                        target.measuredHeight.toFloat()
                    } else {
                        target.measuredWidth.toFloat()
                    }
                }.let {
                    if (it == 0F) {
                        Timber.w("Failed to calculate offset.. Using default translation")
                        if (orientation == VERTICAL) {
                            AppDependencies.displaySize!!.height.times(0.05F)
                        } else {
                            AppDependencies.displaySize!!.width.times(0.5F)
                        }
                    } else {
                        it
                    }
                }

            val previousTargetVisibility = target.visibility
            target.visibility = View.INVISIBLE
            return when (direction) {
                FROM_BOTTOM -> ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, offset, 0F)
                FROM_LEFT -> ObjectAnimator.ofFloat(target, View.TRANSLATION_X, -offset, 0F)
                FROM_RIGHT -> ObjectAnimator.ofFloat(target, View.TRANSLATION_X, offset, 0F)
                FROM_TOP -> ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, -offset, 0F)
                TO_BOTTOM -> ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0F, offset)
                TO_LEFT -> ObjectAnimator.ofFloat(target, View.TRANSLATION_X, 0F, -offset)
                TO_RIGHT -> ObjectAnimator.ofFloat(target, View.TRANSLATION_X, 0F, offset)
                TO_TOP -> ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0F, -offset)
                else -> error("Unknown direction $direction")
            }.apply {
                this.addListener(
                    animatorListener(
                        onAnimationStart = {
                            target.isVisible = true;
                        },
                        onAnimationEnd = {
                            /*target.visibility = previousTargetVisibility*/
                        }
                    )
                )
            }
        }
    }
}

@IntDef(HORIZONTAL, VERTICAL)
@Retention(AnnotationRetention.SOURCE)
annotation class Orientation

@IntDef(FROM_TOP, FROM_RIGHT, FROM_BOTTOM, FROM_LEFT, TO_TOP, TO_RIGHT, TO_BOTTOM, TO_LEFT)
@Retention(AnnotationRetention.SOURCE)
annotation class AnimDirection

data class AnimatableView(
    val view: View,
    val animators: List<Animator>,
    val offsetMultiplier: Float,
)

