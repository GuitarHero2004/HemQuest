package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuestRequest
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite

import com.example.util.l

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuestBuilderSheet(
    initialRequest: QuestRequest,
    onDismiss: () -> Unit,
    onGenerate: (QuestRequest) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val lang = initialRequest.language

    var durationMinutes by remember { mutableIntStateOf(initialRequest.durationMinutes) }
    var selectedLocationName by remember { mutableStateOf(initialRequest.startingLocationName) }
    var selectedLat by remember { mutableStateOf(initialRequest.latitude) }
    var selectedLng by remember { mutableStateOf(initialRequest.longitude) }
    var freeTextNotes by remember { mutableStateOf(initialRequest.freeTextNotes) }
    var language by remember { mutableStateOf(initialRequest.language) }

    val availableInterests = listOf(
        "Hidden Alleys", "Maker & Hacker", "Street Food", "Secret History",
        "Local Crafts", "Architecture", "Coffee Culture", "Canal & River",
        "Antique Masters", "Acoustic & Books", "Sacred Shrines", "Late Night",
        "Green Spaces"
    )

    val selectedInterests = remember {
        mutableStateListOf<String>().apply { addAll(initialRequest.interests) }
    }

    val locationPresets = listOf(
        Triple("Phường Diên Hồng (ĐH Bách Khoa & Hẻm Đồ Án)", 10.7725, 106.6578),
        Triple("Phường Chợ Lớn (Phố Thuốc Bắc & Hào Sĩ Phường)", 10.7533, 106.6601),
        Triple("Phường Thanh Đa (Cư Xá Lô S & Bến Đò Cũ)", 10.8258, 106.7242),
        Triple("Phường Bàn Cờ (Mê Cung Hẻm & Hầm Biệt Động)", 10.7825, 106.6958),
        Triple("Phường Sài Gòn (Hẻm 158 Pasteur & Chợ Cũ)", 10.7764, 106.7011),
        Triple("Phường Tân Định (Hẻm Đặng Dung & Bích Họa)", 10.7851, 106.6982),
        Triple("Phường Hòa Bình (Làng Lồng Đèn Phú Bình)", 10.7638, 106.6492),
        Triple("Phường Xóm Chiếu (Hẻm Ốc & Cầu Mống 1893)", 10.7689, 106.7032),
        Triple("Phường Nhiêu Lộc (Bờ Kênh & Lò Lư Đồng)", 10.7932, 106.6845),
        Triple("Phường Cầu Kho (Hẻm Lò Hủ Tiếu & Đình Cổ)", 10.7589, 106.6892),
        Triple("Phường Bình Đông (Bến Ghe & Hẻm Làm Nhang)", 10.7482, 106.6421),
        Triple("Phường Hạnh Thông Tây (Nhà Thờ Cổ & Lò Tráng)", 10.8351, 106.6654)
    )

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
                .testTag("quest_builder_sheet")
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

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = l(
                            lang,
                            "Tạo Hành Trình HẻmQuest",
                            "Design Your HẻmQuest",
                            "设计您的胡同探索之旅",
                            "HẻmQuestをデザインする",
                            "나만의 골목 퀘스트 만들기"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Ink900
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Ink600
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Starting Location
            Text(
                text = l(
                    lang,
                    "1. ĐIỂM BẮT ĐẦU",
                    "1. STARTING LOCATION",
                    "1. 起始地点",
                    "1. スタート地点",
                    "1. 출발 위치"
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                locationPresets.forEach { (name, lat, lng) ->
                    val isSelected = selectedLocationName == name
                    Surface(
                        color = if (isSelected) PaperSecondary else PaperWhite,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ForestGreen else Color(0xFFE4DDD0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedLocationName = name
                                selectedLat = lat
                                selectedLng = lng
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) ClayOrange else Ink600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Ink900,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Time Budget
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = l(
                        lang,
                        "2. THỜI GIAN DỰ KIẾN",
                        "2. TIME BUDGET",
                        "2. 预计时间",
                        "2. 所要時間",
                        "2. 예상 소요 시간"
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ForestGreen,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(30, 60, 90, 120).forEach { mins ->
                    val isSelected = durationMinutes == mins
                    Surface(
                        color = if (isSelected) ForestGreen else PaperSecondary,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { durationMinutes = mins }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins} min",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Ink900
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Interests
            Text(
                text = l(
                    lang,
                    "3. SỞ THÍCH CỦA BẠN (TỐI ĐA 3)",
                    "3. YOUR INTERESTS (MAX 3)",
                    "3. 您的兴趣 (最多3项)",
                    "3. 興味・関心 (最大3つ)",
                    "3. 관심사 (최대 3개)"
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    Surface(
                        color = if (isSelected) ClayOrange else PaperSecondary,
                        shape = CircleShape,
                        modifier = Modifier.clickable {
                            if (isSelected) {
                                selectedInterests.remove(interest)
                            } else {
                                if (selectedInterests.size < 3) {
                                    selectedInterests.add(interest)
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = interest,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink900
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Special Request / Notes
            Text(
                text = l(
                    lang,
                    "4. GHI CHÚ ĐẶC BIỆT (TÙY CHỌN)",
                    "4. SPECIAL REQUEST (OPTIONAL)",
                    "4. 特别要求 (可选)",
                    "4. 特別なご要望 (任意)",
                    "4. 특별 요청 사항 (선택)"
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = freeTextNotes,
                onValueChange = { freeTextNotes = it },
                placeholder = {
                    Text(
                        text = l(
                            lang,
                            "Ví dụ: Ghé quán trà tĩnh lặng và hẻm có mảng xanh...",
                            "e.g. Quiet places and one local tea stop...",
                            "例如：安静的小茶馆和绿意小巷...",
                            "例：静かなお茶屋や緑豊かな路地...",
                            "예: 조용한 찻집, 녹음이 우거진 골목..."
                        ),
                        fontSize = 13.sp,
                        color = Ink600
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = Color(0xFFE4DDD0),
                    focusedContainerColor = PaperSecondary,
                    unfocusedContainerColor = PaperSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Generate Button
            Button(
                onClick = {
                    val request = QuestRequest(
                        startingLocationName = selectedLocationName,
                        latitude = selectedLat,
                        longitude = selectedLng,
                        durationMinutes = durationMinutes,
                        interests = if (selectedInterests.isNotEmpty()) selectedInterests.toList() else listOf("Architecture", "Hidden History"),
                        freeTextNotes = freeTextNotes,
                        language = initialRequest.language
                    )
                    onGenerate(request)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_quest_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = l(
                            lang,
                            "TẠO HÀNH TRÌNH BẰNG GEMINI AI",
                            "GENERATE QUEST WITH GEMINI AI",
                            "使用 GEMINI AI 生成任务",
                            "GEMINI AIでクエストを生成",
                            "GEMINI AI로 퀘스트 생성"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
