package com.example.todoapp.domain

import android.content.Context
import android.util.Log
import javax.inject.Inject

class CurrentThemeStorage @Inject constructor(
    private val context: Context
) {
    private val SP_FILENAME = "GetTheme"
    private val KEY_THEME = "system_key"


    fun getTheme(): String? {
        val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
            val mode = sharedPrefs?.getString(KEY_THEME, " ")
          //  Log.d("THEME", "GET mode ${mode}")
            if (mode != " ") {
               // Log.d("THEME", "RETURNED $mode")
                return mode

        }
        return null

    }

    fun setTheme(choosedTheme: ChoosedTheme) {
        val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        if (editor != null) {
            Log.d("THEME", "choosedtheme ${choosedTheme.theme}")
            when (choosedTheme) {
                ChoosedTheme.SYSTEM -> {
                    Log.d("THEME", "SYSTEM")
                    editor.putString(KEY_THEME, "system")
                }

                ChoosedTheme.DARK -> {
                    editor.putString(KEY_THEME, "dark")
                    Log.d("THEME", "dark")
                }

                ChoosedTheme.LIGHT -> {
                    editor.putString(KEY_THEME, "light")
                    Log.d("THEME", "light")
                }
            }
            editor.apply()
        }
    }

}


enum class ChoosedTheme(val theme: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light"),
}