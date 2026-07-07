package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String, // Hex color for habit styling
    val streakCount: Int = 0,
    val lastCompletedDate: Long = 0, // Format YYYYMMDD to compare days easily
    val createdAt: Long = System.currentTimeMillis()
)
