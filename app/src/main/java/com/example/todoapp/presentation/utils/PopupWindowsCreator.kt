package com.example.todoapp.presentation.utils

import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.todoapp.R
import com.example.todoapp.domain.model.NoteData


class PopupWindowsCreator {

    companion object {

        fun createPopup(popupWindow: PopupWindow, view: View, popupData: PopupData, callback: PopupResultListener) {

            popupWindow.contentView = view
            popupWindow.isOutsideTouchable = true
            popupWindow.setBackgroundDrawable(null)
            popupWindow.overlapAnchor = true


            when (popupData.popupType) {
                PopupType.Action -> {

                    popupWindow.animationStyle = R.style.PopupAnimationActions
                          popupWindow.height = 500
                    popupWindow.showAsDropDown(popupData.view, 0, 0, Gravity.TOP)

                    view.findViewById<TextView>(R.id.menu_done).setOnClickListener {
                        callback.onPopupResult(CallbackAction.Done, popupData.note)
                        popupWindow.dismiss()
                    }
                    view.findViewById<TextView>(R.id.menu_update).setOnClickListener {
                        callback.onPopupResult(CallbackAction.Update, popupData.note)
                        popupWindow.dismiss()
                    }
                    view.findViewById<TextView>(R.id.menu_delete).setOnClickListener {
                        callback.onPopupResult(CallbackAction.Delete, popupData.note)
                        popupWindow.dismiss()
                    }
                }

                PopupType.Info -> {
                  //  popupWindow.height = 150
                    popupWindow.animationStyle = R.style.PopupAnimationInfo
                    popupWindow.showAsDropDown(popupData.view, 0, 0, Gravity.TOP)
                    val textView = view.findViewById<TextView>(R.id.tv_popup)
                    if (textView != null && (popupData.note as NoteData.ToDoItem).text != "") {
                        textView.text = (popupData.note as NoteData.ToDoItem).text
                        textView.setOnClickListener {
                            popupWindow.dismiss()
                        }
                    }
                }
                PopupType.Empty -> throw IllegalArgumentException("Invalid PopupType Provided")
            }
        }
    }


    data class PopupData(
        val note: NoteData = NoteData.ToDoItem(),
        val view: View? = null,
        val popupType: PopupType = PopupType.Empty
    )

    enum class PopupType(val value: Int) {
        Info(0),
        Action(1),
        Empty(3)
    }

    enum class CallbackAction(val value: Int) {
        Done(0),
        Update(1),
        Delete(2)
    }
}


