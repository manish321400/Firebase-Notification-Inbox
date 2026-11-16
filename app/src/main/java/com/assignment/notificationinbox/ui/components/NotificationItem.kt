package com.assignment.notificationinbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.assignment.notificationinbox.R
import com.assignment.notificationinbox.data.local.NotificationEntity
import com.assignment.notificationinbox.ui.theme.UnreadBackground
import com.assignment.notificationinbox.ui.theme.UnreadDot
import com.assignment.notificationinbox.util.TimeUtils

/**
 * A single notification item card for the inbox list.
 *
 * Displays:
 * - Banner image (with loading spinner & graceful error/fallback placeholder)
 * - Title (with default fallback for missing title)
 * - Description (with support for multiline or missing description)
 * - Timestamp (human-readable relative format)
 * - Read/Unread state (subtle background tint and indicator dot)
 *
 * Edge cases handled:
 * - Missing/empty banner image: banner area smoothly hidden or fallback displayed
 * - Invalid/broken banner URL: error placeholder shown without crashing
 * - Missing title/description: sensible fallbacks displayed
 * - Long text: cleanly truncated with ellipsis
 */
@Composable
fun NotificationItem(
    notification: NotificationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                UnreadBackground
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 2.dp
        )
    ) {
        Column {
            // Banner image (if available) with robust loading & error states
            if (!notification.bannerImg.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(notification.bannerImg)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Notification banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_broken_image),
                                contentDescription = "Image unavailable",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Unread indicator dot
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(8.dp)
                            .background(UnreadDot, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title with fallback
                    val displayTitle = if (notification.title.isNullOrBlank()) {
                        "Notification"
                    } else {
                        notification.title
                    }

                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description (if present)
                    if (!notification.description.isNullOrBlank()) {
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Timestamp
                    Text(
                        text = TimeUtils.getRelativeTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
