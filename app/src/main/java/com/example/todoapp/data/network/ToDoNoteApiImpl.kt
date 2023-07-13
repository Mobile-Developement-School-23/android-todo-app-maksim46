package com.example.todoapp.data.network

import com.example.todoapp.data.model.OnErrorModel
import com.example.todoapp.data.network.model.ToDoDtoModel
import com.example.todoapp.data.network.model.ToDoListResponse
import com.example.todoapp.data.network.model.ToDoResponse
import com.example.todoapp.data.network.model.YaLoginDtoModel
import com.example.todoapp.data.repository.LocalNoteDataRepository
import com.example.todoapp.data.repository.NoteDataRepository
import com.example.todoapp.domain.toPayload
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

/**
 * Description  of API network requests
 * contains logic for network error handling and retries
 */
const val INIT_REVISION = "-1"

class ToDoNoteApiImpl @Inject constructor(
    private val httpClient: NetworkClient,
    private val httpClientHandler: HttpClientHandler,
    private val revisionStorage: RevisionStorage
) : ToDoNoteApi {



    private var dbRevizion = INIT_REVISION

    init {
        dbRevizion =(revisionStorage.getRevision())?: INIT_REVISION

        httpClientHandler.dbRevisionGetter = { getDbRevision {} }

    }

    override suspend fun getListOfToDoNote(onError: (message: OnErrorModel) -> Unit): ToDoListResponse? {
        return httpClientHandler.executeRequest(
            requestCall = {
                httpClient.client.get("${HttpResource.BASE_URL}/list") {
                    header("Authorization", "Bearer ${HttpResource.authToken}")
                }
            },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    revisionStorage.setRevision(dbRevizion)
                }
            }, onError = onError
        )
    }

    override suspend fun getDbRevision(onError: (message: OnErrorModel) -> Unit): Int {
        val response = httpClientHandler.executeRequest<ToDoListResponse>(
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

    override suspend fun saveToDoNote(
        toDoModel: ToDoDtoModel, onError: (message: OnErrorModel) -> Unit
    ): ToDoDtoModel? {
        return httpClientHandler.executeRequest(
            requestCall = { httpClient.postRequest<ToDoResponse>("/list/", toDoModel.toPayload(), dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    revisionStorage.setRevision(dbRevizion)
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.element
    }

    override suspend fun updateToDoNote(
        toDoModel: ToDoDtoModel, onError: (message: OnErrorModel) -> Unit
    ): ToDoDtoModel? {
        return httpClientHandler.executeRequest(
            requestCall = {
                httpClient.putRequest<ToDoResponse>("/list/${toDoModel.id}", toDoModel.toPayload(), dbRevizion)
            },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    revisionStorage.setRevision(dbRevizion)
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.element
    }

    override suspend fun deleteToDoNote(
        toDoModel: ToDoDtoModel, onError: (message: OnErrorModel) -> Unit
    ): ToDoDtoModel? {
        return httpClientHandler.executeRequest(
            requestCall = { httpClient.deleteRequest<ToDoResponse>("/list/${toDoModel.id}", null, dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    revisionStorage.setRevision(dbRevizion)
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.element
    }

    override suspend fun patchListOfToDoNote(
        listOfRequests: ToDoListResponse, onError: (message: OnErrorModel) -> Unit
    ): List<ToDoDtoModel>? {
        return httpClientHandler.executeRequest(
            requestCall = { httpClient.patchRequest<ToDoListResponse>("/list/", listOfRequests, dbRevizion) },
            onCompletion = { response ->
                if (response != null) {
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    revisionStorage.setRevision(dbRevizion)
                }
            }, onError = onError,
            retryIfError = setOf(HttpStatusCode.BadRequest)
        )?.list
    }

    override suspend fun yaLogin(token: String, onError: (message: OnErrorModel) -> Unit): String {
        return httpClientHandler.executeRequest<YaLoginDtoModel>(
            requestCall = {
                httpClient.client.get(HttpResource.YANDEX_LOGIN_URL) {
                    header("Authorization", "OAuth $token")
                }
            }, onError = onError
        )?.realName ?: ""
    }


}
