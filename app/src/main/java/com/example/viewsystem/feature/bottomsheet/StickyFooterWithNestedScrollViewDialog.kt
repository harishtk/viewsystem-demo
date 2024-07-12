package com.example.viewsystem.feature.bottomsheet

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.viewsystem.R
import com.example.viewsystem.databinding.DialogStickyFooterWithNestedscrollviewBinding
import com.example.viewsystem.doOnApplyWindowInsets
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import timber.log.Timber


class StickyFooterWithNestedScrollViewDialog(
    context: Context,
) : BottomSheetDialog(context) {

    private lateinit var binding: DialogStickyFooterWithNestedscrollviewBinding
    private lateinit var resources: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogStickyFooterWithNestedscrollviewBinding.inflate(layoutInflater)
        resources = binding.root.resources
        setContentView(binding.root)

        binding.bindState()

        setOnShowListener { setupStickyFooter() }
    }

    private fun DialogStickyFooterWithNestedscrollviewBinding.bindState() {
        val bottomSheetDialog = this@StickyFooterWithNestedScrollViewDialog
        val bottomSheet: FrameLayout =
            bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) ?: return
        val behavior = BottomSheetBehavior.from(bottomSheet)

        root.setOnScrollChangeListener { _, _, _, _, _ ->
            // val isFooterVisible = root.isChildVisible(footerContainer)
            // Timber.d("BottomSheet: isFooterVisible=$isFooterVisible")

        }
    }

    private fun setupStickyFooter() {
        val bottomSheetDialog: BottomSheetDialog = this
        val behavior = bottomSheetDialog.behavior as BottomSheetBehavior<FrameLayout>

        val bottomSheet: FrameLayout =
            bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        val peekHeight =
            resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_mid_tall)
        behavior.peekHeight = peekHeight
        behavior.skipCollapsed = false
        behavior.isDraggable = true
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isShouldRemoveExpandedCorners = false

        var statusBarHeight = 0
        var navBarHeight = 0
        binding.root.doOnApplyWindowInsets { view, windowInsetsCompat, _ ->
            statusBarHeight = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars())
                .top
            navBarHeight = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars())
                .bottom
            binding.footerContainer.updatePadding(
                bottom = navBarHeight
            )
        }

        binding.root.post {
            val bottomSheetVisibleHeight = bottomSheet.height - bottomSheet.top
            binding.footerContainer.y =
                (bottomSheetVisibleHeight - binding.footerContainer.height).toFloat()
        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        onSlide(bottomSheet, 1.0f)
                    }
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val systemBarsOffset = statusBarHeight.times(slideOffset).toInt()
                val updatedBottomSheetVisibleHeight = (bottomSheet.parent as View).height - bottomSheet.top - systemBarsOffset

                binding.footerContainer.y =
                    (updatedBottomSheetVisibleHeight - binding.footerContainer.height).toFloat()

            }
        })
    }

}