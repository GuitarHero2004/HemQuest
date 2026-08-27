package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.PassportPhotoEntity
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
    currentLanguage: String,
    passportPhotos: List<PassportPhotoEntity> = emptyList()
) {
    val completedStops = remember(quests) {
        quests.flatMap { q -> q.stops.filter { it.status == StopStatus.COMPLETED } }
    }

    var selectedStopForStampDialog by remember { mutableStateOf<QuestStop?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 14.dp)
            .testTag("alley_scrapbook_section")
    ) {
        // Header Bar with Top Margin & Clean Spacing
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 14.dp, start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
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
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink900
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = l(
                            currentLanguage,
                            "Bộ sưu tập con dấu di sản đã mở khóa",
                            "Unlocked heritage achievement stamps",
                            "已解锁的文化遗产印章成就",
                            "解除されたヘリテージスタンプ",
                            "해금된 헤리티지 스탬프 업적"
                        ),
                        fontSize = 11.5.sp,
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
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
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
                                fontSize = 14.5.sp,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Locked Stamp Placeholders Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(92.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${l(currentLanguage, "Dấu", "Stamp", "印章", "スタンプ", "스탬프")} #${index + 1}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
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

        // Digital Passport Photo Gallery (Snapped Marker Photos)
        if (passportPhotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📸", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = l(
                                currentLanguage,
                                "Bộ Sưu Tập Ảnh Passport",
                                "Passport Photo Gallery",
                                "护照相册",
                                "パスポート写真ギャラリー",
                                "여권 사진 갤러리"
                            ),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                        Text(
                            text = l(
                                currentLanguage,
                                "Đã lưu & đồng bộ Firebase Firestore",
                                "Stored & synced to Firebase Firestore",
                                "已保存并同步至 Firebase",
                                "Firebaseに同期済み",
                                "Firebase와 동기화됨"
                            ),
                            fontSize = 11.sp,
                            color = Ink600
                        )
                    }
                }

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "${passportPhotos.size} ${l(currentLanguage, "ảnh", "photos", "张", "枚", "장")}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var selectedPhotoForDialog by remember { mutableStateOf<PassportPhotoEntity?>(null) }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(passportPhotos) { photo ->
                    PassportPhotoCard(
                        photo = photo,
                        currentLanguage = currentLanguage,
                        onClick = { selectedPhotoForDialog = photo }
                    )
                }
            }

            selectedPhotoForDialog?.let { photo ->
                PassportPhotoDetailDialog(
                    photo = photo,
                    currentLanguage = currentLanguage,
                    onDismiss = { selectedPhotoForDialog = null }
                )
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
    val stampThemeColors = getStampColors(stop.category)

    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
        modifier = Modifier
            .width(172.dp)
            .height(245.dp)
            .border(1.2.dp, stampThemeColors.first.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = stampThemeColors.first.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, stampThemeColors.first.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "PASSED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = stampThemeColors.first,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Central Customized Saigon Heritage Postmark Graphic
            CustomHeritageStampGraphic(
                stop = stop,
                stampThemeColors = stampThemeColors,
                sizeDp = 84
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Landmark Name & Category Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stop.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
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

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom XP / Tap clue label Surface Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = stampThemeColors.first.copy(alpha = 0.1f),
                border = BorderStroke(0.8.dp, stampThemeColors.first.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+100 XP • ${l(currentLanguage, "Xem Chi Tiết", "View Details", "查看详情", "詳細を見る", "상세 보기")}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stampThemeColors.first,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CustomHeritageStampGraphic(
    stop: QuestStop,
    stampThemeColors: Pair<Color, Color>,
    sizeDp: Int = 84
) {
    val categoryIcon = getCategoryIcon(stop.category)
    val stampColor = stampThemeColors.first

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size((sizeDp + 10).dp)
    ) {
        // Outer Vintage Stamp Circular Seal Frame
        Surface(
            shape = CircleShape,
            color = stampThemeColors.second,
            border = BorderStroke(2.dp, stampColor),
            modifier = Modifier.size(sizeDp.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Inner Dashed Rings (Postmark Seal Lines)
                Box(
                    modifier = Modifier
                        .size((sizeDp - 12).dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = stampColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Central Color Gradient Emblem
                    Box(
                        modifier = Modifier
                            .size((sizeDp - 22).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        stampColor,
                                        stampColor.copy(alpha = 0.88f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = stop.name,
                            tint = Color.White,
                            modifier = Modifier.size((sizeDp / 2.5).dp)
                        )
                    }
                }
            }
        }

        // Tilted Postmark Rubber Stamp Mark with Postal Cancellation Waves
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .rotate(-14f)
                .offset(y = 2.dp)
        ) {
            // Wavy Postal Ink Lines
            Canvas(modifier = Modifier.size(width = 16.dp, height = 22.dp)) {
                val path = androidx.compose.ui.graphics.Path()
                val waveColor = stampColor.copy(alpha = 0.85f)
                for (i in 0..2) {
                    val y = 5f + i * 6f
                    path.reset()
                    path.moveTo(0f, y)
                    path.quadraticTo(4.dp.toPx(), y - 3.dp.toPx(), 8.dp.toPx(), y)
                    path.quadraticTo(12.dp.toPx(), y + 3.dp.toPx(), 16.dp.toPx(), y)
                    drawPath(
                        path = path,
                        color = waveColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Postmark Rubber Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Ink900.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, Color(0xFFFDE047)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "★ SÀI GÒN STAMP ★",
                        fontSize = (sizeDp * 0.09).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDE047),
                        letterSpacing = 0.4.sp
                    )
                }
            }
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

                // Large Glowing Iconic Saigon Heritage Postmark Seal
                CustomHeritageStampGraphic(
                    stop = stop,
                    stampThemeColors = stampThemeColors,
                    sizeDp = 110
                )

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

@Composable
fun PassportPhotoCard(
    photo: PassportPhotoEntity,
    currentLanguage: String,
    onClick: () -> Unit
) {
    val bitmap = remember(photo.photoBase64) {
        try {
            val decodedBytes = Base64.decode(photo.photoBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(170.dp)
            .height(230.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = photo.stopName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Ink600
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "PASSPORT SNAP",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = photo.stopName,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Ink900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val dateStr = remember(photo.timestamp) {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                sdf.format(java.util.Date(photo.timestamp))
            }

            Text(
                text = dateStr,
                fontSize = 10.sp,
                color = Ink600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (photo.userEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = photo.userEmail,
                    fontSize = 9.5.sp,
                    color = ForestGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PassportPhotoDetailDialog(
    photo: PassportPhotoEntity,
    currentLanguage: String,
    onDismiss: () -> Unit
) {
    val bitmap = remember(photo.photoBase64) {
        try {
            val decodedBytes = Base64.decode(photo.photoBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ForestGreen.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = l(currentLanguage, "Ảnh Passport Đã Xác Nhận", "Verified Passport Photo", "已验证护照照片", "検証済みパスポート写真", "인증된 여권 사진"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Ink600)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = photo.stopName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = photo.stopName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink900,
                    textAlign = TextAlign.Center
                )

                if (photo.questTitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🗺️ ${photo.questTitle}",
                        fontSize = 13.sp,
                        color = Ink600,
                        textAlign = TextAlign.Center
                    )
                }

                val dateStr = remember(photo.timestamp) {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(photo.timestamp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📅 $dateStr",
                            fontSize = 11.5.sp,
                            color = Ink600,
                            fontWeight = FontWeight.Medium
                        )
                        if (photo.userEmail.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "👤 ${photo.userEmail}",
                                fontSize = 11.5.sp,
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
