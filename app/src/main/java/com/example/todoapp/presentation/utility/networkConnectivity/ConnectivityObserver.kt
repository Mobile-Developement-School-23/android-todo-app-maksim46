package com.example.todoapp.presentation.utility.networkConnectivity

import kotlinx.coroutines.flow.Flow

/**
 * Interface of [NetworkConnectivityObserver]  for network connectivity observing
 */

interface ConnectivityObserver {
    fun observe(): Flow<Status>
    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}