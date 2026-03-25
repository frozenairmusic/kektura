package hu.kektura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hu.kektura.app.ui.map.MapScreen
import hu.kektura.app.ui.settings.SettingsScreen
import hu.kektura.app.ui.stampdetail.StampDetailScreen
import hu.kektura.app.ui.stamps.StampsScreen
import hu.kektura.app.ui.theme.KekturaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KekturaTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomBar = currentDestination?.route?.startsWith("stampDetail") != true

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "stamps" } == true,
                                    onClick = {
                                        navController.navigate("stamps") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "Bélyegzők") },
                                    label = { Text("Bélyegzők") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "map" } == true,
                                    onClick = {
                                        navController.navigate("map") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Map, contentDescription = "Térkép") },
                                    label = { Text("Térkép") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Beállítások") },
                                    label = { Text("Beállítások") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "stamps",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("stamps") { StampsScreen(navController = navController) }
                        composable("map") { MapScreen() }
                        composable("settings") { SettingsScreen() }
                        composable("stampDetail/{segmentId}") { backStackEntry ->
                            val segmentId = backStackEntry.arguments
                                ?.getString("segmentId")?.toIntOrNull() ?: return@composable
                            StampDetailScreen(segmentId = segmentId)
                        }
                    }
                }
            }
        }
    }
}
