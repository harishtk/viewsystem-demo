package com.example.viewsystem.commons.util

import android.content.SharedPreferences
import com.example.viewsystem.commons.util.persistent.KeyValueDataSet
import com.example.viewsystem.commons.util.persistent.KeyValuePersistentStorage

class PrefKeyValueStore(
    private val appPreferences: SharedPreferences
) : KeyValuePersistentStorage {

    override fun writeDateSet(dataSet: KeyValueDataSet, removes: Collection<String>) {
        synchronized(this) {
            appPreferences.edit().apply {
                for (entry in dataSet.values) {
                    val key = entry.key
                    val value = entry.value
                    val type = dataSet.getType(key)

                    when (type) {
                        ByteArray::class.java -> {
                            throw IllegalStateException("Failed to insert $key. Blob are not supported yet")
                        }
                        Boolean::class.java -> {
                            putBoolean(key, value as Boolean)
                        }
                        Float::class.java -> {
                            putFloat(key, value as Float)
                        }
                        Int::class.java -> {
                            putInt(key, value as Int)
                        }
                        Long::class.java -> {
                            putLong(key, value as Long)
                        }
                        String::class.java -> {
                            putString(key, value as String)
                        }
                    }
                }
                removes.forEach(this::remove)
            }.apply()
        }
    }

    override fun getDataSet(): KeyValueDataSet {
        val dataSet = KeyValueDataSet()

        val pairs = appPreferences.all
        for (key in pairs.keys) {
            val type    = pairs[key]!!::class.java
            dataSet.put(key, pairs[key]!!)
        }

        return dataSet
    }

    companion object {
        object AccountKeys : kotlin.collections.Iterable<String> {
            const val DEVICE_TOKEN: String = "device_token"
            const val USER_ID: String = "user_id"
            const val USERNAME: String = "username"

            class AccountKeysIterator : Iterator<String> {
                val keys = listOf(
                    DEVICE_TOKEN,
                    USER_ID,
                    USERNAME
                )

                var pointer = 0

                override fun hasNext(): Boolean {
                    return pointer != keys.size
                }

                override fun next(): String {
                    return keys[pointer++]
                }

            }

            override fun iterator(): Iterator<String> = AccountKeysIterator()
        }

        object MiscKeys {
            const val USER_VOLUME_STATE = "user_volume_state"
            const val USER_VIDEO_QUALITY = "user_video_quality"
            const val USER_ENCODER_PREFERENCE = "user_encoder_preference"
        }
    }
}