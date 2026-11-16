package com.assignment.notificationinbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.assignment.notificationinbox.ui.detail.DetailScreen
import com.assignment.notificationinbox.ui.detail.DetailViewModel
import com.assignment.notificationinbox.ui.inbox.InboxScreen
import com.assignment.notificationinbox.ui.inbox.InboxViewModel

/**
 * Navigation graph for the app.
 *
 * Flow: Inbox -> Detail
 * Supports deep-linking from system notification taps via [startNotificationId].
 *
 * @param navController The NavHostController managing navigation.
 * @param startNotificationId Optional notification ID to navigate to directly (from system notification tap).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startNotificationId: String? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inbox.route
    ) {
        // Inbox screen
        composable(route = Screen.Inbox.route) {
            val viewModel: InboxViewModel = hiltViewModel()
            InboxScreen(
                viewModel = viewModel,
                onNotificationClick = { notificationId ->
                    navController.navigate(Screen.Detail.createRoute(notificationId))
                }
            )
        }

        // Detail screen
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("notificationId") { type = NavType.StringType }
            )
        ) {
            val viewModel: DetailViewModel = hiltViewModel()
            DetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
