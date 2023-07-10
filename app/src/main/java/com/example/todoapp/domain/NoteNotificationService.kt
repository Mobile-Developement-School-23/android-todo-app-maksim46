/*
package com.example.todoapp.domain

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todoapp.R
import com.example.todoapp.presentation.view.MainFragment
class NoteNotificationService()
*/
/*
class NoteNotificationService(
*//*

*/
/*
    private val context: Context
    ) {
        private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun showNotification(counter: Int) {
            val activityIntent = Intent(context, MainFragment::class.java)
            val activityPendingIntent = PendingIntent.getActivity(
                context,
                1,
                activityIntent,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val incrementIntent = PendingIntent.getBroadcast(
                context,
                2,
                Intent(context, NoteNotificationReceiver::class.java),
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val notification = NotificationCompat.Builder(context, NOTE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Increment counter")
                .setContentText("The count is $counter")
                .setContentIntent(activityPendingIntent)
                .addAction(
                    R.drawable.ic_notification,
                    "Increment",
                    incrementIntent
                )
                .build()

            notificationManager.notify(1, notification)
        }

        companion object {
            const val NOTE_CHANNEL_ID = "counter_channel"
        }
    }
*//*
*/
/*

*/
