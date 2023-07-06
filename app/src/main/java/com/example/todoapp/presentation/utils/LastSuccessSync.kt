package com.example.todoapp.presentation.utils

import android.content.Context
import com.example.todoapp.ToDoAppApp

class LastSuccessSync {
    val SP_FILENAME = "GetLastTime"
    val LAST_SYNC_TIME_KEY = "last_time_key"
    val context = ToDoAppApp.appContext

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