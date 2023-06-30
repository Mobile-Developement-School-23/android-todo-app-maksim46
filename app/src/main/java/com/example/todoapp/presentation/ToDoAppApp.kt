package com.example.todoapp.presentation

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.todoapp.data.network.SyncWork.MyWorkerFactory
import com.example.todoapp.di.DaggerApplicationComponent
import javax.inject.Inject


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

    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory

    private fun setupWorkerFactory() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(myWorkerFactory)
                .build()
        )
    }
}
