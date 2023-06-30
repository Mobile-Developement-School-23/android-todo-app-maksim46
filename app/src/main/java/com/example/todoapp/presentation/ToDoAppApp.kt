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
    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory

    companion object {
        var appContext: Context? = null
    }


    override fun onCreate() {



        // Выполнение инъекции в этот класс Application

        super.onCreate()
        component.inject(this)
        appContext = applicationContext

        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(myWorkerFactory)
                .build()
        )
    }



    /*    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory

    private fun setupWorkerFactory() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(myWorkerFactory)
                .build()
        )
    }*/




    }



