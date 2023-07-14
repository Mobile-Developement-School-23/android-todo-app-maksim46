package com.example.todoapp.presentation.utility

import android.content.Context
import com.example.todoapp.data.repository.NoteDataRepository
import javax.inject.Inject

/**
 * Shared Preferences for storing time of last success synchronization
 */

class LastSuccessSync @Inject constructor(
    private val context: Context
) {

    private val SP_FILENAME = "GetLastTime"
    private val LAST_SYNC_TIME_KEY = "last_time_key"

    fun getLastSuccessSync(): String? {

        val sharedPrefs = context?.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        return sharedPrefs?.getString(LAST_SYNC_TIME_KEY, " ")
    }

    fun setLastSuccessSync(lastSuccessSync: String) {
        val sharedPrefs = context?.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        if (editor != null) {
            editor.putString(LAST_SYNC_TIME_KEY, lastSuccessSync)
            editor.apply()
        }
    }

}