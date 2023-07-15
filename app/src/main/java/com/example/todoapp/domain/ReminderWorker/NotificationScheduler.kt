package com.example.todoapp.domain.ReminderWorker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class NotificationScheduler @Inject constructor(
    private val context: Context
) {

    fun startReminderNotification() {
        val currentDate = Calendar.getInstance()

        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 0)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val duration = dueDate.timeInMillis - currentDate.timeInMillis

        val notificationWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .addTag(TAG)
            .setInitialDelay(duration, TimeUnit.MILLISECONDS)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, notificationWorkRequest)


        // notify when screen start
        /*                val workRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java).build()
                        val workManager = WorkManager.getInstance(context)
                        workManager.enqueueUniqueWork("as", ExistingWorkPolicy.KEEP, workRequest)*/

    }

    companion object {
        const val TAG = "task"
    }
}
