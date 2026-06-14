package com.wodtimer.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Timer : Screen("timer/{mode}?workoutId={workoutId}") {
        fun createRoute(mode: String, workoutId: Long = -1L) = "timer/$mode?workoutId=$workoutId"
    }
    data object WorkoutEditor : Screen("workout_editor/{workoutId}") {
        fun createRoute(workoutId: Long = -1L) = "workout_editor/$workoutId"
    }
    data object History : Screen("history")
    data object Settings : Screen("settings")
}
