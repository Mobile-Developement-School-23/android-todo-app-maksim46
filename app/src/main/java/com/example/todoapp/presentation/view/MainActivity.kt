package com.example.todoapp.presentation.view

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.R

/**
 * Contains [MainFragment] and [NoteDetailFragment].
 */

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // Если уведомления отключены, показать диалоговое окно
            AlertDialog.Builder(this).apply {
                setTitle("Разрешения на уведомления")
                setMessage("Включите уведомления для лучшего опыта")
                setPositiveButton("Настройки") { _, _ ->
                    // Перенаправить пользователя на экран настроек уведомлений
                    startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    })
                }
                setNegativeButton("Отмена", null)
                show()
            }
        }


/*        val fragmentToOpen = intent.getStringExtra("open_fragment")

        if (fragmentToOpen == "noteDetailFragment") {
            // Открываем фрагмент
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_container, MainFragment())
            transaction.commit()
        }*/
    }
    }

