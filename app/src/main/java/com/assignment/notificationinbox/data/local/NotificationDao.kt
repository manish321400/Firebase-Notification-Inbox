package com.assignment.notificationinbox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notification operations.
 * Provides full CRUD operations and duplicate-safe insertion.
 */
@Dao
interface NotificationDao {

    /**
     * Observe all notifications ordered by received time (newest first).
     */
    @Query("SELECT * FROM notifications ORDER BY receivedAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    /**
     * Get a single notification by its unique ID.
     */
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    /**
     * Check if a notification with the given ID already exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM notifications WHERE id = :notificationId)")
    suspend fun existsById(notificationId: String): Boolean

    /**
     * Insert a notification. If conflict, ignore (repository handles smart upsert).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    /**
     * Update an existing notification.
     */
    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    /**
     * Mark a specific notification as read.
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    /**
     * Mark all notifications as read.
     */
    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    /**
     * Delete a specific notification by ID.
     */
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)

    /**
     * Delete all notifications (clear inbox).
     */
    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    /**
     * Get count of unread notifications (observed as Flow for live updates).
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}
