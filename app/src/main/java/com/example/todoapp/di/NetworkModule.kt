package com.example.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.data.database.ToDoAppDatabase
import com.example.todoapp.data.network.RevisionStorage
import com.example.todoapp.data.network.ToDoNoteApi
import com.example.todoapp.data.network.ToDoNoteApiImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

/**
 * DI module related with network needs
 */

@Module
interface NetworkModule {

    @ApplicationScope
    @Binds
    fun bindsTaskApi(toDoNoteApi: ToDoNoteApiImpl): ToDoNoteApi

    /*
    @ApplicationScope
    @Provides
    fun provideRevisionStoragee(context: Context): RevisionStorage {
        return RevisionStorage(context)
    }
}
*/
}




