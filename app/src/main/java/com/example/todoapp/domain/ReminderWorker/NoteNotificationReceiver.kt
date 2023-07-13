package com.example.todoapp.domain.ReminderWorker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todoapp.data.repository.NoteDataRepository
import javax.inject.Inject


class NoteNotificationReceiver @Inject constructor(): BroadcastReceiver() {

    @Inject
    lateinit var  noteDataRepository:NoteDataRepository


    override fun onReceive(context: Context, intent: Intent?) {

        val id = intent?.extras?.getString("ID") ?: "-1"

        Log.d("REMIONDER", "Broadcast $id")
        noteDataRepository.test()
      //  val service = ReminderService(context)



  /*      if (intent != null) {
            val id = intent.extras?.getString("ID") ?: "-1"
            val text = intent.extras?.getString("text") ?: ""
            val priority = intent.extras?.getInt("Priority") ?: 0
*/



          //  service.showNotification(id, text, priority)

        }
    }



