package com.example.viewsystem.commons.data.source.inmemory

class InMemoryAnalyticsDataSource {

    private val playbackDelayMap = mutableMapOf<String, Int>()

    val averagePlaybackDelay: Int
        get() = playbackDelayMap.values.average().toInt()

    private var _dataUsage: DataUsage = DataUsage.empty
    val dataUsage: DataUsage
        get() = _dataUsage

    fun addPlaybackDelayMs(key: String, delayMillis: Int) {
        playbackDelayMap[key] = delayMillis
    }

    fun appendDataUsage(down: Float = 0f, up: Float = 0f) {
        val upload = dataUsage.upload + up
        val download = dataUsage.download + down
        _dataUsage = DataUsage(upload = upload, download = download)
    }
}

data class DataUsage(
    val upload: Float,
    val download: Float,
) {
    val avg: Double
        get() = arrayOf(upload, download).average()

    companion object {
        internal val empty = DataUsage(0f, 0f)
    }
}

