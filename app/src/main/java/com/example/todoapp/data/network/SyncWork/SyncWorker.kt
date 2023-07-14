package com.example.todoapp.data.network.SyncWork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.data.repository.NoteDataRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * WorkManager for periodic notes synchronization
 */


class SyncWorker (
    private val context: Context,
    workerParam: WorkerParameters,
    private val noteDataRepository: NoteDataRepository) :
    Worker(context, workerParam) {


    override fun doWork(): Result {
        noteDataRepository.syncNotes(true)
        return Result.success()
    }


    class Factory @Inject constructor(
        private val noteDataRepository: NoteDataRepository
    ) : ChildWorkerFactory {

        override fun create(
            context: Context,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return SyncWorker(
                context,
                workerParameters,
                noteDataRepository = noteDataRepository
            )
        }
    }
}



