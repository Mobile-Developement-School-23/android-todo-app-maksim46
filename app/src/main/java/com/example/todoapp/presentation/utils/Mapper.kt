package com.example.todoapp.presentation.utils

import com.example.todoapp.data.database.ToDoListDbModel
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity

fun ToDoEntity.toNoteData(): NoteData.ToDoItem {
    return NoteData.ToDoItem(
        id = this.id,
        text = this.text,
        priority = this.priority,
        deadline = this.deadline,
        isDone = this.isDone,
        createDate = this.createDate,
        updateDate = this.updateDate
    )
}

fun NoteData.toEntity(): ToDoEntity {
    return ToDoEntity(
        id = (this as NoteData.ToDoItem).id,
        text = (this as NoteData.ToDoItem).text,
        priority = (this as NoteData.ToDoItem).priority,
        deadline = (this as NoteData.ToDoItem).deadline,
        isDone = (this as NoteData.ToDoItem).isDone,
        createDate = (this as NoteData.ToDoItem).createDate,
        updateDate = (this as NoteData.ToDoItem).updateDate,
    )
}

fun ToDoEntity.toDbModel(): ToDoListDbModel {
    return ToDoListDbModel(
        id = this.id.toInt(),
        text = this.text,
        priority = choosePriority(this.priority),
        deadline = this.deadline,
        isDone = this.isDone,
        createDate = this.createDate,
        updateDate =this.updateDate
    )
}

fun ToDoListDbModel.toEntity(): ToDoEntity {
    return ToDoEntity(
        id = this.id.toString(),
        text = this.text,
        priority = choosePriorityByInt(this.priority),
        deadline = this.deadline,
        isDone = this.isDone,
        createDate = this.createDate,
        updateDate = this.updateDate
    )

}

fun List<ToDoListDbModel>.toListOfToDoEntyty(): List<ToDoEntity> {
    return map { it.toEntity() }
}

fun List<ToDoEntity>.toListOfNoteData(): List<NoteData> {
    return map { it.toNoteData() }
}

fun choosePriority(priority: Priority): Int {
    return when (priority) {
        Priority.Low -> Priority.Low.value
        Priority.Standart -> Priority.Standart.value
        Priority.High -> Priority.High.value
    }
}

fun choosePriorityByInt(value: Int): Priority {
    return when (value) {
        Priority.Low.value -> Priority.Low
        Priority.High.value -> Priority.High
        else -> {
            Priority.Standart
        }
    }
}

