package com.example.todoapp.presentation.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.presentation.utils.DUtils

class RVListAdapter (
    private val viewModel: MainFragmentViewModel,
    articleDiffCalculator: DUtils,
    ) : ListAdapter<NoteData, RecyclerView.ViewHolder>(AsyncDifferConfig.Builder(DUtils()).build()) {

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
            R.layout.top_note_item -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.top_note_item, parent, false)
                ItemViewHolder(view)
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
        return if (position == 0 && currentList[position] is NoteData.ToDoItem) {
            R.layout.top_note_item
        } else {
            when (currentList[position]) {
                is NoteData.ToDoItem -> R.layout.note_item
                is NoteData.FooterItem -> R.layout.footer_item
                else -> {
                    throw IllegalArgumentException("Invalid ViewType Provided")
                }
            }
        }
    }
}