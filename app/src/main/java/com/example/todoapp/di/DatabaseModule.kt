package com.example.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.data.database.ToDoAppDatabase
import com.example.todoapp.presentation.view.MainFragment
import dagger.Module
import dagger.Provides

/**
 * DI subcomponent related with Database
 */

@Module
class DatabaseModule {

    @ApplicationScope
    @Provides
    fun provideMyAppDatabase(context: Context): ToDoAppDatabase {
        return Room.databaseBuilder(context, ToDoAppDatabase::class.java, "main.db").build()
    }

    @ApplicationScope
    @Provides
    fun provideToDoListDao(db: ToDoAppDatabase) = db.getToDoListDao()

}
