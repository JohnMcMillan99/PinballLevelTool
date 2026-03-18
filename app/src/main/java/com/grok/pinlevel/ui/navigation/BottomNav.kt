package com.grok.pinlevel.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grok.pinlevel.ui.screens.CalibrationScreen
import com.grok.pinlevel.ui.screens.MachineDetailScreen
import com.grok.pinlevel.ui.screens.MachineListScreen
import com.grok.pinlevel.ui.screens.MainLevelScreen
import com.grok.pinlevel.ui.screens.SettingsScreen
import com.grok.pinlevel.ui.theme.DarkSurface
import com.grok.pinlevel.ui.theme.PinGreen
import com.grok.pinlevel.viewmodel.LevelViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Level : Screen("level", "Level Now", Icons.Filled.Home)
    data object Machines : Screen("machines", "Machines", Icons.AutoMirrored.Filled.List)
    data object MachineDetail : Screen("machine/{machineId}", "Machine", Icons.AutoMirrored.Filled.List) {
        fun createRoute(machineId: String) = "machine/$machineId"
    }
    data object Calibration : Screen("calibration", "Calibrate", Icons.Filled.Build)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

private val screens = listOf(Screen.Level, Screen.Machines, Screen.Calibration, Screen.Settings)

@Composable
fun PinLevelApp(viewModel: LevelViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = DarkSurface) {
                screens.forEach { screen ->
                    val selected = when (screen) {
                        Screen.Machines -> currentDestination?.route == "machines" ||
                                currentDestination?.route?.startsWith("machine/") == true
                        else -> currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinGreen,
                            selectedTextColor = PinGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = DarkSurface
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Level.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Level.route) { MainLevelScreen(viewModel) }
            composable(Screen.Machines.route) {
                MachineListScreen(
                    viewModel = viewModel,
                    onMachineTap = { machine ->
                        navController.navigate(Screen.MachineDetail.createRoute(machine.id))
                    }
                )
            }
            composable(
                route = Screen.MachineDetail.route,
                arguments = listOf(navArgument("machineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val machineId = backStackEntry.arguments?.getString("machineId") ?: return@composable
                val machine = viewModel.state.value.machines.find { it.id == machineId }
                if (machine != null) {
                    MachineDetailScreen(
                        machine = machine,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onLevelThisMachine = {
                            viewModel.selectMachine(machine)
                            navController.navigate(Screen.Level.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable(Screen.Calibration.route) { CalibrationScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
