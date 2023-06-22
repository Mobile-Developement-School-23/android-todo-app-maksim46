package com.example.todoapp.domain.model

import java.util.*

data class ToDoEntity(
    val id: String,
    val text: String,
    val priority: Priority,
    val deadline: Long,
    var isDone: Boolean,
    val createDate: Date,
    val updateDate: Date
) : NoteData()