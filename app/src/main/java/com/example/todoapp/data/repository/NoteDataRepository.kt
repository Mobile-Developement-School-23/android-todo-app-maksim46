package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.network.ToDoDtoModel
import com.example.todoapp.domain.model.LastResponce
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.presentation.utils.toDbModel
import com.example.todoapp.presentation.utils.toDtoModel
import com.example.todoapp.presentation.utils.toEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class NoteDataRepository @Inject constructor(
    private val localNoteDataRepositoryImpl: LocalNoteDataRepositoryImpl,
    private val remoteNoteDataRepository: RemoteNoteDataRepository
) {
    private val handler = CoroutineExceptionHandler { _, exception -> Log.d("CoroutineException", "Caught $exception") }

    private val _onErrorMessage = MutableStateFlow<String>("")
    val onErrorMessage: StateFlow<String> = _onErrorMessage.asStateFlow()

    /*    private val _lastResponce = MutableStateFlow<LastResponce>(LastResponce())
        val lastResponce: StateFlow<LastResponce> = _lastResponce.asStateFlow()*/

    private val _uiEvent = Channel<LastResponce>()
    val uiEvent = _uiEvent.receiveAsFlow()

    /*init {
        CoroutineScope(Dispatchers.IO).launch {
            _uiEvent.send(LastResponce(false, isOnline = false))
        }
    }*/
    suspend fun saveToDoNote(note: ToDoEntity, isOnline: Boolean) {
        val addedNoteId = localNoteDataRepositoryImpl.insertToDoNote(note)
        if (isOnline) {
            remoteNoteDataRepository.saveToDoNote(note.copy(id = addedNoteId.toString()).toDtoModel()) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("REMOTE_DB", "SAVED IN REMOTE DB with id  + ${noteResponse.id}")
                }
            }
        }
    }

    suspend fun updateToDoNote(note: ToDoEntity, isOnline: Boolean) {
        val addedNoteId = localNoteDataRepositoryImpl.insertToDoNote(note)
        if (isOnline) {
            remoteNoteDataRepository.updateToDoNote(note.copy(id = addedNoteId.toString()).toDtoModel()) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("REMOTE_DB", "UPDATED IN REMOTE DB with id  + ${noteResponse.id}")
                }
            }
        }
    }

    suspend fun deleteToDoNote(id: String, isOnline: Boolean) {
        if (isOnline) {
            Log.d("DELETE", "inter")
            localNoteDataRepositoryImpl.deleteMarked()
            localNoteDataRepositoryImpl.deleteToDoNote(id)

            remoteNoteDataRepository.deleteToDoNote((NoteData.ToDoItem(id = id)).toEntity().toDtoModel()) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("REMOTE_DB", "DELETED IN REMOTE DB with id  + ${noteResponse.id}")
                }
            }
        } else {
            Log.d("DELETE", "no inter")
            markAsDeleteToDoNote(id)
        }
    }

    suspend fun markAsDeleteToDoNote(id: String) {
        localNoteDataRepositoryImpl.markAsDeleteToDoNote(id, Calendar.getInstance().time.time)

    }


    suspend fun startSyncNotes(mergedList: List<ToDoEntity>, isOnline: Boolean): LastResponce {
        var remoteResponce = LastResponce()
        localNoteDataRepositoryImpl.insertListOfNotes(mergedList.map { it.toDbModel() })

        if (isOnline) {
            remoteNoteDataRepository.patchListOfToDoNote(mergedList.map { it.toDtoModel() }) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                remoteResponce = if (noteResponse != null) {

                    remoteResponce.copy(status = true, date = Calendar.getInstance().time, isOnline = true)
                    // Log.d("REMOTE_DB1", "LIST_SAVED IN REMOTE DB with id  + ${noteResponse.size}")
                } else {
                    remoteResponce.copy(status = false, date = Calendar.getInstance().time, isOnline = true)
                }
            }
        } else {
            remoteResponce = remoteResponce.copy(status = false, date = Calendar.getInstance().time, isOnline = false)

        }
        Log.d("RESPONSE", "LIST_SAVED IN REMOTE DB with id  + ${remoteResponce.status} ${remoteResponce.isOnline}")

        return remoteResponce
    }


    fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>> {
        return localNoteDataRepositoryImpl.getToDoNoteList(doneStatus)
    }

    suspend fun getToDoNote(id: String): ToDoEntity {
        return localNoteDataRepositoryImpl.getToDoNote(id)
    }

    fun getNumberOfDone(): Flow<Int> {
        return localNoteDataRepositoryImpl.getNumberOfDone()
    }

    suspend fun updateDoneStatus(note: NoteData.ToDoItem, isOnline: Boolean) {
        localNoteDataRepositoryImpl.updateDoneStatus(note.id.toInt())
        if (isOnline) {
            remoteNoteDataRepository.updateToDoNote(note.copy(updateDate = Date(), isDone = !note.isDone).toEntity().toDtoModel()) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("REMOTE_DB", "UPDATED IN REMOTE DB with id  + ${noteResponse.id}")
                }
            }
        }
    }

    fun syncNotes(isOnline: Boolean) {
        CoroutineScope(Dispatchers.IO + handler).launch {
            if (!isOnline) {
                _uiEvent.send(LastResponce(status = false, isOnline = false))
            } else {
                val mergedList = mutableListOf<ToDoEntity>()

                mergeDBs {
                    Log.d("SYNC", "MERGED DB LIST  ${it}")
                    Log.d("SYNC", "MERGED DB LIST SIZE  ${it.size}")
                    mergedList.addAll(it)
                }
                withContext(Dispatchers.IO) {
                    Thread.sleep(2000)
                }
                Log.d("SYNC", "MERGED LIST SIZE ${mergedList.size}")
                val responce = startSyncNotes(mergedList.toList(), isOnline)

                //  _lastResponce.value=responce

                _uiEvent.send(responce)

            }


        }
    }

    private suspend fun mergeDBs(onItemsReceived: (List<ToDoEntity>) -> Unit) {

        Log.d("SYNC", "MERGE DB")
        CoroutineScope(Dispatchers.IO + handler).launch {
            val deferred: Deferred<Flow<List<ToDoEntity>>> = async {
                val local = localNoteDataRepositoryImpl.getToDoNoteListForSynk(true)
                val remote = remoteNoteDataRepository.getListOfToDoNote { }

                combineToDoEntityAndDto(local, remote)
            }

            val res: Flow<List<ToDoEntity>> = deferred.await()
            res.collect() { items ->
                //  Log.d("DELETE", "$items")
                onItemsReceived(items)
            }
        }
    }


    private fun combineToDoEntityAndDto(localDbNotes: Flow<List<ToDoEntity>>, remoteDbNotes: Flow<List<ToDoDtoModel>?>): Flow<List<ToDoEntity>> {
        return localDbNotes.combine(remoteDbNotes) { listEntity, listDto ->
            val listDtoToEntity = listDto?.map { it.toEntity() } ?: emptyList()


            Log.d("DELETED1", listEntity.toString())

            Log.d("DELETED2", listDtoToEntity.toString())
            val combinedList = listEntity + listDtoToEntity
            combinedList.groupBy { it.id }
                .map { (_, entitiesWithSameId) ->
                    entitiesWithSameId.maxByOrNull { it.updateDate }!!
                }
                .filter { it.deadline != -1L }
        }
    }


    fun yaLogin(token: String, isOnline: Boolean): Flow<String> {

        return if (isOnline) {
            remoteNoteDataRepository.yaLogin(token) {
                _onErrorMessage.value = it
            }
        } else {
            flow { }
        }

    }

}