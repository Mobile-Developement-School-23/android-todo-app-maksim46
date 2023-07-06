package com.example.todoapp.data.model

import com.example.todoapp.data.network.ToDoDtoModel
import kotlinx.serialization.Serializable

@Serializable
data class ToDoNoteResponse(
    val list: List<ToDoDtoModel>,
    val revision: Int,
    val status: String
)