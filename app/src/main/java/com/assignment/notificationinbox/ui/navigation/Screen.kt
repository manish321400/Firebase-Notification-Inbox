package com.assignment.notificationinbox.ui.navigation

/**
 * Defines the navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Inbox : Screen("inbox")
    data object Detail : Screen("detail/{notificationId}") {
        fun createRoute(notificationId: String): String = "detail/$notificationId"
    }
}
