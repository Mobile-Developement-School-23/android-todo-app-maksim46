package com.example.todoapp.presentation

import android.app.Application
import android.content.Context
import com.example.todoapp.di.DaggerApplicationComponent


class ToDoAppApp: Application() {
    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        var appContext: Context? = null
    }
}
