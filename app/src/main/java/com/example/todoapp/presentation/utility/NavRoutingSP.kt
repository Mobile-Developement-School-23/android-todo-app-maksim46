package com.example.todoapp.presentation.utility

import android.content.Context
import javax.inject.Inject

class NavRoutingSP @Inject constructor(
    private val context: Context
) {
    private val SP_FILENAME = "GetRevision"
    private val ROUTING_KEY = "routing_key"

    fun getRoutingInfo(): String? {
        val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        return sharedPrefs?.getString(ROUTING_KEY, "-1")
    }

    fun setRoutingInfo(lastSuccessSync: String) {
        val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        if (editor != null) {
            editor.putString(ROUTING_KEY, lastSuccessSync)
            editor.apply()
        }
    }

}

