package com.example.todoapp.data.network.SyncWork

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider


class MyWorkerFactory @Inject constructor(
    private  val workerFactories: Map<Class<out Worker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {




    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val factoryClass = workerFactories.keys.find { it.name == workerClassName }
        val workerFactoryProvider = factoryClass?.let { workerFactories[it] }
        return workerFactoryProvider?.get()?.create(appContext, workerParameters)
    }
}


