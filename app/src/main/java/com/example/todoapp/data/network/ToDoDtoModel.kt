package com.example.todoapp.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ToDoDtoModel(

    @SerialName("id")
    val id: String,
    @SerialName("text")
    val text: String,
    @SerialName("importance")
    val importance: String,          //"low" / "basic" / "important"
    @SerialName("deadline")
    val deadline: Long,
    @SerialName("done")
    val done: Boolean,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("changed_at")
    val changedAt: Long,
    @SerialName("last_updated_by")
    val lastUpdatedBy: String
)

@Serializable
data class ToDoPayload(
    val element: ToDoDtoModel,
)

@Serializable
data class ToDoResponse(
    val element: ToDoDtoModel,
    val revision: Int,
    val status: String
)

@Serializable
data class ToDoListResponse(
    val list: List<ToDoDtoModel>,
    val revision: Int,
    val status: String
)