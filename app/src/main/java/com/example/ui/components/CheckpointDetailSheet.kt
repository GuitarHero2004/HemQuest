package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.util.StopPhotosHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Verified
import com.example.util.WardLocationHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuestStop
import com.example.model.StopStatus
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite

import com.example.util.l

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckpointDetailSheet(
    stop: QuestStop,
    stopIndex: Int,
    totalStops: Int,
    isVi: Boolean = false,
    currentLanguage: String = if (isVi) "vi" else "en",
    onDismiss: () -> Unit,
    onStartCameraChallenge: () -> Unit,
    onSkipStop: () -> Unit,
    onConfirmCompletion: (() -> Unit)? = null,
    onOpenGlossaryForTerm: ((String?) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val isCompleted = stop.status == StopStatus.COMPLETED

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .testTag("checkpoint_detail_sheet")
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(ForestGreen.copy(alpha = 0.15f))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = l(
                            currentLanguage,
                            "ĐIỂM DỪNG HIỆN TẠI • ${stopIndex + 1}/$totalStops",
                            "CURRENT STOP • ${stopIndex + 1}/$totalStops",
                            "当前打卡点 • ${stopIndex + 1}/$totalStops",
                            "現在のスポット • ${stopIndex + 1}/$totalStops",
                            "현재 체크포인트 • ${stopIndex + 1}/$totalStops"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ClayOrange,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stop.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = ForestGreen,
                        lineHeight = 28.sp
                    )
                }

                // Refined UI-friendly Close Button
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color(0xFFF1F5F2),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("close_checkpoint_detail_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"),
                            tint = Ink900,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category, Location & Time Badge Row
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Surface(
                        color = ForestGreen.copy(alpha = 0.08f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = stop.category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                item {
                    Surface(
                        color = Color(0xFFF97316).copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = WardLocationHelper.getWardLocation(stop, currentLanguage),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEA580C)
                            )
                        }
                    }
                }

                item {
                    Surface(
                        color = ForestGreen.copy(alpha = 0.08f),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = l(currentLanguage, "~12 phút đi bộ", "12 mins walk", "约12分钟步行", "徒歩約12分", "도보 약 12분"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            // Cultural Glossary Quick Term Badges
            val matchedTerms = remember(stop.name, stop.category, stop.story) {
                com.example.repository.CulturalGlossaryRepository.findMatchingTermsForText("${stop.name} ${stop.category} ${stop.story}")
            }

            if (matchedTerms.isNotEmpty() || onOpenGlossaryForTerm != null) {
                Spacer(modifier = Modifier.height(10.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text(
                            text = "📚",
                            fontSize = 13.sp
                        )
                    }

                    if (matchedTerms.isNotEmpty()) {
                        items(matchedTerms) { termItem ->
                            Surface(
                                onClick = { onOpenGlossaryForTerm?.invoke(termItem.id) },
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = termItem.term,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "🔍",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Surface(
                                onClick = { onOpenGlossaryForTerm?.invoke(null) },
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = l(currentLanguage, "Tra từ điển văn hóa hẻm 📖", "Cultural Glossary 📖", "查阅深巷文化词典 📖", "文化辞典を見る 📖", "골목 문화 용어집 📖"),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D28D9),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Direct Google Maps Real Location & Routing Actions
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
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
                                    "ĐỊA ĐIỂM & ĐIỀU HƯỚNG GOOGLE MAPS",
                                    "GOOGLE MAPS LOCATION & ROUTING",
                                    "谷歌地图实景位置与导航",
                                    "Googleマップ位置＆ルート案内",
                                    "Google 지도 위치 및 길찾기"
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = ForestGreen,
                                letterSpacing = 0.6.sp
                            )
                        }

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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = l(
                            currentLanguage,
                            "Mở trực tiếp trên ứng dụng Google Maps để xem hình ảnh vệ tinh, chế độ 360° Street View và chỉ đường đi bộ chính xác vào hẻm.",
                            "Open directly in Google Maps for satellite view, interactive 360° Street View, and precise walking directions into the alley.",
                            "直接在谷歌地图中打开，查看卫星街景、360° 全景图以及深入小巷的步行导航。",
                            "Googleマップで直接開き、ストリートビューや路地への徒歩ルートを確認できます。",
                            "Google 지도에서 직접 열어 360° 스트리트 뷰 및 골목 보행 길찾기를 확인하세요."
                        ),
                        fontSize = 11.5.sp,
                        color = Ink600,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Directions Route Button
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
                                    imageVector = Icons.Default.Directions,
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

                        // 2. Google Maps 360 / Location Pin Button
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
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Why Selected
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ClayOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = l(currentLanguage, "LÝ DO CHỌN ĐIỂM DỪNG", "WHY THIS STOP", "为何选择此地点", "このスポットの選定理由", "이 장소를 추천하는 이유"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ClayOrange,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stop.whySelected,
                            fontSize = 13.sp,
                            color = Ink900,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Fact-Constrained Cultural Story
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = l(currentLanguage, "CÂU CHUYỆN VĂN HÓA DI SẢN", "CULTURAL HERITAGE STORY", "文化遗产背后的故事", "文化遺産のストーリー", "문화 유산 이야기"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ForestGreen,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stop.story,
                fontSize = 15.sp,
                color = Ink900,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Fact Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFE2EFE9), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stop.factReference,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForestGreen
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Photo Challenge Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF1ED)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ClayOrange.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = ClayOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = l(currentLanguage, "THỬ THÁCH BỨC ẢNH VĂN HÓA", "CULTURAL PHOTO CHALLENGE", "文化摄影挑战", "文化フォトチャレンジ", "문화 사진 챌린지"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ClayOrange,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stop.challenge.prompt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink900,
                        lineHeight = 20.sp
                    )

                    if (!stop.challenge.successGuidance.isNull_or_empty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 ${stop.challenge.successGuidance}",
                            fontSize = 12.sp,
                            color = Ink600
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button inside Card
                    if (isCompleted) {
                        Surface(
                            color = ForestGreen,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(currentLanguage, "ĐÃ HOÀN THÀNH ĐIỂM DỪNG NÀY", "CHECKPOINT COMPLETED", "已完成此打卡点", "スポット完了済み", "체크포인트 완료됨"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartCameraChallenge,
                            colors = ButtonDefaults.buttonColors(containerColor = ClayOrange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("complete_photo_challenge_button")
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
                                    text = l(currentLanguage, "CHỤP ẢNH THỬ THÁCH (MỞ CAMERA)", "TAKE PHOTO CHALLENGE (OPEN CAMERA)", "拍照并完成挑战 (打开相机)", "写真撮影＆チャレンジ達成 (カメラ起動)", "사진 촬영 및 챌린지 완료 (카메라 열기)"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }

                        if (onConfirmCompletion != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onConfirmCompletion,
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("direct_complete_checkpoint_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = l(currentLanguage, "XÁC NHẬN ĐÃ ĐẾN & HOÀN THÀNH (+50 XP)", "CONFIRM ARRIVED & COMPLETE (+50 XP)", "确认到达并完成 (+50 XP)", "到着確認＆完了 (+50 XP)", "도착 확인 및 완료 (+50 XP)"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Buttons Row (Google Maps Direction, Skip & Close)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        openSingleDirectionInMaps(context, stop.latitude, stop.longitude, stop.name)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = l(currentLanguage, "Mở Maps", "Maps", "打开地图", "マップ", "지도"),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                    }
                }

                if (!isCompleted) {
                    OutlinedButton(
                        onClick = onSkipStop,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                tint = Ink600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Bỏ qua", "Skip", "跳过", "スキップ", "건너뛰기"),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink600
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Ink600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

private fun openSingleDirectionInMaps(context: Context, lat: Double, lng: Double, label: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}
