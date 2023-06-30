package com.example.todoapp.di

import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import com.example.todoapp.data.network.SyncWork.ChildWorkerFactory
import com.example.todoapp.data.network.SyncWork.MyWorkerFactory
import com.example.todoapp.data.network.SyncWork.SyncWorker

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

/*@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out Worker>)*/

/*
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out ListenableWorker>)


@Module
abstract class ParcelPollModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    internal abstract fun bindMyWorkerFactory(worker: SyncWorker.Factory): ChildWorkerFactory


}*/

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out Worker>)
@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    internal abstract fun bindMyWorkerFactory(worker: SyncWorker.Factory): ChildWorkerFactory
}