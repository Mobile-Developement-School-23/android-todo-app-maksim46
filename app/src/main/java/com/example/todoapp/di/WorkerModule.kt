package com.example.todoapp.di

import androidx.work.Worker
import com.example.todoapp.data.network.SyncWork.ChildWorkerFactory
import com.example.todoapp.data.network.SyncWork.SyncWorker
import com.example.todoapp.domain.ReminderWorker.ReminderWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

/**
 * DI module related with WorkManager
 */
@Module
interface WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    fun bindRefreshDataWorkerFactory(worker: SyncWorker.Factory): ChildWorkerFactory


    @Binds
    @IntoMap
    @WorkerKey(ReminderWorker::class)
    fun bindRemindDataWorkerFactory(worker: ReminderWorker.Factory): ChildWorkerFactory
}
