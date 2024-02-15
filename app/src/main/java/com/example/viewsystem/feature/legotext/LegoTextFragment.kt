package com.example.viewsystem.feature.legotext

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.viewsystem.MainActivity.Companion.UI_RENDER_WAIT_TIME
import com.example.viewsystem.R
import com.example.viewsystem.commons.util.recyclerview.SmoothScrollingLinearLayoutManager
import com.example.viewsystem.databinding.FragmentLegoTextBinding
import com.example.viewsystem.extensions.showSnack
import com.example.viewsystem.extensions.showToast
import com.example.viewsystem.extensions.themeColor
import com.example.viewsystem.feature.legotext.util.MessageAdapter
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class LegoTextFragment : Fragment(R.layout.fragment_lego_text) {

    private val viewModel: LegoTextViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLegoTextBinding.bind(view)
        binding.bindToolbar()

        val from = arguments?.getString("from", "unknown")
        if (from == "routing") {
            postponeEnterTransition()

            val titleTransition = arguments?.getString("titleTransitionName") ?: ""
            binding.toolbar.transitionName = titleTransition

            startPostponedEnterTransition()
        }

        binding.bindMessagesState(
            uiState = viewModel.messagesUiState,
            uiAction = viewModel.accept,
            uiEvent = viewModel.uiEvent,
        )
    }

    private fun FragmentLegoTextBinding.bindMessagesState(
        uiState: StateFlow<MessagesUiState>,
        uiAction: (MessagesUiAction) -> Unit,
        uiEvent: SharedFlow<MessagesUiEvent>
    ) {
        uiEvent.onEach { event ->
            when (event) {
                is MessagesUiEvent.ShowSnack -> {
                    root.showSnack(event.message.asString(requireContext()))
                }
                is MessagesUiEvent.ShowToast -> {
                    context?.showToast(event.message.asString(requireContext()))
                }
                is MessagesUiEvent.MessageSent -> {
                    edMessage.setText("")
                    scrollToBottom()
                }
            }
        }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        val adapter = MessageAdapter(object : MessageAdapter.Callback {})

        bindList(
            adapter = adapter,
            uiState = uiState,
            uiAction = uiAction
        )

        bindInput(
            uiAction = uiAction
        )

        bindClick(
            uiState = uiState,
            uiAction = uiAction,
        )
    }

    private fun FragmentLegoTextBinding.bindList(
        adapter: MessageAdapter,
        uiState: StateFlow<MessagesUiState>,
        uiAction: (MessagesUiAction) -> Unit,
    ) {
        val layoutManager = SmoothScrollingLinearLayoutManager(requireContext(), true)

        listView.layoutManager = layoutManager
        listView.adapter = adapter

        uiState.map { it.messages }
            .distinctUntilChanged()
            .onEach { data ->
                adapter.submitList(data)
                listView.post { Timber.d("Messages: total fetched ${adapter.itemCount}") }
            }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        /*bindScroller(
            adapter = adapter,
            uiState = uiState,
            onScrolled = uiAction
        )*/
    }

    private fun FragmentLegoTextBinding.bindInput(
        uiAction: (MessagesUiAction) -> Unit
    ) {
        edMessage.hint = getString(R.string.type_a_message)
        edMessage.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                uiAction(MessagesUiAction.SendClick)
                true
            } else {
                false
            }
        }

        edMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                uiAction(MessagesUiAction.SendClick)
                true
            } else {
                false
            }
        }

        edMessage.addTextChangedListener(
            afterTextChanged = { editable ->
                uiAction(MessagesUiAction.TypingMessage(editable.toString().trim()))
            }
        )
    }

    private fun FragmentLegoTextBinding.bindClick(
        uiState: StateFlow<MessagesUiState>,
        uiAction: (MessagesUiAction) -> Unit,
    ) {
        uiState.map { it.typedMessage.isNotBlank() }
            .distinctUntilChanged()
            .onEach { enableSend ->
                ivSend.isEnabled = enableSend
            }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        ivSend.setOnClickListener {
            uiAction(MessagesUiAction.SendClick)
        }
    }

    private fun FragmentLegoTextBinding.scrollToBottom() {
        listView.postDelayed({
            val layoutManager = listView.layoutManager as SmoothScrollingLinearLayoutManager
            if (layoutManager.findFirstVisibleItemPosition() < SCROLL_ANIMATION_THRESHOLD) {
                listView.smoothScrollToPosition(0)
            } else {
                listView.scrollToPosition(0)
            }
        }, UI_RENDER_WAIT_TIME)
    }

    private fun FragmentLegoTextBinding.bindToolbar() {
        toolbarIncluded.toolbarTitle.text =
            getString(R.string.title_lego_text_blocks)
    }

    companion object {
        private const val SCROLL_ANIMATION_THRESHOLD = 50
    }
}