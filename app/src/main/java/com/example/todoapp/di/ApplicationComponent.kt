package com.example.todoapp.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.todoapp.data.network.SyncWork.MyWorkerFactory
import com.example.todoapp.presentation.MainFragment
import com.example.todoapp.presentation.ToDoAppApp
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject

@ApplicationScope
@Component(modules = [ViewModelModule::class, DatabaseModule::class, UtilsModule::class,WorkerModule::class])
interface ApplicationComponent {
    fun inject(application: ToDoAppApp)
    fun inject(fragment: MainFragment)

    @Component.Factory
    interface Factory{
        fun create(
            @BindsInstance context: Context
        ):ApplicationComponent
    }


}