/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.activitytrackerwearos.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.os.Build
import androidx.annotation.RequiresApi
import android.provider.Settings
import android.util.Log
import com.example.activitytrackerwearos.presentation.screens.pairing.PhonePairingScreen
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.activitytrackerwearos.data.DatastoreRepository
import com.example.activitytrackerwearos.presentation.screens.LocationEventsReceiver
import com.example.activitytrackerwearos.presentation.screens.home.HomeScreen
import com.example.activitytrackerwearos.presentation.screens.pairing.PairingViewModel
import com.example.activitytrackerwearos.presentation.screens.reminders.MedicationReminderState
import com.example.activitytrackerwearos.presentation.screens.reminders.ReminderScheduler
import com.example.activitytrackerwearos.presentation.theme.ActivityTrackerWearOSTheme
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import kotlinx.serialization.json.Json

val DATASTORE_NAME = "settings"

class MainActivity : ComponentActivity() {
    private val MESSAGE_PASSING_CAPABILITY_NAME = "message_passing"
    private val MESSAGE_PASSING_MESSAGE_PATH = "/message_passing"
    private val MESSAGE_PASSING_ACK = "/ack"
    private val MESSAGE_PASSING_REMINDER = "/reminder"
    private var transcriptionNodeId: String? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
    private val latitude: Double = 44.378239
    private val longitude: Double = 26.099380
    private val radius: Float = 100f
    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(this)
    }
    private val pairingKey = booleanPreferencesKey("paired")
    var medicationReminders: List<MedicationReminderState> = mutableListOf()
    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(this) }
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
    private val datastoreRepository: DatastoreRepository by lazy{ DatastoreRepository(dataStore)}

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val context = this

        setTheme(android.R.style.Theme_DeviceDefault)
        messageClient.addListener(messageListener)
        lifecycleScope.launch {
            Log.d("error", "onCreate: launch")
            datastoreRepository.readData(pairingKey).collect { value ->
                if (value != null) {
                    setContent {
                        ActivityTrackerWearOSTheme {
                            Scaffold(
                                timeText = {
                                    TimeText()
                                },
                                vignette = {
                                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                                },
                            ) {
                                TrackerApp(paired = value, setUID = { setupMessagePassing() }, datastoreRepository, context)
                            }
                        }
                    }
                } else {
                    Log.d("error", "aici??")
                    setContent {
                        ActivityTrackerWearOSTheme {
                            Scaffold(
                                timeText = {
                                    TimeText()
                                },
                                vignette = {
                                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                                },
                            ) {
                                TrackerApp(paired = false, setUID = { setupMessagePassing() }, datastoreRepository, context)
                            }
                        }
                    }
                }
            }
        }
    }


    private val messageListener = object : MessageClient.OnMessageReceivedListener {
        override fun onMessageReceived(messageEvent: MessageEvent) {
            if (messageEvent.path == MESSAGE_PASSING_REMINDER) {
                val receivedData = messageEvent.data
                // Handle received data here
                val jsonString = receivedData.toString(Charsets.UTF_8)
                // Decode the JSON string into a list of MedicationReminderState objects
                medicationReminders = Json.decodeFromString(jsonString)
                medicationReminders.forEach { reminder ->
                    var days = 0
                    var months = 0

                    if (reminder.days.isNotEmpty()) {
                        days = reminder.days.toInt()
                    }

                    if (reminder.months.isNotEmpty()) {
                        months = reminder.months.toInt()
                    }
                    val numDays = days + 30 * months
                    val amount = reminder.amount.toInt()
                    val medicationName = reminder.medicationName
                    val hour = reminder.hour.toInt()
                    val minute = reminder.minute.toInt()
                    reminderScheduler.schedule(
                        medicineName = medicationName,
                        amount = amount,
                        hour = hour,
                        minute = minute,
                        numDays = numDays
                    )
                }
                Log.d("MessageReceiver", "Received data: ${receivedData.decodeToString()}")
            } else if (messageEvent.path == MESSAGE_PASSING_ACK) {
                lifecycleScope.launch {
                    datastoreRepository.saveData(pairingKey, true)
                }
            }
        }
    }


    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            // Handle capability changes here
        }

    private fun setupMessagePassing() {
        if (applicationContext == null)
            return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val capabilityInfo: CapabilityInfo = Tasks.await(
                    Wearable.getCapabilityClient(applicationContext)
                        .getCapability(
                            MESSAGE_PASSING_CAPABILITY_NAME,
                            CapabilityClient.FILTER_REACHABLE
                        )
                )
                // capabilityInfo has the reachable nodes with the transcription capability
                updateTranscriptionCapability(capabilityInfo).also {
                    Wearable.getCapabilityClient(applicationContext).addListener(
                        capabilityListener,
                        MESSAGE_PASSING_CAPABILITY_NAME
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTranscriptionCapability(capabilityInfo: CapabilityInfo) {
        transcriptionNodeId = pickBestNodeId(capabilityInfo.nodes)
        val deviceId = getDeviceId(this)
        requestSendingUID(deviceId.toByteArray())
        //Log.d("node", "$transcriptionNodeId")
    }

    private fun pickBestNodeId(nodes: Set<Node>): String {
        // Find a nearby node or pick one arbitrarily.
        return nodes.first().id
    }


    private fun requestSendingUID(uid: ByteArray) {
        transcriptionNodeId?.also { nodeId ->
            val sendTask: Task<*> = Wearable.getMessageClient(applicationContext).sendMessage(
                nodeId,
                MESSAGE_PASSING_MESSAGE_PATH,
                uid
            ).apply {
                addOnSuccessListener { Log.d("msg_passing", "Message sent successfully") }
                addOnFailureListener { exception -> Log.d("msg_passing", "Failed $exception") }
            }
        }
    }

    private fun setGeofence() {
        Log.d("Geofence", "in setGeofence")
        val fineLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationPermissionGranted || !coarseLocationPermissionGranted
        ) {
            Log.d("Geofence", "Has to request permissions")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        requestBackgroundLocationPermission()
    }

    private fun requestBackgroundLocationPermission() {
        val backgroundLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!backgroundLocationPermissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Background location permission already granted, proceed to add geofence
        addGeofence()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        Log.d("Geofence", "In functie")
        val geofence = Geofence.Builder()
            .setRequestId("myGeofenceId")
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        // Create geofencing request
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        // Register geofences
        val geofencingClient: GeofencingClient =
            LocationServices.getGeofencingClient(applicationContext)
        geofencingClient.addGeofences(
            geofencingRequest,
            getGeofencePendingIntent(applicationContext)
        )
            .addOnSuccessListener {
                // Geofences added successfully
                Log.d("Geofence", "Geofence addded")
            }
            .addOnFailureListener { e ->
                Log.d("Geofence", "Geofence failed: ${e.message}")
                e.printStackTrace()
                // Failed to add geofences
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Foreground permissions granted, now request background location permission
                    requestBackgroundLocationPermission()
                } else {
                    // Handle permission denied for foreground permissions
                    Log.d("Permission", "Foreground permission denied")
                }
            }

            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Background location permission granted, proceed to add geofence
                    addGeofence()
                } else {
                    // Handle permission denied for background location
                    Log.d("Permission", "Background location permission denied")
                }
            }
        }

    }

    // Create PendingIntent to handle geofence events
    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationEventsReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}

