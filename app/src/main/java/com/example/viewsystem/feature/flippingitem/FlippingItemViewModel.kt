package com.example.viewsystem.feature.flippingitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
)

class FlippingItemViewModel : ViewModel() {

    private val _flippingItems: MutableStateFlow<List<FlippingItemModel>> = MutableStateFlow(listOf())
    val flippingItems = _flippingItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf()
        )

    init {
        _flippingItems.update {
            defaultFlippingItems
                .map { FlippingItemModel.Item(it, false) }
        }
    }

    fun flipItem(id: Int) {
        viewModelScope.launch {
            val newList: List<FlippingItemModel> = flippingItems.value.filterIsInstance<FlippingItemModel.Item>()
                .map {
                    if (it.item.id == id) {
                        it.copy(isFlipped = !it.isFlipped)
                    } else {
                        it
                    }
                }
            Timber.d("Flipped Items: ${newList.count { (it as FlippingItemModel.Item).isFlipped }}")
            _flippingItems.update { newList }
        }
    }
}