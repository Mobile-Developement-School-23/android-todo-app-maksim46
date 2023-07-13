package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.database.ToDoListDao
import com.example.todoapp.data.database.ToDoListDbModel
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.domain.repository.LocalNoteDataRepository
import com.example.todoapp.domain.toDbModel
import com.example.todoapp.domain.toEntity
import com.example.todoapp.domain.toListOfToDoEntyty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Data source contains logic for loading data from local database
 */
class LocalNoteDataRepository @Inject constructor(
    private val myToDoListDao: ToDoListDao
) :
    LocalNoteDataRepository {

    override suspend fun insertToDoNote(note: ToDoEntity): Long {
        return myToDoListDao.insertNote(note.toDbModel())
    }

    override suspend fun getNotesForNotify(currentTime:Long, future24HoursTime:Long): Flow<List<ToDoEntity>>  {
       val a =(myToDoListDao.getNotesForNotify(currentTime, future24HoursTime)).map { it.toListOfToDoEntyty() }
        Log.d("NOTIF_LIST1", a.toString())

        return a
    }
    override suspend fun insertListOfNotes(list: List<ToDoListDbModel>): List<Long> {
        return myToDoListDao.insertListOfNotes(list)
    }

    override suspend fun deleteToDoNote(id: String) {
        myToDoListDao.deleteToDoNote(id.toIntOrNull())
    }

    override suspend fun deleteMarked() {
        myToDoListDao.deleteMarked()
    }

    override suspend fun markAsDeleteToDoNote(id: String, updateDate: Long) {
        myToDoListDao.markAsDeleteToDoNote(id.toIntOrNull(), updateDate)
    }

    override suspend fun updateToDoNote(note: ToDoEntity) {
        myToDoListDao.updateNote(note.toDbModel())
    }

    override suspend fun getToDoNote(id: String): ToDoEntity {
        val tmpId = id.toIntOrNull()
        return myToDoListDao.getOneToDoNote(tmpId).toEntity()
    }

    override fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>> {
        val toDoNoteList = myToDoListDao.getAllToDoList(doneStatus)
        return toDoNoteList.map { it.toListOfToDoEntyty() }
    }

    override fun getToDoNoteListForSynk(doneStatus: Boolean): Flow<List<ToDoEntity>> {
        val toDoNoteList = myToDoListDao.getAllToDoListForSynk(doneStatus)
        return toDoNoteList.map { it.toListOfToDoEntyty() }
    }

    override fun getNumberOfDone(): Flow<Int> {
        return myToDoListDao.getNumberOfDone()
    }

    override suspend fun updateDoneStatus(noteId: Int) {
        return myToDoListDao.updateDoneStatus(noteId)
    }
}


