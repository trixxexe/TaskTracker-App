package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlannerWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.planner_widget)

        // Set Intent to launch MainActivity when widget clicked
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Set Today's Date
        val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.widget_date, dateStr)

        // Fetch tasks on IO dispatcher
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val taskFlow = db.taskDao().getAllTasks()
                val taskList = taskFlow.first()
                val total = taskList.size
                val completed = taskList.count { it.isCompleted }
                val pending = total - completed

                if (total == 0) {
                    views.setTextViewText(R.id.widget_status, "No Tasks Today")
                    views.setTextViewText(R.id.widget_subtext, "Create a task to get started!")
                    views.setProgressBar(R.id.widget_progress, 100, 0, false)
                } else {
                    views.setTextViewText(R.id.widget_status, "$pending Tasks Pending")
                    views.setTextViewText(
                        R.id.widget_subtext, 
                        "$completed of $total completed today"
                    )
                    val progressPercent = ((completed.toFloat() / total.toFloat()) * 100).toInt()
                    views.setProgressBar(R.id.widget_progress, 100, progressPercent, false)
                }
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_status, "Planner Widget")
                views.setTextViewText(R.id.widget_subtext, "Manage your daily tasks")
                views.setProgressBar(R.id.widget_progress, 100, 0, false)
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
