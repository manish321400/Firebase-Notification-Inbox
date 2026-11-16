package com.assignment.notificationinbox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the Notification Inbox.
 * Stores all received FCM notifications locally for persistence across app restarts.
 */
@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
