package com.example.activitytrackerwearos.presentation.screens

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LocationEventsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == "com.example.NEW_REPEATING_ALARM"){
            Log.d("broadcast", "blabla")
        }

    }
}