package com.assignment.notificationinbox.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.notificationinbox.data.local.NotificationEntity
import com.assignment.notificationinbox.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Notification Inbox screen.
 * Exposes reactive notification list and unread count, and handles user actions.
 */
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    /**
     * All notifications, observed as a StateFlow for Compose.
     */
    val notifications: StateFlow<List<NotificationEntity>> =
        repository.getAllNotifications()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Count of unread notifications for badge display.
     */
    val unreadCount: StateFlow<Int> =
        repository.getUnreadCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    /**
     * Mark all notifications as read.
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    /**
     * Delete a single notification.
     */
    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    /**
     * Clear the entire inbox.
     */
    fun clearInbox() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }
}
