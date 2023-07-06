package com.example.todoapp.data.repository

import android.util.Log
import com.example.todoapp.data.network.ToDoDtoModel
import com.example.todoapp.data.network.ToDoListResponse
import com.example.todoapp.data.network.ToDoNoteApi
import com.example.todoapp.data.model.onErrorModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteNoteDataRepository @Inject constructor(private val toDoNoteApi: ToDoNoteApi) {

    fun saveToDoNote(toDoDtoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): Flow<ToDoDtoModel?> {
        return flow { emit(toDoNoteApi.saveToDoNote(toDoDtoModel, onError)) }
    }

    fun updateToDoNote(toDoDtoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): Flow<ToDoDtoModel?> {
        return flow { emit(toDoNoteApi.updateToDoNote(toDoDtoModel, onError)) }
    }

    fun deleteToDoNote(toDoDtoModel: ToDoDtoModel, onError: (message: onErrorModel) -> Unit): Flow<ToDoDtoModel?> {
        return flow { emit(toDoNoteApi.deleteToDoNote(toDoDtoModel, onError)) }
    }

    fun getListOfToDoNote(onError: (message: onErrorModel) -> Unit): Flow<List<ToDoDtoModel>?> {
        Log.d("SYNC", "REQUEST TO REMOTE DB")
        return flow { emit(toDoNoteApi.getListOfToDoNote(onError)?.list) }
    }

    fun patchListOfToDoNote(listOfDto: List<ToDoDtoModel>, onError: (message: onErrorModel) -> Unit): Flow<List<ToDoDtoModel>?> {
        return flow { emit(toDoNoteApi.patchListOfToDoNote(ToDoListResponse(listOfDto, -1, "ok"), onError)) }
    }

    fun yaLogin(token: String, onError: (message: onErrorModel) -> Unit): Flow<String> {
        return flow { emit(toDoNoteApi.yaLogin(token, onError)) }
    }

}

