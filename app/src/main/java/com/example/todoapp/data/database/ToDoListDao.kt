package com.example.todoapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoListDao {

    @Query ("SELECT*FROM todo_list WHERE isDone = 0 OR isDone = :doneStatus ORDER BY id ASC")
    fun getAllToDoList(doneStatus:Boolean): Flow<List<ToDoListDbModel>>

    @Query ("SELECT*FROM todo_list WHERE id==:noteId LIMIT 1")
    fun getOneToDoNote(noteId : Int?): ToDoListDbModel

    @Query ("UPDATE todo_list SET isDone = NOT isDone WHERE id==:noteId")
    fun updateDoneStatus(noteId : Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertNote(note: ToDoListDbModel): Long

    @Query("DELETE FROM todo_list WHERE id=:noteId")
     fun deleteToDoNote(noteId :Int?)

    @Query("SELECT COUNT(isDone) FROM todo_list WHERE isDone=1")
    fun getNumberOfDone(): Flow<Int>

    @Update
    fun updateNote(note: ToDoListDbModel)
}