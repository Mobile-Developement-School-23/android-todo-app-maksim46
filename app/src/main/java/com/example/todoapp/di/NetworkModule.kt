package com.example.todoapp.di

import com.example.todoapp.data.network.ToDoNoteApi
import com.example.todoapp.data.network.ToDoNoteApiImpl
import dagger.Binds
import dagger.Module

/**
 * DI module related with network needs
 */

@Module
interface NetworkModule {

    @ApplicationScope
    @Binds
    fun bindsTaskApi(toDoNoteApi: ToDoNoteApiImpl): ToDoNoteApi


}





