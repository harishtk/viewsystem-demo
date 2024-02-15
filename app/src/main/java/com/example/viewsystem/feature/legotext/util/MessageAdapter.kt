package com.example.viewsystem.feature.legotext.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.viewsystem.R
import com.example.viewsystem.commons.util.DateUtil
import com.example.viewsystem.databinding.ItemRightBubbleBinding
import com.example.viewsystem.feature.legotext.Message
import com.example.viewsystem.feature.legotext.MessageUiModel
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MessageAdapter(
    private val callback: Callback,
    private var getHeader: (position: Int, before: MessageUiModel?, after: MessageUiModel?) -> String? = { _, _, _ -> null },
) : ListAdapter<MessageUiModel, RecyclerView.ViewHolder>(ITEM_CALLBACK) {

    var ownUserId: String = ""
        set(value) {
            field = value
            notifyDataSetChanged() /* just in case */
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM_SENT -> SentItemViewHolder.from(parent)
            else -> throw RuntimeException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position) ?: return
        when (model) {
            is MessageUiModel.Item -> {
                val before: MessageUiModel? = if (position == 0) {
                    /* We're at the very beginning */
                    null
                } else {
                    getItem(position - 1)
                }
                val after: MessageUiModel? = if (position == itemCount - 1) {
                    /* We're at the very end. */
                    null
                } else {
                    getItem(position + 1)
                    //getItem(if (position == 0) position else position + 1)
                }
                /*val before: MessageUiModel? = if (position == itemCount - 1) {
                    *//* We're at the very beginning *//*
                    getItem(position - 1)
                } else {
                    null
                }
                val after: MessageUiModel? = if (position == 0) {
                    *//* We're at the very end. *//*
                    getItem(position)
                } else {
                    getItem(position)
                    //getItem(if (position == 0) position else position + 1)
                }*/

                val showTime = !isMessageWithInClustering(model, after)

                val animatedShow = shouldAnimate(model)

                val headerString = getHeader(position, before, after)
                (holder as SentItemViewHolder).bind(model.message, callback, showTime, animatedShow,  headerString)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val model = getItem(position)) {
            is MessageUiModel.Item -> {
                VIEW_TYPE_ITEM_SENT
                /*if (model.message.senderId == ownUserId) {
                    VIEW_TYPE_ITEM_SENT
                } else {
                    VIEW_TYPE_ITEM_RECEIVE
                }*/
            }
            else -> throw RuntimeException("Unknown type")
        }
    }

    private fun shouldAnimate(current: MessageUiModel.Item): Boolean {
        val delay = 500L
        return current.message.ts > (Instant.now().minusMillis(delay).toEpochMilli())
    }

    private fun isMessageWithInClustering(
        current: MessageUiModel,
        after: MessageUiModel?,
    ): Boolean {
        if (after == null) return false
        if (current !is MessageUiModel.Item || after !is MessageUiModel.Item) return false

        Timber.d("getShowTime: ${current.message.text} ${after.message.text}")
        return abs(current.message.ts - after.message.ts) <= MAX_CLUSTERING_TIME_DIFF
    }

    class SentItemViewHolder private constructor(
        private val binding: ItemRightBubbleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, callback: Callback, showTime: Boolean, animatedShow: Boolean, headerString: String?) = with(binding) {
            this.tvMessage.text = message.text

            time.text = DateUtil.getChatTime(Instant.ofEpochMilli(message.ts))
            time.isVisible = showTime
            receiptStatusIndicator.isVisible = showTime

            if (headerString?.isNotBlank() == true) {
                header.isVisible = true
                header.text = headerString
            } else {
                header.isVisible = false
            }

            if (animatedShow) {
                animatedBubbleVisible(bubbleContainer, reverse = true)
            }
        }

        fun animatedBubbleVisible(v: View, reverse: Boolean = false) {
            if (reverse) {
                ScaleAnimation(0.0F,
                    1.0F,
                    0.0F,
                    1.0F,
                    ScaleAnimation.RELATIVE_TO_PARENT,
                    0.0F,
                    ScaleAnimation.RELATIVE_TO_PARENT,
                    0.0F).apply {
                    duration = 200L
                    interpolator = DecelerateInterpolator()
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) {
                            v.visibility = View.INVISIBLE
                        }

                        override fun onAnimationEnd(p0: Animation?) {
                            v.isVisible = true
                        }

                        override fun onAnimationRepeat(p0: Animation?) {}
                    })
                }.also {
                    v.startAnimation(it)
                }
            } else {
                ScaleAnimation(0.0F,
                    1.0F,
                    0.0F,
                    1.0F,
                    ScaleAnimation.RELATIVE_TO_SELF,
                    0.5F,
                    ScaleAnimation.RELATIVE_TO_SELF,
                    0.5F).apply {
                    // startOffset = 300L
                    duration = 200L
                    interpolator = DecelerateInterpolator()
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) {
                            v.visibility = View.INVISIBLE
                        }

                        override fun onAnimationEnd(p0: Animation?) {
                            v.isVisible = true
                        }

                        override fun onAnimationRepeat(p0: Animation?) {}
                    })
                }.also {
                    v.startAnimation(it)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): SentItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_right_bubble, parent, false)
                val binding = ItemRightBubbleBinding.bind(itemView)
                return SentItemViewHolder(binding)
            }
        }
    }

    class ReceivedItemViewHolder private constructor(
        private val binding: ItemRightBubbleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, callback: Callback, headerString: String?) = with(binding) {
            this.tvMessage.text = message.text

            time.text = DateUtil.getChatTime(Instant.ofEpochMilli(message.ts))

            if (headerString?.isNotBlank() == true) {
                header.isVisible = true
                header.text = headerString
            } else {
                header.isVisible = false
            }
        }

        companion object {
            fun from(parent: ViewGroup): ReceivedItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_left_bubble, parent, false)
                val binding = ItemRightBubbleBinding.bind(itemView)
                return ReceivedItemViewHolder(binding)
            }
        }
    }

    interface Callback

    companion object {
        const val VIEW_TYPE_ITEM_SENT = 0
        const val VIEW_TYPE_ITEM_RECEIVE = 1

        private val MAX_CLUSTERING_TIME_DIFF = TimeUnit.SECONDS.toMillis(15)

        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<MessageUiModel>() {
            override fun areItemsTheSame(
                oldItem: MessageUiModel,
                newItem: MessageUiModel
            ): Boolean {
                return (oldItem is MessageUiModel.Item && newItem is MessageUiModel.Item
                            && oldItem.message.id == newItem.message.id)
            }

            override fun areContentsTheSame(
                oldItem: MessageUiModel,
                newItem: MessageUiModel
            ): Boolean {
                return (oldItem is MessageUiModel.Item && newItem is MessageUiModel.Item
                        && oldItem.message == newItem.message)
            }
        }
    }
}