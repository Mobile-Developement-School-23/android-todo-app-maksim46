package com.example.todoapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.todoapp.data.network.SyncWork.MyWorkerFactory
import com.example.todoapp.di.DaggerApplicationComponent


import javax.inject.Inject

/**
 * Custom Application class allows to hold reference to [applicationComponent]
 * as long as application lives.
 */

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
        super.onCreate()
        component.inject(this)
        appContext = applicationContext

        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(myWorkerFactory)
                .build()
        )
     //   createNotificationChannel()
    }


/*    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NoteNotificationService.NOTE_CHANNEL_ID,
                "Counter",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Used for the increment counter notifications"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }*/

    }




