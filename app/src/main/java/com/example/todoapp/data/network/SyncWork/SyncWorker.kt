package com.example.todoapp.data.network.SyncWork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.data.repository.NoteDataRepository
import javax.inject.Inject

class SyncWorker constructor(context: Context, workerParam: WorkerParameters, private val noteDataRepository: NoteDataRepository) :
    Worker(context, workerParam) {
    val context = context


    override fun doWork(): Result {
        Log.d("WORKM", "WORKER-start")
        if (isNetworkAvailable(context)) {

            Log.d("WORKM", "inetConect")
            noteDataRepository.syncNotes(true)
            return Result.success()
        } else {
            Log.d("WORKM", "no inet")
            return Result.failure()
        }
    }


    /*
        class Factory @Inject constructor(
            val noteDataRepository: NoteDataRepository,

        ): ChildWorkerFactory {

            override fun create(appContext: Context, params: WorkerParameters): SyncWorker {
                return SyncWorker(appContext, params ,noteDataRepository)
            }
        }
    }
    */

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    class Factory @Inject constructor(
        val noteDataRepository: NoteDataRepository,

        ) : ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): Worker {
            return SyncWorker(appContext, params, noteDataRepository)
        }
    }
}
