package com.assignment.notificationinbox.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.notificationinbox.data.local.NotificationEntity
import com.assignment.notificationinbox.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FilterOption {
    ALL, UNREAD, READ
}

enum class SortOrder {
    NEWEST_FIRST, OLDEST_FIRST
}

/**
 * ViewModel for the Notification Inbox screen.
 *
 * Supports:
 * - Reactive notification list with filtering (All / Unread / Read)
 * - Sorting (Newest first / Oldest first) offloaded to Dispatchers.Default
 * - Real-time unread count
 * - Mark all as read
 * - Delete individual notifications
 * - Clear all notifications
 * - Test notification generator for evaluator verification
 */
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _showClearConfirmDialog = MutableStateFlow(false)
    val showClearConfirmDialog: StateFlow<Boolean> = _showClearConfirmDialog.asStateFlow()

    private val _showSimulatorDialog = MutableStateFlow(false)
    val showSimulatorDialog: StateFlow<Boolean> = _showSimulatorDialog.asStateFlow()

    /**
     * Filtered and sorted notifications stream, processed on background dispatcher.
     */
    val notifications: StateFlow<List<NotificationEntity>> = combine(
        repository.getAllNotifications(),
        _selectedFilter,
        _sortOrder
    ) { list, filter, sort ->
        val filtered = when (filter) {
            FilterOption.ALL -> list
            FilterOption.UNREAD -> list.filter { !it.isRead }
            FilterOption.READ -> list.filter { it.isRead }
        }

        when (sort) {
            SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.receivedAt }
            SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.receivedAt }
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Live count of unread notifications for badge display.
     */
    val unreadCount: StateFlow<Int> =
        repository.getUnreadCount()
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    fun setFilter(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.NEWEST_FIRST) {
            SortOrder.OLDEST_FIRST
        } else {
            SortOrder.NEWEST_FIRST
        }
    }

    fun showClearConfirmDialog() {
        _showClearConfirmDialog.value = true
    }

    fun dismissClearConfirmDialog() {
        _showClearConfirmDialog.value = false
    }

    fun showSimulatorDialog() {
        _showSimulatorDialog.value = true
    }

    fun dismissSimulatorDialog() {
        _showSimulatorDialog.value = false
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearInbox() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
            _showClearConfirmDialog.value = false
        }
    }

    /**
     * Simulates receiving a notification (for testing and evaluation).
     */
    fun simulateNotification(
        id: String,
        title: String?,
        description: String?,
        bannerImg: String?,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val entity = NotificationEntity(
                id = id,
                title = title,
                description = description,
                bannerImg = bannerImg,
                timestamp = timestamp,
                isRead = false,
                receivedAt = System.currentTimeMillis()
            )
            repository.insertNotification(entity)
        }
    }
}
