package com.example.todoapp.di

import javax.inject.Scope

/**
 * Set of DI scopes
 */

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope()


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FragmentScope()