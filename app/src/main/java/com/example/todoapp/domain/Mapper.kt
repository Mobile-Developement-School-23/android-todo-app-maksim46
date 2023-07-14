package com.example.todoapp.domain

import androidx.room.TypeConverter
import com.example.todoapp.data.database.ToDoListDbModel
import com.example.todoapp.data.network.model.ToDoDtoModel
import com.example.todoapp.data.network.model.ToDoPayload
import com.example.todoapp.domain.model.NoteData
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.ToDoEntity
import java.util.Date
/**
 * Provides some utility mapper functions
 */

val uniqueID = GetUuid().getDeviceUUID()

fun ToDoDtoModel.toPayload(): ToDoPayload {
    return ToDoPayload(element = this)
}

fun ToDoDtoModel.toEntity(): ToDoEntity {
    return ToDoEntity(
        id = id,
        text = text,
        priority = importanceToEntityPriority(importance),
        deadline = deadline,
        isDone = done,
        createDate = toTimeDate(createdAt),
        updateDate = toTimeDate(changedAt)
    )
}

fun ToDoEntity.toDtoModel() = ToDoDtoModel(
    id = id,
    text = text,
    importance = priorityToDtoModelImportance(priority),
    done = isDone,
    deadline = deadline,
    createdAt = toUnixTime(createDate),
    changedAt = toUnixTime(updateDate),
    lastUpdatedBy = uniqueID
)

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
        updateDate = this.updateDate
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

fun priorityToDtoModelImportance(priority: Priority): String {
    return when (priority) {
        Priority.Low -> "low"
        Priority.Standart -> "basic"
        else -> "important"
    }
}

fun importanceToEntityPriority(importance: String): Priority {
    return when (importance) {
        "low" -> Priority.Low
        "basic" -> Priority.Standart
        else -> Priority.High
    }
}

@TypeConverter
fun toUnixTime(date: Date): Long {
    if (date.time == 0L) return 0
    return date.time / 1000
}

@TypeConverter
fun toTimeDate(time: Long): Date {
    if (time == 0L) return Date(0)
    return Date(time * 1000)
}


fun Int.pixToDp(density: Float ): Int {
   return (this * density).toInt()
}