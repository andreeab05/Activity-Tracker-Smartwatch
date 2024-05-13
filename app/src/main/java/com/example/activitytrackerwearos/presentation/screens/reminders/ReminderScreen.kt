package com.example.activitytrackerwearos.presentation.screens.reminders

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.example.activitytrackerwearos.presentation.theme.DarkerLightOrange
import com.example.activitytrackerwearos.presentation.theme.DarkerLightRed

@Composable
fun ReminderScreen(
    modifier: Modifier = Modifier,
    medicineName: String,
    amount: Int,
    navigateBack: () -> Unit
) {
    val colorAnimation = rememberInfiniteTransition(label = "colorChangingAnimation")

    val color1 by colorAnimation.animateColor(
        initialValue = DarkerLightRed,
        targetValue = DarkerLightOrange,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "colorChanging1"
    )

    val color2 by colorAnimation.animateColor(
        initialValue = DarkerLightOrange,
        targetValue = DarkerLightRed,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "colorChanging2"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(color1, color2)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Medication, contentDescription = null)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = medicineName,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount pill(s)",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            FloatingActionButton(
                onClick = { navigateBack() },
                shape = CircleShape,
                containerColor = Color.White
            ) {
                Icon(Icons.Filled.Close, "Large floating action button", tint = DarkerLightRed)
            }
        }
        // Your alarm screen content goes here
    }
}