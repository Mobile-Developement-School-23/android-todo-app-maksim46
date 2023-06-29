package com.example.todoapp.data.network

import android.util.Log
import com.example.todoapp.presentation.utils.toPayload
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject

class ToDoNoteApiImpl @Inject constructor() : ToDoNoteApi {
    private val httpClient: NetworkClient by lazy {
        NetworkClient()
    }

    /*    private val _remoteDbRevision = MutableStateFlow<Int>(0)
    val remoteDbRevision: StateFlow<Int> = _remoteDbRevision.asStateFlow()*/

    private var dbRevizion = "-1"
    private val handler = CoroutineExceptionHandler { _, exception -> println("Caught $exception") }

    /*    init {
            CoroutineScope(Dispatchers.IO + handler).launch {
                dbRevizion = dbRevizion.toInt().coerceAtLeast(getListOfToDoNote {}!!.revision).toString()

            //  dbRevizion = dbRevizion.toInt().coerceAtLeast(getDbRevision{}).toString()
                Log.d("REMOTE_DB", "API IMPL get last known revision  + ${dbRevizion}")
            }
        }*/


    override suspend fun getListOfToDoNote(onError: (message: String) -> Unit): ToDoListResponse? {
        var response: ToDoListResponse? = null
        var isError = true
        try {
            response = httpClient.client.get("${HttpResource.BASE_URL}/list") {
                header("Authorization", "Bearer ${HttpResource.authToken}")
            }
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
            response?.let {
                dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                Log.d("REMOTE_DB", "API IMPL returned list of Notes + ${response.list}")
            }
        }
        return response
    }


    override suspend fun getDbRevision(onError: (message: String) -> Unit): Int? {
        var response: ToDoListResponse? = null
        var isError = true
        try {
            response = httpClient.client.get("${HttpResource.BASE_URL}/list") {
                header("Authorization", "Bearer ${HttpResource.authToken}")
            }
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
        }
        return response?.revision ?: -1
    }


    override suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
        var response: ToDoResponse? = null
        var isError = true
        try {
            response = httpClient.postRequest<ToDoResponse>("/list/", toDoModel.toPayload(), dbRevizion)
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
            if (response != null) {
                Log.d("REMOTE_DB", "API IMPL saveToDoNote save new element  + ${response.element.text}")
                dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                Log.d("REMOTE_DB", "API IMPL saveToDoNote new revizion  + ${response.revision}")
            }
        }
        return response?.let { response.element }
    }


    override suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
        var response: ToDoResponse? = null
        var isError = true
        try {
            response = httpClient.putRequest<ToDoResponse>("/list/${toDoModel.id}", toDoModel.toPayload(), dbRevizion)
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
            if (response != null) {
                Log.d("REMOTE_DB", "API IMPL updateToDoNote updated new element  + ${response.element.text}")
                dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                Log.d("REMOTE_DB", "API IMPL updateToDoNote new revizion  + ${response.revision}")
            }
        }
        return response?.let { response.element }
    }


    override suspend fun deleteToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {

        var response: ToDoResponse? = null
        var isError = true
        try {
            response = httpClient.deleteRequest<ToDoResponse>("/list/${toDoModel.id}", null, dbRevizion)
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
            if (response != null) {
                Log.d("REMOTE_DB", "API IMPL deleteToDoNote deleted new element  + ${response.element.text}")
                dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                Log.d("REMOTE_DB", "API IMPL updateToDoNote new revizion  + ${response.revision}")
            }
        }
        return response?.let { response.element }
    }

    override suspend fun patchListOfToDoNote(listOfRequests: ToDoListResponse, onError: (message: String) -> Unit): List<ToDoDtoModel>? {
        Log.d("MERGE", "remote patchListOfToDoNote API  list size ${listOfRequests.list.size}")
        var response: ToDoListResponse? = null
        var isError = true
        try {
            response = httpClient.patchRequest<ToDoListResponse>("/list/", listOfRequests, dbRevizion)
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
            if (response != null) {
                Log.d("REMOTE_DB", "API IMPL patchToDoNote patched list count + ${response.list.count()}")
                dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                Log.d("REMOTE_DB", "API IMPL patchToDoNote new revizion  + ${response.revision}")
            }
        }
        return response?.list
    }


    override suspend fun yaLogin(token:String, onError: (message: String) -> Unit): String {
        Log.d("yaLogin", token)
        var response: YaLoginDtoModel? = null
        var isError = true
        try {
            response = httpClient.client.get(HttpResource.YANDEX_LOGIN_URL) {
                header("Authorization", "OAuth $token")
            }
            isError = false
        } catch (ae: AuthenticationException) {
            onError(ae.response.status.toString())
            isError = false
        } catch (se: ServerResponseException) {
            onError(se.response.status.toString())
            isError = false
        } catch (ce: ClientRequestException) {
            onError(ce.response.status.toString())
            isError = false
        } catch (ee: ElementNotFoundException) {
            onError(ee.response.status.toString())
            isError = false
        } finally {
            if (response == null && isError) {
                onError("error Unknown")
            }
            if (response != null) {
                Log.d("REMOTE_DB", "API IMPL patchToDoNote patched list count + ${response}")
                Log.d("REMOTE_DB", "API IMPL patchToDoNote new revizion  + ${response}")
            }
        }

            return response?.realName?:""
        }
    }
