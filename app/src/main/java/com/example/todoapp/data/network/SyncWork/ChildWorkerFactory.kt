package com.example.todoapp.data.network.SyncWork

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
/**
 * Interface for work manager factory
 */
interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): ListenableWorker

}