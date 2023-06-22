package com.example.todoapp.presentation.utils

import com.example.todoapp.domain.model.NoteData

interface PopupResultListener {
    fun onPopupResult(action:PopupWindowsCreator.CallbackAction, result: NoteData)
}