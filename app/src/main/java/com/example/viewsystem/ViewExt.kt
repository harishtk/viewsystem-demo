package com.example.viewsystem

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.TouchDelegate
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber


const val DOUBLE_CLICK_DELAY_SHORT = 200L
private const val DEFAULT_DOUBLE_CLICK_DELAY = 500L

fun View.setOnSingleClickListener(
    delay: Long = DEFAULT_DOUBLE_CLICK_DELAY,
    onClickListener: View.OnClickListener,
) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        val delta = (currentTime - lastClickTime)
            .coerceAtLeast(0)
        lastClickTime = currentTime
        if (delta <= delay) {
            return@setOnClickListener
        }
        onClickListener.onClick(view)
    }
}

fun View.showSnack(
    message: String,
    withBottomNavigation: Boolean = false,
    isLong: Boolean = false,
    autoCancel: Boolean = true,
    showAction: Boolean = false,
    actionTitle: String? = null,
    onActionClick: (() -> Unit)? = null,
): Snackbar {
    val length = when {
        showAction && !autoCancel -> {
            Snackbar.LENGTH_INDEFINITE
        }
        isLong -> Snackbar.LENGTH_LONG
        else -> Snackbar.LENGTH_SHORT
    }
    Timber.d("Snack: length=$length")
    val snack = Snackbar.make(this, message, length).apply {
        val snackBarView = view
        if (withBottomNavigation) {
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams

            val marginBottom = resources.getDimensionPixelSize(R.dimen.default_bottom_bar_height)
            params.setMargins(
                params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + marginBottom
            )
            snackBarView.layoutParams = params
        }
        actionTitle?.let { positiveTitle ->
            setAction(positiveTitle) { onActionClick?.invoke(); dismiss() }
        }
    }.also { it.show() }
    return snack
}

fun View.showSnack(
    message: String,
    @FloatRange(from = 0.0, to = 3.0, fromInclusive = true, toInclusive = true)
    bottomMarginMultiplier: Float,
    isLong: Boolean = false,
    actionTitle: String? = null,
    actionCallback: (() -> Unit)? = null,
): Snackbar {
    val length = when {
        actionTitle?.isNotEmpty() == true -> Snackbar.LENGTH_INDEFINITE
        isLong -> Snackbar.LENGTH_LONG
        else -> Snackbar.LENGTH_SHORT
    }
    val snack = Snackbar.make(this, message, length).apply {
        val snackBarView = view
        val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams

        val marginBottom = resources.getDimensionPixelSize(R.dimen.default_bottom_bar_height)
        params.setMargins(
            params.leftMargin,
            params.topMargin,
            params.rightMargin,
            ((params.bottomMargin + marginBottom) * bottomMarginMultiplier).toInt()
        )
        snackBarView.layoutParams = params
        actionTitle?.let { positiveTitle ->
            setAction(positiveTitle) { actionCallback?.invoke(); dismiss() }
        }
    }.also { it.show() }
    return snack
}

fun View.isSoftInputVisible(): Boolean {
    val insets = ViewCompat.getRootWindowInsets(this) ?: return false
    return insets.isVisible(WindowInsetsCompat.Type.ime())
}

fun ImageView.loadWithGlide(url: String) {
    Glide.with(this)
        .load(url)
        .into(this)
}

@Throws(IndexOutOfBoundsException::class)
private fun setClickable(textView: TextView, subString: String, handler: () -> Unit, drawUnderline: Boolean = false) {
    val text = textView.text
    val start = text.indexOf(subString, startIndex = 0)
    val end = start + subString.length

    val span = SpannableString(text)
    span.setSpan(ClickHandler(handler, drawUnderline), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

    textView.linksClickable = true
    textView.isClickable = true
    textView.movementMethod = LinkMovementMethod.getInstance()

    textView.text = span
}

private fun setClickable(span: SpannableString, handler: () -> Unit, drawUnderline: Boolean = false) {

}

private class ClickHandler(
    private val handler: () -> Unit,
    private val drawUnderline: Boolean,
) : ClickableSpan() {
    override fun onClick(widget: View) {
        handler()
    }

    override fun updateDrawState(ds: TextPaint) {
        if (drawUnderline) {
            ds?.bgColor = Color.TRANSPARENT
            super.updateDrawState(ds)
        } else {
            ds?.isUnderlineText = false
        }
    }
}

fun TextView.makeLinks(links: List<Pair<String, () -> Unit>>, drawUnderline: Boolean = true) {
    try { links.forEach { setClickable(this, it.first, it.second, drawUnderline) } }
    catch (ignore: Exception) { }
}

fun View.fakeDisable(disabled: Boolean = true) {
    if (disabled) {
        this.alpha = 0.5f
    } else {
        this.alpha = 1f
    }
}

fun View.startFloat(onAnimationEnd: () -> Unit = {}) {
    val floatAnimation = AnimationUtils.loadAnimation(context, R.anim.float_up_down).apply {
        duration = 1000L
        interpolator = AccelerateDecelerateInterpolator()
        repeatMode = Animation.REVERSE
        repeatCount = Animation.INFINITE
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                // Noop.
            }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd()
            }

            override fun onAnimationRepeat(p0: Animation?) {
                // Noop.
            }
        })
    }
    startAnimation(floatAnimation)
}

fun View.updateLayoutParams(width: Int, height: Int) {
    layoutParams.width = width
    layoutParams.height = height
    requestLayout()
}

fun PopupWindow.dimBehind(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float = 0.6f) {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = dimAmount
    wm.updateViewLayout(container, p)
}

@Deprecated("in development")
fun PopupWindow.blurBehind(@IntRange(from = 0, to = 25) blurRadius: Int, @ColorRes color: Int) {
    if (VersionCompat.isAtLeastS) {
        val container = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        p.blurBehindRadius = blurRadius
        wm.updateViewLayout(container, p)
    }
}

/**
 * Calls the given block [f] after the view has finished rendering
 */
inline fun <T: View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun View?.removeSelfFromParent() {
    this ?: return
    val parentView = parent as? ViewGroup ?: return
    parentView.removeView(this)
}

fun View.expandTouchArea(
    bigView: View,
    extraPaddingTopPx: Int = 0,
    extraPaddingLeftPx: Int = 0,
    extraPaddingRightPx: Int = 0,
    extraPaddingBottomPx: Int = 0,
) {
    bigView.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= extraPaddingTopPx
        rect.left -= extraPaddingLeftPx
        rect.right += extraPaddingRightPx
        rect.bottom += extraPaddingBottomPx
        bigView.touchDelegate = TouchDelegate(rect, this)
    }
}

fun View.expandTouchArea(
    bigView: View,
    extraPaddingPx: Int,
) {
    expandTouchArea(
        bigView = bigView,
        extraPaddingTopPx = extraPaddingPx,
        extraPaddingLeftPx = extraPaddingPx,
        extraPaddingRightPx = extraPaddingPx,
        extraPaddingBottomPx = extraPaddingPx
    )
}

fun View.expandTouchArea(
    bigView: View,
    extraPaddingPx: Int,
    orientation: Int,
) {
    bigView.post {
        if (orientation == 0 /* horizontal */) {
            expandTouchArea(
                bigView = bigView,
                extraPaddingTopPx = 0,
                extraPaddingLeftPx = extraPaddingPx,
                extraPaddingRightPx = extraPaddingPx,
                extraPaddingBottomPx = 0
            )
        } else {
            expandTouchArea(
                bigView = bigView,
                extraPaddingTopPx = extraPaddingPx,
                extraPaddingLeftPx = 0,
                extraPaddingRightPx = 0,
                extraPaddingBottomPx = extraPaddingPx
            )
        }
    }
}

fun View.updateMargins(
    left: Int = this.marginLeft,
    top: Int = this.marginTop,
    right: Int = this.marginRight,
    bottom: Int = this.marginBottom,
) {
    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
        setMargins(left, top, right, bottom)
    }
}

/**
 * Attaches the PopupWindow to the [lifecycleOwner] and automatically
 * dismisses it when [DefaultLifecycleObserver.onStop]
 */
fun PopupWindow.bindWithLifecycle(
    lifecycleOwner: LifecycleOwner,
) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            try { dismiss() } catch (ignore: Exception) {}
        }
    })
}

/**
 * Attaches the AlertDialog to the [lifecycleOwner] and automatically
 * dismisses it when [DefaultLifecycleObserver.onStop]
 */
fun AlertDialog.bindWithLifecycle(
    lifecycleOwner: LifecycleOwner,
) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            try { dismiss() } catch (ignore: Exception) {}
        }
    })
}

/**
 * Attaches the [BottomSheetDialog] to the [lifecycleOwner] and automatically
 * dismisses it when [DefaultLifecycleObserver.onStop]
 */
fun BottomSheetDialog.bindWithLifecycle(
    lifecycleOwner: LifecycleOwner,
) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            try { dismiss() } catch (ignore: Exception) {}
        }
    })
}

fun BottomSheetDialogFragment.getBottomSheetBehavior(): BottomSheetBehavior<FrameLayout>? {
    return (dialog as? BottomSheetDialog)?.behavior
}

/* Window Insets */
// TODO: Update this utility function w.r.t
//  Video - Animating your keyboard using WindowInsets by @chrisbanes - https://www.youtube.com/watch?v=acC7SR1EXsI
//  Sample - https://github.com/android/user-interface-samples/tree/master/WindowInsetsAnimation
fun View.animateKeyboardVisibility(
    ignoresStatusBar: Boolean = false,
    ignoreNavigationBar: Boolean = false,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        var isKeyboardAnimationRunning = false
        doOnApplyWindowInsets { view, windowInsetsCompat, initialPadding ->
            val systemBars =
                windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
            if (!isKeyboardAnimationRunning) {
                updatePadding(
                    top = if (!ignoresStatusBar) systemBars.top else 0,
                    bottom = if (!ignoreNavigationBar) systemBars.bottom + initialPadding.bottom else 0
                )
            }
        }
        setWindowInsetsAnimationCallback(object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
            // Create a snapshot of the view's padding state
            val initialPadding = recordInitialPaddingForView(this@animateKeyboardVisibility)

            override fun onStart(
                animation: WindowInsetsAnimation,
                bounds: WindowInsetsAnimation.Bounds
            ): WindowInsetsAnimation.Bounds {
                isKeyboardAnimationRunning = animation.typeMask == WindowInsets.Type.ime()
                return super.onStart(animation, bounds)
            }

            override fun onPrepare(animation: WindowInsetsAnimation) {
                super.onPrepare(animation)
                /* onPrepare and onStart is guaranteed to be called on same frame by the system */
            }

            override fun onEnd(animation: WindowInsetsAnimation) {
                super.onEnd(animation)
                if (animation.typeMask == WindowInsets.Type.ime()) {
                    isKeyboardAnimationRunning = false
                }
            }

            override fun onProgress(
                insets: WindowInsets,
                runningAnimations: MutableList<WindowInsetsAnimation>
            ): WindowInsets {
                if (isKeyboardAnimationRunning) {
                    val systemBarsBottom = insets.getInsets(WindowInsets.Type.systemBars()).bottom
                    val imeBottom = insets.getInsets(WindowInsets.Type.ime()).bottom
                    updatePadding(
                        bottom = imeBottom.coerceAtLeast(systemBarsBottom + initialPadding.bottom)
                    )
                }
                return insets
            }
        })
    } else {
        doOnApplyWindowInsets { view, insets, initialPadding ->
            val systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            if (imeBottom > 0) {
                view.updatePadding(
                    bottom = imeBottom.coerceAtLeast(systemBarsBottom)
                )
            } else {
                view.updatePadding(
                    bottom = imeBottom.coerceAtLeast(systemBarsBottom) + initialPadding.bottom
                )
            }
        }
    }
}

fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnWindowApplyInsetsListener which proxies to the given,
    // lambda, also passing in the original padding state
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding)
        // Always return the insets, so that children can also use them!
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        ViewCompat.requestApplyInsets(this)
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}
/* END - Window Insets */

fun TextView.animateCounterText(
    from: Int,
    to: Int,
    prefix: String = "",
    suffix: String = "",
    formatter: String = "%d"
): Animator {
    val animDuration = resources.getInteger(android.R.integer.config_longAnimTime)
    return ValueAnimator.ofInt(from, to).apply {
        duration = animDuration.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            val animatedValue = it.animatedValue as Int
            text = StringBuilder().apply {
                if (prefix.isNotBlank()) append(prefix)
                append(
                    if (to > 0) {
                        String.format(formatter, animatedValue)
                    } else {
                        animatedValue
                    }
                )
                if (suffix.isNotBlank()) append(suffix)
            }
        }
        start()
    }
}