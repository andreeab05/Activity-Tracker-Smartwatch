package com.example.activitytrackerwearos.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.activitytrackerwearos.data.DatastoreRepository
import com.example.activitytrackerwearos.presentation.screens.home.HomeScreen
import com.example.activitytrackerwearos.presentation.screens.pairing.PairingViewModel
import com.example.activitytrackerwearos.presentation.screens.pairing.PhonePairingScreen

object Graph {
    const val ROOT = "root_graph"
    const val PAIRING = "pairing_graph"
    const val HOME = "home_graph"
}

@Composable
fun TrackerApp(
    paired: Boolean,
    setUID: () -> Unit,
    datastoreRepository: DatastoreRepository,
    activity: ComponentActivity
    ) {
    val navController = rememberNavController()
    val startDestination = if (paired) {  //Graph.AUTHENTICATION
        Graph.HOME
    } else {
        Graph.PAIRING
    }
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = startDestination
    ) {
        navigation(
            startDestination = ScreenRoutes.Pairing.route, route = Graph.PAIRING
        ) {
            composable(ScreenRoutes.Pairing.route) { PhonePairingScreen(
                sendUID = setUID,
                setLocationMonitor = { /*TODO*/ },
                viewModel = PairingViewModel(datastoreRepository = datastoreRepository),
                navController = navController,
                activity = activity
            ) }
        }

        navigation(
            startDestination = ScreenRoutes.Home.route, route = Graph.HOME
        ) {
            composable(ScreenRoutes.Home.route) {
                HomeScreen(navController = navController)
            }
        }

    }
}