package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FirestoreSyncState
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.SunGold
import kotlinx.coroutines.delay

/**
 * Reusable Compose Modifier for rendering a smooth shimmer animation over any UI component.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    shimmerColors: List<Color> = listOf(
        Color(0xFFE2E8F0).copy(alpha = 0.6f),
        Color(0xFFFFFFFF).copy(alpha = 0.95f),
        Color(0xFFE2E8F0).copy(alpha = 0.6f)
    ),
    durationMillis: Int = 1200
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 500f, translateAnimation - 500f),
        end = Offset(translateAnimation, translateAnimation)
    )

    this
        .clip(shape)
        .background(brush)
}

/**
 * Dark theme specific shimmer for night / quest mode
 */
fun Modifier.darkShimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    durationMillis: Int = 1200
): Modifier = shimmerEffect(
    shape = shape,
    shimmerColors = listOf(
        Color(0xFF1E293B),
        Color(0xFF334155),
        Color(0xFF1E293B)
    ),
    durationMillis = durationMillis
)

/**
 * Global Floating Sync & Shimmer Feedback Banner.
 * Displays a non-intrusive, animated pill at the top of the screen whenever
 * data syncs with Cloud Firestore or local Room DB.
 */
@Composable
fun GlobalSyncStatusBar(
    syncState: FirestoreSyncState,
    currentLanguage: String = "vi",
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    var showSuccessTemporarily by remember { mutableStateOf(false) }

    // Track when sync finishes successfully to briefly display "Synced"
    LaunchedEffect(syncState.isSyncing) {
        if (!syncState.isSyncing && syncState.errorMessage == null && syncState.isSynced) {
            showSuccessTemporarily = true
            delay(2400)
            showSuccessTemporarily = false
        }
    }

    val isVisible = syncState.isSyncing || showSuccessTemporarily || (syncState.errorMessage != null)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("global_sync_status_bar")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = when {
                syncState.errorMessage != null -> Color(0xFFFEF2F2)
                syncState.isSyncing -> Color(0xFF0F172A).copy(alpha = 0.94f)
                else -> Color(0xFFF0FDF4)
            },
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = when {
                    syncState.errorMessage != null -> Color(0xFFFCA5A5)
                    syncState.isSyncing -> GrabGreen.copy(alpha = 0.6f)
                    else -> GrabGreen.copy(alpha = 0.4f)
                }
            ),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        when {
                            syncState.isSyncing -> {
                                val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")
                                val angle by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "syncSpinAngle"
                                )
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Syncing",
                                    tint = GrabGreen,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(angle)
                                )
                            }
                            syncState.errorMessage != null -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Sync Error",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Synced",
                                    tint = GrabGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = when {
                                    syncState.isSyncing -> when (currentLanguage) {
                                        "en" -> "Syncing data to Cloud..."
                                        "zh" -> "正在同步云端数据..."
                                        "ja" -> "クラウドとデータを同期中..."
                                        "ko" -> "클라우드 데이터 동기화 중..."
                                        else -> "Đang đồng bộ dữ liệu đám mây..."
                                    }
                                    syncState.errorMessage != null -> when (currentLanguage) {
                                        "en" -> "Sync interrupted: Offline mode"
                                        "zh" -> "同步未完成：离线模式"
                                        "ja" -> "同期中断：オフライン保存"
                                        "ko" -> "동기화 중단: 오프라인 저장"
                                        else -> "Lưu trữ ngoại tuyến an toàn"
                                    }
                                    else -> when (currentLanguage) {
                                        "en" -> "Cloud sync up to date ✓"
                                        "zh" -> "云端数据已完全同步 ✓"
                                        "ja" -> "最新データに同期完了 ✓"
                                        "ko" -> "최신 데이터 동기화 완료 ✓"
                                        else -> "Dữ liệu đã đồng bộ an toàn ✓"
                                    }
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    syncState.errorMessage != null -> Color(0xFF991B1B)
                                    syncState.isSyncing -> Color.White
                                    else -> ForestGreen
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (syncState.isSyncing) {
                                Text(
                                    text = when (currentLanguage) {
                                        "en" -> "XP, badges & quest checkpoints"
                                        "zh" -> "经验值、徽章与任务记录"
                                        "ja" -> "XP、バッジ、チェックイン記録"
                                        "ko" -> "XP, 배지 및 체크인 기록"
                                        else -> "XP, huy hiệu & nhật ký check-in"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Retry button for errors
                    if (syncState.errorMessage != null && onRetry != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.1f),
                            modifier = Modifier.clickable { onRetry() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        "en" -> "Retry"
                                        "zh" -> "重试"
                                        "ja" -> "再試行"
                                        "ko" -> "다시 시도"
                                        else -> "Thử lại"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }

                // Bottom Shimmer Scanning Bar when syncing
                if (syncState.isSyncing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .shimmerEffect(
                                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                                shimmerColors = listOf(
                                    GrabGreen.copy(alpha = 0.3f),
                                    SunGold,
                                    GrabGreen.copy(alpha = 0.3f)
                                ),
                                durationMillis = 900
                            )
                    )
                }
            }
        }
    }
}

/**
 * Reusable Card Skeleton with Shimmer effect during data synchronization / initial load.
 */
@Composable
fun ShimmerCardSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .shimmerEffect(shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(16.dp)
                            .shimmerEffect(shape = RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .shimmerEffect(shape = RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .shimmerEffect(shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .shimmerEffect(shape = RoundedCornerShape(4.dp))
            )
        }
    }
}
