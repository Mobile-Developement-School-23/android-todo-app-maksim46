package com.example.todoapp.presentation.utils


import androidx.recyclerview.widget.DiffUtil
import com.example.todoapp.domain.model.NoteData

class DUtils: DiffUtil.ItemCallback<NoteData>() {

    override fun areItemsTheSame(oldItem: NoteData, newItem: NoteData): Boolean {

        val isSameItem =
                oldItem is NoteData.ToDoItem
                        && newItem is NoteData.ToDoItem
                        && oldItem.id == newItem.id
        return isSameItem

    }
    override fun areContentsTheSame(oldItem: NoteData, newItem: NoteData): Boolean {
       return   oldItem is NoteData.ToDoItem
                && newItem is NoteData.ToDoItem
                && oldItem.id == newItem.id
                && oldItem.text == newItem.text
                && oldItem.priority == newItem.priority
                && oldItem.deadline == newItem.deadline
                && oldItem.isDone == newItem.isDone
    }
}
