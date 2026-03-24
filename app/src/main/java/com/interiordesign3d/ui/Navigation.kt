package com.interiordesign3d.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.*

// ─── Navigation Routes ────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object Catalog      : Screen("catalog")
    object Measurement  : Screen("measurement")
    object RoomDesigner : Screen("room_designer/{roomId}") {
        fun createRoute(roomId: String) = "room_designer/$roomId"
    }
    object ARView       : Screen("ar_view/{roomId}") {
        fun createRoute(roomId: String) = "ar_view/$roomId"
    }
    object AISuggestions: Screen("ai_suggestions")
    object SavedDesigns : Screen("saved_designs")
    object ColorPicker  : Screen("color_picker/{roomId}") {
        fun createRoute(roomId: String) = "color_picker/$roomId"
    }
    object Settings     : Screen("settings")
    object FurnitureDetail: Screen("furniture_detail/{furnitureId}") {
        fun createRoute(id: String) = "furniture_detail/$id"
    }
}

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home",    Screen.Home.route,
        Icons.Filled.Home,      Icons.Outlined.Home),
    BottomNavItem("Catalog", Screen.Catalog.route,
        Icons.Filled.Chair,     Icons.Outlined.Chair),
    BottomNavItem("Measure", Screen.Measurement.route,
        Icons.Filled.Straighten,Icons.Outlined.Straighten),
    BottomNavItem("Designs", Screen.SavedDesigns.route,
        Icons.Filled.Bookmark,  Icons.Outlined.BookmarkBorder),
    BottomNavItem("AI",      Screen.AISuggestions.route,
        Icons.Filled.AutoAwesome,Icons.Outlined.AutoAwesome)
)

// ─── Main App Scaffold ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteriorDesignNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Catalog.route,
        Screen.Measurement.route, Screen.SavedDesigns.route,
        Screen.AISuggestions.route
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit  = slideOutVertically(targetOffsetY = { it })
            ) {
                InteriorBottomNavBar(navController, currentRoute)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition  = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
            exitTransition   = { fadeOut(tween(200)) },
            popEnterTransition  = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 } },
            popExitTransition   = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 4 } }
        ) {
            composable(Screen.Home.route) {
                com.interiordesign3d.ui.screens.HomeScreen(
                    onNavigateToDesigner = { roomId ->
                        navController.navigate(Screen.RoomDesigner.createRoute(roomId))
                    },
                    onNavigateToAI = { navController.navigate(Screen.AISuggestions.route) },
                    onNavigateToMeasure = { navController.navigate(Screen.Measurement.route) }
                )
            }
            composable(Screen.Catalog.route) {
                com.interiordesign3d.ui.screens.CatalogScreen(
                    onFurnitureClick = { id ->
                        navController.navigate(Screen.FurnitureDetail.createRoute(id))
                    }
                )
            }
            composable(Screen.Measurement.route) {
                com.interiordesign3d.ui.screens.MeasurementScreen(
                    onMeasurementComplete = { width, length, height ->
                        navController.navigate(Screen.Home.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.RoomDesigner.route,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                com.interiordesign3d.ui.screens.RoomDesignerScreen(
                    roomId = roomId,
                    onNavigateToAR = { navController.navigate(Screen.ARView.createRoute(roomId)) },
                    onNavigateToColorPicker = { navController.navigate(Screen.ColorPicker.createRoute(roomId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.ARView.route,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                com.interiordesign3d.ui.screens.ARViewScreen(
                    roomId = roomId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AISuggestions.route) {
                com.interiordesign3d.ui.screens.AISuggestionsScreen(
                    onApplySuggestion = { roomId ->
                        navController.navigate(Screen.RoomDesigner.createRoute(roomId))
                    }
                )
            }
            composable(Screen.SavedDesigns.route) {
                com.interiordesign3d.ui.screens.SavedDesignsScreen(
                    onDesignClick = { roomId ->
                        navController.navigate(Screen.RoomDesigner.createRoute(roomId))
                    }
                )
            }
            composable(
                route = Screen.ColorPicker.route,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                com.interiordesign3d.ui.screens.ColorPickerScreen(
                    roomId = roomId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.FurnitureDetail.route,
                arguments = listOf(navArgument("furnitureId") { type = NavType.StringType })
            ) { backStackEntry ->
                val furnitureId = backStackEntry.arguments?.getString("furnitureId") ?: ""
                com.interiordesign3d.ui.screens.FurnitureDetailScreen(
                    furnitureId = furnitureId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ─── Bottom Nav Bar Component ─────────────────────────────────────────────────

@Composable
fun InteriorBottomNavBar(navController: NavController, currentRoute: String?) {
    NavigationBar(tonalElevation = 8.dp) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
