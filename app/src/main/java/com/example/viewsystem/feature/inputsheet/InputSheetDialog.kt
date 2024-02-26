package com.example.viewsystem.feature.inputsheet

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.viewsystem.R
import com.example.viewsystem.commons.util.Util
import com.example.viewsystem.databinding.DialogInputSheetBinding
import com.example.viewsystem.extensions.autoCleared
import com.example.viewsystem.extensions.fakeDisable
import com.example.viewsystem.extensions.showSoftInputMode
import com.example.viewsystem.view.animation.ResizeAnimation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class InputSheetDialog : BottomSheetDialogFragment(R.layout.dialog_input_sheet) {

    private var binding: DialogInputSheetBinding by autoCleared()

    private var bottomSheet: FrameLayout? = null
    private var fullScreenMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(R.style.ModalBottomSheetDialog, theme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheetDialog: BottomSheetDialog = it as BottomSheetDialog
            val behavior = bottomSheetDialog.behavior as BottomSheetBehavior<FrameLayout>

            val peekHeight =
                resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_mid_tall)
            behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true)
            behavior.skipCollapsed = false
            behavior.isDraggable = true
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                @SuppressLint("SwitchIntDef")
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    Timber.d("BottomSheet.State: $newState")
                    when (newState) {
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })

            val bottomSheet: FrameLayout =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                    ?: return@setOnShowListener
            this@InputSheetDialog.bottomSheet = bottomSheet
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogInputSheetBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                binding.edMessage.requestFocus()
                binding.edMessage.showSoftInputMode()
            }
        }

        val messageTextState = MessageTextState()

        binding.bindState(
            messageTextState = messageTextState
        )
        binding.switchViews(fullScreenMode)
    }

    private fun DialogInputSheetBinding.bindState(messageTextState: MessageTextState) {
        messageTextState.text
            .onEach { typed ->
                val t = AutoTransition().apply {
                    addTarget(ivFullscreenToggle)
                    addTarget(messageInputLayout)
                }
                TransitionManager.beginDelayedTransition(root as ViewGroup, t)
                // ivFullscreenToggle.isVisible = typed.isNotBlank()
            }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        ivFullscreenToggle.isVisible = false
        ivFullscreenToggle.setOnClickListener {
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet!!).let {
                    // toggleFullScreen()
                    it.state = BottomSheetBehavior.STATE_EXPANDED
                    if (fullScreenMode) {
                        ivFullscreenToggle.setImageResource(R.drawable.baseline_fullscreen_exit_24)
                    } else {
                        ivFullscreenToggle.setImageResource(R.drawable.baseline_fullscreen_24)
                    }
                }
            }
        }

        val chevRonState = MutableStateFlow(false)
        ivExpandToggle.setOnClickListener {
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet!!).let {
                    toggleFullScreen()
                    // it.state = BottomSheetBehavior.STATE_EXPANDED
                    chevRonState.update { state -> !state }
                    switchViews(fullScreenMode)
                }
            }
        }

        var chevRonAnimator: Animator? = null
        chevRonState.onEach { chevronState ->
            val icon = ivExpandToggle
            chevRonAnimator?.cancel()
            chevRonAnimator = run {
                val animator = if (chevronState) {
                    ValueAnimator.ofFloat(icon.rotation, 180f)
                } else {
                    ValueAnimator.ofFloat(icon.rotation, 0f)
                }
                animator.addUpdateListener {
                    val rotation = it.animatedValue as Float
                    icon.rotation = rotation
                }
                animator
            }
            chevRonAnimator?.start()
        }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        bindInput(
            messageTextState = messageTextState,
            onSubmit = {
                edMessage.text = null
            }
        )
    }

    private fun DialogInputSheetBinding.bindInput(
        messageTextState: MessageTextState,
        onSubmit: () -> Unit,
    ) {
        messageTextState.getError()
            .map { error ->
                messageInputLayout.isErrorEnabled = !error.isNullOrBlank()
                messageInputLayout.error = error
            }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        edMessage.setOnFocusChangeListener { v, hasFocus ->
            messageTextState.onFocusChange(hasFocus)
        }

        edMessage.addTextChangedListener(
            afterTextChanged = { typed ->
                messageTextState.text.update { typed.toString().trim() }
            }
        )

        edMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onSubmit()
                true
            } else {
                false
            }
        }

        edMessage.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                onSubmit()
                true
            } else {
                false
            }
        }

        messageTextState.isValid()
            .map { enabled ->
                ivSend.fakeDisable(!enabled)
            }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        ivSend.setOnClickListener {
            messageTextState.enableShowErrors(ignoreFocus = true)
            if (messageTextState.isValid) {
                onSubmit()
                messageTextState.reset()
            }
        }
    }

    private fun DialogInputSheetBinding.switchViews(fullScreenMode: Boolean) {
        if (fullScreenMode) {
            tvTitleMessage.isVisible = true
            input1.isVisible = true
            input2.isVisible = true
            input3.isVisible = true
            input4.isVisible = true
            ivSend.isVisible = false
            /*(inputContainer.layoutParams as? ConstraintLayout.LayoutParams)?.let { lp ->
                lp.verticalBias = 0f
            }*/

        } else {
            tvTitleMessage.isVisible = false
            ivSend.isVisible = true
            input1.isVisible = false
            input2.isVisible = false
            input3.isVisible = false
            input4.isVisible = false
            /*(inputContainer.layoutParams as? ConstraintLayout.LayoutParams)?.let { lp ->
                lp.verticalBias = 1f
            }*/
        }
    }

    private fun toggleFullScreen() {
        bottomSheet?.layoutParams?.let { lp ->
            lp.height = if (!fullScreenMode) {
                WindowManager.LayoutParams.MATCH_PARENT
            } else {
                WindowManager.LayoutParams.WRAP_CONTENT
            }
            lp
        }.also {
            /*val t = AutoTransition()
            TransitionManager.beginDelayedTransition(bottomSheet as ViewGroup, t)*/
            bottomSheet?.layoutParams = it
            fullScreenMode = !fullScreenMode
        }
    }
}