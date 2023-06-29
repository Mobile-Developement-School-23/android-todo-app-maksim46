package com.example.todoapp.data.network.SyncWork

class SyncWorkManager(){

}
/*class SyncWorkManager(context: Context, private val vm: MainFragmentViewModel) {
}*/
/*
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }
    fun synchTask(){
        vm.syncNotes()
    }
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
