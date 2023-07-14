package com.example.todoapp.presentation.view

import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.databinding.NoteItemBinding
import com.example.todoapp.domain.model.ClickData
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.PressType
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.presentation.utility.PopupWindowsHandler
import java.text.SimpleDateFormat
/**
 * RecyclerView.ItemViewHolder implementation for [RVListAdapter].
 */

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding by viewBinding(NoteItemBinding::bind)

    fun bind(noteItem: NoteData.ToDoItem) {
        with(binding) {
            groupForClick.setOnClickListener { noteItem.onNoteClick(ClickData(PressType.SHORT, noteItem, null)) }
            groupForClick.setOnLongClickListener {
                noteItem.onNoteClick(ClickData(PressType.LONG, noteItem, groupForClick))
                true
            }
            ivInfo.setOnClickListener { noteItem.onInfoClick(
                PopupWindowsHandler.PopupData(noteItem, ivInfo, PopupWindowsHandler.PopupType.Info)) }

            tvNote.text = noteItem.text
            when (noteItem.priority) {
                Priority.High -> {
                    checkBox.setImageResource(R.drawable.ic_unchecked_red)
                    ivEmergency.setImageResource(R.drawable.ic_emergency_hight_flag)
                    ivEmergency.visibility = View.VISIBLE
                }
                Priority.Low -> {
                    ivEmergency.setImageResource(R.drawable.ic_emergency_low_flag)
                    ivEmergency.visibility = View.VISIBLE
                }
                Priority.Standart -> {
                    ivEmergency.visibility = View.GONE
                }
            }
            if (noteItem.isDone) {
                checkBox.setImageResource(R.drawable.ic_check_done)
                tvNote.paintFlags = tvNote.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvNote.setTextAppearance(R.style.TextView_Body_Grey)
            } else {
                if (noteItem.priority != Priority.High) {
                    checkBox.setImageResource(R.drawable.ic_unchecked)
                }
                tvNote.paintFlags =
                    tvNote.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvNote.setTextAppearance(R.style.TextView_Body)
            }
            if (noteItem.deadline != 0L) {
                tvDoneBefore.text = SimpleDateFormat(
                    "dd MMMM yyyy", itemView.context.resources.configuration.locales.get(0)).format(noteItem.deadline)
                tvDoneBefore.visibility = View.VISIBLE
                tvSubHeadDoneBefore.visibility = View.VISIBLE
            }else{
                tvDoneBefore.visibility = View.GONE
                tvSubHeadDoneBefore.visibility = View.GONE
            }
        }
    }
}