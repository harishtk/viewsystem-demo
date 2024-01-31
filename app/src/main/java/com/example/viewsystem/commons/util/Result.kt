package com.example.viewsystem.commons.util

import kotlinx.coroutines.flow.*

/**
 * A generic class that holds data and it's state
 *
 * @param <T>
 */
sealed class Result<out R> {

    data class Success<R>(val data: R): Result<R>()
    data class Error(val exception: Throwable): Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*>   -> "Success[data=$data]"
            is Error        -> "Error[exception=$exception]"
            Loading         -> "Loading"
        }
    }
}

/**
 * `true` if [Result] is of type [Result.Success] & holds a non-null [Result.Success.data].
 */
val Result<*>.succeeded
    get() = this is Result.Success && this.data != null

val Result<*>.succeededResult get() = (this as? Result.Success)?.data

fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> {
            Result.Success(it)
        }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it as Exception)) }
}