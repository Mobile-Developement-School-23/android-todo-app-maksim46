package com.example.todoapp.di

import android.content.Context
import com.example.todoapp.ToDoAppApp
import dagger.BindsInstance
import dagger.Component

/**
 * DI subcomponent related with Application [ToDoAppApp]
 * Initialization of subcomponent factories
 */
@ApplicationScope
@Component(modules = [DatabaseModule::class, WorkerModule::class, NetworkModule::class])
interface ApplicationComponent {
    fun inject(application: ToDoAppApp)
    fun fragmentMainComponent(): FragmentMainComponent.Factory
    fun fragmentNoteDetailComponent(): FragmentNoteDetailComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context
        ): ApplicationComponent
    }
}