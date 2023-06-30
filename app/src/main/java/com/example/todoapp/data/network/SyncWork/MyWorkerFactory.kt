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
    ): ListenableWorker {
        val foundEntry =  workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key)  }
        return if (foundEntry != null) {
            val factoryProvider = foundEntry.value
            factoryProvider.get().create(appContext, workerParameters)
        }else{
            val workerClass = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
            val constructor = workerClass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
            constructor.newInstance(appContext, workerParameters)


        }
/*        val workerFactoryProvider:ChildWorkerFactory= foundEntry?.get()
            ?: throw IllegalArgumentException("Unknown worker class name : $workerClassName")
            return workerFactoryProvider.create(appContext, workerParameters)*/
    }
}