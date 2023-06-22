package com.example.todoapp.domain.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.ToDoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NoteDataRepository {

    suspend fun addToDoNote(note: ToDoEntity):Long

    suspend fun deleteToDoNote(id: String)

    suspend fun insertToDoNote(note: ToDoEntity)

    suspend fun updateToDoNote(note: ToDoEntity)

    suspend fun getToDoNote(id: String): ToDoEntity

    fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>>

    fun getNumberOfDone(): Flow<Int>

    suspend fun updateDoneStatus(noteId: Int)

}
