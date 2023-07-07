package com.example.todoapp.presentation.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Provider

/**
 * Responsible for creating ViewModels.
 */

class ViewModelFactory @Inject constructor(private val viewModelProviders: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModelProviders[modelClass]?.get() as T
    }

}
