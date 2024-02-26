package com.example.viewsystem.feature.inputsheet

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.viewsystem.R
import com.example.viewsystem.databinding.FragmentInputSheetBinding
import com.example.viewsystem.databinding.FragmentLegoTextBinding
import com.example.viewsystem.extensions.themeColor
import com.google.android.material.transition.MaterialContainerTransform

class InputSheetFragment : Fragment(R.layout.fragment_input_sheet) {

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
        val binding = FragmentInputSheetBinding.bind(view)

        val from = arguments?.getString("from", "unknown")
        if (from == "routing") {
            postponeEnterTransition()

            val titleTransition = arguments?.getString("titleTransitionName") ?: ""
            binding.toolbar.transitionName = titleTransition

            startPostponedEnterTransition()
        }

        binding.bindState()
    }

    private fun FragmentInputSheetBinding.bindState() {
        footerContainer.setOnClickListener { showInputSheet() }
        edMessage.setOnClickListener { showInputSheet() }
        bindToolbar()
    }

    private fun FragmentInputSheetBinding.bindToolbar() {
        toolbarIncluded.toolbarTitle.text =
            getString(R.string.title_input_sheet)
    }

    private fun showInputSheet() {
        InputSheetDialog()
            .show(childFragmentManager, "input-sheet-dialog")
    }
}