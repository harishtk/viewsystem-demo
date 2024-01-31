package com.example.viewsystem.commons.util.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.viewsystem.R
import timber.log.Timber


class SwipeToDeleteCallback(
    private val context: Context,
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    @ColorInt
    var backgroundColor: Int = context
        .getColor(R.color.material_red400)
    var icon: Drawable? = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.baseline_delete_outline_24,
        context.theme
    )

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        Timber.d("onMove() called with: recyclerView = [$recyclerView], viewHolder = [$viewHolder], target = [$target]")
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Timber.d("onSwiped() called with: viewHolder = [$viewHolder], direction = [$direction]")
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20
        val background = ColorDrawable(backgroundColor)

        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
        val iconBottom = iconTop + icon!!.intrinsicHeight

        if (dX > 0) { /* Swiping to the right */
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + (dX + backgroundCornerOffset).toInt(),
                itemView.bottom
            )
            val iconLeft = itemView.left + iconMargin
            val iconRight = itemView.left + iconMargin + icon!!.intrinsicWidth
            icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)

        } else if (dX < 0) { /* Swiping to the left */
            background.setBounds(
                itemView.right + (dX + backgroundCornerOffset).toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )

            val iconLeft = itemView.right - iconMargin - icon!!.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        } else {
            icon?.setBounds(0, 0, 0, 0)
            background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        icon?.draw(c)
    }
}