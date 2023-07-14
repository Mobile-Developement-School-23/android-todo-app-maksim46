package com.example.todoapp.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass
/**
 * MapKey for ViewModelFactory
 */
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)