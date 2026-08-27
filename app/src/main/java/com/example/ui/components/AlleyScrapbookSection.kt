package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.Quest
import com.example.model.QuestStop
import com.example.model.StopStatus
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.util.l

@Composable
fun AlleyScrapbookSection(
    quests: List<Quest>,
    currentLanguage: String
) {
    val completedStops = remember(quests) {
        quests.flatMap { q -> q.stops.filter { it.status == StopStatus.COMPLETED } }
    }

    var selectedStopForStampDialog by remember { mutableStateOf<QuestStop?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("alley_scrapbook_section")
    ) {
        // Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = l(
                            currentLanguage,
                            "Sổ Passport & Con Dấu Hẻm",
                            "Alley Stamp Passport",
                            "巷弄印章护照",
                            "路地スタンプパスポート",
                            "골목 스탬프 여권"
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink900
                    )
                    Text(
                        text = l(
                            currentLanguage,
                            "Bộ sưu tập con dấu di sản đã mở khóa",
                            "Unlocked heritage achievement stamps",
                            "已解锁的文化遗产印章成就",
                            "解除されたヘリテージスタンプ",
                            "해금된 헤리티지 스탬프 업적"
                        ),
                        fontSize = 11.sp,
                        color = Ink600
                    )
                }
            }

            Surface(
                color = ForestGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(100.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${completedStops.size} ${l(currentLanguage, "Dấu", "Stamps", "枚", "個", "개")}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }
            }
        }

        if (completedStops.isEmpty()) {
            // Empty State: Digitized Passport Book Preview with locked stamp slots
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Sổ Passport Hẻm Đang Trống",
                                    "Passport Book Available",
                                    "巷弄护照尚未盖章",
                                    "パスポート帳はまだ空です",
                                    "골목 여권 스탬프가 비어있습니다"
                                ),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Hoàn thành trạm dừng để nhận con dấu di sản độc bản!",
                                    "Complete stops to unlock digital heritage stamps & badges!",
                                    "完成打卡点即可解锁专属数字遗产印章成就！",
                                    "チェックポイントを完了してデジタルスタンプをゲット！",
                                    "체크포인트를 완료하고 디지털 헤리티지 스탬프를 해금하세요!"
                                ),
                                fontSize = 12.sp,
                                color = Ink600,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Locked Stamp Placeholders Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF9FAFB))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFD1D5DB),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${l(currentLanguage, "Dấu", "Stamp", "印章", "スタンプ", "스탬프")} #${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Active Stamps Grid/Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(completedStops) { stop ->
                    DigitalStampCard(
                        stop = stop,
                        currentLanguage = currentLanguage,
                        onClick = { selectedStopForStampDialog = stop }
                    )
                }
            }
        }
    }

    // Detail Stamp Dialog Modal
    selectedStopForStampDialog?.let { stop ->
        StampCertificateDialog(
            stop = stop,
            currentLanguage = currentLanguage,
            onDismiss = { selectedStopForStampDialog = null }
        )
    }
}

@Composable
fun DigitalStampCard(
    stop: QuestStop,
    currentLanguage: String,
    onClick: () -> Unit
) {
    val categoryIcon = getCategoryIcon(stop.category)
    val stampThemeColors = getStampColors(stop.category)

    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Stamp Header Seal Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PASSED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = stampThemeColors.first,
                    modifier = Modifier
                        .background(
                            stampThemeColors.first.copy(alpha = 0.12f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Central Iconic Stamp Badge Emblem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Outer Dashed Circle Effect
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(stampThemeColors.second)
                        .border(2.dp, stampThemeColors.first, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(stampThemeColors.first, stampThemeColors.first.copy(alpha = 0.85f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = stop.name,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Diagonal Rubber Postmark Watermark overlay
                Box(
                    modifier = Modifier
                        .rotate(-15f)
                        .background(
                            Color(0xDD111827),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "HẺM STAMP",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047)
                    )
                }
            }

            // Landmark Details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stop.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stop.category,
                    fontSize = 11.sp,
                    color = Ink600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom XP / Tap clue label
            Text(
                text = "+100 XP • ${l(currentLanguage, "Xem Bằng Chứng", "View Badge", "查看勋章", "詳細を見る", "뱃지 보기")}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = stampThemeColors.first
            )
        }
    }
}

@Composable
fun StampCertificateDialog(
    stop: QuestStop,
    currentLanguage: String,
    onDismiss: () -> Unit
) {
    val categoryIcon = getCategoryIcon(stop.category)
    val stampThemeColors = getStampColors(stop.category)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = stampThemeColors.first,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = l(currentLanguage, "CHỨNG NHẬN CON DẤU", "HERITAGE STAMP SEAL", "遗产印章证书", "ヘリテージスタンプ証明", "헤리티지 스탬프 증명"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = stampThemeColors.first,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Ink600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Large Glowing Iconic Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(stampThemeColors.second)
                        .border(3.dp, stampThemeColors.first, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(stampThemeColors.first, stampThemeColors.first.copy(alpha = 0.85f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = stop.name,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stop Name
                Text(
                    text = stop.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink900,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stop.category,
                    fontSize = 13.sp,
                    color = stampThemeColors.first,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Verification Stamp Info Box
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Đã Xác Thực Bởi AI Camera HẻmQuest",
                                    "Verified by HẻmQuest AI Camera",
                                    "已通过 HẻmQuest AI 相机验证",
                                    "HẻmQuest AIカメラで認証済み",
                                    "HẻmQuest AI 카메라인증 완료"
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }

                        if (!stop.story.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stop.story,
                                fontSize = 12.sp,
                                color = Ink600,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // If user uploaded/captured a photo, show polaroid style proof frame below
                stop.photoUri?.let { photoUrl ->
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = stop.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = l(currentLanguage, "Đóng Passport", "Close Passport", "关闭护照", "閉じる", "닫기"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    val lower = category.lowercase()
    return when {
        lower.contains("cà phê") || lower.contains("cafe") || lower.contains("trà") -> Icons.Default.Restaurant
        lower.contains("di sản") || lower.contains("chùa") || lower.contains("nhà thờ") || lower.contains("kiến trúc") || lower.contains("chung cư") -> Icons.Default.Architecture
        lower.contains("lịch sử") || lower.contains("cổ") || lower.contains("bảo tàng") -> Icons.Default.History
        lower.contains("ẩm thực") || lower.contains("ăn") || lower.contains("bánh") -> Icons.Default.Restaurant
        else -> Icons.Default.Explore
    }
}

private fun getStampColors(category: String): Pair<Color, Color> {
    val lower = category.lowercase()
    return when {
        lower.contains("cà phê") || lower.contains("cafe") || lower.contains("trà") -> 
            Pair(Color(0xFFD97706), Color(0xFFFEF3C7)) // Amber Gold
        lower.contains("di sản") || lower.contains("chùa") || lower.contains("kiến trúc") || lower.contains("chung cư") -> 
            Pair(Color(0xFF059669), Color(0xFFD1FAE5)) // Emerald Green
        lower.contains("lịch sử") || lower.contains("cổ") -> 
            Pair(Color(0xFFDC2626), Color(0xFFFEE2E2)) // Heritage Crimson
        else -> 
            Pair(Color(0xFF2563EB), Color(0xFFDBEAFE)) // Royal Blue
    }
}
