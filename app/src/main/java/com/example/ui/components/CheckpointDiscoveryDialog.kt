package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.QuestStop
import com.example.util.StopPhotosHelper
import com.example.util.WardLocationHelper
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite
import com.example.util.l

/**
 * Reusable Dialog component that appears when a user hits or reaches a checkpoint/landmark.
 * Displays a celebratory header, a visual image, a brief historical snippet,
 * a verified heritage source reference, and an optional challenge prompt.
 */
@Composable
fun CheckpointDiscoveryDialog(
    stop: QuestStop? = null,
    landmarkName: String = stop?.name ?: "Saigon Cultural Alley Landmark",
    category: String = stop?.category ?: "Cultural Heritage",
    historicalSnippet: String = stop?.story ?: "This historical alley checkpoint represents generations of traditional Saigon community life, artisanal crafts, and architectural preservation.",
    imageUrl: String? = stop?.photoUri ?: getLandmarkSampleImageUrl(landmarkName),
    factReference: String = stop?.factReference ?: "Verified Saigon Heritage Inventory",
    challengePrompt: String? = stop?.challenge?.prompt,
    currentLanguage: String = "vi",
    onDismiss: () -> Unit,
    onStartChallenge: (() -> Unit)? = null,
    onConfirmCompletion: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("checkpoint_discovery_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = PaperWhite,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Celebratory Hero Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CyberPurple, GrabGreen)
                            )
                        )
                ) {
                    // Refined Glassmorphic Close Button
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.38f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(38.dp)
                            .testTag("close_checkpoint_discovery_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "BẠN ĐÃ ĐẾN MỤC TIÊU!",
                                        "CHECKPOINT REACHED!",
                                        "已到达打卡点！",
                                        "スポットに到着！",
                                        "체크포인트 도착!"
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = landmarkName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 24.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Category Badge & Pin
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ForestGreen.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ClayOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = WardLocationHelper.getWardLocation(
                                    stopName = landmarkName,
                                    lang = currentLanguage,
                                    latitude = stop?.latitude ?: 0.0,
                                    longitude = stop?.longitude ?: 0.0
                                ),
                                fontSize = 12.sp,
                                color = Ink600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct Google Maps Real Location & Routing Actions
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🗺️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "XEM VỊ TRÍ GOOGLE MAPS",
                                            "GOOGLE MAPS LOCATION",
                                            "谷歌地图实景位置",
                                            "Googleマップ位置確認",
                                            "Google 지도 위치 확인"
                                        ),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen,
                                        letterSpacing = 0.6.sp
                                    )
                                }

                                if (stop != null) {
                                    Surface(
                                        color = ForestGreen.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.4f", stop.latitude)}, ${String.format(java.util.Locale.US, "%.4f", stop.longitude)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = l(
                                    currentLanguage,
                                    "Mở trực tiếp trên Google Maps để xem hình ảnh thực tế, chế độ 360° Street View và nhận chỉ đường chính xác đến điểm dừng này.",
                                    "Open directly in Google Maps for real street view, 360° panoramas, and step-by-step walking directions to this spot.",
                                    "直接在谷歌地图中打开，查看实景街景、360° 全景图以及前往该地点的详细步行路线。",
                                    "Googleマップで直接開き、ストリートビューや360°パノラマ、詳細なルート案内を確認できます。",
                                    "Google 지도에서 직접 열어 실시간 거리 뷰, 360° 파노라마 및 도보 경로를 확인하세요."
                                ),
                                fontSize = 11.5.sp,
                                color = com.example.ui.theme.Ink600,
                                lineHeight = 16.sp
                            )

                            if (stop != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            StopPhotosHelper.openGoogleMapsRoute(context, stop.latitude, stop.longitude, stop.name)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Explore,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = l(currentLanguage, "Chỉ đường", "Directions", "路线导航", "ルート案内", "길찾기"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            StopPhotosHelper.openGoogleStreetView360(context, stop.latitude, stop.longitude)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "🌐", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = l(currentLanguage, "Street View 360°", "360° Street View", "360° 街景", "360° ビュー", "360° 뷰"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Historical Snippet Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "SƠ LƯỢC LỊCH SỬ & VĂN HÓA",
                                        "HISTORICAL & CULTURAL SNIPPET",
                                        "历史与文化概要",
                                        "歴史と文化の概要",
                                        "역사 및 문화 개요"
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForestGreen,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = historicalSnippet,
                                fontSize = 13.1.sp,
                                color = Ink900,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Verified Heritage Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(ForestGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = factReference,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }

                    // Optional Challenge Prompt Highlight
                    if (!challengePrompt.isNull_or_blank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3ED)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ClayOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ClayOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "THỬ THÁCH CHECK-IN",
                                            "CHECK-IN CHALLENGE",
                                            "打卡挑战",
                                            "チェックインチャレンジ",
                                            "체크인 챌린지"
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ClayOrange
                                    )
                                    Text(
                                        text = challengePrompt ?: "",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink900,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons Action Row
                    if (onStartChallenge != null && !challengePrompt.isNull_or_blank()) {
                        Button(
                            onClick = {
                                onDismiss()
                                onStartChallenge()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_landmark_challenge_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "CHỤP ẢNH THỬ THÁCH (CAMERA)",
                                        "TAKE CHALLENGE PHOTO (CAMERA)",
                                        "拍摄打卡照片 (相机)",
                                        "チャレンジ写真を撮影 (カメラ)",
                                        "챌린지 사진 촬영 (카메라)"
                                    ),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (onConfirmCompletion != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onDismiss()
                                    onConfirmCompletion()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("discovery_direct_complete_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "XÁC NHẬN ĐÃ ĐẾN & HOÀN THÀNH (+50 XP)",
                                            "CONFIRM ARRIVED & COMPLETE (+50 XP)",
                                            "确认到达并完成 (+50 XP)",
                                            "到着確認＆完了 (+50 XP)",
                                            "도착 확인 및 완료 (+50 XP)"
                                        ),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Ink600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Đóng & Tiếp Tục",
                                        "Close & Continue",
                                        "关闭并继续",
                                        "閉じて続ける",
                                        "닫고 계속하기"
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink600
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Đóng & Tiếp Tục Khám Phá",
                                        "Close & Continue Exploring",
                                        "关闭并继续探索",
                                        "閉じて散策を続ける",
                                        "닫고 계속 탐험하기"
                                    ),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

/**
 * Returns a high quality sample landmark image URL based on landmark title keywords
 */
private fun getLandmarkSampleImageUrl(title: String): String {
    val lower = title.lowercase()
    return when {
        lower.contains("bách khoa") || lower.contains("bach khoa") || lower.contains("hcmut") || lower.contains("giảng đường") ->
            "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=800&q=80"
        lower.contains("tô hiến thành") || lower.contains("ăn vặt") ->
            "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80"
        lower.contains("lữ gia") || lower.contains("học nhóm") ->
            "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=800&q=80"
        lower.contains("lồng đèn") || lower.contains("lantern") || lower.contains("phú bình") ->
            "https://images.unsplash.com/photo-1574870111867-089730e5a72b?auto=format&fit=crop&w=800&q=80"
        lower.contains("mộc") || lower.contains("wood") || lower.contains("carpentry") ->
            "https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?auto=format&fit=crop&w=800&q=80"
        lower.contains("gốm") || lower.contains("pottery") || lower.contains("ceramic") ->
            "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?auto=format&fit=crop&w=800&q=80"
        lower.contains("sủi cảo") || lower.contains("dumpling") ->
            "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?auto=format&fit=crop&w=800&q=80"
        lower.contains("thuốc bắc") || lower.contains("herbal") ->
            "https://images.unsplash.com/photo-1514733670139-4d87a1941d55?auto=format&fit=crop&w=800&q=80"
        lower.contains("đỗ phủ") || lower.contains("ba lù") || lower.contains("cà phê") || lower.contains("coffee") ->
            "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80"
        lower.contains("thiên hậu") || lower.contains("chùa") || lower.contains("temple") || lower.contains("pagoda") || lower.contains("phụng sơn") || lower.contains("xá lợi") ->
            "https://images.unsplash.com/photo-1548625361-185121e7eb68?auto=format&fit=crop&w=800&q=80"
        lower.contains("bí mật") || lower.contains("bunker") || lower.contains("vũ khí") ->
            "https://images.unsplash.com/photo-1590523741831-ab7e8b8f9c7f?auto=format&fit=crop&w=800&q=80"
        lower.contains("biệt thự") || lower.contains("villa") ->
            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80"
        lower.contains("bích họa") || lower.contains("mural") || lower.contains("art") ->
            "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=800&q=80"
        lower.contains("cơm tấm") || lower.contains("broken rice") ->
            "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=800&q=80"
        else -> "https://images.unsplash.com/photo-1583417319070-4a69db38a482?auto=format&fit=crop&w=800&q=80"
    }
}
