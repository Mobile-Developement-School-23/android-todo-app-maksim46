package com.example.todoapp.presentation.view

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.todoapp.databinding.FooterItemBinding
import com.example.todoapp.domain.model.NoteData
const val THRESHOLD = 2

/**
 * RecyclerView.FooterViewHolder implementation for [RVListAdapter].
 */
class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding by viewBinding(FooterItemBinding::bind)
    fun bind(footerNoteData: NoteData.FooterItem) {
        with(binding) {
            etNewNote.doAfterTextChanged {
                etNewNote.requestFocus()
                if (it?.let { it.length > THRESHOLD } == true) {
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