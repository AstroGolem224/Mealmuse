package com.mealmuse.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    this
        .map<T, Result<T>> { Result.success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.failure(it)) }

fun <T> Flow<T>.onSuccess(action: suspend (T) -> Unit): Flow<T> =
    map { value ->
        action(value)
        value
    }

fun <T> Flow<T>.onFailure(action: suspend (Throwable) -> Unit): Flow<T> =
    catch { throwable ->
        action(throwable)
        throw throwable
    }
