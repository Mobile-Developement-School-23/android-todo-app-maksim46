package com.example.todoapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.*

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

//INSERT INTO todo_list (text, priority,deadline,isDone) VALUES ('test4','2','123','true')