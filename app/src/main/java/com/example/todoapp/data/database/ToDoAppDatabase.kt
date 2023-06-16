package com.example.todoapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todoapp.presentation.utils.Converters


@Database(entities = [ToDoListDbModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ToDoAppDatabase : RoomDatabase() {
    abstract fun getToDoListDao(): ToDoListDao
}

