package com.example.todoapp.domain.model

import java.util.Date

/**
 * This is a model, that is used for displaying UI.
 */
data class ToDoEntity(
    val id: String,
    val text: String,
    val priority: Priority,
    val deadline: Long,
    var isDone: Boolean,
    val createDate: Date,
    val updateDate: Date
) : NoteData()