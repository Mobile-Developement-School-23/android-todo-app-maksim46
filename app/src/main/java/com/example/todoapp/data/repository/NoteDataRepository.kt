package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.network.ToDoDtoModel
import com.example.todoapp.data.model.onErrorModel
import com.example.todoapp.domain.model.LastResponse
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.domain.toDbModel
import com.example.todoapp.domain.toDtoModel
import com.example.todoapp.domain.toEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class NoteDataRepository @Inject constructor(
    private val localNoteDataRepository: LocalNoteDataRepository,
    private val remoteNoteDataRepository: RemoteNoteDataRepository
) {


    private val _onErrorMessage = MutableSharedFlow<onErrorModel>(0, 16)
    val onErrorMessage: SharedFlow<onErrorModel> = _onErrorMessage.asSharedFlow()

    private val _syncStatusResponse = Channel<LastResponse>()
    val syncStatusResponse = _syncStatusResponse.receiveAsFlow()


    private val handler = CoroutineExceptionHandler { _, exception ->
        _onErrorMessage.tryEmit(onErrorModel.ER_INTERNAL)
        Log.d("CoroutineException", "Caught $exception")
    }


    private val repoCoroutineScope = CoroutineScope(Job() + Dispatchers.Default+handler)
    suspend fun saveToDoNote(note: ToDoEntity, isOnline: Boolean) {
        val addedNoteId = localNoteDataRepository.insertToDoNote(note)
        if (isOnline) {
            remoteNoteDataRepository.saveToDoNote(note.copy(id = addedNoteId.toString()).toDtoModel()) {
                _onErrorMessage.tryEmit(it)
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("SAVE", noteResponse.toString())
                }
            }
        }
    }

    suspend fun updateToDoNote(note: ToDoEntity, isOnline: Boolean) {
        val addedNoteId = localNoteDataRepository.insertToDoNote(note)
        if (isOnline) {
            remoteNoteDataRepository.updateToDoNote(note.copy(id = addedNoteId.toString()).toDtoModel()) {
                _onErrorMessage.tryEmit(it)
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("UPDATE", noteResponse.toString())
                }
            }
        }
    }

    suspend fun deleteToDoNote(id: String, isOnline: Boolean) {
        if (isOnline) {
            localNoteDataRepository.deleteMarked()
            localNoteDataRepository.deleteToDoNote(id)
            remoteNoteDataRepository.deleteToDoNote((NoteData.ToDoItem(id = id)).toEntity().toDtoModel()) {
                _onErrorMessage.tryEmit(it)
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("DELETE", noteResponse.toString())
                }
            }
        } else {
            markAsDeleteToDoNote(id)
        }
    }

    private suspend fun markAsDeleteToDoNote(id: String) {
        localNoteDataRepository.markAsDeleteToDoNote(id, Calendar.getInstance().time.time)
    }

    private suspend fun startSyncNotes(mergedList: List<ToDoEntity>, isOnline: Boolean): LastResponse {
        var remoteResponse = LastResponse()
        localNoteDataRepository.insertListOfNotes(mergedList.map { it.toDbModel() })
        if (isOnline) {
            remoteNoteDataRepository.patchListOfToDoNote(mergedList.map { it.toDtoModel() }) {
                _onErrorMessage.tryEmit(it)
            }.collect { noteResponse ->
                remoteResponse = if (noteResponse != null) {
                    remoteResponse.copy(status = true, date = Calendar.getInstance().time, isOnline = true)
                } else {
                    remoteResponse.copy(status = false, date = Calendar.getInstance().time, isOnline = true)
                }
            }
        } else {
            remoteResponse = remoteResponse.copy(status = false, date = Calendar.getInstance().time, isOnline = false)
        }
        return remoteResponse
    }

    fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>> {
        return localNoteDataRepository.getToDoNoteList(doneStatus)
    }
    fun setErrorMessage(error: onErrorModel) {
       _onErrorMessage.tryEmit(error)
    }
    suspend fun getToDoNote(id: String): ToDoEntity {
        return localNoteDataRepository.getToDoNote(id)
    }

    fun getNumberOfDone(): Flow<Int> {
        return localNoteDataRepository.getNumberOfDone()
    }

    suspend fun updateDoneStatus(note: NoteData.ToDoItem, isOnline: Boolean) {
        localNoteDataRepository.updateDoneStatus(note.id.toInt())
        if (isOnline) {
            remoteNoteDataRepository.updateToDoNote(note.copy(updateDate = Date(), isDone = !note.isDone).toEntity().toDtoModel()) {
                _onErrorMessage.tryEmit(it)
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("DELETE", noteResponse.toString())
                }
            }
        }
    }

    fun syncNotes(isOnline: Boolean) {
        repoCoroutineScope.launch {
            if (!isOnline) {
                _syncStatusResponse.send(LastResponse(status = false, isOnline = false))
            } else {
                val mergedList = mergeDBs()
                val response = startSyncNotes(mergedList, isOnline)
                _syncStatusResponse.send(response)
            }
        }
    }
/*    fun syncNotes(isOnline: Boolean) {
        vmscope.launch(Dispatchers.IO + handler) {
            if (!isOnline) {
                _syncStatusResponse.send(LastResponse(status = false, isOnline = false))
            } else {
                val mergedList = mergeDBs(vmscope)
                val response = startSyncNotes(mergedList, isOnline)
                _syncStatusResponse.send(response)
            }
        }
    }*/

    private suspend fun mergeDBs(): List<ToDoEntity> {
        return repoCoroutineScope.async(Dispatchers.IO + handler) {
            val local = localNoteDataRepository.getToDoNoteListForSynk(true).first()
            Log.d("AAALoc", local.size.toString())
            val remote = remoteNoteDataRepository.getListOfToDoNote { }.firstOrNull()
            if (remote != null) {
                Log.d("AAARem", remote.size.toString())
            }
            combineToDoEntityAndDto(local, remote)
        }.await()
    }

    private fun combineToDoEntityAndDto(localDbNotes: List<ToDoEntity>, remoteDbNotes: List<ToDoDtoModel>?): List<ToDoEntity> {
        val listDtoToEntity = remoteDbNotes?.map { it.toEntity() } ?: emptyList()
        val combinedList = localDbNotes + listDtoToEntity
        return combinedList.groupBy { it.id }
            .map { (_, entitiesWithSameId) ->
                entitiesWithSameId.maxByOrNull { it.updateDate }!!
            }
            .filter { it.deadline != -1L }
    }



    fun yaLogin(token: String, isOnline: Boolean): Flow<String> {
        return if (isOnline) {
            remoteNoteDataRepository.yaLogin(token) {
                _onErrorMessage.tryEmit(it)
            }
        } else {
            flow { }
        }

    }
    fun cancelCoroutine() {
        repoCoroutineScope.cancel()
    }
}