package com.example.todoapp.presentation.utils

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthSdk
import com.yandex.authsdk.YandexAuthToken

class YandexLoginHandler(private val loginResultLauncher: ActivityResultLauncher<Intent>, private val yandexLoginSdk: YandexAuthSdk, private val onTokenReceived: (String) -> Unit) {

    fun login() {
        val intent = yandexLoginSdk.createLoginIntent(YandexAuthLoginOptions.Builder().build())
        loginResultLauncher.launch(intent)
    }

    fun handleResult(result: ActivityResult) {
        try {
            val yandexAuthToken: YandexAuthToken? = yandexLoginSdk.extractToken(result.resultCode, result.data)
            if (yandexAuthToken != null) {
                onTokenReceived(yandexAuthToken.value)
            }
        } catch (e: YandexAuthException) {
            println("ERROR+${e.toString()}")
        }
    }
}