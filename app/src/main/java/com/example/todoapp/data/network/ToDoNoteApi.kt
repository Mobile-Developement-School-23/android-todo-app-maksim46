package com.example.todoapp.data.network

import com.example.todoapp.data.model.OnErrorModel
import com.example.todoapp.data.network.model.ToDoDtoModel
import com.example.todoapp.data.network.model.ToDoListResponse

/**
 * Interface of  network requests
 */
interface ToDoNoteApi {
    suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: OnErrorModel) -> Unit): ToDoDtoModel?
    suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: OnErrorModel) -> Unit): ToDoDtoModel?
    suspend fun deleteToDoNote(toDoModel: ToDoDtoModel, onError: (message: OnErrorModel) -> Unit): ToDoDtoModel?
    suspend fun getDbRevision(onError: (message: OnErrorModel) -> Unit): Int?
    suspend fun getListOfToDoNote(onError: (message: OnErrorModel) -> Unit): ToDoListResponse?
    suspend fun patchListOfToDoNote(
        listOfRequests: ToDoListResponse, onError: (message: OnErrorModel) -> Unit): List<ToDoDtoModel>?
    suspend fun yaLogin(token: String, onError: (message: OnErrorModel) -> Unit): String
}