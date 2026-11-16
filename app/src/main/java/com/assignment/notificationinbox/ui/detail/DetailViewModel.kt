package com.assignment.notificationinbox.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.notificationinbox.data.local.NotificationEntity
import com.assignment.notificationinbox.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Notification Detail screen.
 * Loads a single notification by ID and marks it as read automatically.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NotificationRepository
) : ViewModel() {

    private val notificationId: String = savedStateHandle.get<String>("notificationId") ?: ""

    private val _notification = MutableStateFlow<NotificationEntity?>(null)
    val notification: StateFlow<NotificationEntity?> = _notification.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotification()
    }

    private fun loadNotification() {
        viewModelScope.launch {
            try {
                val entity = repository.getNotificationById(notificationId)
                _notification.value = entity

                // Automatically mark as read when opened
                if (entity != null && !entity.isRead) {
                    repository.markAsRead(notificationId)
                    // Update local state to reflect read status
                    _notification.value = entity.copy(isRead = true)
                }
            } catch (e: Exception) {
                _notification.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
