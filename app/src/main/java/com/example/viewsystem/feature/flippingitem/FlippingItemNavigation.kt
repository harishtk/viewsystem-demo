package com.example.viewsystem.feature.flippingitem

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.viewsystem.R
import com.example.viewsystem.defaultNavOptsBuilder

internal fun NavController.navigateToFlippingItemScreen(
    options: NavOptions.Builder = defaultNavOptsBuilder()
) {
    navigate(R.id.flipping_item_page, null, options.build())
}