/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.activitytrackerwearos.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.activitytrackerwearos.presentation.theme.ActivityTrackerWearOSTheme
import android.provider.Settings
import android.util.Log
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.activitytrackerwearos.presentation.screens.PhonePairingScreen
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val MESSAGE_PASSING_CAPABILITY_NAME = "message_passing"
    private val MESSAGE_PASSING_MESSAGE_PATH = "/message_passing"
    private var transcriptionNodeId: String? = null

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp { setupMessagePassing() }
        }
    }

    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private val capabilityListener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
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
}

@Composable
fun WearApp(sendUID: () -> Unit) {

    ActivityTrackerWearOSTheme {
        Scaffold(
            timeText = {
                TimeText()
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
        ) {
            PhonePairingScreen(sendUID = sendUID)
        }
    }
}

