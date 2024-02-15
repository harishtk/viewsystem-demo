package com.example.viewsystem.core.presentation.routing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.viewsystem.R
import com.example.viewsystem.databinding.FragmentRoutingBinding
import com.example.viewsystem.databinding.ItemDemoListingBinding
import com.example.viewsystem.feature.calendarview.navigateToCalendarView
import com.example.viewsystem.feature.flippingitem.navigateToFlippingItemScreen
import com.example.viewsystem.feature.legotext.navigateToLegoTextBlocks
import com.google.android.material.transition.MaterialElevationScale
import timber.log.Timber

class RoutingFragment : Fragment(R.layout.fragment_routing) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val binding = FragmentRoutingBinding.bind(view)

        binding.bindState()
    }

    private fun FragmentRoutingBinding.bindState() {
        val adapter = DemoListAdapter { view, demoItem ->
            when (demoItem.id) {
                0 -> gotoFlippingRecyclerViewItem(view)
                1 -> gotoLegoTextBlocksFragment(view)
                2 -> gotoCalendarViewFragment(view)
            }
        }.apply {
            submitList(
                listOf(
                    DemoItem(0, getString(R.string.title_flipping_recyclerview_item)),
                    DemoItem(1, getString(R.string.title_lego_text_blocks)),
                    DemoItem(2, getString(R.string.title_calendar_view))
                )
            )
        }

        listView.adapter = adapter
    }

    private fun gotoFlippingRecyclerViewItem(
        view: View
    ) {
        resetTransitions()
        enterTransition = MaterialElevationScale(/* growing= */ false)
        exitTransition = MaterialElevationScale(/* growing= */ false)
        reenterTransition = MaterialElevationScale(/* growing= */ false)

        val args = bundleOf(
            "from" to "routing",
            "titleTransitionName" to view.transitionName
        )
        val extras = FragmentNavigatorExtras(
            view to view.transitionName
        )

        findNavController().navigateToFlippingItemScreen(
            args = args, null, extras = extras
        )
    }

    private fun gotoLegoTextBlocksFragment(
        view: View
    ) {
        resetTransitions()
        enterTransition = MaterialElevationScale(/* growing= */ false)
        exitTransition = MaterialElevationScale(/* growing= */ false)
        reenterTransition = MaterialElevationScale(/* growing= */ true)

        val args = bundleOf(
            "from" to "routing",
            "titleTransitionName" to view.transitionName
        )
        val extras = FragmentNavigatorExtras(
            view to view.transitionName
        )

        findNavController().navigateToLegoTextBlocks(
            args = args, null, extras = extras
        )
    }

    private fun gotoCalendarViewFragment(
        view: View
    ) {
        resetTransitions()
        enterTransition = MaterialElevationScale(/* growing= */ false)
        exitTransition = MaterialElevationScale(/* growing= */ false)
        reenterTransition = MaterialElevationScale(/* growing= */ true)

        val args = bundleOf(
            "from" to "routing",
            "titleTransitionName" to view.transitionName
        )
        val extras = FragmentNavigatorExtras(
            view to view.transitionName
        )

        findNavController().navigateToCalendarView(
            args = args, null, extras = extras
        )
    }

    private fun resetTransitions() {
        enterTransition = null
        exitTransition = null
        reenterTransition = null
    }
}

private data class DemoItem(
    val id: Int,
    val title: String,
)

private class DemoListAdapter(
    private val onItemClick: (View, DemoItem) -> Unit = { _, _ -> }
) : ListAdapter<DemoItem, DemoListAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick)
    }

    class ItemViewHolder private constructor(
        private val binding: ItemDemoListingBinding
    ) : ViewHolder(binding.root) {

        fun bind(data: DemoItem, onItemClick: (View, DemoItem) -> Unit) = with(binding) {
            title.text = data.title
            root.transitionName = "transition_${data.title.lowercase().replace(" ", "_")}"
            root.setOnClickListener { onItemClick(it, data) }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_demo_listing, parent, false)
                val binding = ItemDemoListingBinding.bind(itemView)
                return ItemViewHolder(binding)
            }
        }
    }

    companion object {
        private val DiffCallback = object : ItemCallback<DemoItem>() {
            override fun areItemsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}