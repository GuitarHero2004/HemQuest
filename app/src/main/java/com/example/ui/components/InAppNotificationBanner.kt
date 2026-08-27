package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink900
import com.example.ui.theme.SunGold
import com.example.util.InAppNotification
import kotlinx.coroutines.delay

@Composable
fun InAppNotificationBanner(
    notification: InAppNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (notification != null) {
        LaunchedEffect(notification.id) {
            delay(4500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        ),
        modifier = modifier
    ) {
        if (notification != null) {
            val bgBrush = if (notification.isLevelUp) {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF78350F),
                        Color(0xFFD97706),
                        Color(0xFFB45309)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0F291E),
                        Color(0xFF1E5B3A),
                        Color(0xFF0F291E)
                    )
                )
            }

            val borderColor = if (notification.isLevelUp) SunGold else GrabGreen

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, borderColor),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(top = 44.dp, bottom = 8.dp)
                    .clickable { onDismiss() }
                    .testTag("in_app_notification_banner")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgBrush)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Badge Icon
                        Surface(
                            shape = CircleShape,
                            color = if (notification.isLevelUp) SunGold.copy(alpha = 0.3f) else GrabGreen.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = notification.iconEmoji,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Body text
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = notification.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (notification.isLevelUp) Color(0xFFFEF3C7) else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (notification.xpEarned > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SunGold.copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, SunGold)
                                    ) {
                                        Text(
                                            text = "+${notification.xpEarned} XP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SunGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = notification.message,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
