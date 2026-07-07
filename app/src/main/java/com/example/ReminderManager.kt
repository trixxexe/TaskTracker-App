package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.Calendar

object ReminderManager {

    fun scheduleReminder(
        context: Context,
        id: Long,
        title: String,
        description: String,
        hour: Int,
        minute: Int,
        type: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("description", description)
            putExtra("type", type)
        }

        // Use a unique request code for each task or habit using its ID + type offset
        val requestCode = getRequestCode(id, type)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time is in the past, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            val timeFormatted = String.format("%02d:%02d", hour, minute)
            Toast.makeText(
                context,
                "Daily reminder scheduled for $type at $timeFormatted",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to schedule reminder: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun cancelReminder(context: Context, id: Long, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestCode = getRequestCode(id, type)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRequestCode(id: Long, type: String): Int {
        // Offset habits request codes to avoid overlapping IDs between tasks and habits
        val baseId = (id % 100000).toInt()
        return if (type == "Habit") baseId + 100000 else baseId
    }
}
