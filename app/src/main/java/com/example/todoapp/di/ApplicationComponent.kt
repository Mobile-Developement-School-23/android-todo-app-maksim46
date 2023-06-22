package com.example.todoapp.di

import android.content.Context
import com.example.todoapp.presentation.MainFragment
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [ViewModelModule::class, DatabaseModule::class, UtilsModule::class])
interface ApplicationComponent {

    fun inject(fragment: MainFragment)

    @Component.Factory
    interface Factory{
        fun create(
            @BindsInstance context: Context
        ):ApplicationComponent
    }
}