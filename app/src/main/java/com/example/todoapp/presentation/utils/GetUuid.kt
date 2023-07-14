package com.example.todoapp.presentation.utils

import android.content.Context
import com.example.todoapp.presentation.ToDoAppApp
import java.util.UUID

class GetUuid {

    private val SP_FILENAME = "GetUuid"
    private val UUID_KEY = "uuid_key"
    val context = ToDoAppApp.appContext

    fun getDeviceUUID(): String {
        val sharedPrefs = context?.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        var uuid = sharedPrefs?.getString(UUID_KEY, null)

        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            val editor = sharedPrefs?.edit()
            if (editor != null) {
                editor.putString(UUID_KEY, uuid)
                editor.apply()
            }
        }
        return uuid
    }
}