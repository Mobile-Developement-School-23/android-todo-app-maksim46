package com.example.todoapp.domain.model

import android.view.View

import com.example.todoapp.presentation.utils.PopupWindowsCreator
import java.util.*

sealed class NoteData {

    data class ToDoItem(
        val id: String = "0",
        val text: String = "",
        val priority: Priority = Priority.Standart,
        val deadline: Long = 0,
        val isDone: Boolean = false,
        val onNoteClick: (id: ClickData) -> Unit = {},
        val onInfoClick: (text: PopupWindowsCreator.PopupData) -> Unit = {},
        val createDate: Date = Date(0),
        val updateDate: Date = Date(0)
    ) : NoteData()

    data class FooterItem(
        val onAddClick: (text: String) -> Unit
    ) : NoteData()
}

enum class Priority(val value: Int) {
    Low(0),
    Standart(1),
    Hight(2)
}

data class ClickData(
    val pressType: PressType,
    val id: String,
    val view: View?
)


enum class PressType(val type: String) {
    SHORT("short"),
    LONG("long"),
}

data class InfoForNavigationToScreenB(
    val id: Int = NEW_NOTE,
    val navigateToScreenB: Boolean = false
)

const val NEW_NOTE = -1