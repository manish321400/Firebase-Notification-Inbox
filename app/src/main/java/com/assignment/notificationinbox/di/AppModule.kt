package com.assignment.notificationinbox.di

import android.content.Context
import androidx.room.Room
import com.assignment.notificationinbox.data.local.AppDatabase
import com.assignment.notificationinbox.data.local.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide the Room database instance as a singleton.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notification_inbox_db"
        ).build()

    /**
     * Provide the NotificationDao from the database.
     */
    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao =
        database.notificationDao()
}
