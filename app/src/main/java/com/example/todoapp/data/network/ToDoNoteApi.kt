package com.example.todoapp.data.network

import com.example.todoapp.data.model.onErrorModel
import com.example.todoapp.data.network.model.ToDoDtoModel
import com.example.todoapp.data.network.model.ToDoListResponse

/**
 * Interface of  network requests
 */
interface ToDoNoteApi {
    suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): ToDoDtoModel?
    suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): ToDoDtoModel?
    suspend fun deleteToDoNote(toDoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): ToDoDtoModel?
    suspend fun getDbRevision(onError: (message: onErrorModel) -> Unit): Int?
    suspend fun getListOfToDoNote(onError: (message: onErrorModel) -> Unit): ToDoListResponse?
    suspend fun patchListOfToDoNote(listOfRequests: ToDoListResponse, onError: (message: onErrorModel) -> Unit): List<ToDoDtoModel>?
    suspend fun yaLogin(token: String, onError: (message: onErrorModel) -> Unit): String
}