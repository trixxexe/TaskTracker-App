package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request runtime notification permission on Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            MyApplicationTheme {
                PlannerApp()
            }
        }
    }
}

// Keep Greeting Composable to ensure full test/screenshot compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hello $name!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Welcome to your personal productivity dashboard.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PlannerApp() {
    val viewModel: PlannerViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }

    val todayDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TASKS,
                    onClick = { viewModel.navigateTo(AppScreen.TASKS) },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Tasks") },
                    label = { Text("Tasks") },
                    modifier = Modifier.testTag("nav_tasks")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HABITS,
                    onClick = { viewModel.navigateTo(AppScreen.HABITS) },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Habits") },
                    label = { Text("Habits") },
                    modifier = Modifier.testTag("nav_habits")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.INSIGHTS,
                    onClick = { viewModel.navigateTo(AppScreen.INSIGHTS) },
                    icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "Insights") },
                    label = { Text("Insights") },
                    modifier = Modifier.testTag("nav_insights")
                )
            }
        },
        floatingActionButton = {
            if (currentScreen != AppScreen.INSIGHTS) {
                FloatingActionButton(
                    onClick = {
                        if (currentScreen == AppScreen.TASKS) {
                            showAddTaskDialog = true
                        } else {
                            showAddHabitDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add New")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            // Premium Header with custom gradient canvas background
            HeaderSection(title = when(currentScreen) {
                AppScreen.TASKS -> "My Tasks"
                AppScreen.HABITS -> "Daily Habits"
                AppScreen.INSIGHTS -> "Analytics"
            }, subtitle = todayDate)

            // Dynamic screen swap with smooth animations based on enum state
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)).togetherWith(fadeOut(animationSpec = tween(200)))
                },
                label = "screen_transition",
                modifier = Modifier.weight(1f)
            ) { screen ->
                when (screen) {
                    AppScreen.TASKS -> TasksScreen(
                        tasks = tasks,
                        viewModel = viewModel
                    )
                    AppScreen.HABITS -> HabitsScreen(
                        habits = habits,
                        logs = logs,
                        viewModel = viewModel
                    )
                    AppScreen.INSIGHTS -> InsightsScreen(
                        tasks = tasks,
                        habits = habits,
                        logs = logs
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, category, dueDate, priority ->
                viewModel.addTask(title, desc, category, dueDate, priority)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { name, desc, colorHex ->
                viewModel.addHabit(name, desc, colorHex)
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// =================== TASKS TAB ===================

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    viewModel: PlannerViewModel
) {
    val searchQuery by viewModel.taskSearchQuery.collectAsStateWithLifecycle()
    val activeCategory by viewModel.taskCategoryFilter.collectAsStateWithLifecycle()
    val activePriority by viewModel.taskPriorityFilter.collectAsStateWithLifecycle()
    val activeStatus by viewModel.taskStatusFilter.collectAsStateWithLifecycle()
    val currentSortOrder by viewModel.taskSortOrder.collectAsStateWithLifecycle()

    val categories = listOf("All", "Work", "Personal", "Health", "Finance")
    val priorities = listOf("All", "Low", "Medium", "High")
    val statuses = listOf("All" to "All", "Pending" to "Pending", "Completed" to "Completed")

    var quickTaskTitle by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Task Bar (Direct high-speed entry)
        OutlinedTextField(
            value = quickTaskTitle,
            onValueChange = { quickTaskTitle = it },
            placeholder = { Text("Quick add task (press Enter or +)...", fontSize = 13.sp) },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (quickTaskTitle.trim().isNotEmpty()) {
                            viewModel.addTask(
                                title = quickTaskTitle.trim(),
                                description = "",
                                category = "Work",
                                dueDate = Calendar.getInstance().timeInMillis,
                                priority = "Medium"
                            )
                            quickTaskTitle = ""
                        }
                    },
                    enabled = quickTaskTitle.trim().isNotEmpty()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("task_quick_add"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setTaskSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("task_search"),
            placeholder = { Text("Search tasks...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setTaskSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories selector
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == activeCategory
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(containerColor)
                        .clickable { viewModel.setTaskCategoryFilter(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("cat_chip_$cat")
                ) {
                    Text(
                        text = cat,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-filters row (Status and Priority)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Selector
            Column(modifier = Modifier.weight(1f)) {
                Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    statuses.forEach { (label, statusValue) ->
                        val isSel = activeStatus == statusValue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { viewModel.setTaskStatusFilter(statusValue) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Priority Selector
            Column(modifier = Modifier.weight(1f)) {
                Text("Priority", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(priorities) { prio ->
                        val isSel = activePriority == prio
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { viewModel.setTaskPriorityFilter(prio) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                prio,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sort Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Sort by",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            listOf(
                TaskSortOrder.DUE_DATE to "Due Date",
                TaskSortOrder.PRIORITY to "Priority",
                TaskSortOrder.TITLE to "Title",
                TaskSortOrder.MANUAL to "Manual"
            ).forEach { (order, label) ->
                val isSelected = currentSortOrder == order
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerColor)
                        .clickable { viewModel.setTaskSortOrder(order) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tasks list
        if (tasks.isEmpty()) {
            EmptyStateView(
                title = "No Tasks Found",
                message = "Plan your day by adding your very first task using the Add button below!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                    TaskCard(
                        task = task,
                        isManualOrder = currentSortOrder == TaskSortOrder.MANUAL,
                        onMoveUp = if (index > 0) { { viewModel.reorderTasks(tasks, index, index - 1) } } else null,
                        onMoveDown = if (index < tasks.size - 1) { { viewModel.reorderTasks(tasks, index, index + 1) } } else null,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    isManualOrder: Boolean,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var showReminderDialog by remember { mutableStateOf(false) }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Task Reminder") },
            text = { Text("Set a repeating daily local reminder for this task.") },
            confirmButton = {
                Button(
                    onClick = {
                        showReminderDialog = false
                        val timePickerDialog = android.app.TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                ReminderManager.scheduleReminder(
                                    context = context,
                                    id = task.id,
                                    title = "Task Reminder: ${task.title}",
                                    description = task.description.ifEmpty { "Time to work on your task!" },
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    type = "Task"
                                )
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        timePickerDialog.show()
                    }
                ) {
                    Text("Set Time")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReminderDialog = false
                        ReminderManager.cancelReminder(context, task.id, "Task")
                    }
                ) {
                    Text("Cancel Existing", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    val categoryColor = when (task.category) {
        "Work" -> Color(0xFF3F51B5)
        "Personal" -> Color(0xFF4CAF50)
        "Health" -> Color(0xFFE91E63)
        "Finance" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }

    val priorityColor = when (task.priority) {
        "High" -> Color(0xFFEF5350)
        "Medium" -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("task_check_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tags row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            task.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    // Priority Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${task.priority} Priority",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    // Due Date Tag
                    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(task.dueDate))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Due Date",
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            dateStr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (isManualOrder) {
                if (onMoveUp != null) {
                    IconButton(onClick = onMoveUp) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (onMoveDown != null) {
                    IconButton(onClick = onMoveDown) {
                        Icon(
                            Icons.Filled.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(
                onClick = { showReminderDialog = true },
                modifier = Modifier.testTag("task_reminder_${task.id}")
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Set Reminder",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.testTag("task_delete_${task.id}")
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// =================== HABITS TAB ===================

@Composable
fun HabitsScreen(
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    viewModel: PlannerViewModel
) {
    if (habits.isEmpty()) {
        EmptyStateView(
            title = "No Habits Logged",
            message = "Build positive routines by adding your first daily habit!"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                val todayLong = viewModel.getYYYYMMDD(Calendar.getInstance())
                val isCompletedToday = logs.any { it.habitId == habit.id && it.completedDate == todayLong }

                HabitCard(
                    habit = habit,
                    logs = logs,
                    isCompletedToday = isCompletedToday,
                    onToggleCompletion = { viewModel.toggleHabitCompletion(habit) },
                    onToggleDate = { dateVal -> viewModel.toggleHabitCompletion(habit, dateVal) },
                    onDelete = { viewModel.deleteHabit(habit) }
                )
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    isCompletedToday: Boolean,
    onToggleCompletion: () -> Unit,
    onToggleDate: (Long) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var showReminderDialog by remember { mutableStateOf(false) }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Habit Reminder") },
            text = { Text("Set a repeating daily local reminder to stay on track with: ${habit.name}") },
            confirmButton = {
                Button(
                    onClick = {
                        showReminderDialog = false
                        val timePickerDialog = android.app.TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                ReminderManager.scheduleReminder(
                                    context = context,
                                    id = habit.id,
                                    title = "Habit Reminder: ${habit.name}",
                                    description = habit.description.ifEmpty { "Time to complete your daily habit!" },
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    type = "Habit"
                                )
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        timePickerDialog.show()
                    }
                ) {
                    Text("Set Time")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReminderDialog = false
                        ReminderManager.cancelReminder(context, habit.id, "Habit")
                    }
                ) {
                    Text("Cancel Existing", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    val accentColor = Color(android.graphics.Color.parseColor(habit.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive circular completion button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isCompletedToday) accentColor else accentColor.copy(alpha = 0.15f))
                        .clickable { onToggleCompletion() }
                        .border(2.dp, accentColor, CircleShape)
                        .testTag("habit_toggle_${habit.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompletedToday) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Completed Today",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            "GO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (habit.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            habit.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stats / Streak indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🔥 Streak: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            "${habit.streakCount} days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = { showReminderDialog = true },
                    modifier = Modifier.testTag("habit_reminder_${habit.id}")
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Set Reminder",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.testTag("habit_delete_${habit.id}")
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Last 7 Days Interactive Completion Bubbles Row
            val last7Days = remember(logs) {
                (0..6).map { offset ->
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -offset)
                    val dateVal = (cal.get(Calendar.YEAR) * 10000L) + ((cal.get(Calendar.MONTH) + 1) * 100L) + cal.get(Calendar.DAY_OF_MONTH)
                    val letter = SimpleDateFormat("EEEEE", Locale.getDefault()).format(cal.time) // Single-letter day name (e.g. M, T, W, T, F, S, S)
                    Triple(dateVal, letter, offset == 0) // IsToday is true if offset is 0
                }.reversed()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                last7Days.forEach { (dateVal, letter, isToday) ->
                    val isCompleted = logs.any { it.habitId == habit.id && it.completedDate == dateVal }
                    val bubbleColor = if (isCompleted) accentColor else Color.Transparent
                    val textColor = if (isCompleted) Color.White else if (isToday) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    val borderColor = if (isToday) accentColor else accentColor.copy(alpha = 0.3f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onToggleDate(dateVal) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(bubbleColor)
                                .border(1.5.dp, borderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                        }
                        if (isToday) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("•", fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =================== INSIGHTS TAB ===================

@Composable
fun HabitTrendChart(logs: List<HabitLogEntity>, totalHabits: Int) {
    val last7Days = remember(logs) {
        (0..6).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val dateVal = (cal.get(Calendar.YEAR) * 10000L) + ((cal.get(Calendar.MONTH) + 1) * 100L) + cal.get(Calendar.DAY_OF_MONTH)
            val label = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
            val completedCount = logs.count { it.completedDate == dateVal }
            label to completedCount
        }.reversed()
    }

    val maxCompleted = if (totalHabits > 0) totalHabits else 5

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Habit Trend (Last 7 Days)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                last7Days.forEach { (label, count) ->
                    val fraction = if (maxCompleted > 0) count.toFloat() / maxCompleted.toFloat() else 0f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Count badge above the bar
                        Text(
                            text = count.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Visual Bar
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height((80 * fraction.coerceIn(0.01f, 1f)).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsScreen(
    tasks: List<TaskEntity>,
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val taskCompletionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 0f

    val totalHabits = habits.size
    val totalLogs = logs.size
    val averageStreak = if (totalHabits > 0) habits.map { it.streakCount }.average().toFloat() else 0f
    val maxStreak = if (totalHabits > 0) habits.maxOf { it.streakCount } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // High-level statistics summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Monthly Performance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Tasks Completed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$completedTasks / $totalTasks",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Habit Consistency",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                String.format(Locale.getDefault(), "%.1f%%", (if(totalHabits > 0) (totalLogs.toFloat() / (totalHabits * 7)) * 100 else 0f).coerceAtMost(100f)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Peak Streak",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🔥 $maxStreak d",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Habit Trend Bar Chart
        item {
            HabitTrendChart(logs = logs, totalHabits = totalHabits)
        }

        // Custom Donut Chart for Tasks Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Task Completion Ratio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalTasks == 0) {
                        Text(
                            "No task analytics available. Add tasks to see beautiful distribution charts!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            Canvas(modifier = Modifier.size(130.dp)) {
                                // Background circle
                                drawCircle(
                                    color = trackColor,
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 16.dp.toPx())
                                )
                                // Active arc representation
                                drawArc(
                                    color = primaryColor,
                                    startAngle = -90f,
                                    sweepAngle = taskCompletionRate * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx())
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.0f%%", taskCompletionRate * 100),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Completed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            LegendItem(color = primaryColor, label = "Completed ($completedTasks)")
                            LegendItem(color = trackColor, label = "Pending ($pendingTasks)")
                        }
                    }
                }
            }
        }

        // Habit details overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Habit Streaks Tracker",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (habits.isEmpty()) {
                        Text(
                            "No habits available. Create daily routines to track streaks!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        habits.forEach { habit ->
                            val color = Color(android.graphics.Color.parseColor(habit.colorHex))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    habit.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "🔥 ${habit.streakCount} days",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

// =================== DIALOGS ===================

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: String, dueDate: Long, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDaysOffset by remember { mutableStateOf(0) } // 0: Today, 1: Tomorrow, 3: 3 Days, 7: Next Week

    val categories = listOf("Work", "Personal", "Health", "Finance")
    val priorities = listOf("Low", "Medium", "High")
    val offsets = listOf(0 to "Today", 1 to "Tomorrow", 3 to "3 Days", 7 to "1 Week")

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_task_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Add New Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_desc"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // Category Selection
                Column {
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Priority Selection
                Column {
                    Text("Priority", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        priorities.forEach { prio ->
                            val isSel = prio == selectedPriority
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { selectedPriority = prio }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    prio,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Due Date Offset Selection
                Column {
                    Text("Due Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        offsets.forEach { (days, label) ->
                            val isSel = days == selectedDaysOffset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { selectedDaysOffset = days }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isNotEmpty()) {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, selectedDaysOffset)
                                onConfirm(title.trim(), desc.trim(), selectedCategory, cal.timeInMillis, selectedPriority)
                            }
                        },
                        enabled = title.trim().isNotEmpty(),
                        modifier = Modifier.testTag("add_task_confirm")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#4CAF50") } // Default Green

    val colors = listOf(
        "#4CAF50" to "Green",
        "#2196F3" to "Blue",
        "#E91E63" to "Pink",
        "#FF9800" to "Orange",
        "#9C27B0" to "Purple",
        "#00BCD4" to "Teal"
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_habit_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Create Daily Habit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_habit_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (e.g. Drink 8 glasses)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_habit_desc"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Color Selection dots
                Column {
                    Text("Theme Color", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colors.forEach { (hex, _) ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSel = hex == selectedColorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSel) 3.dp else 0.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onConfirm(name.trim(), desc.trim(), selectedColorHex)
                            }
                        },
                        enabled = name.trim().isNotEmpty(),
                        modifier = Modifier.testTag("add_habit_confirm")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
