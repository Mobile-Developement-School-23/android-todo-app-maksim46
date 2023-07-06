package com.example.todoapp.di

import android.content.Context
import com.example.todoapp.presentation.view.MainFragment
import com.example.todoapp.presentation.view.NoteDetailFragment
import com.example.todoapp.ToDoAppApp
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [ViewModelModule::class, DatabaseModule::class, UtilsModule::class,WorkerModule::class])
interface ApplicationComponent {
    fun inject(application: ToDoAppApp)
    fun inject(fragment: MainFragment)
    fun inject(fragment: NoteDetailFragment)



    @Component.Factory
    interface Factory{
        fun create(
            @BindsInstance context: Context
        ):ApplicationComponent
    }


}