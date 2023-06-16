package com.example.todoapp.presentation


import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.R
import com.example.todoapp.databinding.FooterItemBinding


import com.example.todoapp.databinding.NoteItemBinding
import com.example.todoapp.domain.model.*
import com.example.todoapp.presentation.utils.DUtils
import com.example.todoapp.presentation.utils.PopupWindowsCreator

import java.text.SimpleDateFormat
import java.util.*


class MainRVAdapter() : ListAdapter<NoteData, RecyclerView.ViewHolder>(AsyncDifferConfig.Builder(DUtils()).build()) {

    fun submit(list: List<NoteData>) {
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.note_item -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
                ItemViewHolder(view)
            }
            R.layout.footer_item -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.footer_item, parent, false)
                FooterViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = currentList[position]

        when (holder) {
            is ItemViewHolder -> holder.bind(item as NoteData.ToDoItem)
            is FooterViewHolder -> holder.bind(item as NoteData.FooterItem)
        }
    }

    override fun getItemCount() = currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is NoteData.ToDoItem -> R.layout.note_item
            is NoteData.FooterItem -> R.layout.footer_item
            else -> {
                throw IllegalArgumentException("Invalid ViewType Provided")
            }
        }
    }


    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding(NoteItemBinding::bind)

        fun bind(noteItem: NoteData.ToDoItem) {
            with(binding) {
                groupForClick.setOnClickListener { noteItem.onNoteClick(ClickData(PressType.SHORT, noteItem.id, null)) }
                groupForClick.setOnLongClickListener {
                    noteItem.onNoteClick(ClickData(PressType.LONG, noteItem.id, groupForClick))
                    true
                }

                ivInfo.setOnClickListener { noteItem.onInfoClick(PopupWindowsCreator.PopupData(noteItem, ivInfo, PopupWindowsCreator.PopupType.Info)) }

                tvNote.text = noteItem.text
                when (noteItem.priority) {
                    Priority.Hight -> {
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
                    if (noteItem.priority != Priority.Hight) {
                        checkBox.setImageResource(R.drawable.ic_unchecked)
                    }
                    tvNote.paintFlags =
                        tvNote.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvNote.setTextAppearance(R.style.TextView_Body)
                }
                if (noteItem.deadline != 0L) {
                    tvDoneBefore.text = SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(noteItem.deadline)
                    tvDoneBefore.visibility = View.VISIBLE
                    tvSubHeadDoneBefore.visibility = View.VISIBLE
                }else{
                    tvDoneBefore.visibility = View.GONE
                    tvSubHeadDoneBefore.visibility = View.GONE
                }
            }
        }
    }

    private inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding(FooterItemBinding::bind)
        fun bind(footerNoteData: NoteData.FooterItem) {
            with(binding) {
                etNewNote.doAfterTextChanged {
                    etNewNote.requestFocus()
                    if (it?.let { it.length > 2 } == true) {
                        ibAddNew.visibility = View.VISIBLE
                        ibAddNew.setOnClickListener {
                            footerNoteData.onAddClick(etNewNote.text.toString())
                            etNewNote.text.clear()
                            etNewNote.clearFocus()
                        }
                    } else {
                        ibAddNew.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }


}

