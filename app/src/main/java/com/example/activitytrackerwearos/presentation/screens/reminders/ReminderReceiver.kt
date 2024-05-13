package com.example.activitytrackerwearos.presentation.screens.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.NEW_REPEATING_ALARM") {
            Log.d("broadcast", "Merge si pe ceas!!!")
        }
        val reminderIntent = Intent(context, ReminderActivity::class.java)
        reminderIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // Retrieve data from the old intent
        val medicineName = intent.getStringExtra("medicineName")
        val amount = intent.getIntExtra("amount", 1)

        // Add the retrieved data to the new intent
        reminderIntent.putExtra("medicineName", medicineName)
        reminderIntent.putExtra("amount", amount)

        context.startActivity(reminderIntent)
    }
}