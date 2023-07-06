package com.example.todoapp.data.network.SyncWork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.data.repository.NoteDataRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class SyncWorker constructor(private val context: Context, workerParam: WorkerParameters, private val noteDataRepository: NoteDataRepository) :
    Worker(context, workerParam) {
    private val handler = CoroutineExceptionHandler { _, exception -> Log.d("CoroutineException", "Caught $exception") }
   // private val scope = CoroutineScope(Dispatchers.IO + handler)

    override  fun doWork(): Result {
        return if (isNetworkAvailable(context)) {
            noteDataRepository.syncNotes(true)
            Result.success()
        } else {
            Result.failure()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    class Factory @Inject constructor(val noteDataRepository: NoteDataRepository) : ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): Worker {
            return SyncWorker(appContext, params, noteDataRepository)
        }
    }
}
