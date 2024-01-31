package com.example.viewsystem.feature.flippingitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val defaultFlippingItems = listOf(
    FlippingItem(
        0,
        "Item 0",
        "Subtitle 0",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2)
    ),
    FlippingItem(
        1,
        "Item 1",
        "Subtitle 1",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2)
    ),
    FlippingItem(
        2,
        "Item 2",
        "Subtitle 2",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)
    ),
    FlippingItem(
        3,
        "Item 3",
        "Subtitle 3",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
    ),
    FlippingItem(
        4,
        "Item 4",
        "Subtitle 4",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
    ),
    FlippingItem(
        5,
        "Item 5",
        "Subtitle 5",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
    ),
    FlippingItem(
        6,
        "Item 6",
        "Subtitle 6",
        "This is a sample description which is a little bit long just to cover two lines.",
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6)
    ),
)

class FlippingItemViewModel : ViewModel() {

    private val _flippingItems: MutableStateFlow<List<FlippingUiModel>> = MutableStateFlow(listOf())
    val flippingItems = _flippingItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf()
        )

    init {
        _flippingItems.update {
            defaultFlippingItems
                .map { FlippingUiModel.Item(it, false) }
        }
    }

    fun handleBackPressed(): Boolean {
        val consumed = flippingItems.value.count { (it as FlippingUiModel.Item).isFlipped } > 0
        viewModelScope.launch {
            if (consumed) {
                val newList: List<FlippingUiModel> = flippingItems.value.filterIsInstance<FlippingUiModel.Item>()
                    .map { it.copy(isFlipped = false) }
                _flippingItems.update { newList }
            }
        }
        return consumed
    }

    fun flipItem(id: Int) {
        viewModelScope.launch {
            val newList: List<FlippingUiModel> = flippingItems.value.filterIsInstance<FlippingUiModel.Item>()
                .map {
                    if (it.item.id == id) {
                        it.copy(isFlipped = !it.isFlipped)
                    } else {
                        // it.copy(isFlipped = false)
                        it
                    }
                }
            _flippingItems.update { newList }
        }
    }
}

interface FlippingUiModel {
    data class Item(val item: FlippingItem, val isFlipped: Boolean) : FlippingUiModel
}