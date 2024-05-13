package com.example.activitytrackerwearos.presentation.screens.reminders

import kotlinx.serialization.Serializable

@Serializable
data class MedicationReminderState(
    var medicationName: String = "",
    var days: String = "",
    var months: String = "",
    var amount: String = "1",
    var hour: String = "",
    var minute: String = ""
)