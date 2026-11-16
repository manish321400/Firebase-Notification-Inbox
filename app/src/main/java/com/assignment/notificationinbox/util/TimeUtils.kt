package com.assignment.notificationinbox.util

import java.util.concurrent.TimeUnit

/**
 * Utility for formatting timestamps into human-readable relative time strings.
 */
object TimeUtils {

    /**
     * Converts an epoch timestamp (in milliseconds) to a relative time string.
     * Examples: "Just now", "5 min ago", "2 hours ago", "Yesterday", "3 days ago",
     *           "Nov 15, 2024"
     */
    fun getRelativeTime(timestampMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestampMillis

        if (diff < 0) return formatDate(timestampMillis)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes} min ago"
            hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> formatDate(timestampMillis)
        }
    }

    /**
     * Formats an epoch timestamp to a readable date string (e.g., "Nov 15, 2024").
     */
    fun formatDate(timestampMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestampMillis))
    }

    /**
     * Formats an epoch timestamp to a full date-time string (e.g., "Nov 15, 2024 at 3:45 PM").
     */
    fun formatDateTime(timestampMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestampMillis))
    }
}
