package com.example.todoapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ToDoNoteResponse(
    val list: List<ToDoDtoModel>,
    val revision: Int,
    val status: String
)