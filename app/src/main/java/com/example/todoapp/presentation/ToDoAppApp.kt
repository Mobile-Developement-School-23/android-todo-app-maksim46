package com.example.todoapp.presentation

import android.app.Application
import com.example.todoapp.di.DaggerApplicationComponent


class ToDoAppApp: Application() {
    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}