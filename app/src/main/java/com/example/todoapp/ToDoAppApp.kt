package com.example.todoapp


import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import com.example.todoapp.data.network.SyncWork.MyWorkerFactory
import com.example.todoapp.di.DaggerApplicationComponent

import com.example.todoapp.domain.ReminderWorker.ReminderWorker
import javax.inject.Inject

/**
 * Custom Application class allows to hold reference to [applicationComponent]
 * as long as application lives.
 */

class ToDoAppApp : Application(), Configuration.Provider {

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
        createNotificationChannel()
    }


    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setWorkerFactory(myWorkerFactory).build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderWorker.NOTE_CHANNEL_ID,
                "NoteDeadline",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = getString(R.string.channel_description)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}





