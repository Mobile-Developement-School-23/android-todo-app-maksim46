package com.example.todoapp.data.network

import android.util.Log
import com.example.todoapp.data.model.OnErrorModel
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import javax.inject.Inject
/**
 *
 * Contains general logic for requests, network error handling and retries
 */

const val RETRIES_COUNTER = 2

class HttpClientHandler @Inject constructor(
) {
    var dbRevisionGetter: (suspend () -> Int)? = null


    suspend fun <T> executeRequest(
        requestCall: suspend () -> T, onCompletion: ((T) -> Unit)? = null,
        onError: (message: OnErrorModel) -> Unit, retryIfError: Set<HttpStatusCode> = setOf()
    ): T? {
        var response: T? = null
        var isError = true
        var retries = 0
        while (retries < RETRIES_COUNTER) {
            try {
                response = requestCall()
                isError = false
                break
            } catch (ce: ClientRequestException) {
                if (ce.response.status in retryIfError && retries == 0) {
                    retries++
                    Log.d("RETRIES", "$retries")
                    dbRevisionGetter?.invoke()
                    delay(3000)
                    continue
                }
                when (ce.response.status) {
                    HttpStatusCode.BadRequest -> {
                        onError(OnErrorModel.ER_400)
                        isError = false
                    }

                    HttpStatusCode.Unauthorized -> {
                        onError(OnErrorModel.ER_401)
                        isError = false
                    }

                    HttpStatusCode.NotFound -> {
                        onError(OnErrorModel.ER_404)
                        isError = false
                    }

                    else -> {
                        onError(OnErrorModel.ER_UNKNOWN)
                        isError = false
                    }
                }
                break
            } catch (se: ServerResponseException) {
                if (retries == 0) {
                    retries++
                    dbRevisionGetter?.invoke()
                    delay(1000)
                    continue
                }
                onError(OnErrorModel.ER_SERVER)
                isError = false
                break
            } finally {
                if (response == null && isError) {
                    Log.d("onError", "error Unknown")
                }
                response?.let { onCompletion?.invoke(it) }
            }
        }
        return response
    }

}
