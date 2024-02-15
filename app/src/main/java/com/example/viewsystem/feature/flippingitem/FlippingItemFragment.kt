package com.example.viewsystem.feature.flippingitem

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.viewsystem.R
import com.example.viewsystem.core.util.time.TimeAgo
import com.example.viewsystem.databinding.FragmentFlippingItemBinding
import com.example.viewsystem.databinding.ItemFlippingViewBinding
import com.example.viewsystem.extensions.themeColor
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class FlippingItemFragment : Fragment(R.layout.fragment_flipping_item) {

    private val viewModel: FlippingItemViewModel by viewModels()

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private val onBackPressedHandler: () -> Unit = {
        if (!viewModel.handleBackPressed()) {
            findNavController().navigateUp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))

            /*transitionListener(
                onTransitionStart = { binding.icCamera.isVisible = false; },
                onTransitionEnd = {
                    binding.icCamera.apply { scaleX = 0.8f; scaleY = 0.8f; }
                    TransitionManager.beginDelayedTransition(
                        binding.profileImageContainer as ViewGroup,
                        ScaleTransition().apply {
                            duration = 150L
                            interpolator = OvershootInterpolator(2f)
                        }
                    )
                    binding.icCamera.apply { scaleX = 1f; scaleY = 1f; }
                    binding.icCamera.isVisible = true
                }
            )*/
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFlippingItemBinding.bind(view)
        binding.bindToolbar()

        val from = arguments?.getString("from", "unknown")
        if (from == "routing") {
            postponeEnterTransition()

            val titleTransition = arguments?.getString("titleTransitionName") ?: ""
            binding.toolbarIncluded.toolbarTitle.transitionName = titleTransition

            startPostponedEnterTransition()
        }

        binding.bindState(
            flippingItems = viewModel.flippingItems,
            onItemClick = { item ->
                Timber.d("Clicked: $item")
                viewModel.flipItem(item.id)
            }
        )

        handleBackPressed()
    }

    private fun FragmentFlippingItemBinding.bindState(
        flippingItems: StateFlow<List<FlippingUiModel>>,
        onItemClick: (FlippingItem) -> Unit
    ) {
        val adapter = FlippingItemAdapter(onItemClick)
        listView.adapter = adapter

        flippingItems
            .onEach(adapter::submitList)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun FragmentFlippingItemBinding.bindToolbar() {
        toolbarIncluded.toolbarTitle.text =
            getString(R.string.title_flipping_recyclerview_item)
    }

    private fun handleBackPressed() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedHandler.invoke()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }
}

private class FlippingItemAdapter(
    private val onItemClick: (FlippingItem) -> Unit
) : ListAdapter<FlippingUiModel, ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> FlipItemViewHolder.from(parent)
            else -> error("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val model = getItem(position)) {
            is FlippingUiModel.Item -> {
                holder as FlipItemViewHolder
                holder.bind(model.item, model.isFlipped, onItemClick)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (isValidPayload(payloads)) {
            val bundle = (payloads.firstOrNull() as? Bundle) ?: kotlin.run {
                super.onBindViewHolder(holder, position, payloads); return
            }
            if (bundle.containsKey(PAYLOAD_FLIP_STATE)) {
                val isFlipped = bundle.getBoolean(PAYLOAD_FLIP_STATE, false)
                (holder as? FlipItemViewHolder)?.updateFlipState(isFlipped)
            }
        } else {
            return super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FlippingUiModel.Item -> VIEW_TYPE_ITEM
            else -> error("Unable to decide a viewType for item $position")
        }
    }

    private fun isValidPayload(payloads: MutableList<Any>?): Boolean {
        return (payloads?.firstOrNull() as? Bundle)?.keySet()?.any {
            it == PAYLOAD_FLIP_STATE
        } ?: false
    }

    private class FlipItemViewHolder private constructor(
        private val binding: ItemFlippingViewBinding
    ) : ViewHolder(binding.root) {

        private var animatorSet: AnimatorSet? = null

        fun bind(data: FlippingItem, flipped: Boolean, onItemClick: (FlippingItem) -> Unit) = with(binding) {
            frontViewTitle.text = data.title

            backViewTitle.text = data.title
            backViewDescription.text = data.description
            backViewDate.text = TimeAgo.using(data.timestamp)

            updateFlipState(flipped, false)

            frontView.cameraDistance = root.cameraDistance * 4f
            backView.cameraDistance = root.cameraDistance * 4f
            root.setOnClickListener {
                onItemClick(data)
            }

            if (bindingAdapterPosition <= 10) {
                val translateAnimation = TranslateAnimation(
                    0f, 0f, 100f, 0f
                )
                val alphaAnimation = AlphaAnimation(0f, 1f)
                AnimationSet(true).apply {
                    duration = 300
                    interpolator = DecelerateInterpolator()
                    startOffset = bindingAdapterPosition * 100L
                    addAnimation(translateAnimation)
                    addAnimation(alphaAnimation)
                }.also {
                    root.startAnimation(it)
                }
            }
        }

        fun updateFlipState(flipped: Boolean, shouldAnimate: Boolean = true) = with(binding) {
            if (shouldAnimate) {
                val frontViewAnimator = if (flipped) {
                    ValueAnimator.ofFloat(0f, 90f).apply {
                        startDelay = 0
                    }
                } else {
                    ValueAnimator.ofFloat(90f, 0f).apply {
                        startDelay = FLIP_ANIMATION_DURATION
                    }
                }

                val backViewAnimator = if (flipped) {
                    ValueAnimator.ofFloat(-90f, 0f).apply {
                        startDelay = FLIP_ANIMATION_DURATION
                    }
                } else {
                    ValueAnimator.ofFloat(0f, -90f).apply {
                        startDelay = 0
                    }
                }

                frontViewAnimator.setDuration(FLIP_ANIMATION_DURATION)
                frontViewAnimator.addUpdateListener {
                    Timber.d("Anim: front ${it.animatedFraction} ${it.animatedValue}")
                    frontView.rotationX = it.animatedValue as Float
                }

                backViewAnimator.setDuration(FLIP_ANIMATION_DURATION)
                backViewAnimator.addUpdateListener {
                    Timber.d("Anim: back ${it.animatedFraction} ${it.animatedValue}")
                    backView.rotationX = it.animatedValue as Float
                }
                animatorSet?.cancel()
                AnimatorSet().apply {
                    this.interpolator = FastOutSlowInInterpolator()
                    this.playTogether(frontViewAnimator, backViewAnimator)
                    doOnCancel {
                        if (flipped) {
                            frontView.rotationX = 90f
                            backView.rotationX = 0f
                        } else {
                            frontView.rotationX = 0f
                            backView.rotationX = -90f
                        }
                        backViewDescription.isVisible = flipped
                        backViewDate.isVisible = flipped
                    }
                    doOnEnd { animatorSet = null }
                }.also {
                    animatorSet = it
                    animatorSet?.start()
                }

                backViewDescription.isVisible = flipped
                backViewDate.isVisible = flipped
            } else {
                if (flipped) {
                    frontView.rotationX = 90f
                    backView.rotationX = 0f
                } else {
                    frontView.rotationX = 0f
                    backView.rotationX = -90f
                }
                backViewDescription.isVisible = flipped
                backViewDate.isVisible = flipped
            }

            /*if (flipped) {
                frontView.isVisible = false
                backView.isVisible = true
            } else {
                frontView.isVisible = true
                backView.isVisible = false
            }*/
        }

        companion object {
            private const val FLIP_ANIMATION_DURATION: Long = 300
            fun from(parent: ViewGroup): FlipItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_flipping_view, parent, false)
                val binding = ItemFlippingViewBinding.bind(itemView)
                return FlipItemViewHolder(binding)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val PAYLOAD_FLIP_STATE = "flip_state"

        private val DiffCallback = object : ItemCallback<FlippingUiModel>() {
            override fun areItemsTheSame(
                oldItem: FlippingUiModel,
                newItem: FlippingUiModel
            ): Boolean {
                return when {
                    oldItem is FlippingUiModel.Item && newItem is FlippingUiModel.Item -> {
                        return oldItem.item.id == newItem.item.id
                    }

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: FlippingUiModel,
                newItem: FlippingUiModel
            ): Boolean {
                return when {
                    oldItem is FlippingUiModel.Item && newItem is FlippingUiModel.Item -> {
                        return oldItem.item == newItem.item && oldItem.isFlipped == newItem.isFlipped
                    }

                    else -> false
                }
            }

            override fun getChangePayload(
                oldItem: FlippingUiModel,
                newItem: FlippingUiModel
            ): Any {
                val payload = Bundle()
                when {
                    oldItem is FlippingUiModel.Item && newItem is FlippingUiModel.Item -> {
                        if (oldItem.isFlipped != newItem.isFlipped) {
                            payload.putBoolean(PAYLOAD_FLIP_STATE, newItem.isFlipped)
                        }
                    }
                }
                return payload
            }
        }
    }

}