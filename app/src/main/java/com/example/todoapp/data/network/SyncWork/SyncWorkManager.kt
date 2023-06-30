package com.example.todoapp.data.network.SyncWork

import android.content.Context
import com.example.todoapp.presentation.MainFragmentViewModel
import javax.inject.Inject


class SyncWorkManager(context: Context) {
}
/*
    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory


    WorkManager.initialize(
    this,
    Configuration.Builder()
    .setWorkerFactory(myWorkerFactory)
    .build()
    )
    fun createWork() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            // val syncWork = PeriodicWorkRequestBuilder<SynkWorker>(8,   TimeUnit.HOURS, 30, TimeUnit.MINUTES)
        val syncWork = PeriodicWorkRequestBuilder<SynkWorker>(5,   TimeUnit.MINUTES, 1, TimeUnit.MINUTES)
            .addTag(TAG)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWork
        )
    }
}*/
