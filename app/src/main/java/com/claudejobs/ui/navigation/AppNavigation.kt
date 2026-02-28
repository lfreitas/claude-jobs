package com.claudejobs.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.claudejobs.ui.screens.*

private sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Tasks   : Screen("tasks",   "Tasks",   Icons.Default.List)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings: Screen("settings","Settings",Icons.Default.Settings)
}

private val bottomScreens = listOf(Screen.Tasks, Screen.History, Screen.Settings)

@Composable
fun AppNavigation(openResultId: Long = -1L) {
    val navController = rememberNavController()

    // Deep-link from notification: navigate to result detail once
    LaunchedEffect(openResultId) {
        if (openResultId > 0L) {
            navController.navigate("result_detail/$openResultId")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Tasks.route) {
                TaskListScreen(
                    onAddTask = { navController.navigate("task_form/-1") },
                    onEditTask = { id -> navController.navigate("task_form/$id") }
                )
            }
            composable(
                route = "task_form/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { backStack ->
                AddEditTaskScreen(
                    taskId = backStack.arguments!!.getLong("taskId"),
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onOpenResult = { id -> navController.navigate("result_detail/$id") }
                )
            }
            composable(
                route = "result_detail/{resultId}",
                arguments = listOf(navArgument("resultId") { type = NavType.LongType })
            ) { backStack ->
                ResultDetailScreen(
                    resultId = backStack.arguments!!.getLong("resultId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
