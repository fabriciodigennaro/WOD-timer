package com.wodtimer.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.presentation.history.HistoryScreen
import com.wodtimer.app.presentation.home.HomeScreen
import com.wodtimer.app.presentation.settings.SettingsScreen
import com.wodtimer.app.presentation.timer.TimerScreen
import com.wodtimer.app.presentation.workout.WorkoutEditorScreen

@Composable
fun WODNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { it / 4 }
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { -it / 4 }
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { -it / 4 }
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { it / 4 }
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTimer = { mode, workoutId ->
                    navController.navigate(Screen.Timer.createRoute(mode, workoutId))
                },
                onNavigateToWorkoutEditor = { id ->
                    navController.navigate(Screen.WorkoutEditor.createRoute(id))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("workoutId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "FOR_TIME"
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: -1L
            TimerScreen(
                mode = mode,
                workoutId = workoutId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WorkoutEditor.route,
            arguments = listOf(navArgument("workoutId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: -1L
            WorkoutEditorScreen(
                workoutId = workoutId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWorkout = { id ->
                    navController.navigate(Screen.WorkoutEditor.createRoute(id))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
