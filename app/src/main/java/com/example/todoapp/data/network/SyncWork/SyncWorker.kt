package com.example.todoapp.data.network.SyncWork

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.data.repository.NoteDataRepository
import javax.inject.Inject

class SyncWorker  constructor(context: Context, workerParam: WorkerParameters, private val noteDataRepository: NoteDataRepository) : Worker(context, workerParam) {

    override fun doWork(): Result {
Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA111111", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
noteDataRepository.checkWorker()
      return  Result.success()
    }



    class Factory @Inject constructor(
        val noteDataRepository: NoteDataRepository,

    ): ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): SyncWorker {
            return SyncWorker(appContext, params ,noteDataRepository)
        }
    }
}

