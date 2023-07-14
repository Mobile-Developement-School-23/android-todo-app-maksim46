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
            if (mode != " ") {
                return mode
        }
        return SYSTEM
    }

    fun setTheme(choosedTheme: ChoosedTheme) {
        val sharedPrefs = context.getSharedPreferences(SP_FILENAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        if (editor != null) {
            when (choosedTheme) {
                ChoosedTheme.SYSTEM -> {
                    editor.putString(KEY_THEME, SYSTEM)
                }
                ChoosedTheme.DARK -> {
                    editor.putString(KEY_THEME, DARK)
                }
                ChoosedTheme.LIGHT -> {
                    editor.putString(KEY_THEME, LIGHT)
                }
            }
            editor.apply()
        }
    }
    companion object {
        const val SYSTEM = "system"
        const val DARK = "dark"
        const val LIGHT = "light"
    }
}


enum class ChoosedTheme(val theme: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light"),
}
