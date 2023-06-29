package com.example.todoapp.data.network.SyncWork

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class SyncWorker(context: Context, workerParam:WorkerParameters): Worker(context,workerParam) {

    override fun doWork(): Result {

            //  vm.syncNotes()
        return if (myFunction()) {
            Result.success()
        } else {
            Result.failure()
        }

    }

    private fun myFunction(): Boolean {
        Log.d("MERGE", "WORKER-work")
        print("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        return true
    }
}