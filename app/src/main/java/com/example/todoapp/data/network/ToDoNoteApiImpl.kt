package com.example.todoapp.data.network

import android.util.Log
import com.example.todoapp.data.model.onErrorModel
import com.example.todoapp.data.network.model.ToDoDtoModel
import com.example.todoapp.data.network.model.ToDoListResponse
import com.example.todoapp.data.network.model.ToDoResponse
import com.example.todoapp.data.network.model.YaLoginDtoModel
import com.example.todoapp.domain.toPayload
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import javax.inject.Inject
/**
 * Description  of API network requests
 * contains logic for network error handling and retries
 */
const val INIT_REVISION = "-1"

class ToDoNoteApiImpl @Inject constructor(
    private val httpClient: NetworkClient
) : ToDoNoteApi {

    private var dbRevizion = INIT_REVISION
    suspend fun <T> executeRequest(
        requestCall: suspend () -> T, onCompletion: ((T) -> Unit)? = null,
        onError: (message: onErrorModel) -> Unit, retryIfError: Set<HttpStatusCode> = setOf()
    ): T? {
        var response: T? = null
        var isError = true
        var retries = 0
        while (retries < 2) {
            try {
                response = requestCall()
                isError = false
                break
            } catch (ce: ClientRequestException) {
                if (ce.response.status in retryIfError && retries == 0) {
                    retries++
                    getDbRevision {}
                    delay(3000)
                    continue
                }
                when (ce.response.status) {
                    HttpStatusCode.BadRequest -> {
                        onError(onErrorModel.ER_400)
                        isError = false
                    }

                    HttpStatusCode.Unauthorized -> {
                        onError(onErrorModel.ER_401)
                        isError = false
                    }

                    HttpStatusCode.NotFound -> {
                        onError(onErrorModel.ER_404)
                        isError = false
                    }

                    else -> {
                        onError(onErrorModel.ER_UNKNOWN)
                        isError = false
                    }
                }
                break
            } catch (se: ServerResponseException) {
                if (retries == 0) {
                    retries++
                    getDbRevision {}
                    delay(1000)
                    continue
                }
                onError(onErrorModel.ER_SERVER)
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

    override suspend fun getListOfToDoNote(onError: (message: onErrorModel) -> Unit): ToDoListResponse? {
        return executeRequest(
            requestCall = {
                httpClient.client.get("${HttpResource.BASE_URL}/list") {
                    header("Authorization", "Bearer ${HttpResource.authToken}")
                }
            },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                }
            }, onError = onError
        )
    }

    override suspend fun getDbRevision(onError: (message: onErrorModel) -> Unit): Int? {
        val response = executeRequest<ToDoListResponse>(
            requestCall = {
                httpClient.client.get("${HttpResource.BASE_URL}/list") {
                    header("Authorization", "Bearer ${HttpResource.authToken}")
                }
            },
            onCompletion = { response ->
                dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
            }, onError = onError
        )
        return response?.revision ?: -1
    }

    override suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): ToDoDtoModel? {
        return executeRequest(
            requestCall = { httpClient.postRequest<ToDoResponse>("/list/", toDoModel.toPayload(), dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.element
    }

    override suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): ToDoDtoModel? {
        return executeRequest(
            requestCall = { httpClient.putRequest<ToDoResponse>("/list/${toDoModel.id}", toDoModel.toPayload(), dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.element
    }

    override suspend fun deleteToDoNote(toDoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): ToDoDtoModel? {
        return executeRequest(
            requestCall = { httpClient.deleteRequest<ToDoResponse>("/list/${toDoModel.id}", null, dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.element
    }

    override suspend fun patchListOfToDoNote(listOfRequests: ToDoListResponse, onError: (message: onErrorModel) -> Unit): List<ToDoDtoModel>? {
        return executeRequest(
            requestCall = { httpClient.patchRequest<ToDoListResponse>("/list/", listOfRequests, dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.list
    }

    override suspend fun yaLogin(token: String, onError: (message: onErrorModel) -> Unit): String {
        return executeRequest<YaLoginDtoModel>(
            requestCall = {
                httpClient.client.get(HttpResource.YANDEX_LOGIN_URL) {
                    header("Authorization", "OAuth $token")
                }
            }, onError = onError
        )?.realName ?: ""
    }


}
