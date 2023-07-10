package com.example.todoapp.data.network

import android.content.Context
import javax.inject.Inject

class RevisionStorage @Inject constructor(
        private val context: Context
    ) {
        private val SP_FILENAME = "GetRevision"
        private val REVISION_KEY = "revision_key"

        fun getRevision(): String? {
            val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
            return sharedPrefs?.getString(REVISION_KEY, "-1")
        }

        fun setRevision(lastSuccessSync: String) {
            val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
            val editor = sharedPrefs?.edit()
            if (editor != null) {
                editor.putString(REVISION_KEY, lastSuccessSync)
                editor.apply()
            }
        }

    }
