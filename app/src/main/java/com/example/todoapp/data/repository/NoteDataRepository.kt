package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.network.model.ToDoDtoModel
import com.example.todoapp.data.model.OnErrorModel
import com.example.todoapp.data.network.RevisionStorage
import com.example.todoapp.di.ApplicationScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Common repository for communication with data sources
 * It contains logic about retrieving and updating notes.
 */

@ApplicationScope
class NoteDataRepository @Inject constructor(
    private val localNoteDataRepository: LocalNoteDataRepository,
    private val remoteNoteDataRepository: RemoteNoteDataRepository,
    private val revisionStorage: RevisionStorage
) {

    private val _listOfNotesForNotify = MutableStateFlow<List<ToDoEntity>>(emptyList())
    val listOfNotesForNotify: StateFlow<List<ToDoEntity>> = _listOfNotesForNotify.asStateFlow()


    private val _onErrorMessage = MutableSharedFlow<OnErrorModel>(0, 16)
    val onErrorMessage: SharedFlow<OnErrorModel> = _onErrorMessage.asSharedFlow()

    private val _currentRevision = MutableStateFlow<Int>(0)
    val currentRevision: StateFlow<Int> = _currentRevision.asStateFlow()


    private val _syncStatusResponse = Channel<LastResponse>()
    val syncStatusResponse = _syncStatusResponse.receiveAsFlow()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _onErrorMessage.tryEmit(OnErrorModel.ER_INTERNAL)
        Log.d("CoroutineException", "Caught $exception")
    }

    private val repoCoroutineScope = CoroutineScope(Job() + Dispatchers.Default + handler)


     fun test() {
         println("REMIONDER")
         Log.d("REMIONDER", "test")
    }

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

    suspend fun updateDoneStatus(note: NoteData.ToDoItem, isOnline: Boolean) {
        val updatedNote = note.copy(updateDate = Date(), isDone = !note.isDone)
        updateToDoNote(updatedNote.toEntity(), isOnline)
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

    suspend fun getNotesForNotify(currentTime:Long, future24HoursTime:Long):Flow<List<ToDoEntity>>{
        return    localNoteDataRepository.getNotesForNotify(currentTime, future24HoursTime)

     /*           localNoteDataRepository.getNotesForNotify(currentTime, future24HoursTime).collect {list ->
                    Log.d("NOTIF_LIST2", list.toString())
                    _listOfNotesForNotify.update { list }*/
            }








/*    suspend fun getNotesForNotify(currentTime:Long, future24HoursTime:Long): Flow<List<ToDoEntity>>{


      return  localNoteDataRepository.getNotesForNotify(currentTime, future24HoursTime)



    }*/
    private suspend fun markAsDeleteToDoNote(id: String) {
      return  localNoteDataRepository.markAsDeleteToDoNote(id, Date().time)
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

    fun setErrorMessage(error: OnErrorModel) {
        _onErrorMessage.tryEmit(error)
    }

    suspend fun getToDoNote(id: String): ToDoEntity {
        return localNoteDataRepository.getToDoNote(id)
    }

    fun getNumberOfDone(): Flow<Int> {
        return localNoteDataRepository.getNumberOfDone()
    }


    private suspend fun checkRevision(): Boolean {
        var result = false
        repoCoroutineScope.async {
            var incomRevision = 0
            val revision = (remoteNoteDataRepository.getRevision() {}).map { it ?: 0 }
            revision.collect {
                incomRevision = it

            }


            var lastKnownRevision = revisionStorage.getRevision()
            Log.d("assssssLast", lastKnownRevision.toString())
            Log.d("assssssOnl", incomRevision.toString())
            if (lastKnownRevision != null) {
                if (lastKnownRevision.toInt() < incomRevision) {
                    result = true
                }

            }
        }.await()
        return result
    }


    fun syncNotes(isOnline: Boolean) {
        repoCoroutineScope.launch {
            if (!isOnline) {
                _syncStatusResponse.send(LastResponse(status = false, isOnline = false))
            } else {
              val check =  checkRevision()
                val mergedList = mergeDBs(check)
                val response = startSyncNotes(mergedList, isOnline)
                _syncStatusResponse.send(response)


            }
        }
    }


    private suspend fun mergeDBs(check:Boolean): List<ToDoEntity> {
        return repoCoroutineScope.async(Dispatchers.IO + handler) {
            val local = localNoteDataRepository.getToDoNoteListForSynk(true).first()
            val remote = remoteNoteDataRepository.getListOfToDoNote { }.firstOrNull()
            if (remote != null) {
            }
            if (check) {
                combineToDoEntityAndDtoIfUnsync(local, remote)
            } else {
                combineToDoEntityAndDto(local, remote)
            }
        }.await()
    }

    private fun combineToDoEntityAndDto(localDbNotes: List<ToDoEntity>, remoteDbNotes: List<ToDoDtoModel>?): List<ToDoEntity> {
        Log.d("assssssOnl", "SYNC")
        val listDtoToEntity = remoteDbNotes?.map { it.toEntity() } ?: emptyList()
        val combinedList = localDbNotes + listDtoToEntity
        return combinedList.groupBy { it.id }
            .map { (_, entitiesWithSameId) ->
                entitiesWithSameId.maxByOrNull { it.updateDate }!!
            }
            .filter { it.deadline != -1L }
    }


private fun combineToDoEntityAndDtoIfUnsync(localDbNotes: List<ToDoEntity>, remoteDbNotes: List<ToDoDtoModel>?): List<ToDoEntity> {
    val localMap = localDbNotes.associateBy { it.id }
    val result = mutableListOf<ToDoEntity>()

    remoteDbNotes?.forEach { remoteDto ->
        val remote = remoteDto.toEntity()
        val local = localMap[remote.id]

        result.add(
            if (local != null && local.updateDate > remote.updateDate) {
                local
            } else {
                remote
            }
        )
    }

    localMap.values.filter { it.id !in remoteDbNotes.orEmpty().map { remote -> remote.toEntity().id } }
        .forEach { item ->
            val updatedItem = item.copy(deadline = -1L)
            result.add(updatedItem)
        }

    return result
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