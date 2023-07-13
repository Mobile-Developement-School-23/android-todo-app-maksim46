package com.example.todoapp.di

import android.app.Activity
import androidx.fragment.app.Fragment
import com.example.todoapp.presentation.view.BottomSheetFragment
import com.example.todoapp.presentation.view.MainFragment
import dagger.BindsInstance
import dagger.Subcomponent

@FragmentScope
@Subcomponent()
interface FragmentBottomSheetComponent {

        fun inject(fragment: BottomSheetFragment)

        @Subcomponent.Factory
        interface Factory {
            fun create(
            ): FragmentBottomSheetComponent
        }


    }

