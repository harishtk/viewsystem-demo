package com.example.viewsystem.commons.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.graphics.ColorSpace
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.annotation.ColorInt
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.text.DecimalFormat
import java.util.*
import java.util.function.DoubleUnaryOperator
import kotlin.math.ln
import kotlin.math.pow

public fun parseViews(viewCount: Int): String = when {
    viewCount < 1000 -> "$viewCount"
    else -> {
        val exp = (ln(viewCount.toDouble()) / ln(1000.0)).toInt()
        val format = DecimalFormat("##.#")
        String.format(
            "${format.format(viewCount / 1000.0.pow(exp.toDouble()))}%c",
            "kMGTPE"[exp - 1]
        )
    }
}

fun getMimeType(context: Context, uri: Uri): String? {
    var mimeType: String? = null
    mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val cr: ContentResolver = context.contentResolver
        cr.getType(uri)
    } else {
        val fileExtension: String = MimeTypeMap.getFileExtensionFromUrl(
            uri
                .toString()
        )
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.lowercase(Locale.getDefault())
        )
    }
    return mimeType
}

fun parseJsonFromString(context: Context, jsonFile: Int): String {
    var resultString: String = ""
    try {
        val raw = context.resources.openRawResource(jsonFile)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        raw.use { rawData ->
            val reader: Reader = BufferedReader(InputStreamReader(rawData, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        }
        resultString = writer.toString()
        return resultString
    } catch (e: Exception) {
        Timber.tag("ERROR").d(e.toString())
        return resultString
    }
}

fun getEmoji(unicode: Int): String {
    return String(Character.toChars(unicode))
}

/**
 * Returns the relative luminance of a color.
 *
 *
 * Assumes sRGB encoding. Based on the formula for relative luminance
 * defined in WCAG 2.0, W3C Recommendation 11 December 2008.
 *
 * @return a value between 0 (darkest black) and 1 (lightest white)
 */
fun luminance(@ColorInt color: Int): Float {
    val cs = ColorSpace.get(ColorSpace.Named.SRGB) as ColorSpace.Rgb
    val eotf: DoubleUnaryOperator = cs.eotf
    val r: Double = eotf.applyAsDouble(Color.red(color) / 255.0)
    val g: Double = eotf.applyAsDouble(Color.green(color) / 255.0)
    val b: Double = eotf.applyAsDouble(Color.blue(color) / 255.0)
    return (0.2126 * r + 0.7152 * g + 0.0722 * b).toFloat()
}

@ColorInt
fun getContrastColor(color: Color): Int {
    val y: Float = 299 * color.red() + 587 * color.green() + 114 * color.blue() / 1000
    return if (y >= 128) Color.BLACK else Color.WHITE
}

/**
 * A suspend function to obtain Firebase token
 */
/*suspend fun getFirebaseToken() = suspendCancellableCoroutine<String?> { result ->
    // Attempt to obtain a new token..
    try {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                result.resume(task.result) {}
            }
    } catch (e: Exception) {
        ifDebug { Timber.w(e) }
        result.resume(null) {}
    }
}*/
