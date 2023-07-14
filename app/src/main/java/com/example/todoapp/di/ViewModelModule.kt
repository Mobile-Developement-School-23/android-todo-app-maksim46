package com.example.todoapp.di

import androidx.lifecycle.ViewModel
import com.example.todoapp.presentation.view.MainFragmentViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * DI module related with ViewModel
 */
@Module
interface ViewModelModule {

    @IntoMap

    @ViewModelKey(MainFragmentViewModel::class)
    @Binds
    fun bindMainFragmentViewModel(impl: MainFragmentViewModel): ViewModel

}