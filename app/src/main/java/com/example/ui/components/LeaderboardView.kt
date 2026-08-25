package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthUiState
import com.example.auth.AuthViewModel
import com.example.data.UserStatsEntity
import com.example.ui.UserStatsViewModel
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DuoLime
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.SunGold
import com.example.util.l

enum class LeaderboardCriterion {
    QUESTS, DISTANCE, XP
}

enum class LeaderboardTimeframe {
    WEEKLY, MONTHLY, ALL_TIME
}

data class ExplorerProfile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val avatarBgColor: Color,
    val titleBadge: String,
    val district: String,
    val questsWeekly: Int,
    val questsMonthly: Int,
    val questsAllTime: Int,
    val distanceWeeklyMeters: Double,
    val distanceMonthlyMeters: Double,
    val distanceAllTimeMeters: Double,
    val xpPoints: Int,
    val streakDays: Int,
    val favoriteHem: String,
    val baseCheers: Int = 12,
    val isCurrentUser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardView(
    currentLanguage: String = "vi",
    userStatsViewModel: UserStatsViewModel? = null,
    authViewModel: AuthViewModel? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val authUiState by (authViewModel?.uiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(AuthUiState()) })
    val userStatsState = userStatsViewModel?.userStats?.collectAsStateWithLifecycle()
    val userStats = userStatsState?.value ?: UserStatsEntity()

    var selectedCriterion by remember { mutableStateOf(LeaderboardCriterion.QUESTS) }
    var selectedTimeframe by remember { mutableStateOf(LeaderboardTimeframe.WEEKLY) }
    var selectedDistrictFilter by remember { mutableStateOf("ALL") }
    var selectedExplorerForPassport by remember { mutableStateOf<ExplorerProfile?>(null) }
    val cheersState = remember { mutableStateMapOf<String, Int>() }

    // Live active user profile integrated into ranking calculations
    val currentUserName = authUiState.userProfile?.displayName ?: l(currentLanguage, "Bạn (Người Khám Phá)", "You (Explorer)", "你 (探索者)", "あなた (探検家)", "당신 (탐험가)")
    val currentUserCompletedQuests = userStats.completedQuestsCount
    val currentUserDistanceMeters = userStats.totalDistanceMeters

    // Base roster of Saigon Alleyway Explorers with authentic community presence
    val communityExplorers = remember(currentUserCompletedQuests, currentUserDistanceMeters, userStats.totalXp) {
        listOf(
            ExplorerProfile(
                id = "user_1",
                name = "Minh Khôi",
                avatarEmoji = "🛵",
                avatarBgColor = Color(0xFFFFE082),
                titleBadge = "Thổ Địa Tân Định",
                district = "Phường Tân Định",
                questsWeekly = 8,
                questsMonthly = 24,
                questsAllTime = 56,
                distanceWeeklyMeters = 14200.0,
                distanceMonthlyMeters = 48500.0,
                distanceAllTimeMeters = 112000.0,
                xpPoints = 2850,
                streakDays = 14,
                favoriteHem = "Hẻm 59 Lý Tự Trọng (P. Sài Gòn)",
                baseCheers = 48
            ),
            ExplorerProfile(
                id = "user_2",
                name = "Lan Anh Saigon",
                avatarEmoji = "🍜",
                avatarBgColor = Color(0xFFFFCCBC),
                titleBadge = "Nữ Hoàng Ăn Vặt Hẻm",
                district = "Phường Bàn Cờ",
                questsWeekly = 7,
                questsMonthly = 21,
                questsAllTime = 49,
                distanceWeeklyMeters = 12800.0,
                distanceMonthlyMeters = 42100.0,
                distanceAllTimeMeters = 98000.0,
                xpPoints = 2420,
                streakDays = 11,
                favoriteHem = "Hẻm Bàn Cờ 284 Lê Văn Sỹ",
                baseCheers = 39
            ),
            ExplorerProfile(
                id = "user_3",
                name = "Gia Bảo",
                avatarEmoji = "📸",
                avatarBgColor = Color(0xFFC8E6C9),
                titleBadge = "Sứ Giả Di Sản Chợ Lớn",
                district = "Phường Chợ Lớn",
                questsWeekly = 6,
                questsMonthly = 19,
                questsAllTime = 43,
                distanceWeeklyMeters = 11500.0,
                distanceMonthlyMeters = 39000.0,
                distanceAllTimeMeters = 89500.0,
                xpPoints = 2190,
                streakDays = 9,
                favoriteHem = "Hào Sĩ Phường 206 Trần Hưng Đạo",
                baseCheers = 31
            ),
            ExplorerProfile(
                id = "current_user",
                name = currentUserName,
                avatarEmoji = "🧭",
                avatarBgColor = GrabGreen.copy(alpha = 0.2f),
                titleBadge = when {
                    currentUserCompletedQuests >= 10 -> "Bậc Thầy Hẻm Sài Gòn"
                    currentUserCompletedQuests >= 5 -> "Thổ Địa Hẻm Tập Sự"
                    else -> "Nhà Thám Hiểm Hẻm"
                },
                district = "Phường Sài Gòn",
                questsWeekly = currentUserCompletedQuests,
                questsMonthly = currentUserCompletedQuests,
                questsAllTime = currentUserCompletedQuests,
                distanceWeeklyMeters = currentUserDistanceMeters,
                distanceMonthlyMeters = currentUserDistanceMeters,
                distanceAllTimeMeters = currentUserDistanceMeters,
                xpPoints = userStats.totalXp,
                streakDays = userStats.currentStreak,
                favoriteHem = "Hẻm Cà Phê Vợt Phan Đình Phùng",
                baseCheers = 0,
                isCurrentUser = true
            ),
            ExplorerProfile(
                id = "user_4",
                name = "Takeshi & Yuka",
                avatarEmoji = "☕",
                avatarBgColor = Color(0xFFE1BEE7),
                titleBadge = "Cà Phê Vợt Hunter",
                district = "Phường Đức Nhuận",
                questsWeekly = 5,
                questsMonthly = 16,
                questsAllTime = 38,
                distanceWeeklyMeters = 9800.0,
                distanceMonthlyMeters = 34500.0,
                distanceAllTimeMeters = 76000.0,
                xpPoints = 1950,
                streakDays = 7,
                favoriteHem = "Hẻm 330 Phan Đình Phùng",
                baseCheers = 27
            ),
            ExplorerProfile(
                id = "user_5",
                name = "Trúc Mai",
                avatarEmoji = "🎨",
                avatarBgColor = Color(0xFFBBDEFB),
                titleBadge = "Nhiếp Ảnh Hẻm Bích Họa",
                district = "Phường Hòa Bình",
                questsWeekly = 4,
                questsMonthly = 14,
                questsAllTime = 32,
                distanceWeeklyMeters = 8400.0,
                distanceMonthlyMeters = 29000.0,
                distanceAllTimeMeters = 64000.0,
                xpPoints = 1680,
                streakDays = 5,
                favoriteHem = "Hẻm Lồng Đèn Phú Bình (P. Hòa Bình)",
                baseCheers = 22
            ),
            ExplorerProfile(
                id = "user_6",
                name = "Hoàng Long",
                avatarEmoji = "🥟",
                avatarBgColor = Color(0xFFFFD180),
                titleBadge = "Thực Thần Sủi Cảo",
                district = "Phường Minh Phụng",
                questsWeekly = 3,
                questsMonthly = 12,
                questsAllTime = 29,
                distanceWeeklyMeters = 7200.0,
                distanceMonthlyMeters = 25000.0,
                distanceAllTimeMeters = 55000.0,
                xpPoints = 1420,
                streakDays = 4,
                favoriteHem = "Hẻm Sủi Cảo Hà Tôn Quyền (P. Minh Phụng)",
                baseCheers = 19
            ),
            ExplorerProfile(
                id = "user_7",
                name = "Elena Rostova",
                avatarEmoji = "🌸",
                avatarBgColor = Color(0xFFF8BBD0),
                titleBadge = "Saigon Heritage Wanderer",
                district = "Phường Cầu Ông Lãnh",
                questsWeekly = 3,
                questsMonthly = 10,
                questsAllTime = 25,
                distanceWeeklyMeters = 6500.0,
                distanceMonthlyMeters = 22000.0,
                distanceAllTimeMeters = 48000.0,
                xpPoints = 1250,
                streakDays = 3,
                favoriteHem = "Hẻm 14 Tôn Thất Đạm",
                baseCheers = 16
            ),
            ExplorerProfile(
                id = "user_8",
                name = "Đức Thắng",
                avatarEmoji = "🌿",
                avatarBgColor = Color(0xFFD7CCC8),
                titleBadge = "Chiến Binh Đi Bộ Xanh",
                district = "Phường Bình Thới",
                questsWeekly = 2,
                questsMonthly = 8,
                questsAllTime = 20,
                distanceWeeklyMeters = 5100.0,
                distanceMonthlyMeters = 18000.0,
                distanceAllTimeMeters = 39000.0,
                xpPoints = 980,
                streakDays = 2,
                favoriteHem = "Hẻm Di Tích Phụng Sơn Cổ Tự",
                baseCheers = 11
            )
        )
    }

    // Dynamic filtering & sorting
    val filteredExplorers = remember(communityExplorers, selectedDistrictFilter, selectedCriterion, selectedTimeframe) {
        val byDistrict = if (selectedDistrictFilter == "ALL") {
            communityExplorers
        } else {
            communityExplorers.filter { it.district.contains(selectedDistrictFilter, ignoreCase = true) || it.isCurrentUser }
        }

        byDistrict.sortedWith { a, b ->
            when (selectedCriterion) {
                LeaderboardCriterion.QUESTS -> {
                    val countA = when (selectedTimeframe) {
                        LeaderboardTimeframe.WEEKLY -> a.questsWeekly
                        LeaderboardTimeframe.MONTHLY -> a.questsMonthly
                        LeaderboardTimeframe.ALL_TIME -> a.questsAllTime
                    }
                    val countB = when (selectedTimeframe) {
                        LeaderboardTimeframe.WEEKLY -> b.questsWeekly
                        LeaderboardTimeframe.MONTHLY -> b.questsMonthly
                        LeaderboardTimeframe.ALL_TIME -> b.questsAllTime
                    }
                    countB.compareTo(countA)
                }
                LeaderboardCriterion.DISTANCE -> {
                    val distA = when (selectedTimeframe) {
                        LeaderboardTimeframe.WEEKLY -> a.distanceWeeklyMeters
                        LeaderboardTimeframe.MONTHLY -> a.distanceMonthlyMeters
                        LeaderboardTimeframe.ALL_TIME -> a.distanceAllTimeMeters
                    }
                    val distB = when (selectedTimeframe) {
                        LeaderboardTimeframe.WEEKLY -> b.distanceWeeklyMeters
                        LeaderboardTimeframe.MONTHLY -> b.distanceMonthlyMeters
                        LeaderboardTimeframe.ALL_TIME -> b.distanceAllTimeMeters
                    }
                    distB.compareTo(distA)
                }
                LeaderboardCriterion.XP -> {
                    b.xpPoints.compareTo(a.xpPoints)
                }
            }
        }
    }

    val currentUserRankIndex = filteredExplorers.indexOfFirst { it.isCurrentUser }
    val currentUserRank = if (currentUserRankIndex != -1) currentUserRankIndex + 1 else 4
    val top1 = filteredExplorers.getOrNull(0)
    val top2 = filteredExplorers.getOrNull(1)
    val top3 = filteredExplorers.getOrNull(2)
    val restOfList = filteredExplorers.drop(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF8))
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header Section with Title & Live Rank Summary
        item {
            LeaderboardHeroHeader(
                currentLanguage = currentLanguage,
                selectedCriterion = selectedCriterion,
                onCriterionSelected = { selectedCriterion = it },
                onBack = onBack
            )
        }

        // Timeframe & District Filter Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Timeframe Filter (Weekly, Monthly, All-time)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeaderboardTimeframe.values().forEach { tf ->
                        val isSelected = selectedTimeframe == tf
                        val label = when (tf) {
                            LeaderboardTimeframe.WEEKLY -> l(currentLanguage, "Tuần Này", "This Week", "本周", "今週", "이번 주")
                            LeaderboardTimeframe.MONTHLY -> l(currentLanguage, "Tháng Này", "This Month", "本月", "今月", "이번 달")
                            LeaderboardTimeframe.ALL_TIME -> l(currentLanguage, "Tất Cả", "All Time", "全部", "全期間", "전체")
                        }
                        Surface(
                            color = if (isSelected) GrabGreen else PaperWhite,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) GrabGreen else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTimeframe = tf }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ward Filter Chips Row
                val wards = listOf(
                    "ALL" to l(currentLanguage, "Toàn Thành Phố", "All Wards", "全西贡", "全サイゴン", "전체 지역"),
                    "Phường Sài Gòn" to "P. Sài Gòn",
                    "Phường Cầu Ông Lãnh" to "P. Cầu Ông Lãnh",
                    "Phường Tân Định" to "P. Tân Định",
                    "Phường Bàn Cờ" to "P. Bàn Cờ",
                    "Phường Chợ Lớn" to "P. Chợ Lớn",
                    "Phường Hòa Bình" to "P. Hòa Bình",
                    "Phường Bình Thới" to "P. Bình Thới",
                    "Phường Minh Phụng" to "P. Minh Phụng",
                    "Phường Đức Nhuận" to "P. Đức Nhuận"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(wards, key = { it.first }) { (filterKey, filterLabel) ->
                        val isSelected = selectedDistrictFilter == filterKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDistrictFilter = filterKey },
                            label = {
                                Text(
                                    text = filterLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrabGreen,
                                selectedLabelColor = Color.White,
                                containerColor = PaperWhite,
                                labelColor = Ink900
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) GrabGreen else Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Top 3 Podium Card Component
        item {
            LeaderboardPodiumSection(
                top1 = top1,
                top2 = top2,
                top3 = top3,
                criterion = selectedCriterion,
                timeframe = selectedTimeframe,
                currentLanguage = currentLanguage,
                onExplorerClick = { selectedExplorerForPassport = it }
            )
        }

        // Sticky / Featured Current User Rank Card
        item {
            CurrentUserRankBanner(
                currentRank = currentUserRank,
                currentUser = communityExplorers.firstOrNull { it.isCurrentUser },
                criterion = selectedCriterion,
                timeframe = selectedTimeframe,
                currentLanguage = currentLanguage,
                onViewPassport = {
                    communityExplorers.firstOrNull { it.isCurrentUser }?.let {
                        selectedExplorerForPassport = it
                    }
                }
            )
        }

        // Leaderboard List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = l(
                        currentLanguage,
                        "Danh Sách Người Khám Phá (${filteredExplorers.size})",
                        "Explorers Ranking (${filteredExplorers.size})",
                        "探索者榜单 (${filteredExplorers.size})",
                        "探検家ランキング (${filteredExplorers.size})",
                        "탐험가 랭킹 (${filteredExplorers.size})"
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink900
                )

                Text(
                    text = when (selectedCriterion) {
                        LeaderboardCriterion.QUESTS -> l(currentLanguage, "Theo số nhiệm vụ", "By completed quests", "按任务数", "クエスト数順", "퀘스트 순")
                        LeaderboardCriterion.DISTANCE -> l(currentLanguage, "Theo quãng đường", "By walking distance", "按总距离", "歩行距離順", "거리 순")
                        LeaderboardCriterion.XP -> l(currentLanguage, "Theo điểm Thổ Địa", "By explorer XP", "按探索积分", "XP順", "XP 순")
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink600
                )
            }
        }

        // Ranked Explorers List (Rank #4+)
        itemsIndexed(restOfList, key = { _, explorer -> explorer.id }) { index, explorer ->
            val rank = index + 4
            val currentCheers = (cheersState[explorer.id] ?: 0) + explorer.baseCheers

            ExplorerRankCard(
                rank = rank,
                explorer = explorer,
                criterion = selectedCriterion,
                timeframe = selectedTimeframe,
                cheers = currentCheers,
                currentLanguage = currentLanguage,
                onCheerClick = {
                    cheersState[explorer.id] = (cheersState[explorer.id] ?: 0) + 1
                },
                onClick = { selectedExplorerForPassport = explorer }
            )
        }
    }

    // Modal Passport Dialog for Selected Explorer
    selectedExplorerForPassport?.let { explorer ->
        ExplorerPassportDialog(
            explorer = explorer,
            currentLanguage = currentLanguage,
            criterion = selectedCriterion,
            timeframe = selectedTimeframe,
            cheers = (cheersState[explorer.id] ?: 0) + explorer.baseCheers,
            onCheer = {
                cheersState[explorer.id] = (cheersState[explorer.id] ?: 0) + 1
            },
            onDismiss = { selectedExplorerForPassport = null }
        )
    }
}

@Composable
private fun LeaderboardHeroHeader(
    currentLanguage: String,
    selectedCriterion: LeaderboardCriterion,
    onCriterionSelected: (LeaderboardCriterion) -> Unit,
    onBack: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (onBack != null) {
                        Surface(
                            onClick = onBack,
                            shape = CircleShape,
                            color = Color(0xFFF1F5F2),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = l(currentLanguage, "Quay lại", "Back", "返回", "戻る", "뒤로"),
                                    tint = Ink900,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = SunGold.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = SunGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = l(
                                currentLanguage,
                                "Bảng Xếp Hạng Hẻm",
                                "Saigon Alleyway Leaderboard",
                                "西贡胡同探索排行榜",
                                "サイゴン路地裏リーダーボード",
                                "사이공 골목길 리더보드"
                            ),
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = l(
                                currentLanguage,
                                "Vinh danh những bước chân khám phá hẻm",
                                "Honoring top alleyway explorers & walkers",
                                "致敬穿梭于西贡弄堂的探索者",
                                "路地裏を歩き続ける探検家たちの記録",
                                "골목을 탐험하는 모든 발걸음"
                            ),
                            fontSize = 11.5.sp,
                            color = Ink600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = GrabGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = l(currentLanguage, "Trực tiếp", "Live", "实时", "LIVE", "실시간"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sort Metric Tabs (Quests Completed, Distance Walked, Cultural XP)
            TabRow(
                selectedTabIndex = selectedCriterion.ordinal,
                containerColor = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCriterion.ordinal]),
                        height = 3.dp,
                        color = GrabGreen
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedCriterion == LeaderboardCriterion.QUESTS,
                    onClick = { onCriterionSelected(LeaderboardCriterion.QUESTS) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (selectedCriterion == LeaderboardCriterion.QUESTS) GrabGreen else Ink600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Nhiệm Vụ", "Quests", "任务数", "クエスト", "퀘스트"),
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedCriterion == LeaderboardCriterion.QUESTS) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedCriterion == LeaderboardCriterion.QUESTS) GrabGreen else Ink600,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedCriterion == LeaderboardCriterion.DISTANCE,
                    onClick = { onCriterionSelected(LeaderboardCriterion.DISTANCE) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (selectedCriterion == LeaderboardCriterion.DISTANCE) GrabGreen else Ink600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Quãng Đường", "Distance", "总里程", "歩行距離", "거리"),
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedCriterion == LeaderboardCriterion.DISTANCE) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedCriterion == LeaderboardCriterion.DISTANCE) GrabGreen else Ink600,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedCriterion == LeaderboardCriterion.XP,
                    onClick = { onCriterionSelected(LeaderboardCriterion.XP) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (selectedCriterion == LeaderboardCriterion.XP) GrabGreen else Ink600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Điểm Xanh", "Eco XP", "探索积分", "エコXP", "에코 XP"),
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedCriterion == LeaderboardCriterion.XP) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedCriterion == LeaderboardCriterion.XP) GrabGreen else Ink600,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LeaderboardPodiumSection(
    top1: ExplorerProfile?,
    top2: ExplorerProfile?,
    top3: ExplorerProfile?,
    criterion: LeaderboardCriterion,
    timeframe: LeaderboardTimeframe,
    currentLanguage: String,
    onExplorerClick: (ExplorerProfile) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 " + l(currentLanguage, "Top 3 Bậc Thầy Hẻm Sài Gòn", "Top 3 Alleyway Masters", "前三名西贡弄堂领袖", "トップ3路地裏マスター", "골목 마스터 TOP 3"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Ink900
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Pillar Podium Row (2nd on left, 1st in center elevated, 3rd on right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Silver)
                if (top2 != null) {
                    PodiumColumn(
                        explorer = top2,
                        rank = 2,
                        pedestalHeight = 90.dp,
                        crownEmoji = "🥈",
                        pedestalColor = Color(0xFFE2E8F0),
                        accentColor = Color(0xFF64748B),
                        criterion = criterion,
                        timeframe = timeframe,
                        currentLanguage = currentLanguage,
                        modifier = Modifier.weight(1f),
                        onClick = { onExplorerClick(top2) }
                    )
                }

                // Rank 1 (Gold - Center & Elevated)
                if (top1 != null) {
                    PodiumColumn(
                        explorer = top1,
                        rank = 1,
                        pedestalHeight = 120.dp,
                        crownEmoji = "👑",
                        pedestalColor = SunGold.copy(alpha = 0.25f),
                        accentColor = SunGold,
                        criterion = criterion,
                        timeframe = timeframe,
                        currentLanguage = currentLanguage,
                        modifier = Modifier.weight(1.15f),
                        onClick = { onExplorerClick(top1) }
                    )
                }

                // Rank 3 (Bronze)
                if (top3 != null) {
                    PodiumColumn(
                        explorer = top3,
                        rank = 3,
                        pedestalHeight = 75.dp,
                        crownEmoji = "🥉",
                        pedestalColor = ClayOrange.copy(alpha = 0.2f),
                        accentColor = ClayOrange,
                        criterion = criterion,
                        timeframe = timeframe,
                        currentLanguage = currentLanguage,
                        modifier = Modifier.weight(1f),
                        onClick = { onExplorerClick(top3) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    explorer: ExplorerProfile,
    rank: Int,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    crownEmoji: String,
    pedestalColor: Color,
    accentColor: Color,
    criterion: LeaderboardCriterion,
    timeframe: LeaderboardTimeframe,
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crown / Rank Medal Badge
        Text(text = crownEmoji, fontSize = if (rank == 1) 22.sp else 18.sp)

        // Explorer Avatar with Rank Border
        Surface(
            color = explorer.avatarBgColor,
            shape = CircleShape,
            border = BorderStroke(if (rank == 1) 3.dp else 2.dp, accentColor),
            modifier = Modifier.size(if (rank == 1) 56.dp else 48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = explorer.avatarEmoji,
                    fontSize = if (rank == 1) 26.sp else 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Name
        Text(
            text = explorer.name,
            fontSize = if (rank == 1) 13.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = Ink900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Primary Stat Metric
        val primaryStatText = when (criterion) {
            LeaderboardCriterion.QUESTS -> {
                val qCount = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> explorer.questsWeekly
                    LeaderboardTimeframe.MONTHLY -> explorer.questsMonthly
                    LeaderboardTimeframe.ALL_TIME -> explorer.questsAllTime
                }
                "$qCount " + l(currentLanguage, "nhiệm vụ", "quests", "任务", "クエ", "퀘스트")
            }
            LeaderboardCriterion.DISTANCE -> {
                val distMeters = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> explorer.distanceWeeklyMeters
                    LeaderboardTimeframe.MONTHLY -> explorer.distanceMonthlyMeters
                    LeaderboardTimeframe.ALL_TIME -> explorer.distanceAllTimeMeters
                }
                String.format(java.util.Locale.US, "%.1f km", distMeters / 1000.0)
            }
            LeaderboardCriterion.XP -> "${explorer.xpPoints} XP"
        }

        Text(
            text = primaryStatText,
            fontSize = if (rank == 1) 11.sp else 10.sp,
            fontWeight = FontWeight.Black,
            color = ForestGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal block with Rank Number
        Surface(
            color = pedestalColor,
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "#$rank",
                        fontSize = if (rank == 1) 24.sp else 18.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                    Text(
                        text = explorer.district,
                        fontSize = 9.sp,
                        color = Ink600,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentUserRankBanner(
    currentRank: Int,
    currentUser: ExplorerProfile?,
    criterion: LeaderboardCriterion,
    timeframe: LeaderboardTimeframe,
    currentLanguage: String,
    onViewPassport: () -> Unit
) {
    if (currentUser == null) return

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, GrabGreen.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onViewPassport() }
            .testTag("current_user_rank_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Rank badge + Avatar + Name & location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Rank badge
                Surface(
                    color = GrabGreen,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#$currentRank",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar
                Surface(
                    color = currentUser.avatarBgColor,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, GrabGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = currentUser.avatarEmoji, fontSize = 19.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentUser.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = ForestGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = l(currentLanguage, "Bạn", "You", "你", "あなた", "나"),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📍 ${currentUser.district}",
                            fontSize = 11.sp,
                            color = Ink600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔥 ${currentUser.streakDays}d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StreakFlame,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Primary stat display + tap indicator
            val statValue = when (criterion) {
                LeaderboardCriterion.QUESTS -> {
                    val qCount = when (timeframe) {
                        LeaderboardTimeframe.WEEKLY -> currentUser.questsWeekly
                        LeaderboardTimeframe.MONTHLY -> currentUser.questsMonthly
                        LeaderboardTimeframe.ALL_TIME -> currentUser.questsAllTime
                    }
                    "$qCount" to l(currentLanguage, "nhiệm vụ", "quests", "任务", "クエ", "퀘스트")
                }
                LeaderboardCriterion.DISTANCE -> {
                    val distMeters = when (timeframe) {
                        LeaderboardTimeframe.WEEKLY -> currentUser.distanceWeeklyMeters
                        LeaderboardTimeframe.MONTHLY -> currentUser.distanceMonthlyMeters
                        LeaderboardTimeframe.ALL_TIME -> currentUser.distanceAllTimeMeters
                    }
                    String.format(java.util.Locale.US, "%.1f", distMeters / 1000.0) to "km"
                }
                LeaderboardCriterion.XP -> "${currentUser.xpPoints}" to "XP"
            }

            Surface(
                color = PaperWhite,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = statValue.first,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = ForestGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = statValue.second,
                            fontSize = 10.sp,
                            color = Ink600,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "›",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink600
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplorerRankCard(
    rank: Int,
    explorer: ExplorerProfile,
    criterion: LeaderboardCriterion,
    timeframe: LeaderboardTimeframe,
    cheers: Int,
    currentLanguage: String,
    onCheerClick: () -> Unit,
    onClick: () -> Unit
) {
    val isMe = explorer.isCurrentUser
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFFF0FDF4) else PaperWhite
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isMe) BorderStroke(1.2.dp, GrabGreen.copy(alpha = 0.4f)) else BorderStroke(0.8.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMe) 2.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.5.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { onClick() }
            .testTag("explorer_rank_card_$rank")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "#$rank",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (rank <= 5) ForestGreen else Ink600,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Avatar
            Surface(
                color = explorer.avatarBgColor,
                shape = CircleShape,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = explorer.avatarEmoji, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name & District
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = explorer.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = GrabGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = l(currentLanguage, "Bạn", "You", "你", "あなた", "나"),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "📍 ${explorer.district}",
                    fontSize = 11.sp,
                    color = Ink600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Stat Value for active tab
            val statText = when (criterion) {
                LeaderboardCriterion.QUESTS -> {
                    val qCount = when (timeframe) {
                        LeaderboardTimeframe.WEEKLY -> explorer.questsWeekly
                        LeaderboardTimeframe.MONTHLY -> explorer.questsMonthly
                        LeaderboardTimeframe.ALL_TIME -> explorer.questsAllTime
                    }
                    "$qCount " + l(currentLanguage, "q", "q", "次", "件", "회")
                }
                LeaderboardCriterion.DISTANCE -> {
                    val distMeters = when (timeframe) {
                        LeaderboardTimeframe.WEEKLY -> explorer.distanceWeeklyMeters
                        LeaderboardTimeframe.MONTHLY -> explorer.distanceMonthlyMeters
                        LeaderboardTimeframe.ALL_TIME -> explorer.distanceAllTimeMeters
                    }
                    String.format(java.util.Locale.US, "%.1f km", distMeters / 1000.0)
                }
                LeaderboardCriterion.XP -> "${explorer.xpPoints} XP"
            }

            Text(
                text = statText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = ForestGreen,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Cheer Button
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.clickable { onCheerClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "👏", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$cheers",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink900,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplorerPassportDialog(
    explorer: ExplorerProfile,
    currentLanguage: String,
    criterion: LeaderboardCriterion,
    timeframe: LeaderboardTimeframe,
    cheers: Int,
    onCheer: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            shape = RoundedCornerShape(26.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Top close & header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛂", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Hộ Chiếu Thám Hiểm Hẻm",
                                "Saigon Explorer Passport",
                                "西贡弄堂探索护照",
                                "サイゴン探検パスポート",
                                "사이공 탐험 여권"
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Ink600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar & Identity Card
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = explorer.avatarBgColor,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, ForestGreen),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = explorer.avatarEmoji, fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = explorer.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink900
                            )
                            Text(
                                text = "🎖️ ${explorer.titleBadge}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Text(
                                text = "📍 ${explorer.district} • 🔥 ${explorer.streakDays} ${l(currentLanguage, "ngày liên tiếp", "days streak", "天连胜", "日連続", "일 연속")}",
                                fontSize = 11.sp,
                                color = Ink600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Key Statistics 3-Box Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: Quests
                    Surface(
                        color = GrabGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${explorer.questsAllTime}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = ForestGreen
                            )
                            Text(
                                text = l(currentLanguage, "Nhiệm Vụ", "Quests", "完成任务", "クエスト", "퀘스트"),
                                fontSize = 10.sp,
                                color = Ink600,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Box 2: Total Walking Distance
                    val totalKm = String.format(java.util.Locale.US, "%.1f km", explorer.distanceAllTimeMeters / 1000.0)
                    Surface(
                        color = Color(0xFF0288D1).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = totalKm,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0288D1)
                            )
                            Text(
                                text = l(currentLanguage, "Đã Đi Bộ", "Walked", "步行里程", "歩行総計", "총 도보"),
                                fontSize = 10.sp,
                                color = Ink600,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Box 3: Total XP Points
                    Surface(
                        color = SunGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${explorer.xpPoints}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = l(currentLanguage, "Điểm Xanh", "Eco XP", "探索积分", "エコXP", "에코 XP"),
                                fontSize = 10.sp,
                                color = Ink600,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Favorite Alleyway Quote / Specialty
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SunGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "❤️ " + l(currentLanguage, "Hẻm Yêu Thích Nhất:", "Favorite Saigon Alley:", "最钟爱的西贡弄堂：", "お気に入りの路地裏:", "가장 좋아하는 골목:"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = explorer.favoriteHem,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Cheer Button Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCheer,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "👏 " + l(currentLanguage, "Gửi Cổ Vũ ($cheers)", "Send Cheers ($cheers)", "送上掌声 ($cheers)", "拍手を送る ($cheers)", "응원 보내기 ($cheers)"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
