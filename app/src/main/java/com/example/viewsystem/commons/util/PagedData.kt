package com.example.viewsystem.commons.util

data class PagedData<Key: Any, T>(
    val data: List<T>,
    val totalCount: Int,
    val prevKey: Key?,
    val nextKey: Key?
)