package com.example.todoapp.domain.repository

import com.example.todoapp.data.database.ToDoListDbModel
import com.example.todoapp.domain.model.ToDoEntity
import kotlinx.coroutines.flow.Flow

interface LocalNoteDataRepository {

    suspend fun insertToDoNote(note: ToDoEntity):Long

    suspend fun insertListOfNotes(list: List<ToDoListDbModel>)
    suspend fun deleteToDoNote(id: String)


    suspend fun updateToDoNote(note: ToDoEntity)

    suspend fun getToDoNote(id: String): ToDoEntity

    fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>>

    fun getNumberOfDone(): Flow<Int>

    suspend fun updateDoneStatus(noteId: Int)

}
