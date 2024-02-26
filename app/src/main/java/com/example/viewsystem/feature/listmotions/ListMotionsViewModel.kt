package com.example.viewsystem.feature.listmotions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewsystem.makeColoredSubstring
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class ListMotionsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _items = MutableStateFlow<List<SortingItem>>(emptyList())
    val items = _items
        // .map { list -> list.sortedBy { it.sortPosition } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun handleBackPressed(): Boolean {
        return false
    }

    fun addItem() {
        val nextItemNumber: Int = getCount().plus(1)
        Timber.d("addItem() called next=$nextItemNumber")
        val newItems = _items.value.toMutableList().apply {
            add(
                SortingItem("Item ${nextItemNumber}", nextItemNumber)
            )
        }
        _items.update { newItems }
        savedStateHandle["count"] = nextItemNumber
    }

    fun removeItem(position: Int): Boolean {
        val consumed = position >= 0 && position < _items.value.size
        val newItems = _items.value.toMutableList().apply {
            removeAt(position)
        }
        _items.update { newItems }
        return consumed
    }

    fun swapItems(fromPosition: Int, toPosition: Int) {
        Timber.d("swapItems() called with: fromPosition = [$fromPosition], toPosition = [$toPosition]")
        /*try {
            val currentList = _items.value.toMutableList()
            if (fromPosition < toPosition) {
                for (i in fromPosition..<toPosition) {
                    Collections.swap(currentList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition -1 ) {
                    Collections.swap(currentList, i, i - 1)
                }
            }
            _items.update { currentList }
        } catch (ignore: IndexOutOfBoundsException) {}*/
        /*try {
            val currentList = _items.value.toMutableList()
        } catch (ignore: IndexOutOfBoundsException) {}*/
    }

    fun reorderItems(fromPosition: Int, toPosition: Int) {
        val currentList = _items.value.toMutableList()
        val fromItem = currentList[fromPosition]
        val toItem = currentList[toPosition]

        _items.update {
            currentList.map {
                when (it.data) {
                    fromItem.data -> {
                        it.copy(sortPosition = toItem.sortPosition)
                    }
                    toItem.data -> {
                        it.copy(sortPosition = fromItem.sortPosition)
                    }
                    else -> {
                        it
                    }
                }
            }
        }
    }

    private fun getCount(): Int {
        return savedStateHandle["count"] ?: 0
    }

}

data class SortingItem(
    val data: String,
    /* Note: sortPosition != index but sortPosition.default = index */
    val sortPosition: Int,
)