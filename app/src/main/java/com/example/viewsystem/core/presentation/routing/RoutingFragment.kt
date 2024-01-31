package com.example.viewsystem.core.presentation.routing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.viewsystem.R
import com.example.viewsystem.databinding.FragmentRoutingBinding
import com.example.viewsystem.databinding.ItemDemoListingBinding
import com.example.viewsystem.feature.flippingitem.navigateToFlippingItemScreen

class RoutingFragment : Fragment(R.layout.fragment_routing) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRoutingBinding.bind(view)

        binding.bindState()
    }

    private fun FragmentRoutingBinding.bindState() {
        val adapter = DemoListAdapter { demoItem ->
            when (demoItem.id) {
                0 -> findNavController().navigateToFlippingItemScreen()
            }
        }.apply {
            submitList(
                listOf(
                    DemoItem(0, "Flipping RecyclerView Item")
                )
            )
        }

        listView.adapter = adapter
    }
}

private data class DemoItem(
    val id: Int,
    val title: String,
)

private class DemoListAdapter(
    private val onItemClick: (DemoItem) -> Unit = {}
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

        fun bind(data: DemoItem, onItemClick: (DemoItem) -> Unit) = with(binding) {
            title.text = data.title
            root.setOnClickListener { onItemClick(data) }
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