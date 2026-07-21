package com.tinhome.momreminder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tinhome.momreminder.permissions.PermissionsGate

private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_FORM = "form"
private const val ARG_REMINDER_ID = "reminderId"

class MainActivity : ComponentActivity() {
    private val viewModel: ReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PermissionsGate {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = ROUTE_HOME) {
                            composable(ROUTE_HOME) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onAddClick = { navController.navigate("$ROUTE_FORM/-1") },
                                    onReminderClick = { id -> navController.navigate("$ROUTE_FORM/$id") },
                                    onSettingsClick = { navController.navigate(ROUTE_SETTINGS) }
                                )
                            }
                            composable(
                                route = "$ROUTE_FORM/{$ARG_REMINDER_ID}",
                                arguments = listOf(navArgument(ARG_REMINDER_ID) { type = NavType.LongType })
                            ) { backStackEntry ->
                                val reminderId = backStackEntry.arguments?.getLong(ARG_REMINDER_ID) ?: -1L
                                ReminderFormScreen(
                                    viewModel = viewModel,
                                    reminderId = if (reminderId == -1L) null else reminderId,
                                    onDone = { navController.popBackStack() }
                                )
                            }
                            composable(ROUTE_SETTINGS) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onDone = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
