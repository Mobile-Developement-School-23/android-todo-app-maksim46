package com.example.todoapp.presentation.utility

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager

/**
 * Provides some utility extension functions
 */

fun Context.hideKeyboard(view: View) {
    val inputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.startAnimation(animation: Animation, onEnd: () -> Unit) {
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = Unit

        override fun onAnimationEnd(animation: Animation?) {
            onEnd()
        }

        override fun onAnimationRepeat(animation: Animation?) = Unit
    })
    this.startAnimation(animation)
}

fun <T> T.applyCustom(action: T.() -> Unit): T {
    this.action()
    return this
}