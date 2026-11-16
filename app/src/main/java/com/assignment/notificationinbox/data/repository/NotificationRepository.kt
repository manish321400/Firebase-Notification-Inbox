package com.assignment.notificationinbox.data.repository

import com.assignment.notificationinbox.data.local.NotificationDao
import com.assignment.notificationinbox.data.local.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer that abstracts data operations for notifications.
 * Acts as a single source of truth between the DAO and the UI layer.
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    /**
     * Observe all notifications as a Flow, ordered by most recent first.
     */
    fun getAllNotifications(): Flow<List<NotificationEntity>> =
        notificationDao.getAllNotifications()

    /**
     * Get a single notification by its unique ID.
     */
    suspend fun getNotificationById(id: String): NotificationEntity? =
        notificationDao.getNotificationById(id)

    /**
     * Insert a new notification. Duplicates (same ID) are silently ignored.
     */
    suspend fun insertNotification(notification: NotificationEntity) =
        notificationDao.insertNotification(notification)

    /**
     * Mark a specific notification as read.
     */
    suspend fun markAsRead(id: String) =
        notificationDao.markAsRead(id)

    /**
     * Mark all notifications as read.
     */
    suspend fun markAllAsRead() =
        notificationDao.markAllAsRead()

    /**
     * Delete a single notification by ID.
     */
    suspend fun deleteNotification(id: String) =
        notificationDao.deleteNotification(id)

    /**
     * Clear the entire notification inbox.
     */
    suspend fun deleteAllNotifications() =
        notificationDao.deleteAllNotifications()

    /**
     * Observe the count of unread notifications.
     */
    fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()
}
