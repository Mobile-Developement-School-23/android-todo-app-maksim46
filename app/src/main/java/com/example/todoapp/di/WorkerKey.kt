package com.example.todoapp.di

import androidx.work.ListenableWorker
import androidx.work.Worker
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * MapKey for WorkManagerFactory
 */

/*
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out Worker>)*/
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)