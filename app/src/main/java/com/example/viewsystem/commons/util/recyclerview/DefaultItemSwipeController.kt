package com.example.viewsystem.commons.util.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.viewsystem.R
import com.example.viewsystem.commons.util.AndroidUtils
import com.example.viewsystem.commons.util.ViewUtil
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.min

class ResizeAnim(var view: View, private val startHeight: Int, private val targetHeight: Int) :
    Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        if (startHeight == 0 || targetHeight == 0) {
            view.layoutParams.height =
                (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        } else {
            view.layoutParams.height = (startHeight + targetHeight * interpolatedTime).toInt()
            android.util.Log.d("TAG", "applyTransformation: height = ${view.layoutParams.height}")
        }
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}

interface SwipeControllerActions {
    fun onAction(position: Int)
}

class DefaultItemSwipeController(
    private val context: Context,
    private val swipeControllerActions: SwipeControllerActions? = null,
) : ItemTouchHelper.Callback() {

    private lateinit var imageDrawable: Drawable
    private lateinit var imageBackgroundDrawable: Drawable
    private lateinit var background: Drawable

    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private lateinit var mView: View
    private var dX = 0f

    private var actionBtnProgress: Float = 0F
    private var lastActionBtnAnimationTime: Long = 0
    private var swipeBack = false
    private var isVibrate = false
    private var startTracking = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        mView = viewHolder.itemView
        imageDrawable = context.getDrawable(R.drawable.ic_trashcan)!!

        val cornerSize = context.resources.getDimensionPixelSize(R.dimen.default_corner_size)
        val green700a30 = ResourcesCompat.getColor(context.resources, R.color.best_deals_green700a30, context.theme)
        imageBackgroundDrawable = ViewUtil.getRoundedDrawable(
            cornerSize,
            green700a30
        )
        background = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.material_red400, context.theme))
        return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder)
        }

        if (mView.translationX < convertTodp(130) || dX < this.dX) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            this.dX = dX
            startTracking = true
        }

        currentItemViewHolder = viewHolder
        drawBackground(c)
        drawActionButton(c)
    }

    private fun drawActionButton(canvas: Canvas) {
        if (currentItemViewHolder == null) {
            Timber.w("No canvas. Nothing to draw.")
            return
        }

        val translationX = mView.translationX
        val newTime = System.currentTimeMillis()
        val dt = min(17, newTime - lastActionBtnAnimationTime)
        lastActionBtnAnimationTime = newTime
        val showing = translationX >= convertTodp(30)
        if (showing) {
            if (actionBtnProgress < 1.0F) {
                actionBtnProgress += dt / 180.0F
                if (actionBtnProgress > 1.0F) {
                    actionBtnProgress = 1F
                } else {
                    mView.invalidate()
                }
            }
        } else if (translationX <= 0.0F) {
            actionBtnProgress = 0F
            startTracking = false
            isVibrate = false
        } else {
            if (actionBtnProgress > 0.0F) {
                actionBtnProgress -= dt / 180F
                if (actionBtnProgress < 0.1F) {
                    actionBtnProgress = 0F
                } else {
                    mView.invalidate()
                }
            }
        }

        val alpha: Int
        val scale: Float
        if (showing) {
            scale = if (actionBtnProgress <= 0.8F) {
                1.2F * (actionBtnProgress / 0.8F)
            } else {
                1.2F - 0.2F * ((actionBtnProgress - 0.8F) / 0.2F)
            }
            alpha = min(255F, 255 * (actionBtnProgress / 0.8F)).toInt()
        } else {
            scale = actionBtnProgress
            alpha = min(255F, 255 * actionBtnProgress).toInt()
        }

        imageBackgroundDrawable.alpha = alpha
        imageDrawable.alpha = alpha
        if (startTracking) {
            if (!isVibrate && mView.translationX >= convertTodp(100)) {
                mView.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
                isVibrate = true
            }
        }

        val x: Int = if (mView.translationX > convertTodp(130)) {
            convertTodp(130) / 2
        } else {
            (mView.translationX / 2).toInt()
        }

        val y: Float = (mView.top + mView.measuredHeight / 2).toFloat()
        imageBackgroundDrawable.colorFilter =
            PorterDuffColorFilter(ContextCompat.getColor(context, R.color.best_deals_green400), PorterDuff.Mode.MULTIPLY)

        Timber.d("Bounds: x=$x y=$y delta=${convertTodp(18) * scale}")
        imageBackgroundDrawable.setBounds(
            (x - convertTodp(18) * scale).toInt(),
            (y - convertTodp(18) * scale).toInt(),
            (x + convertTodp(18) * scale).toInt(),
            (y + convertTodp(18) * scale).toInt()
        )
        // imageBackgroundDrawable.draw(canvas)
        imageDrawable.setBounds(
            (x - convertTodp(24) * scale).toInt(),
            (y - convertTodp(22) * scale).toInt(),
            (x + convertTodp(24) * scale).toInt(),
            (y + convertTodp(22) * scale).toInt()
        )
        imageDrawable.draw(canvas)

        imageBackgroundDrawable.alpha = 255
        imageDrawable.alpha = 255
    }

    private fun drawBackground(canvas: Canvas) {
        val backgroundCornerOffset = 20
        val itemView = mView
        if (dX > 0) { /* Swiping to the right */
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + (dX + backgroundCornerOffset).toInt(),
                itemView.bottom
            )

        } else if (dX < 0) { /* Swiping to the left */
            background.setBounds(
                itemView.right + (dX + backgroundCornerOffset).toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )

        } else {
            background.setBounds(0, 0, 0, 0)
        }

        background.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (abs(mView.translationX) >= this@DefaultItemSwipeController.convertTodp(100)) {
                    swipeControllerActions?.onAction(viewHolder.bindingAdapterPosition)
                }
            }
            false
        }
    }

    private fun convertTodp(pixel: Int): Int {
        return AndroidUtils.dp(pixel.toFloat(), context)
    }

}