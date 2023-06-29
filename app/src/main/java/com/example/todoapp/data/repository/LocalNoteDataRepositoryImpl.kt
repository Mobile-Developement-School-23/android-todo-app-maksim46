package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.database.ToDoListDao
import com.example.todoapp.data.database.ToDoListDbModel
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.domain.repository.LocalNoteDataRepository
import com.example.todoapp.presentation.utils.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class LocalNoteDataRepositoryImpl @Inject constructor(private val myToDoListDao: ToDoListDao) :
    LocalNoteDataRepository {


    override suspend fun insertToDoNote(note: ToDoEntity): Long {
        val addedNoteId = myToDoListDao.insertNote(note.toDbModel())
        return addedNoteId
    }

    override suspend fun insertListOfNotes(list: List<ToDoListDbModel>) {
        Log.d("MERGE", "repo in local")
        val a = myToDoListDao.insertListOfNotes(list)
        Log.d("MERGE", "repo dobavlen0 $a")
        //  return addedNoteId
    }

    override suspend fun deleteToDoNote(id: String) {
        myToDoListDao.deleteToDoNote(id.toIntOrNull())
    }


    override suspend fun updateToDoNote(note: ToDoEntity) {
        myToDoListDao.updateNote(note.toDbModel())
    }

    override suspend fun getToDoNote(id: String): ToDoEntity {
        val tmpId = id.toIntOrNull()
        val oneNoteByID = myToDoListDao.getOneToDoNote(tmpId).toEntity()
        return oneNoteByID
    }

    override fun getToDoNoteList(doneStatus: Boolean): Flow<List<ToDoEntity>> {
        val toDoNoteList = myToDoListDao.getAllToDoList(doneStatus)
        val convertedToDoNoteList = toDoNoteList.map { it.toListOfToDoEntyty() }
        return convertedToDoNoteList
    }

    override fun getNumberOfDone(): Flow<Int> {
        return myToDoListDao.getNumberOfDone()
    }

    override suspend fun updateDoneStatus(noteId: Int) {
        return myToDoListDao.updateDoneStatus(noteId)
    }
}


