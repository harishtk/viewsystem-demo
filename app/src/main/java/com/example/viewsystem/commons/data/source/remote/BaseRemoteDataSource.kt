package com.example.viewsystem.commons.data.source.remote

import android.util.Log
import androidx.annotation.WorkerThread
import com.example.viewsystem.BuildConfig
import com.example.viewsystem.core.domain.model.BaseResponse
import com.google.gson.GsonBuilder
import com.example.viewsystem.commons.util.NetworkResult
import com.example.viewsystem.commons.util.net.NoInternetException
import com.example.viewsystem.core.util.NetworkMonitor
import org.jetbrains.annotations.Contract
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection

private const val Tag = "BaseRemoteDataSource"

open class BaseRemoteDataSource(
    private val netWorkHelper: NetworkMonitor
) {

    private val logTag : String = BaseRemoteDataSource::class.java.simpleName

    @WorkerThread
    protected suspend fun <T> safeApiCall(call : suspend() -> Response<T>) : NetworkResult<T> {
        return when {
            netWorkHelper.hasNetwork() -> {
                try {
                    call.invoke().let { response: Response<T> ->
                        Timber.d("Response: body ${response.body()} errorBody ${response.errorBody()} raw $response")
                        if (response.isSuccessful) {
                            response.body()?.let {
                                NetworkResult.Success(it, response.message(), code = response.code())
                            } ?: error("Success. But no data")
                        } else {
                            apiErrorHandler(response)
                        }
                    }
                } catch (e: Exception) {
                    NetworkResult.Error(e, e.toString(), null)
                }
            }
            else -> {
                val t = NoInternetException("Please check internet connection")
                NetworkResult.Error(t, "Unable to connect to the internet", null)
            }
        }
    }

    @WorkerThread
    protected suspend fun <T> synchronizedCall(call : suspend() -> T): NetworkResult<T> {
        return try {
            val response = call.invoke() ?: throw IllegalStateException("Failed to make call.")
            Timber.d("Response: body $response")
            NetworkResult.Success(response, null)
        } catch (e: HttpException) {
            NetworkResult.Error(e, e.message(), code = e.code())
        } catch (e: IOException) {
            NetworkResult.Error(e, e.message ?: "Unknown Error")
        }
    }

    private fun <T> apiErrorHandler(response: Response<T>) : NetworkResult<T> {
        val errorBodyJson = response.errorBody()?.string()
        Timber.d("Response: errorBody $errorBodyJson")
        val errorBody = parseErrorBody(errorBodyJson)
        val message = response.message()
        var errorMessage: String
        try {
            when (val code = response.code()) {
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    errorMessage = "$code ${HttpURLConnection.HTTP_INTERNAL_ERROR}"
                }
                else -> {
                    errorMessage = "$code $message"
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) { Timber.tag(logTag).e(e, e.toString()) }
            errorMessage = "${response.code()} $message"
        }
        Timber.tag(Tag).d("Api Error: ${errorBody?.message}")
        val t = Exception(errorBody?.message)
        return NetworkResult.Error(t, "Api Error Response: $errorMessage", uiMessage = errorBody?.message, code = response.code())
    }

    @Contract("null -> null")
    private fun parseErrorBody(errorBodyJson: String?): BaseResponse? {
        Log.d(Tag, "parseErrorBody: $errorBodyJson")
        errorBodyJson ?: return null
        return try {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            return gson.fromJson(errorBodyJson, BaseResponse::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

}