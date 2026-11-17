package com.assignment.notificationinbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.assignment.notificationinbox.service.FCMService
import com.assignment.notificationinbox.ui.navigation.AppNavGraph
import com.assignment.notificationinbox.ui.navigation.Screen
import com.assignment.notificationinbox.ui.theme.NotificationInboxTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/**
 * Main entry activity for the Notification Inbox application.
 *
 * Handles:
 * - Runtime notification permission request (Android 13+) with smooth delay to avoid cold-start focus timeout
 * - System notification click routing to Notification Inbox (per Requirement 4)
 * - SingleTop intent updates via onNewIntent
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingNotificationId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract any notification intent data (from system notification click)
        extractNotificationIntent(intent)

        setContent {
            NotificationInboxTheme {
                // Runtime permission request for POST_NOTIFICATIONS on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Notification permission state updated
                }

                LaunchedEffect(Unit) {
                    // Small delay to let initial composition and window focus settle smoothly
                    // Prevents input dispatching FocusEvent timeout during cold start on slower emulators
                    delay(600)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Handle navigation if launched from notification tap
                    LaunchedEffect(pendingNotificationId) {
                        pendingNotificationId?.let {
                            // Per Requirement 4: System Notification -> Notification Inbox
                            // Pop back to Inbox if navigated elsewhere
                            navController.popBackStack(Screen.Inbox.route, inclusive = false)
                            pendingNotificationId = null
                        }
                    }

                    AppNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationIntent(intent)
    }

    private fun extractNotificationIntent(intent: Intent?) {
        val notifId = intent?.getStringExtra(FCMService.EXTRA_NOTIFICATION_ID)
        if (!notifId.isNullOrBlank()) {
            pendingNotificationId = notifId
        }
    }
}
