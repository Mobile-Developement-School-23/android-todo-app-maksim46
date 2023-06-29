package com.example.todoapp.di

import com.example.todoapp.data.network.ToDoNoteApi
import com.example.todoapp.data.network.ToDoNoteApiImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindsTaskApi(toDoNoteApi: ToDoNoteApiImpl): ToDoNoteApi
}