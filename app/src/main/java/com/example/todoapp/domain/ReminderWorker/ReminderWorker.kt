package com.example.todoapp.domain.ReminderWorker

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.todoapp.R
import com.example.todoapp.data.network.SyncWork.ChildWorkerFactory
import com.example.todoapp.data.repository.NoteDataRepository
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * WorkManager defined for notifications
 */

class ReminderWorker(
    private val context: Context,
    private val workerParams: WorkerParameters,
    private val noteDataRepository: NoteDataRepository,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val timeOfCurrentDay = getStartAndEndOfCurrentDay()
        val now = timeOfCurrentDay.first
        val in24Hours = timeOfCurrentDay.second
        val list = getTasksForNotification(now, in24Hours)

        list.forEach { toDoEntity ->
            sendNotification(toDoEntity)
        }
        notificationScheduler.startReminderNotification()
        return Result.success()
    }


    private fun sendNotification(note: ToDoEntity) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val args = Bundle().apply {
            putString("navigation_info", "noteDetailFragment")
            putString("id", note.id)
            putString("flag", System.currentTimeMillis().toString())

        }
        val navigationIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_main)
            .setDestination(R.id.mainFragment)
            .setArguments(args)
            .createPendingIntent()


        /*        val postponeIntent = Intent(context, MainFragment::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                   // putExtra("open_fragment", "noteDetailFragment")
                    putExtra("noteID", args)
                }

                val postponePendingIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    postponeIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0*/
        //)
        val notePriority = when (note.priority) {
            Priority.Standart -> context.getString(R.string.priority_standart)
            Priority.Low -> context.getString(R.string.priority_low)
            Priority.High -> context.getString(R.string.priority_hight)
        }

        val mBuilder = NotificationCompat.Builder(context, NOTE_CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_main_pic)
            .setContentTitle("${context.getString(R.string.notify_message)} $notePriority")
            .setContentText(note.text)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(navigationIntent)
            /*                    .addAction(
                                    R.drawable.ic_notification,
                                    "Отложить",
                                    postponePendingIntent
                                )*/
            .build()

        notificationManager.notify(note.id.toInt(), mBuilder)
    }

    private suspend fun getTasksForNotification(now: Long, in24Hours: Long): List<ToDoEntity> {
        val tasksForNotification = mutableListOf<ToDoEntity>()
        noteDataRepository.getNotesForNotify(now, in24Hours).first { list ->
            tasksForNotification.addAll(list)
        }
        return tasksForNotification
    }

    private fun getStartAndEndOfCurrentDay(): Pair<Long, Long> {
        val now = Calendar.getInstance()

        val startOfDay = now.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = now.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        return startOfDay.timeInMillis to endOfDay.timeInMillis
    }


    class Factory @Inject constructor(
        private val noteDataRepository: NoteDataRepository,
        private val notificationScheduler: NotificationScheduler
    ) : ChildWorkerFactory {

        override fun create(
            context: Context,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return ReminderWorker(
                context,
                workerParameters,
                noteDataRepository = noteDataRepository,
                notificationScheduler = notificationScheduler
            )
        }
    }

    companion object {
        const val NOTE_CHANNEL_ID = "reminders"
    }


}
