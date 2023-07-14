package com.example.todoapp.di

import android.app.Activity
import androidx.fragment.app.Fragment
import com.example.todoapp.presentation.view.MainFragment
import dagger.BindsInstance
import dagger.Subcomponent

/**
 * DI subcomponent related with [MainFragment]
 */
@FragmentScope
@Subcomponent(
    modules = [UtilsModule::class, ViewModelModule::class]
)
interface FragmentMainComponent {

    fun inject(fragment: MainFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance fragment: Fragment,
            @BindsInstance activity: Activity,
        ): FragmentMainComponent
    }


}
