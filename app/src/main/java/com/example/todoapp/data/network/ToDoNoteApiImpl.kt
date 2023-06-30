package com.example.todoapp.data.network

import android.util.Log
import com.example.todoapp.presentation.utils.toPayload
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import java.util.Calendar
import javax.inject.Inject

class ToDoNoteApiImpl @Inject constructor() : ToDoNoteApi {
    private val httpClient: NetworkClient by lazy {
        NetworkClient()
    }

    /*    private val _remoteDbRevision = MutableStateFlow<Int>(0)
    val remoteDbRevision: StateFlow<Int> = _remoteDbRevision.asStateFlow()*/

    private var dbRevizion = "-1"
    private val handler = CoroutineExceptionHandler { _, exception -> println("Caught $exception") }


    private suspend fun <T> executeRequest(
        requestCall: suspend () -> T,
        onCompletion: ((T) -> Unit)? = null,
        onError: (message: String) -> Unit,
        retryIfError: Set<HttpStatusCode> = setOf()
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
                        onError("Ошибка 400: Неверный запрос")
                        Log.d("RESPONSE", "Ошибка 400: Неверный запрос")
                        isError = false
                    }

                    HttpStatusCode.Unauthorized -> {
                        onError("Ошибка 401: Ошибка авторизации")
                        Log.d("RESPONSE", "Ошибка 401: Ошибка авторизации")
                        isError = false
                    }

                    HttpStatusCode.NotFound -> {
                        onError("Ошибка 404: Элемент не найден")
                        Log.d("RESPONSE", "Ошибка 404: Элемент не найден")
                        isError = false
                    }

                    else -> {
                        onError("Неожиданный код состояния HTTP")
                        Log.d("RESPONSE", "Неожиданный код состояния HTTP")
                        isError = false
                    }
                }
               // isError = false
                break
            } catch (se: ServerResponseException) {
                if (retries == 0) {
                    retries++
                    getDbRevision {}
                    delay(1000)
                    continue
                }
                onError(se.response.status.toString())
                isError = false
                break
            } finally {
                if (response == null && isError) {
                    Log.d("onError","error Unknown" )
                  //  onError("error Unknown")
                }
                response?.let { onCompletion?.invoke(it) }
            }
        }
        return response
    }


    override suspend fun getListOfToDoNote(onError: (message: String) -> Unit): ToDoListResponse? {
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
            },
            onError = onError
        )
    }

        override suspend fun getDbRevision(onError: (message: String) -> Unit): Int? {
            val response = executeRequest<ToDoListResponse>(
                requestCall = {
                    httpClient.client.get("${HttpResource.BASE_URL}/list") {
                        header("Authorization", "Bearer ${HttpResource.authToken}")
                    }
                },
                onCompletion = { response ->
                    dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                },
                onError = onError
            )
            return response?.revision ?: -1
        }

        override suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
            return executeRequest(
                requestCall = { httpClient.postRequest<ToDoResponse>("/list/", toDoModel.toPayload(), dbRevizion) },
                onCompletion = { response ->
                    if (response != null) {
                        dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    }
                },
                onError = onError,
                retryIfError = setOf(HttpStatusCode.BadRequest)
            )?.element
        }

        override suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
            return executeRequest(
                requestCall = { httpClient.putRequest<ToDoResponse>("/list/${toDoModel.id}", toDoModel.toPayload(), dbRevizion) },
                onCompletion = { response ->
                    if (response != null) {
                        dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    }
                },
                onError = onError,
                retryIfError = setOf(HttpStatusCode.BadRequest)
            )?.element
        }

        override suspend fun deleteToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
            return executeRequest(
                requestCall = { httpClient.deleteRequest<ToDoResponse>("/list/${toDoModel.id}", null, dbRevizion) },
                onCompletion = { response ->
                    if (response != null) {
                        dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    }
                },
                onError = onError,
                retryIfError = setOf(HttpStatusCode.BadRequest)
            )?.element
        }

        override suspend fun patchListOfToDoNote(listOfRequests: ToDoListResponse, onError: (message: String) -> Unit): List<ToDoDtoModel>? {
            return executeRequest(
                requestCall = { httpClient.patchRequest<ToDoListResponse>("/list/", listOfRequests, dbRevizion) },
                onCompletion = { response ->
                    if (response != null) {
                        dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
                    }
                },
                onError = onError,
                retryIfError = setOf(HttpStatusCode.BadRequest)
            )?.list
        }

        override suspend fun yaLogin(token: String, onError: (message: String) -> Unit): String {
            return executeRequest<YaLoginDtoModel>(
                requestCall = {
                    httpClient.client.get(HttpResource.YANDEX_LOGIN_URL) {
                        header("Authorization", "OAuth $token")
                    }
                },
                onError = onError
            )?.realName ?: ""
        }
    }


/*        init {
        CoroutineScope(Dispatchers.IO + handler).launch {
            dbRevizion = dbRevizion.toInt().coerceAtLeast(getListOfToDoNote {}!!.revision).toString()

        //  dbRevizion = dbRevizion.toInt().coerceAtLeast(getDbRevision{}).toString()
            Log.d("REMOTE_DB", "API IMPL get last known revision  + ${dbRevizion}")
        }
    }*/


/*    override suspend fun getListOfToDoNote(onError: (message: String) -> Unit): ToDoListResponse? {
    Log.d("SYNC", "IN API  ")
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
        Log.d("CATCHER", response.toString() )
        if (response == null && isError) {
            onError("error Unknown")
        }
        response?.let {
            dbRevizion = dbRevizion.toInt().coerceAtLeast(response.revision).toString()
            Log.d("SYNC", "API IMPL returned list of Notes + ${response.list}")
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
        Log.d("CATCHER", response.toString() )
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
        Log.d("CATCHER", response.toString() )
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
        Log.d("CATCHER", " ae ${ae.response.status}")
        onError(ae.response.status.toString())
        isError = false
    } catch (se: ServerResponseException) {
        Log.d("CATCHER", " se ${se.response.status}")
        onError(se.response.status.toString())
        isError = false
    } catch (ce: ClientRequestException) {
        Log.d("CATCHER", " 1ce ${ce.response.status}")
        Log.d("CATCHER", " 2ce ${ce.response.status.toString()}")
            Log.d("CATCHER", " 3ce ${ce.response.status.value.toString()}")
                Log.d("CATCHER", " 4ce ${ce.response.status.description.toString()}")
                    Log.d("CATCHER", "5 ce ${ce.response.toString()}")
                        Log.d("CATCHER", "6 ce ${ce.response.content.toString()}")
                            Log.d("CATCHER", "7 ce ${ce.response.content.closedCause.toString()}")
        Log.d("CATCHER", "9 ce ${ce.response.readText()}")
        isError = false
    } catch (ee: ElementNotFoundException) {
        Log.d("CATCHER", " ee ${ee.response.status}")
        onError(ee.response.status.toString())
        isError = false
    } finally {
        Log.d("CATCHER", response.toString() )
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
    Log.d("SYNC", "remote patchListOfToDoNote API  list size ${listOfRequests.list.size}")
    Log.d("SYNC", "patch revision ${dbRevizion}")
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
        Log.d("CATCHER", response.toString() )
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
        Log.d("CATCHER", response.toString() )
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
}*/


/*
suspend fun <T> executeRequest(
requestCall: suspend () -> T,
onError: (message: String) -> Unit
): T? {
var response: T? = null
var isError = true
try {
    response = requestCall()
    isError = false
} catch (se: ServerResponseException) {
    onError(se.response.status.toString())
    isError = false
} catch (ce: ClientRequestException) {
    onError(ce.response.status.toString())
    isError = false
} finally {
    if (response == null && isError) {
        onError("error Unknown")
    }
}
return response
}
override suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
return executeRequest(
    requestCall = { httpClient.postRequest<ToDoResponse>("/list/", toDoModel.toPayload(), dbRevizion) },
    onError = onError
)?.element
}

override suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel? {
return executeRequest(
    requestCall = { httpClient.putRequest<ToDoResponse>("/list/${toDoModel.id}", toDoModel.toPayload(), dbRevizion) },
    onError = onError
)?.element
}
}*/
