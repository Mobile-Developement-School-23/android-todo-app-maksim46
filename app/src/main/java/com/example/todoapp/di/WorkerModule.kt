package com.example.todoapp.di

import androidx.work.Worker
import com.example.todoapp.data.network.SyncWork.ChildWorkerFactory
import com.example.todoapp.data.network.SyncWork.SyncWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

/**
 * DI module related with WorkManager
 */
@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    internal abstract fun bindMyWorkerFactory(worker: SyncWorker.Factory): ChildWorkerFactory
}