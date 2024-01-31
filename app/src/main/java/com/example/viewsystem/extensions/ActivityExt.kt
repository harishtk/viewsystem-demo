package com.example.viewsystem.extensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowInsets
import androidx.core.net.toUri
import com.example.viewsystem.R
import timber.log.Timber
import java.net.URLEncoder

@Suppress("DEPRECATION")
fun Activity.getDisplaySize(): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val width = windowMetrics.bounds.width() - insets.left - insets.right
        val height = windowMetrics.bounds.height() - insets.top - insets.bottom
        Size(width, height)
    } else {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        Size(metrics.widthPixels, metrics.heightPixels)
    }
}

internal fun Activity.openMaps(
    address: String,
    onToast: (message: String) -> Unit = {}
) {
    // val mapsQuery = "https://maps.google.com/maps?q=loc:" + address
    val mapsQuery = "geo:0,0?q=" + URLEncoder.encode(address, "utf-8")
    val mapsIntent = Intent(Intent.ACTION_VIEW, mapsQuery.toUri())

    try {
        val resolveInfo: ResolveInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                mapsIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(
                mapsIntent,
                PackageManager.MATCH_ALL
            )
        }
        if (resolveInfo != null) {
            startActivity(mapsIntent)
        } else {
            onToast("No apps can perform this action.")
        }
    } catch (e: Exception) {
        ifDebug { Timber.e(e) }
        onToast(getString(R.string.unable_to_perform_this_action))
    }
}