package com.example

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PlannerRepository(private val taskDao: TaskDao, private val habitDao: HabitDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    suspend fun insertTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<TaskEntity>) {
        taskDao.insertTasks(tasks)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) {
        taskDao.updateTaskCompletion(id, isCompleted)
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habit: HabitEntity, dateLong: Long, existingLogs: List<HabitLogEntity>) {
        val habitId = habit.id
        val isAlreadyCompleted = existingLogs.any { it.habitId == habitId && it.completedDate == dateLong }

        if (isAlreadyCompleted) {
            // Delete log
            habitDao.deleteLog(habitId, dateLong)
        } else {
            // Insert log
            habitDao.insertLog(HabitLogEntity(habitId = habitId, completedDate = dateLong))
        }

        // Recalculate streak count based on all logs for this habit
        // We need a list of all logged dates for this habit
        val updatedLogs = existingLogs.toMutableList()
        if (isAlreadyCompleted) {
            updatedLogs.removeAll { it.habitId == habitId && it.completedDate == dateLong }
        } else {
            updatedLogs.add(HabitLogEntity(habitId = habitId, completedDate = dateLong))
        }

        val sortedDates = updatedLogs
            .filter { it.habitId == habitId }
            .map { it.completedDate }
            .distinct()
            .sortedDescending()

        val streak = calculateStreak(sortedDates, dateLong)
        val lastCompleted = if (sortedDates.isNotEmpty()) sortedDates.first() else 0L
        habitDao.updateHabitStreak(habitId, streak, lastCompleted)
    }

    private fun calculateStreak(sortedDates: List<Long>, todayDate: Long): Int {
        if (sortedDates.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val today = getYYYYMMDD(calendar)
        
        // Yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = getYYYYMMDD(calendar)

        val latest = sortedDates.first()
        // If the latest logged completion is neither today nor yesterday, streak is broken
        if (latest != today && latest != yesterday) {
            return 0
        }

        var streak = 1
        var currentCheck = latest

        for (i in 1 until sortedDates.size) {
            val prevDate = sortedDates[i]
            if (isConsecutive(currentCheck, prevDate)) {
                streak++
                currentCheck = prevDate
            } else if (prevDate == currentCheck) {
                // Ignore duplicates
                continue
            } else {
                break
            }
        }
        return streak
    }

    private fun isConsecutive(current: Long, prev: Long): Boolean {
        // Simple consecutive checker by converting YYYYMMDD back to Calendar and subtracting a day
        val curCal = getCalendarFromYYYYMMDD(current)
        curCal.add(Calendar.DAY_OF_YEAR, -1)
        val prevCheck = getYYYYMMDD(curCal)
        return prevCheck == prev
    }

    private fun getCalendarFromYYYYMMDD(date: Long): Calendar {
        val year = (date / 10000).toInt()
        val month = ((date % 10000) / 100).toInt() - 1
        val day = (date % 100).toInt()
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 12, 0, 0)
        return cal
    }

    private fun getYYYYMMDD(cal: Calendar): Long {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return (y * 10000L) + (m * 100L) + d
    }
}
