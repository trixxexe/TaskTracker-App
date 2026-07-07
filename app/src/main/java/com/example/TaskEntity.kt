package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "Work", "Personal", "Health", "Finance"
    val dueDate: Long, // Epoch millis
    val priority: String, // "Low", "Medium", "High"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
