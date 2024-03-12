package com.example.viewsystem.feature.listmotions

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.viewsystem.R
import com.example.viewsystem.databinding.FragmentListMotionsBinding
import com.example.viewsystem.extensions.themeColor
import com.example.viewsystem.feature.listmotions.util.OnItemDragListener
import com.example.viewsystem.feature.listmotions.util.ListMotionsAdapter
import com.example.viewsystem.feature.listmotions.util.SimpleItemTouchHelperCallback
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class ListMotionsFragment : Fragment(R.layout.fragment_list_motions), OnItemDragListener {

    private val viewModel: ListMotionsViewModel by viewModels()

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private val onBackPressedHandler: () -> Unit = {
        if (!viewModel.handleBackPressed()) {
            findNavController().navigateUp()
        }
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

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
        val binding = FragmentListMotionsBinding.bind(view)
        binding.bindToolbar()

        val from = arguments?.getString("from", "unknown")
        if (from == "routing") {
            postponeEnterTransition()

            val titleTransition = arguments?.getString("titleTransitionName") ?: ""
            binding.toolbarIncluded.toolbarTitle.transitionName = titleTransition

            startPostponedEnterTransition()
        }

        binding.bindState(
            items = viewModel.items,
        )

        handleBackPressed()

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                for (i in 1..3) {
                    viewModel.addItem()
                }
            }
        }
    }

    private fun FragmentListMotionsBinding.bindState(
        items: StateFlow<List<SortingItem>>,
    ) {
        val layoutManager = listView.layoutManager as? LinearLayoutManager
        fab.setOnClickListener {
            viewModel.addItem()
            listView.post { layoutManager?.scrollToPosition(viewModel.items.value.size - 1) }
        }

        val adapter = ListMotionsAdapter(
            onSwiped = { position, _ ->
                viewModel.removeItem(position)
            },
            onMoved = { fromPosition, toPosition ->
                // TODO: reordering is buggy
                viewModel.reorderItems(fromPosition, toPosition)
                true
            },
            onItemDragListener = this@ListMotionsFragment
        )

        val itemTouchCallback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(listView)

        listView.adapter = adapter

        items
            .onEach(adapter::submitList)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun FragmentListMotionsBinding.bindToolbar() {
        toolbarIncluded.toolbarTitle.text =
            getString(R.string.title_list_motions)
    }

    private fun handleBackPressed() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedHandler.invoke()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onDragStart(viewHolder: ViewHolder): Boolean {
        itemTouchHelper.startDrag(viewHolder)
        return true
    }
}

