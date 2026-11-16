package com.assignment.notificationinbox.data.repository

import android.util.Log
import com.assignment.notificationinbox.data.local.NotificationDao
import com.assignment.notificationinbox.data.local.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer that abstracts data operations for notifications.
 *
 * Implements intelligent duplicate handling:
 * When a notification arrives with an ID that already exists in the database:
 * - It will NOT create a duplicate entry in the database/inbox.
 * - It updates the content fields (title, description, bannerImg) while preserving
 *   the user's read/unread state (`isRead`) and original order.
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    companion object {
        private const val TAG = "NotificationRepository"
    }

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
     * Check if a notification with this ID already exists.
     */
    suspend fun exists(id: String): Boolean =
        notificationDao.existsById(id)

    /**
     * Insert a new notification or handle duplicates gracefully:
     * - If the ID is new: inserts as unread.
     * - If the ID already exists: updates notification fields without overwriting
     *   the user's existing read state, and prevents creating duplicate records.
     */
    suspend fun insertNotification(notification: NotificationEntity) {
        val existing = notificationDao.getNotificationById(notification.id)
        if (existing == null) {
            notificationDao.insertNotification(notification)
            Log.d(TAG, "Inserted new notification: ${notification.id}")
        } else {
            // Duplicate ID detected: update content without resetting user's read state
            val updated = notification.copy(
                isRead = existing.isRead,
                receivedAt = existing.receivedAt
            )
            notificationDao.updateNotification(updated)
            Log.d(TAG, "Duplicate ID detected (${notification.id}): updated record while preserving read state (${existing.isRead})")
        }
    }

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
