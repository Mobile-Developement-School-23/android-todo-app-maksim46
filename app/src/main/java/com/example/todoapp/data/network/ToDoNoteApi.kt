package com.example.todoapp.data.network

interface ToDoNoteApi {
    suspend fun saveToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel?
    suspend fun updateToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel?
    suspend fun deleteToDoNote(toDoModel: ToDoDtoModel, onError: (message: String) -> Unit): ToDoDtoModel?
    suspend fun getDbRevision(onError: (message: String) -> Unit): Int?
    suspend fun getListOfToDoNote(onError: (message: String) -> Unit): ToDoListResponse?
    suspend fun patchListOfToDoNote(listOfRequests: ToDoListResponse, onError: (message: String) -> Unit): List<ToDoDtoModel>?
    suspend fun yaLogin(token: String, onError: (message: String) -> Unit): String
}