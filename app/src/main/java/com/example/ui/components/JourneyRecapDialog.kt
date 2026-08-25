package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Quest
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JourneyRecapDialog(
    quest: Quest?,
    isVi: Boolean = false,
    currentLanguage: String = if (isVi) "vi" else "en",
    actualWalkedDistanceMeters: Double = 0.0,
    onSaveAndExit: () -> Unit,
    onNewQuest: () -> Unit,
    onDismiss: () -> Unit
) {
    if (quest == null) return

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Overview & Badge, 1 = Checkpoint Stories, 2 = Analytics & Impact
    var userRating by remember { mutableIntStateOf(5) }
    var userComment by remember { mutableStateOf("") }
    var isFeedbackSubmitted by remember { mutableStateOf(false) }
    var isShareToastVisible by remember { mutableStateOf(false) }
    val selectedTags = remember { mutableStateListOf<String>() }

    // Computed Health & Eco Metrics
    val distanceMetres = if (actualWalkedDistanceMeters > 300) {
        actualWalkedDistanceMeters.toInt()
    } else {
        quest.estimatedDistanceMetres.coerceAtLeast(1100)
    }
    val distanceKm = distanceMetres / 1000f
    val estimatedSteps = (distanceMetres * 1.35f).toInt()
    val caloriesBurned = (distanceMetres * 0.062f).toInt().coerceAtLeast(45)
    val co2SavedGrams = (distanceKm * 154f).toInt().coerceAtLeast(120) // grams of CO2 saved compared to 150cc scooter
    val xpEarned = 150 + quest.stops.size * 30 + if (distanceKm > 2.0f) 50 else 0

    // Infinite breathing glow for trophy badge
    val infiniteTransition = rememberInfiniteTransition(label = "badge_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    val quickReviewTags = listOf(
        l(currentLanguage, "🏮 Đậm Chất Sài Gòn", "🏮 Saigon Heritage", "🏮 地道西贡风情", "🏮 サイゴン風情", "🏮 사이공 정취"),
        l(currentLanguage, "📸 Góc Sống Ảo Đẹp", "📸 Photogenic Spots", "📸 出片打卡圣地", "📸 映えスポット", "📸 사진 명소"),
        l(currentLanguage, "🍜 Ẩm Thực Ngon", "🍜 Delicious Eateries", "🍜 街巷地道美食", "🍜 美味しいグルメ", "🍜 맛있는 먹거리"),
        l(currentLanguage, "🌿 Đi Bộ Mát Mẻ", "🌿 Cool Alleyways", "🌿 漫步惬意清凉", "🌿 快適な散策路", "🌿 시원한 산책길"),
        l(currentLanguage, "📜 Kiến Thức Hay", "📜 Rich Cultural Lore", "📜 历史知识丰富", "📜 充実した歴史知識", "📜 유익한 역사 지식")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = PaperWhite,
            border = BorderStroke(1.5.dp, GrabGreen.copy(alpha = 0.4f)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .navigationBarsPadding()
                .padding(vertical = 12.dp)
                .testTag("journey_recap_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Hero Celebration Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F3822),
                                    Color(0xFF1E5B3A),
                                    Color(0xFF132A1C)
                                )
                            )
                        )
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Glowing Trophy & XP Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(78.dp)
                                .scale(pulseScale)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                SunGold,
                                                ClayOrange.copy(alpha = 0.85f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF132A1C),
                                border = BorderStroke(2.dp, SunGold),
                                shadowElevation = 10.dp,
                                modifier = Modifier.size(62.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = SunGold,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category & Honorific Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrabGreen.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SunGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "BẬC THẦY KHÁM PHÁ HẺM SÀI GÒN",
                                        "SAIGON ALLEYWAY MASTER",
                                        "西贡胡同探索大师",
                                        "サイゴン路地裏マスター",
                                        "사이공 골목길 탐험 마스터"
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF86EFAC),
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = l(
                                currentLanguage,
                                "HOÀN THÀNH XUẤT SẮC!",
                                "QUEST COMPLETED!",
                                "探索任务圆满达成！",
                                "クエスト達成おめでとう！",
                                "퀘스트 완주를 축하합니다!"
                            ),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = quest.title,
                            fontSize = 14.sp,
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary 4-Pillar Performance Metrics Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF4F8F5),
                    border = BorderStroke(1.dp, Color(0xFFE2EBE2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = l(currentLanguage, "CHỈ SỐ HÀNH TRÌNH", "JOURNEY METRICS", "探索关键指标", "探索データ", "여정 핵심 지표"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 1.sp
                            )

                            // XP Reward Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SunGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, SunGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ClayOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+$xpEarned XP",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink900
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Walking Distance & Steps
                            MetricPillarCard(
                                icon = Icons.Default.DirectionsWalk,
                                iconColor = GrabGreen,
                                value = "${"%.1f".format(distanceKm)} km",
                                subValue = "~$estimatedSteps ${l(currentLanguage, "bước", "steps", "步", "歩", "걸음")}",
                                label = l(currentLanguage, "Quãng đường", "Distance", "步行距离", "歩行距離", "거리"),
                                modifier = Modifier.weight(1f)
                            )

                            // 2. Active Exploration Duration
                            MetricPillarCard(
                                icon = Icons.Default.Timer,
                                iconColor = ForestGreen,
                                value = "${quest.estimatedMinutes} ${l(currentLanguage, "phút", "min", "分", "分", "분")}",
                                subValue = "${quest.stops.size} ${l(currentLanguage, "chặng", "stops", "站", "区間", "구간")}",
                                label = l(currentLanguage, "Thời gian", "Duration", "探索耗时", "所要時間", "소요 시간"),
                                modifier = Modifier.weight(1f)
                            )

                            // 3. Calories Burned
                            MetricPillarCard(
                                icon = Icons.Default.LocalFireDepartment,
                                iconColor = ClayOrange,
                                value = "$caloriesBurned",
                                subValue = "kcal",
                                label = l(currentLanguage, "Năng lượng", "Calories", "卡路里消耗", "消費カロリー", "칼로리 소모"),
                                modifier = Modifier.weight(1f)
                            )

                            // 4. Green Eco Score
                            MetricPillarCard(
                                icon = Icons.Default.Eco,
                                iconColor = GrabGreen,
                                value = "${quest.greenScore.score}",
                                subValue = "-${co2SavedGrams}g CO₂",
                                label = l(currentLanguage, "Sinh thái", "Green Score", "环保评分", "エコアワード", "에코 점수"),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Tab Selector (Overview / Cultural Stories / Analytics)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PaperSecondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            l(currentLanguage, "🌟 Tổng Quan", "🌟 Overview", "🌟 概览与成就", "🌟 概要・実績", "🌟 개요 및 업적"),
                            l(currentLanguage, "📖 Ký Ức Chặng", "📖 Stop Stories", "📖 站点故事", "📖 スポット記憶", "📖 지점별 이야기"),
                            l(currentLanguage, "📊 Phân Tích", "📊 Analytics", "📊 数据图表", "📊 分析チャート", "📊 분석 차트")
                        )

                        tabs.forEachIndexed { index, title ->
                            val isSelected = activeTab == index
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PaperWhite else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, Color(0xFFCBD5E1)) else null,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeTab = index }
                                    .testTag("recap_tab_$index")
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ForestGreen else Ink600,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content Body
                Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    when (activeTab) {
                        0 -> {
                            // TAB 0: OVERVIEW & NEW BADGE UNLOCK
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Newly Unlocked Cultural Badge Card
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    border = BorderStroke(1.2.dp, SunGold.copy(alpha = 0.8f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = SunGold.copy(alpha = 0.25f),
                                            border = BorderStroke(1.5.dp, SunGold),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = when (quest.theme.lowercase()) {
                                                        "food", "ẩm thực", "culinary" -> "🍜"
                                                        "architecture", "kiến trúc" -> "🏛️"
                                                        "coffee", "cà phê" -> "☕"
                                                        "art", "nghệ thuật" -> "🎨"
                                                        else -> "🏮"
                                                    },
                                                    fontSize = 26.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = ClayOrange,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                ) {
                                                    Text(
                                                        text = l(currentLanguage, "HUY HIỆU MỚI", "NEW BADGE", "新徽章", "新バッジ", "새 배지"),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = l(currentLanguage, "Hiếm • RARE", "Rare Tier", "稀有等级", "レア", "레어"),
                                                    fontSize = 10.5.sp,
                                                    color = Ink600,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = l(
                                                    currentLanguage,
                                                    "Sứ Giả Di Sản Hẻm Sài Gòn",
                                                    "Saigon Heritage Alley Envoy",
                                                    "西贡胡同文化传承使者",
                                                    "サイゴン路地裏遺産アンバサダー",
                                                    "사이공 골목 유산 앰버서더"
                                                ),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Ink900
                                            )

                                            Text(
                                                text = l(
                                                    currentLanguage,
                                                    "Đã ghi danh vào bảng vàng bảo tồn văn hóa ngõ hẻm.",
                                                    "Enshrined in the community cultural heritage registry.",
                                                    "已荣登社区胡同文化保育先锋榜。",
                                                    "コミュニティ路地裏遺産保護リストに記録されました。",
                                                    "커뮤니티 골목길 문화 보존 명예의 전당에 등재되었습니다."
                                                ),
                                                fontSize = 11.sp,
                                                color = Ink600,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Eco Impact Positive Contribution Banner
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(GrabGreen.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Nature,
                                                contentDescription = null,
                                                tint = GrabGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = l(
                                                    currentLanguage,
                                                    "Tác Động Sinh Thái & Cộng Đồng",
                                                    "Eco & Community Impact",
                                                    "生态环保与社区贡献",
                                                    "環境＆コミュニティ貢献",
                                                    "친환경 및 커뮤니티 기여"
                                                ),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreen
                                            )
                                            Text(
                                                text = l(
                                                    currentLanguage,
                                                    "Bằng việc đi bộ, bạn đã giảm ${co2SavedGrams}g khí thải CO₂ và giữ gìn sự thanh bình cho các con hẻm cổ kính.",
                                                    "By walking, you prevented ${co2SavedGrams}g of CO₂ emissions and preserved the serenity of historic alleys.",
                                                    "通过徒步探索，您减少了${co2SavedGrams}克碳排放，并守护了老胡同的宁静与温度。",
                                                    "徒歩で巡ることで${co2SavedGrams}gのCO2排出を削減し、歴史ある路地裏の静寂を守りました。",
                                                    "도보 탐험을 통해 ${co2SavedGrams}g의 탄소 배출을 줄이고 고요한 골목의 정취를 지켰습니다."
                                                ),
                                                fontSize = 11.sp,
                                                color = Ink600,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 1: CHECKPOINT STORIES & CULTURAL LORE
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                quest.stops.forEachIndexed { index, stop ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF8F5)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEFE8DE)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(GrabGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${index + 1}. ${stop.name}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        color = Ink900
                                                    )
                                                    Text(
                                                        text = stop.category,
                                                        fontSize = 10.5.sp,
                                                        color = ForestGreen,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFDCFCE7)
                                                ) {
                                                    Text(
                                                        text = "+50 XP",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ForestGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = stop.story,
                                                fontSize = 11.5.sp,
                                                color = Ink600,
                                                lineHeight = 16.sp
                                            )

                                            if (stop.whySelected.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.Top) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = SunGold,
                                                        modifier = Modifier.size(13.dp).padding(top = 1.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = stop.whySelected,
                                                        fontSize = 10.5.sp,
                                                        color = Ink600.copy(alpha = 0.85f),
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 2: ANALYTICS & INTERACTIVE PROGRESS TRACKER
                            Column(modifier = Modifier.fillMaxWidth()) {
                                QuestProgressTrackerChart(
                                    activeQuest = quest,
                                    totalWalkedMeters = distanceMetres,
                                    totalXp = xpEarned,
                                    currentLanguage = currentLanguage,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Rating & Feedback Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = l(
                                currentLanguage,
                                "ĐÁNH GIÁ TRẢI NGHIỆM HÀNH TRÌNH",
                                "RATE YOUR ALLEYWAY EXPERIENCE",
                                "为您本次胡同探索打分",
                                "体験を評価してください",
                                "골목 여정을 평가해주세요"
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = ForestGreen,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Star Rating Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= userRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Star $star",
                                    tint = SunGold,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { userRating = star }
                                )
                            }
                        }

                        val ratingLabel = when (userRating) {
                            5 -> l(currentLanguage, "⭐ 5/5 • Tuyệt hảo! Trải nghiệm vượt mong đợi", "⭐ 5/5 • Outstanding! Beyond expectations", "⭐ 5/5 • 超越预期的绝妙体验！", "⭐ 5/5 • 期待を超える素晴らしい体験！", "⭐ 5/5 • 기대를 뛰어넘는 완벽한 경험!")
                            4 -> l(currentLanguage, "⭐ 4/5 • Rất ấn tượng & bổ ích", "⭐ 4/5 • Very engaging & memorable", "⭐ 4/5 • 非常有意义且精彩", "⭐ 4/5 • とても魅力的で充実", "⭐ 4/5 • 매우 흥미롭고 보람찬 여정")
                            3 -> l(currentLanguage, "⭐ 3/5 • Tốt, cần thêm nhiều góc ẩn", "⭐ 3/5 • Good, could have more hidden spots", "⭐ 3/5 • 良好，期待发现更多秘境", "⭐ 3/5 • 良い、もっと隠れ家スポットが欲しい", "⭐ 3/5 • 좋음, 더 많은 숨은 명소 기대")
                            2 -> l(currentLanguage, "⭐ 2/5 • Tạm được", "⭐ 2/5 • Fair", "⭐ 2/5 • 尚可", "⭐ 2/5 • 普通", "⭐ 2/5 • 보통")
                            else -> l(currentLanguage, "⭐ 1/5 • Cần cải thiện", "⭐ 1/5 • Needs improvement", "⭐ 1/5 • 有待改进", "⭐ 1/5 • 改善を希望", "⭐ 1/5 • 개선 필요")
                        }

                        Text(
                            text = ratingLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        // Quick Tag Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickReviewTags.forEach { tag ->
                                val isTagSelected = selectedTags.contains(tag)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isTagSelected) ForestGreen else PaperWhite,
                                    border = BorderStroke(1.dp, if (isTagSelected) ForestGreen else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .clickable {
                                            if (isTagSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                        }
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isTagSelected) Color.White else Ink900,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = userComment,
                            onValueChange = {
                                userComment = it
                                if (isFeedbackSubmitted) isFeedbackSubmitted = false
                            },
                            placeholder = {
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Chia sẻ cảm nhận về con hẻm, văn hóa, ẩm thực...",
                                        "Share your thoughts about alley culture, coffee, heritage...",
                                        "分享您对胡同市井文化与美食的心得...",
                                        "路地裏の文化やグルメの思い出をシェア...",
                                        "골목 문화와 음식에 대한 생생한 소감을 들려주세요..."
                                    ),
                                    fontSize = 11.5.sp,
                                    color = Ink600.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PaperWhite,
                                unfocusedContainerColor = PaperWhite,
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            maxLines = 3,
                            singleLine = false
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isFeedbackSubmitted) {
                            Surface(
                                color = GrabGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
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
                                            "Cảm ơn bạn! Đánh giá đã được lưu vào hồ sơ cộng đồng.",
                                            "Thank you! Your feedback has been recorded.",
                                            "感谢您！您的评价已同步至社区探索档案。",
                                            "ありがとうございます！レビューがコミュニティに保存されました。",
                                            "감사합니다! 후기가 커뮤니티 아카이브에 등록되었습니다."
                                        ),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { isFeedbackSubmitted = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ClayOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "Gửi Đánh Giá (+20 XP)",
                                            "Submit Review (+20 XP)",
                                            "提交评价 (+20 XP)",
                                            "レビューを送信 (+20 XP)",
                                            "후기 제출 (+20 XP)"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Row: Share Receipt
                AnimatedVisibility(visible = isShareToastVisible) {
                    Surface(
                        color = ForestGreen,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = l(
                                currentLanguage,
                                "✨ Đã tạo hình ảnh chứng nhận hành trình! Hãy chia sẻ cùng bạn bè.",
                                "✨ Quest certificate generated! Ready to share.",
                                "✨ 已生成探索成就证书！快与好友分享吧。",
                                "✨ 達成証明書を作成しました！シェアしましょう。",
                                "✨ 여정 완주 증명서가 생성되었습니다! 친구들과 공유해보세요."
                            ),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // CTA Buttons
                Button(
                    onClick = onSaveAndExit,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_and_exit_quest_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = l(
                            currentLanguage,
                            "LƯU & KẾT THÚC HÀNH TRÌNH",
                            "SAVE & COMPLETE QUEST",
                            "保存并完成本次探索",
                            "保存してクエストを完了",
                            "저장 및 퀘스트 완주"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isShareToastVisible = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF64748B)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_quest_summary_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Ink600,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = l(currentLanguage, "Chia sẻ", "Share", "分享", "シェア", "공유"),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Ink900
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNewQuest,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, ClayOrange),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = ClayOrange
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("create_new_quest_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ClayOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = l(currentLanguage, "Hành trình mới", "New Quest", "开启新探索", "新しい旅", "새 퀘스트"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPillarCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    value: String,
    subValue: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PaperWhite,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = Ink900,
                maxLines = 1
            )
            Text(
                text = subValue,
                fontSize = 9.5.sp,
                color = ForestGreen,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = Ink600,
                maxLines = 1
            )
        }
    }
}
