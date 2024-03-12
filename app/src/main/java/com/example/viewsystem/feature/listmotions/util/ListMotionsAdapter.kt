package com.example.viewsystem.feature.listmotions.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnCancel
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.viewsystem.R
import com.example.viewsystem.databinding.SimpleListItem1Binding
import com.example.viewsystem.feature.listmotions.SortingItem
import timber.log.Timber
import java.util.Collections

class ListMotionsAdapter(
    private val onSwiped: (position: Int, direction: Int) -> Boolean = { _, _ -> false },
    private val onMoved: (fromPosition: Int, toPosition: Int) -> Boolean = { _, _ -> true },
    private val onItemDragListener: OnItemDragListener? = null,
) : RecyclerView.Adapter<ListMotionsAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {

    private val asyncListDiffer: AsyncListDiffer<SortingItem> = AsyncListDiffer(this, DiffCallback)

    fun submitList(data: List<SortingItem>) {
        asyncListDiffer.submitList(data)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Timber.d("onItemMove() called with: fromPosition = [$fromPosition], toPosition = [$toPosition]")
        try {
            val currentList = asyncListDiffer.currentList.toMutableList()
            if (fromPosition < toPosition) {
                for (i in fromPosition..<toPosition) {
                    Collections.swap(currentList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition) {
                    Collections.swap(currentList, i, i - 1)
                }
            }
            submitList(currentList)
        } catch (ignore: IndexOutOfBoundsException) {}

        onMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        if (!onSwiped(position, -1)) {
            notifyItemChanged(position)
        } else {
            // notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val model = getItem(position) ?: return
        holder.bind(model) {
            onItemDragListener?.onDragStart(holder) == true
        }
    }

    private fun getItem(position: Int): SortingItem? {
        return try {
            asyncListDiffer.currentList[position]
        } catch (e: IndexOutOfBoundsException) { null }
    }

    class ItemViewHolder private constructor(
        private val binding: SimpleListItem1Binding
    ) : ViewHolder(binding.root), SelectableViewHolder {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(data: SortingItem, onDrag: () -> Boolean) = with(binding) {
            /*root.setBackgroundColor(
                ResourcesCompat.getColor(root.resources, R.color.core_grey_02, null)
            )*/
            textView1.setText(data.data)
            tvHintLabel.setText("\u2022 ${data.sortPosition}")

            ivDragIndicator.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onDrag()
                    }

                    else -> false
                }
            }
        }

        override fun onItemSelected() = with(binding) {
            root.isSelected = true
            ValueAnimator.ofFloat(1f, 1.02f).apply {
                duration = 100
                interpolator = LinearInterpolator()
                addUpdateListener {
                    val targetScale = it.animatedValue as Float
                    root.scaleX = targetScale
                    root.scaleY = targetScale
                }
                doOnCancel {
                    root.scaleX = 1f
                    root.scaleY = 1f
                }
                start()
            }
            Unit
        }

        override fun onItemClear() = with(binding) {
            root.isSelected = false
            ValueAnimator.ofFloat(1.02f, 01f).apply {
                duration = 100
                interpolator = LinearInterpolator()
                addUpdateListener {
                    val targetScale = it.animatedValue as Float
                    root.scaleX = targetScale
                    root.scaleY = targetScale
                }
                doOnCancel {
                    root.scaleX = 1f
                    root.scaleY = 1f
                }
                start()
            }
            Unit
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(
                    R.layout.simple_list_item_1, parent, false
                )
                val binding = SimpleListItem1Binding.bind(itemView)
                return ItemViewHolder(binding)
            }
        }
    }

    companion object {
        private val DiffCallback = object : ItemCallback<SortingItem>() {
            override fun areItemsTheSame(oldItem: SortingItem, newItem: SortingItem): Boolean {
                return oldItem.data == newItem.data
            }

            override fun areContentsTheSame(oldItem: SortingItem, newItem: SortingItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}