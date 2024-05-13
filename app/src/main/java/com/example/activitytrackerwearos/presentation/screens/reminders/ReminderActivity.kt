package com.example.activitytrackerwearos.presentation.screens.reminders

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.activitytrackerwearos.presentation.theme.ActivityTrackerWearOSTheme

class ReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val medicineName = intent.getStringExtra("medicineName")
        val amount = intent.getIntExtra("amount", 1)
        setContent {
            ActivityTrackerWearOSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (medicineName != null) {
                        ReminderScreen(medicineName = medicineName, amount = amount, navigateBack = {navigateBack()})
                    }
                }
            }
        }
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(5000)
        }
    }

    private fun navigateBack() {
        finish()
    }
}
