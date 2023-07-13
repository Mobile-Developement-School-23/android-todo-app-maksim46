package com.example.todoapp.domain.repository

import com.example.todoapp.data.database.ToDoListDbModel
import com.example.todoapp.domain.model.ToDoEntity
import kotlinx.coroutines.flow.Flow

interface LocalNoteDataRepository {

    suspend fun insertToDoNote(note: ToDoEntity): Long
    suspend fun insertListOfNotes(list: List<ToDoListDbModel>): List<Long>
    suspend fun getNotesForNotify(currentTime: Long, future24HoursTime: Long): Flow<List<ToDoEntity>>
    suspend fun deleteToDoNote(id: String)
    suspend fun deleteMarked()
    suspend fun markAsDeleteToDoNote(id: String, updateDate: Long)
    suspend fun updateToDoNote(note: ToDoEntity)
    suspend fun getToDoNote(id: String): ToDoEntity
    fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>>
    fun getToDoNoteListForSynk(doneStatus: Boolean): Flow<List<ToDoEntity>>
    fun getNumberOfDone(): Flow<Int>
    suspend fun updateDoneStatus(noteId: Int)

}
