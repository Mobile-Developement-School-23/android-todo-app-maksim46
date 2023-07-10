package com.example.todoapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Description of possible operation with local data base
 */
@Dao
interface ToDoListDao {

    @Query("SELECT*FROM todo_list WHERE (isDone = 0 OR isDone = :doneStatus) AND deadline != -1 ORDER BY id ASC")
    fun getAllToDoList(doneStatus: Boolean): Flow<List<ToDoListDbModel>>

    @Query("SELECT*FROM todo_list WHERE (isDone = 0 OR isDone = :doneStatus)  ORDER BY id ASC")
    fun getAllToDoListForSynk(doneStatus: Boolean): Flow<List<ToDoListDbModel>>

    @Query("SELECT*FROM todo_list WHERE id==:noteId LIMIT 1")
    fun getOneToDoNote(noteId: Int?): ToDoListDbModel

    @Query("UPDATE todo_list SET isDone = NOT isDone WHERE id==:noteId")
    fun updateDoneStatus(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: ToDoListDbModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertListOfNotes(list: List<ToDoListDbModel>): List<Long>


    @Query("DELETE FROM todo_list WHERE id=:noteId")
    fun deleteToDoNote(noteId: Int?)

    @Query("UPDATE todo_list SET deadline = -1, updateDate = :updateDate  WHERE id==:noteId")
    fun markAsDeleteToDoNote(noteId: Int?, updateDate: Long)

    @Query("DELETE FROM todo_list WHERE deadline=-1")
    fun deleteMarked()

    @Query("SELECT COUNT(isDone) FROM todo_list WHERE isDone=1 AND deadline != -1")
    fun getNumberOfDone(): Flow<Int>

    @Update
    fun updateNote(note: ToDoListDbModel)
}