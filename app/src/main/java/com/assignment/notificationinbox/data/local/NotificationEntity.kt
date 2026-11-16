package com.assignment.notificationinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a notification stored in the local inbox.
 *
 * @param id Unique identifier from the FCM payload (e.g., "notification_101").
 * @param title Notification title (nullable for edge-case handling).
 * @param description Notification body text (nullable for edge-case handling).
 * @param bannerImg URL of the banner image (nullable — missing/invalid images are handled gracefully).
 * @param timestamp Epoch millis from the payload, used for display.
 * @param isRead Whether the user has opened this notification.
 * @param receivedAt Epoch millis when this notification was stored locally, used for ordering.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val bannerImg: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val receivedAt: Long = System.currentTimeMillis()
)
