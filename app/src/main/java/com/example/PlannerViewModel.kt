package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    TASKS,
    HABITS,
    INSIGHTS
}

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlannerRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlannerRepository(database.taskDao(), database.habitDao())
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.TASKS)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Raw database states
    val allTasks = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHabits = repository.allHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLogs = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Task filters & Search state
    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

    private val _taskCategoryFilter = MutableStateFlow("All")
    val taskCategoryFilter: StateFlow<String> = _taskCategoryFilter.asStateFlow()

    private val _taskPriorityFilter = MutableStateFlow("All")
    val taskPriorityFilter: StateFlow<String> = _taskPriorityFilter.asStateFlow()

    private val _taskStatusFilter = MutableStateFlow("All")
    val taskStatusFilter: StateFlow<String> = _taskStatusFilter.asStateFlow()

    // Combined filtered tasks flow
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        _taskSearchQuery,
        _taskCategoryFilter,
        _taskPriorityFilter,
        _taskStatusFilter
    ) { tasks, query, category, priority, status ->
        tasks.filter { task ->
            val matchesSearch = task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || task.category == category
            val matchesPriority = priority == "All" || task.priority == priority
            val matchesStatus = when (status) {
                "All" -> true
                "Completed" -> task.isCompleted
                "Pending" -> !task.isCompleted
                else -> true
            }
            matchesSearch && matchesCategory && matchesPriority && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Setters for filters
    fun setTaskSearchQuery(query: String) {
        _taskSearchQuery.value = query
    }

    fun setTaskCategoryFilter(category: String) {
        _taskCategoryFilter.value = category
    }

    fun setTaskPriorityFilter(priority: String) {
        _taskPriorityFilter.value = priority
    }

    fun setTaskStatusFilter(status: String) {
        _taskStatusFilter.value = status
    }

    // Core actions
    fun addTask(title: String, description: String, category: String, dueDate: Long, priority: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    category = category,
                    dueDate = dueDate,
                    priority = priority
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskCompletion(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addHabit(name: String, description: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    colorHex = colorHex
                )
            )
        }
    }

    fun toggleHabitCompletion(habit: HabitEntity) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val dateLong = getYYYYMMDD(calendar)
            val currentLogs = allLogs.value
            repository.toggleHabitCompletion(habit, dateLong, currentLogs)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Date Helper
    fun getYYYYMMDD(cal: Calendar): Long {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return (y * 10000L) + (m * 100L) + d
    }
}
