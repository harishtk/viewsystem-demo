package com.example.viewsystem

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewsystem.core.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _jumpToDestination = MutableSharedFlow<Int?>()
    val jumpToDestination = _jumpToDestination
        .filterNotNull()
        .shareIn(
            scope = viewModelScope,
            replay = 0,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun jumpToPage(@IdRes destinationId: Int?) = viewModelScope.launch {
        if (currentDestination.value != destinationId) {
            _jumpToDestination.emit(destinationId)
        }
    }

    /* Navigation current destination */
    val currentDestination: MutableStateFlow<Int> = MutableStateFlow(-1)

    fun setCurrentDestination(destinationId: Int) {
        currentDestination.update { destinationId }
    }

    /* Connection Listener */
    val isOffline =
        combine(
            networkMonitor.isOnline
                .map(Boolean::not),
            currentDestination
                .map { destinationId ->
                    destinationId !in noOfflineAlertDestinations
                },
            Boolean::and
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )
}

private val noOfflineAlertDestinations: List<Int> =
    listOf()