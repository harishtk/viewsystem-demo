package com.example.viewsystem.feature.listmotions

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import com.example.viewsystem.R
import com.example.viewsystem.defaultNavOptsBuilder

internal fun NavController.navigateToListMotionsScreen(
    args: Bundle? = null,
    options: NavOptions.Builder? = defaultNavOptsBuilder(),
    extras: FragmentNavigator.Extras? = null,
) {
    navigate(R.id.list_motions_page, args, options?.build(), extras)
}