package com.example.todoapp.domain.ReminderWorker

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.data.network.SyncWork.SyncWorker

import com.example.todoapp.domain.ReminderWorker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class NotificationScheduler @Inject constructor(
    private val context: Context
) {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    fun startReminderNotification() {
        val currentDate = Calendar.getInstance()
        Log.d("REMIONDER", "startReminderNotification")

        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 10)
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
        /*       val notificationWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                   .addTag(TAG)
                   .setInitialDelay(currentDate.timeInMillis, TimeUnit.MILLISECONDS)
                   .build()*/

        /*                val workRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                    .setConstraints(constraints)
                    .build()



                currentDate.add(Calendar.SECOND, 15)*/
  /*      val notificationWorkRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(currentDate.timeInMillis, TimeUnit.MILLISECONDS)
            .build()*/


        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, notificationWorkRequest)


/*        val workRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java).build()

        //val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniqueWork("as", ExistingWorkPolicy.KEEP, workRequest)*/


    }

    companion object {
        const val TITLE = "title"
        const val SUB_TITLE = "subtitle"
        const val TASK_COUNT = "taskcount"
        const val TAG = "task"
    }
}
