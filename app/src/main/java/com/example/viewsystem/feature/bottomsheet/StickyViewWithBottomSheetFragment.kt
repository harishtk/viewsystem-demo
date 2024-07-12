package com.example.viewsystem.feature.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.viewsystem.R
import com.example.viewsystem.databinding.FragmentStickyFooterBottomSheetBinding
import com.example.viewsystem.extensions.themeColor
import com.google.android.material.transition.MaterialContainerTransform

class StickyViewWithBottomSheetFragment : Fragment(R.layout.fragment_sticky_footer_bottom_sheet) {

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
        val binding = FragmentStickyFooterBottomSheetBinding.bind(view)
        binding.bindToolbar()

        val from = arguments?.getString("from", "unknown")
        if (from == "routing") {
            postponeEnterTransition()

            val titleTransition = arguments?.getString("titleTransitionName") ?: ""
            binding.toolbarIncluded.toolbarTitle.transitionName = titleTransition

            startPostponedEnterTransition()
        }

        binding.bindState()
    }

    private fun FragmentStickyFooterBottomSheetBinding.bindState() {
        btnStickyFooterWithNestedScrollview.setOnClickListener {
            showStickyFooterWithNestedScrollviewDialog()
        }
    }

    private fun FragmentStickyFooterBottomSheetBinding.bindToolbar() {
        toolbarIncluded.toolbarTitle.text =
            getString(R.string.title_sticky_footer_with_bottom_sheet)
    }

    private fun showStickyFooterWithNestedScrollviewDialog() {
        StickyFooterWithNestedScrollViewDialog(requireContext()).show()
    }
}