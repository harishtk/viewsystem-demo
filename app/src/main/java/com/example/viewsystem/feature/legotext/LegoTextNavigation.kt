package com.example.viewsystem.feature.legotext

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import com.example.viewsystem.R
import com.example.viewsystem.defaultNavOptsBuilder

internal fun NavController.navigateToLegoTextBlocks(
    args: Bundle? = null,
    options: NavOptions.Builder? = defaultNavOptsBuilder(),
    extras: FragmentNavigator.Extras? = null,
) {
    navigate(R.id.lego_text_page, args, options?.build(), extras)
}