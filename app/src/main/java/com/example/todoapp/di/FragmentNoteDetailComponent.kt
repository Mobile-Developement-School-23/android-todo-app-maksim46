package com.example.todoapp.di

import android.app.Activity
import androidx.fragment.app.Fragment
import com.example.todoapp.presentation.view.NoteDetailFragment
import dagger.BindsInstance
import dagger.Subcomponent

/**
 * DI subcomponent related with [NoteDetailFragment]
 */

@FragmentScope
@Subcomponent(
    modules = [UtilsModule::class, ViewModelModule::class]
)
interface FragmentNoteDetailComponent {


    fun inject(fragment: NoteDetailFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance fragment: Fragment,
            @BindsInstance activity: Activity,
        ): FragmentNoteDetailComponent
    }


}
