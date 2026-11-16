package com.assignment.notificationinbox.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.assignment.notificationinbox.MainActivity
import com.assignment.notificationinbox.R
import com.assignment.notificationinbox.data.local.NotificationEntity
import com.assignment.notificationinbox.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service that handles incoming push notifications.
 *
 * Responsibilities:
 * - Receive Firebase push notifications
 * - Store notifications in the local Room database
 * - Display Android system notifications (Channel: "assignment" / "Assignment")
 * - Handle duplicate notification IDs gracefully
 * - Graceful fallback when banner image is missing or invalid
 */
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var repository: NotificationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "assignment"
        const val CHANNEL_NAME = "Assignment"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Called when a new FCM message is received.
     * Handles both data payloads and notification payloads.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // 1. Extract data payload
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d(TAG, "Data payload: $data")
            handleDataMessage(data)
            return
        }

        // 2. Fallback: handle notification payload (e.g. from Firebase Console tester)
        remoteMessage.notification?.let { notification ->
            val fallbackData = mutableMapOf<String, String>()
            fallbackData["id"] = remoteMessage.messageId ?: "notif_${System.currentTimeMillis()}"
            notification.title?.let { fallbackData["title"] = it }
            notification.body?.let { fallbackData["description"] = it }
            notification.imageUrl?.let { fallbackData["bannerImg"] = it.toString() }
            fallbackData["timestamp"] = System.currentTimeMillis().toString()

            handleDataMessage(fallbackData)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: $token")
    }

    /**
     * Parse and store notification payload, then show system notification.
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val id = data["id"]
        if (id.isNullOrBlank()) {
            Log.w(TAG, "Received notification payload missing 'id'. Discarding.")
            return
        }

        val title = data["title"]
        val description = data["description"]
        val bannerImg = data["bannerImg"]
        val timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

        val entity = NotificationEntity(
            id = id,
            title = title,
            description = description,
            bannerImg = bannerImg,
            timestamp = timestamp,
            isRead = false,
            receivedAt = System.currentTimeMillis()
        )

        serviceScope.launch {
            try {
                // Insert into local Room database (duplicates handled by DAO)
                repository.insertNotification(entity)

                // Download banner bitmap if available, falling back gracefully
                val bannerBitmap = if (!bannerImg.isNullOrBlank()) {
                    downloadBitmap(bannerImg)
                } else null

                // Display system notification
                showSystemNotification(entity, bannerBitmap)
                Log.d(TAG, "Notification stored and displayed successfully: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification message: $id", e)
            }
        }
    }

    /**
     * Display the Android system notification on the 'assignment' channel.
     * Tapping opens MainActivity which displays the Notification Inbox.
     */
    private fun showSystemNotification(notification: NotificationEntity, bannerBitmap: Bitmap?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title ?: "New Notification")
            .setContentText(notification.description ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // If banner image was successfully downloaded, attach BigPictureStyle
        if (bannerBitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bannerBitmap)
                    .setSummaryText(notification.description ?: "")
            )
        } else if (!notification.description.isNullOrBlank()) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notification.description)
            )
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(notification.id.hashCode(), builder.build())
    }

    /**
     * Safely downloads a bitmap from URL with timeout.
     * Returns null on any failure (network error, invalid URL, 404, etc.)
     * ensuring that an invalid banner image never prevents the notification from being displayed.
     */
    private suspend fun downloadBitmap(urlStr: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to download banner image from $urlStr: ${e.message}")
            null
        }
    }

    /**
     * Creates notification channel required for Android O (API 26) and above.
     * Channel ID: assignment
     * Channel Name: Assignment
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification Inbox notifications"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
