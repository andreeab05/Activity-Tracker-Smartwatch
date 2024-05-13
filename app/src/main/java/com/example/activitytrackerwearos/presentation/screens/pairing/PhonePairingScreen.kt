package com.example.activitytrackerwearos.presentation.screens.pairing

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.activitytrackerwearos.presentation.Graph

@Composable
fun PhonePairingScreen(
    modifier: Modifier = Modifier,
    sendUID: () -> Unit,
    setLocationMonitor: () -> Unit,
    viewModel: PairingViewModel,
    navController: NavController,
    activity: ComponentActivity
) {
    val pairedStatus by remember {
        viewModel.pairedState
    }
    var buttonPressed by remember {mutableStateOf(false)}
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connect to phone app",
                modifier = Modifier.wrapContentWidth(),
                maxLines = 4
            )
            Button(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    sendUID()
                    viewModel.updatePairingStatus()
                    buttonPressed = true
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.SendToMobile, contentDescription = null)
            }
            if(buttonPressed) {
                if (pairedStatus) {
                    navController.popBackStack()
                    navController.navigate(Graph.HOME)
                } else {
                    Toast.makeText(
                        activity, "Try pairing again!",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }
}