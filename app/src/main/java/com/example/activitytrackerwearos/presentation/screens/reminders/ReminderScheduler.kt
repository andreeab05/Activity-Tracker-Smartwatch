package com.example.activitytrackerwearos.presentation.screens.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val ID = 100
    private fun createPendingIntent(
        medicineName: String,
        amount: Int,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.NEW_REPEATING_ALARM"
            putExtra("medicineName", medicineName)
            putExtra("amount", amount)
        }

        if (intent != null) {
            Log.d("Alarm", "Intent created")
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(medicineName: String, amount: Int, hour: Int, minute: Int, numDays: Int) {
        val currentTime = Calendar.getInstance()

        for (i in 0 until numDays) {
            val currentDay = Calendar.getInstance()
            currentDay.add(Calendar.DAY_OF_YEAR, i)
            val intent = createPendingIntent(medicineName, amount, i * 1000 + hour * 100 + minute)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentDay.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            // Check if the time is in the past, if so, add a day
            if (calendar.timeInMillis < currentTime.timeInMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

//            alarmManager.setRepeating(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                AlarmManager.INTERVAL_DAY,
//                intent
//            )
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                intent
            )
            Log.d("Alarm", "Scheduled")
        }
    }
}