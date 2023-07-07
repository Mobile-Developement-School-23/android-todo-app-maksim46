package com.example.todoapp.presentation.utility

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager

import com.example.todoapp.data.network.SyncWork.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Defined WorkManager for periodic synchronization settings
 */
class SyncWM @Inject constructor(
    private val context: Context
){

    fun startSynchWM() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequest.Builder(SyncWorker::class.java, 8, TimeUnit.HOURS, 30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniquePeriodicWork(
            "SyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

    }
}