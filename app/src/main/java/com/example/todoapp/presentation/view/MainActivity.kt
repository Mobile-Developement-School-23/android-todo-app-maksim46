package com.example.todoapp.presentation.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.R
import com.example.todoapp.domain.CurrentThemeStorage

/**
 * Contains [MainFragment] and [NoteDetailFragment].
 */

class MainActivity : AppCompatActivity(R.layout.activity_main) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
}


    /*        val fragmentToOpen = intent.getStringExtra("open_fragment")

            if (fragmentToOpen == "noteDetailFragment") {
                // Открываем фрагмент
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.nav_host_container, MainFragment())
                transaction.commit()
            }*/






