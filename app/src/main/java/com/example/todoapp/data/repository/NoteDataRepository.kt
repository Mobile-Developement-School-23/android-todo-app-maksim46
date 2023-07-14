package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.network.ToDoDtoModel
import com.example.todoapp.domain.model.LastResponse
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class NoteDataRepository @Inject constructor(
    private val localNoteDataRepositoryImpl: LocalNoteDataRepositoryImpl,
    private val remoteNoteDataRepository: RemoteNoteDataRepository
) {
    private val handler = CoroutineExceptionHandler { _, exception ->
        _onErrorMessage.tryEmit("Непредвиденная ошибка")
        Log.d("CoroutineException", "Caught $exception")
    }

    private val _onErrorMessage = MutableSharedFlow<String>(0, 16)
    val onErrorMessage: SharedFlow<String> = _onErrorMessage.asSharedFlow()

    private val _syncStatusResponce = Channel<LastResponse>()
    val syncStatusResponce = _syncStatusResponce.receiveAsFlow()


    suspend fun saveToDoNote(note: ToDoEntity, isOnline: Boolean) {
        val addedNoteId = localNoteDataRepositoryImpl.insertToDoNote(note)
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
        val addedNoteId = localNoteDataRepositoryImpl.insertToDoNote(note)
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
            localNoteDataRepositoryImpl.deleteMarked()
            localNoteDataRepositoryImpl.deleteToDoNote(id)
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
        localNoteDataRepositoryImpl.markAsDeleteToDoNote(id, Calendar.getInstance().time.time)
    }

    private suspend fun startSyncNotes(mergedList: List<ToDoEntity>, isOnline: Boolean): LastResponse {
        var remoteResponse = LastResponse()
        localNoteDataRepositoryImpl.insertListOfNotes(mergedList.map { it.toDbModel() })
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
                _onErrorMessage.tryEmit(it)
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("DELETE", noteResponse.toString())
                }
            }
        }
    }
    fun syncNotes(isOnline: Boolean, vmscope: CoroutineScope) {
        vmscope.launch(Dispatchers.IO + handler) {
            if (!isOnline) {
                _syncStatusResponce.send(LastResponse(status = false, isOnline = false))
            } else {
                val mergedList = mergeDBs(vmscope)
                val response = startSyncNotes(mergedList, isOnline)
                _syncStatusResponce.send(response)
            }
        }
    }

    private suspend fun mergeDBs(vmscope: CoroutineScope): List<ToDoEntity> {
        return vmscope.async(Dispatchers.IO + handler) {
            val local = localNoteDataRepositoryImpl.getToDoNoteListForSynk(true).first()
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
}