package com.assignment.notificationinbox.ui.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assignment.notificationinbox.data.local.NotificationEntity
import com.assignment.notificationinbox.ui.components.NotificationItem

/**
 * Notification Inbox screen displaying all received push notifications.
 *
 * Features & Optional Enhancements:
 * - Live unread notification count badge in top bar
 * - Filter chips: All, Unread, Read
 * - Sort order toggle: Newest / Oldest
 * - Mark all as read
 * - Swipe-to-delete with animated exit (Material 3 SwipeToDismissBox)
 * - Clear all inbox notifications with confirmation dialog
 * - Empty state UI for empty inbox or no matches
 * - In-app notification simulator dialog (for quick reviewer evaluation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    onNotificationClick: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val showClearDialog by viewModel.showClearConfirmDialog.collectAsStateWithLifecycle()
    val showSimulator by viewModel.showSimulatorDialog.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InboxTopBar(
                unreadCount = unreadCount,
                hasNotifications = notifications.isNotEmpty(),
                sortOrder = sortOrder,
                scrollBehavior = scrollBehavior,
                onToggleSort = { viewModel.toggleSortOrder() },
                onMarkAllRead = { viewModel.markAllAsRead() },
                onClearInbox = { viewModel.showClearConfirmDialog() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showSimulatorDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Test Notification")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Notification", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips row
            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) },
                unreadCount = unreadCount
            )

            if (notifications.isEmpty()) {
                EmptyInboxState(
                    selectedFilter = selectedFilter,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                NotificationList(
                    notifications = notifications,
                    onNotificationClick = onNotificationClick,
                    onDelete = { viewModel.deleteNotification(it) }
                )
            }
        }
    }

    // Confirmation dialog for clearing entire inbox
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearConfirmDialog() },
            title = { Text("Clear Inbox") },
            text = { Text("Are you sure you want to delete all notifications? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearInbox() }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearConfirmDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Simulator dialog for test notifications
    if (showSimulator) {
        SimulatorDialog(
            onDismiss = { viewModel.dismissSimulatorDialog() },
            onSend = { id, title, desc, img ->
                viewModel.simulateNotification(id, title, desc, img)
                viewModel.dismissSimulatorDialog()
            }
        )
    }
}

@Composable
private fun FilterChipRow(
    selectedFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == FilterOption.ALL,
            onClick = { onFilterSelected(FilterOption.ALL) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selectedFilter == FilterOption.UNREAD,
            onClick = { onFilterSelected(FilterOption.UNREAD) },
            label = {
                Text(if (unreadCount > 0) "Unread ($unreadCount)" else "Unread")
            }
        )
        FilterChip(
            selected = selectedFilter == FilterOption.READ,
            onClick = { onFilterSelected(FilterOption.READ) },
            label = { Text("Read") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxTopBar(
    unreadCount: Int,
    hasNotifications: Boolean,
    sortOrder: SortOrder,
    scrollBehavior: TopAppBarScrollBehavior,
    onToggleSort: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearInbox: () -> Unit
) {
    LargeTopAppBar(
        title = {
            if (unreadCount > 0) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text(text = unreadCount.toString())
                        }
                    }
                ) {
                    Text("Inbox")
                }
            } else {
                Text("Inbox")
            }
        },
        actions = {
            // Sort order toggle
            IconButton(onClick = onToggleSort) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = if (sortOrder == SortOrder.NEWEST_FIRST) "Sort: Newest" else "Sort: Oldest"
                )
            }

            if (hasNotifications) {
                // Mark all as read
                if (unreadCount > 0) {
                    IconButton(onClick = onMarkAllRead) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all as read"
                        )
                    }
                }
                // Clear inbox
                IconButton(onClick = onClearInbox) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear inbox"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun NotificationList(
    notifications: List<NotificationEntity>,
    onNotificationClick: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            SwipeToDeleteItem(
                notification = notification,
                onClick = { onNotificationClick(notification.id) },
                onDelete = { onDelete(notification.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    if (isRemoved) {
        onDelete()
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            content = {
                NotificationItem(
                    notification = notification,
                    onClick = onClick,
                    modifier = Modifier.animateContentSize()
                )
            }
        )
    }
}

@Composable
private fun EmptyInboxState(
    selectedFilter: FilterOption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        val emptyMessage = when (selectedFilter) {
            FilterOption.ALL -> "No notifications yet"
            FilterOption.UNREAD -> "No unread notifications"
            FilterOption.READ -> "No read notifications"
        }
        Text(
            text = emptyMessage,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap 'Test Notification' below to simulate\nincoming notifications or send via Firebase Console.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Dialog enabling evaluators to test all notification scenarios without
 * needing a running Firebase Console session.
 */
@Composable
private fun SimulatorDialog(
    onDismiss: () -> Unit,
    onSend: (id: String, title: String?, desc: String?, img: String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simulate Notification") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select a test scenario to evaluate functional requirements and edge cases:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Scenario 1: Standard notification (from PDF)
                OutlinedButton(
                    onClick = {
                        val id = "notification_${System.currentTimeMillis() % 1000}"
                        onSend(
                            id,
                            "New Notification",
                            "This is a sample notification for the assignment.",
                            "https://picsum.photos/600/300"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("1. Standard Notification (with Image)")
                }

                // Scenario 2: Duplicate ID test
                OutlinedButton(
                    onClick = {
                        onSend(
                            "notification_101",
                            "Duplicate ID Test",
                            "This notification shares ID 'notification_101' to verify duplicate prevention.",
                            "https://picsum.photos/600/301"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("2. Duplicate ID ('notification_101')")
                }

                // Scenario 3: Broken/Invalid Image
                OutlinedButton(
                    onClick = {
                        val id = "notification_broken_${System.currentTimeMillis() % 1000}"
                        onSend(
                            id,
                            "Broken Image Test",
                            "This notification tests graceful error handling when banner image URL is invalid.",
                            "https://invalid-domain.example/does_not_exist.jpg"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("3. Invalid/Broken Banner URL")
                }

                // Scenario 4: Missing Image (null bannerImg)
                OutlinedButton(
                    onClick = {
                        val id = "notification_no_img_${System.currentTimeMillis() % 1000}"
                        onSend(
                            id,
                            "Text-Only Notification",
                            "This notification has no banner image at all.",
                            null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("4. No Banner Image (null)")
                }

                // Scenario 5: Missing Optional Data
                OutlinedButton(
                    onClick = {
                        val id = "notification_minimal_${System.currentTimeMillis() % 1000}"
                        onSend(
                            id,
                            null,
                            null,
                            null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("5. Missing Optional Data (Minimal)")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
