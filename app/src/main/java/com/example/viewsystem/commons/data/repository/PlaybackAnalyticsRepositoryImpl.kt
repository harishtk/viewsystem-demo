package com.example.viewsystem.commons.data.repository

import com.example.viewsystem.commons.data.source.inmemory.InMemoryAnalyticsDataSource
import com.example.viewsystem.commons.domain.repository.PlaybackAnalyticsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PlaybackAnalyticsRepositoryImpl(
    private val inMemoryDataSource: InMemoryAnalyticsDataSource,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PlaybackAnalyticsRepository {

    private val mLock = Object()

    private val avgPlaybackDelayFlow: Flow<Int> = flow {
        while (true) {
            emit(inMemoryDataSource.averagePlaybackDelay)
            withContext(dispatcher) {
                synchronized(mLock) {
                    mLock.wait()
                }
            }
        }
    }.flowOn(dispatcher)

    override fun addFirstFrameRenderDelay(key: String, delayMs: Int) {
        inMemoryDataSource.addPlaybackDelayMs(key, delayMillis = delayMs)
        synchronized(mLock) {
            mLock.notify()
        }
    }

    override fun getAveragePlaybackTime(): Flow<Int> {
        return avgPlaybackDelayFlow
    }

}