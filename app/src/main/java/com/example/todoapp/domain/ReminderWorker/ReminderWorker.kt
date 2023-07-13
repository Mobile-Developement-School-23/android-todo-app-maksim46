package com.example.todoapp.domain.ReminderWorker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.R
import com.example.todoapp.data.network.SyncWork.ChildWorkerFactory
import com.example.todoapp.data.network.SyncWork.SyncWorker
import com.example.todoapp.data.repository.NoteDataRepository
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import com.example.todoapp.presentation.view.MainActivity
import io.ktor.network.selector.SelectInterest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

class ReminderWorker(
    private val context: Context,
    private val workerParams: WorkerParameters,
    private val noteDataRepository: NoteDataRepository,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    val selectedDate = SimpleDateFormat("dd MMMM yyyy HH mm ss", context.resources?.configuration?.locales?.get(0))  //tmp

    override suspend fun doWork(): Result {

        //  CoroutineScope(Dispatchers.IO).launch {
        Log.d("REMIONDER", "doWOrk")
        val timeOfCurrentDay = getStartAndEndOfCurrentDay()
        val now = timeOfCurrentDay.first
        val in24Hours = timeOfCurrentDay.second
        val list = getTasksForNotification(now, in24Hours)

        //   val intent = Intent(context, NoteNotificationReceiver::class.java)


        list.forEach { toDoEntity ->
            Log.d("REMIONDER_ENT11", "${toDoEntity.text}  ${selectedDate.format(toDoEntity.deadline)}")  //tmp
            val noteId = toDoEntity.id
            val noteText = toDoEntity.text
            val notePriority: Priority = toDoEntity.priority

            sendNotification(noteId, noteText, notePriority)
        }
/*          val reminderService = ReminderService(context)
          reminderService.showNotification()*/
        notificationScheduler.startReminderNotification()
        return Result.success()
    }



fun sendNotification(noteId: String, noteText: String, notePriority: Priority) {
    Log.d("REMIONDER", "sendNotification")
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val navigationIntent = NavDeepLinkBuilder(context)
        .setComponentName(MainActivity::class.java)
        .setGraph(R.navigation.nav_main)
        .setDestination(R.id.mainFragment)
        .createPendingIntent()


    val postponeIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("open_fragment", "noteDetailFragment")
        putExtra("noteID", noteId)
    }



    val postponePendingIntent = PendingIntent.getBroadcast(
        context,
        2,
        postponeIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )

    val mBuilder = NotificationCompat.Builder(context, NOTE_CHANNEL_ID)
        .setAutoCancel(true)
        // .setCategory(NotificationCompat.CATEGORY_ALARM)
        //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setSmallIcon(R.drawable.ic_main_pic)
        //  .setColor(ContextCompat.getColor(context, R.color.light_primary))
        .setContentTitle("Требуется завршить дело. Приоритет $notePriority")
        //.setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
        .setContentText(noteText)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentIntent(navigationIntent)
/*        .addAction(
            R.drawable.ic_notification,
            "Отложить",
            postponePendingIntent
        )*/
        .build()

    notificationManager.notify(noteId.toInt(), mBuilder)


}


private suspend fun getTasksForNotification(now: Long, in24Hours: Long): List<ToDoEntity> {
    Log.d("REMIONDER", "getTasksForNotification")
    val tasksForNotification = mutableListOf<ToDoEntity>()
    noteDataRepository.getNotesForNotify(now, in24Hours).first { list ->
        tasksForNotification.addAll(list)
    }
    Log.d("NOTIF_tasksForNotification", tasksForNotification.toString())
    return tasksForNotification
}


fun getStartAndEndOfCurrentDay(): Pair<Long, Long> {
    Log.d("REMIONDER", "getStartAndEndOfCurrentDay")
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
