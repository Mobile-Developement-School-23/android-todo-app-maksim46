package com.example.todoapp.di

import androidx.lifecycle.ViewModel
import com.example.todoapp.presentation.view.MainFragmentViewModel
import com.example.todoapp.presentation.utils.itemTouchHelper.IntItemTouchHelper
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module

interface ViewModelModule {

    @IntoMap
    @ViewModelKey(MainFragmentViewModel::class)
    @Binds

    fun bindMainFragmentViewModel(impl: MainFragmentViewModel): ViewModel

    @Binds
    fun bindItemTouchHelperAdapter(impl : MainFragmentViewModel):IntItemTouchHelper
}