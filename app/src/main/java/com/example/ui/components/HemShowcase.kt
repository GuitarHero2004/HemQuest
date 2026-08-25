package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuestRequest
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.PaperWhite
import com.example.util.l

data class LocalizedHemPoi(
    val id: String,
    val nameVi: String,
    val nameEn: String,
    val lat: Double,
    val lng: Double,
    val descVi: String,
    val descEn: String,
    val icon: String,
    val request: QuestRequest
)

val sampleHemPois = listOf(
    LocalizedHemPoi(
        id = "1",
        nameVi = "Quán Cà Phê Vợt Ba Lù",
        nameEn = "Ba Lù Net Filter Coffee",
        lat = 10.7533,
        lng = 106.6631,
        descVi = "Cà phê vợt truyền thống có tuổi đời hơn 70 năm ở Chợ Lớn",
        descEn = "Traditional net filter coffee with over 70 years of history in Chợ Lớn",
        icon = "☕",
        request = QuestRequest(
            startingLocationName = "Chợ Lớn, TP.HCM",
            latitude = 10.7533,
            longitude = 106.6631,
            durationMinutes = 45,
            interests = listOf("Coffee Culture", "Street Food"),
            freeTextNotes = "Ba Lù net filter coffee & historic Chợ Lớn alleys",
            language = "vi"
        )
    ),
    LocalizedHemPoi(
        id = "2",
        nameVi = "Chùa Bà Thiên Hậu",
        nameEn = "Thien Hau Pagoda",
        lat = 10.7538,
        lng = 106.6625,
        descVi = "Ngôi chùa cổ kính linh thiêng bậc nhất Chợ Lớn",
        descEn = "One of the oldest and most sacred Cantonese pagodas in Chợ Lớn",
        icon = "⛩️",
        request = QuestRequest(
            startingLocationName = "Chợ Lớn, TP.HCM",
            latitude = 10.7538,
            longitude = 106.6625,
            durationMinutes = 40,
            interests = listOf("History", "Architecture"),
            freeTextNotes = "Thien Hau Pagoda & heritage alleys",
            language = "vi"
        )
    ),
    LocalizedHemPoi(
        id = "3",
        nameVi = "Tiệm Mì Gia Truyền Ba Chấm",
        nameEn = "Heritage Noodle House",
        lat = 10.7530,
        lng = 106.6635,
        descVi = "Mì kéo tay làm thủ công 3 thế hệ thơm ngon nức tiếng",
        descEn = "3rd generation handmade pulled noodle shop loved by locals",
        icon = "🍜",
        request = QuestRequest(
            startingLocationName = "Chợ Lớn, TP.HCM",
            latitude = 10.7530,
            longitude = 106.6635,
            durationMinutes = 50,
            interests = listOf("Street Food"),
            freeTextNotes = "Handmade noodle heritage & street eats",
            language = "vi"
        )
    ),
    LocalizedHemPoi(
        id = "4",
        nameVi = "Góc Check-in Tường Rêu Cổ Kính",
        nameEn = "Mossy Heritage Wall Spot",
        lat = 10.7536,
        lng = 106.6632,
        descVi = "Mảng tường rêu phong xưa cũ lý tưởng để chụp ảnh hoài niệm",
        descEn = "Vintage mossy brick wall alley perfect for nostalgia photography",
        icon = "📸",
        request = QuestRequest(
            startingLocationName = "Chợ Lớn, TP.HCM",
            latitude = 10.7536,
            longitude = 106.6632,
            durationMinutes = 30,
            interests = listOf("Photography", "Hidden Alleys"),
            freeTextNotes = "Vintage brick wall photography & hidden alleys",
            language = "vi"
        )
    )
)

@Composable
fun HemShowcase(
    currentLanguage: String = "vi",
    onStartQuest: ((QuestRequest) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedPoi by remember { mutableStateOf<LocalizedHemPoi?>(null) }
    var showDiscoveryDialogForPoi by remember { mutableStateOf<LocalizedHemPoi?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l(
                    currentLanguage,
                    "Khám Phá Hẻm Spotlights 🗺️",
                    "Explore Alley Spotlights 🗺️",
                    "探索胡同亮点地图 🗺️",
                    "路地スポットライト探訪 🗺️",
                    "골목 스포트라이트 탐방 🗺️"
                ),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.Ink900
            )
        }

        // Interactive Native Alley Spotlights Carousel (Zero extra webviews)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sampleHemPois) { poi ->
                val isSelected = selectedPoi?.id == poi.id
                Card(
                    onClick = { selectedPoi = poi },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GrabGreen.copy(alpha = 0.08f) else PaperWhite
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) GrabGreen else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                    modifier = Modifier.width(220.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) GrabGreen else Color(0xFFF1F5F9),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = poi.icon,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) GrabGreen else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = if (isSelected) "✓ SELECTED" else "SPOTLIGHT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else com.example.ui.theme.Ink600,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLanguage == "vi") poi.nameVi else poi.nameEn,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = com.example.ui.theme.Ink900,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLanguage == "vi") poi.descVi else poi.descEn,
                            fontSize = 11.5.sp,
                            color = com.example.ui.theme.Ink600,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        selectedPoi?.let { poi ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${poi.icon} ${if (currentLanguage == "vi") poi.nameVi else poi.nameEn}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = com.example.ui.theme.Ink900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLanguage == "vi") poi.descVi else poi.descEn,
                            fontSize = 12.sp,
                            color = com.example.ui.theme.Ink600
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { showDiscoveryDialogForPoi = poi },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "📜 Info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = com.example.ui.theme.ForestGreen
                            )
                        }

                        if (onStartQuest != null) {
                            Button(
                                onClick = { onStartQuest(poi.request.copy(language = currentLanguage)) },
                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = l(currentLanguage, "Đi ngay", "Go", "前往", "行く", "시작"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        showDiscoveryDialogForPoi?.let { poi ->
            CheckpointDiscoveryDialog(
                landmarkName = if (currentLanguage == "vi") poi.nameVi else poi.nameEn,
                category = "Alley Heritage Landmark",
                historicalSnippet = if (currentLanguage == "vi") poi.descVi + ". Con hẻm Sài Gòn lâu đời lưu giữ nhịp sống dịu dàng, những mảng tường rêu và nét văn hóa độc đáo qua hàng chục năm." else poi.descEn + ". A historic Saigon alley preserving quiet daily rhythms, heritage architecture, and authentic local stories across decades.",
                currentLanguage = currentLanguage,
                onDismiss = { showDiscoveryDialogForPoi = null },
                onStartChallenge = if (onStartQuest != null) {
                    {
                        val req = poi.request.copy(language = currentLanguage)
                        showDiscoveryDialogForPoi = null
                        onStartQuest(req)
                    }
                } else null
            )
        }
    }
}

