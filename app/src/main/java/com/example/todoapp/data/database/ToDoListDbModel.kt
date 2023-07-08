package com.example.todoapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 *Model of notes for local databasr
 */
@Entity(tableName = "todo_list")
data class ToDoListDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val text: String,
    val priority: Int,
    val deadline: Long,
    val isDone: Boolean,
    val createDate: Date,
    val updateDate: Date
)

