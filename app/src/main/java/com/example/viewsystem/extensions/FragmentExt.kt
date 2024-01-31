package com.example.viewsystem.extensions

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asFlow
import androidx.navigation.fragment.findNavController
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal const val FRAGMENT_RESULT = "fragment_result"
internal const val RESULT_OK = -1
internal const val RESULT_CANCELED = 0

fun <R> Fragment.getNavigationResultFlow(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<R?>(key)?.asFlow()

fun Fragment.setNavigationResult(result: Bundle?, key: String = "result") {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

fun <T> Fragment.clearNavigationResult(key: String = "result") {
    findNavController().currentBackStackEntry?.savedStateHandle?.remove<T>(key)
}

class AutoClearedValue<T : Any>(val fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifeCycleOwner ->
                    viewLifeCycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            _value = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: error("should never call-autocleared-value get when it might not be available")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}

/**
 * Creates an [AutoClearedValue] associated with this fragment.
 */
fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T>(this)

/**
 * This holds the alert dialog reference and auto-dismisses it when the fragment is destroyed
 */
class AutoDismissValue<T : AlertDialog>(val fragment: Fragment) : ReadWriteProperty<Fragment, T?> {
    private var _value: WeakReference<T> = WeakReference(null)

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifeCycleOwner ->
                    viewLifeCycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            _value.get()?.dismiss()
                            _value = WeakReference(null)
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        return _value.get()
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        _value = WeakReference(value)
    }
}

/**
 * Creates an [AutoDismissValue] associated with this fragment.
 */
fun <T : AlertDialog> Fragment.autoDismissed() = AutoDismissValue<T>(this)


