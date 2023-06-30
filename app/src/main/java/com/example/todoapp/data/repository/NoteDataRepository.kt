package com.example.todoapp.data.repository

import android.util.Log
import android.widget.Toast
import com.example.todoapp.data.network.ToDoDtoModel
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.presentation.ToDoAppApp
import com.example.todoapp.presentation.utils.toDbModel
import com.example.todoapp.presentation.utils.toDtoModel
import com.example.todoapp.presentation.utils.toEntity
import io.ktor.client.utils.EmptyContent.status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date
import javax.inject.Inject


class NoteDataRepository @Inject constructor(
    private val localNoteDataRepositoryImpl: LocalNoteDataRepositoryImpl,
    private val remoteNoteDataRepository: RemoteNoteDataRepository
) {

    private val _onErrorMessage = MutableStateFlow<String>("")
    val onErrorMessage: StateFlow<String> = _onErrorMessage.asStateFlow()


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
        localNoteDataRepositoryImpl.deleteToDoNote(id)
        if (isOnline) {
            remoteNoteDataRepository.deleteToDoNote((NoteData.ToDoItem(id = id)).toEntity().toDtoModel()) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                if (noteResponse != null) {
                    Log.d("REMOTE_DB", "DELETED IN REMOTE DB with id  + ${noteResponse.id}")
                }
            }
        }
    }

    suspend fun syncNotes(mergedList: List<ToDoEntity>, isOnline: Boolean) {
        localNoteDataRepositoryImpl.insertListOfNotes(mergedList.map { it.toDbModel() })
        if (isOnline) {

            remoteNoteDataRepository.patchListOfToDoNote(mergedList.map { it.toDtoModel() }) {
                _onErrorMessage.value = it
            }.collect { noteResponse ->
                if (noteResponse != null) {

                    Log.d("REMOTE_DB", "LIST_SAVED IN REMOTE DB with id  + ${noteResponse.size}")
                }
            }
        }

    }
fun checkWorker(){
    println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
    Log.d("AAAAAAAA","AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
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

    fun mergeNotes(): Flow<List<ToDoEntity>> {
        val local = localNoteDataRepositoryImpl.getToDoNoteList(true)
        val remote = remoteNoteDataRepository.getListOfToDoNote {}
        return combineToDoEntityAndDto(local, remote)
    }

    private fun combineToDoEntityAndDto(localDbNotes: Flow<List<ToDoEntity>>, remoteDbNotes: Flow<List<ToDoDtoModel>?>): Flow<List<ToDoEntity>> {
        return localDbNotes.combine(remoteDbNotes) { listEntity, listDto ->
            val listDtoToEntity = listDto?.map { it.toEntity() } ?: emptyList()
            val combinedList = listEntity + listDtoToEntity
            combinedList.groupBy { it.id }
                .map { (_, entitiesWithSameId) ->
                    entitiesWithSameId.maxByOrNull { it.updateDate }!!
                }
        }
    }

    fun yaLogin(token: String, isOnline: Boolean): Flow<String> {

        return if (isOnline) {
            remoteNoteDataRepository.yaLogin(token) {
                _onErrorMessage.value = it
            }
        } else{
            flow { }
        }

    }

}