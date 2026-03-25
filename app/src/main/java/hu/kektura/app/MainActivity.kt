package hu.kektura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hu.kektura.app.ui.map.MapScreen
import hu.kektura.app.ui.settings.SettingsScreen
import hu.kektura.app.ui.stampdetail.StampDetailScreen
import hu.kektura.app.ui.stamps.StampsScreen
import hu.kektura.app.ui.theme.KekturaTheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Map      : Screen("map",      "Térkép",       Icons.Default.Map)
    data object Stamps   : Screen("stamps",   "Bélyegzők",    Icons.Default.Approval)
    data object Settings : Screen("settings", "Beállítások",  Icons.Default.Settings)
}

private val bottomScreens = listOf(Screen.Map, Screen.Stamps, Screen.Settings)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { KekturaTheme { KekturaNavHost() } }
    }
}

@Composable
fun KekturaNavHost() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val entry by navController.currentBackStackEntryAsState()
                val current = entry?.destination
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = current?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Stamps.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Map.route)      { MapScreen() }
            composable(Screen.Stamps.route)   { StampsScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                "stampDetail/{segmentId}",
                arguments = listOf(navArgument("segmentId") { type = NavType.IntType })
            ) { back ->
                StampDetailScreen(segmentId = back.arguments?.getInt("segmentId") ?: 0)
            }
        }
    }
}
