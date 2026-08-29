package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuestCategory
import com.example.model.QuestRequest
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DuoLime
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

data class FeaturedQuest(
    val title: String,
    val district: String,
    val duration: String,
    val distance: String,
    val stopsCount: Int,
    val rating: String,
    val reviewsCount: Int,
    val xp: String,
    val emoji: String,
    val tagline: String,
    val tags: List<String>,
    val bgColors: List<Color>,
    val request: QuestRequest,
    val category: QuestCategory = QuestCategory.HIDDEN_ALLEYS
)

@Composable
fun ExploreScreen(
    currentLanguage: String = "vi",
    greenScore: Int = 120,
    streak: Int = 3,
    xp: Int = 450,
    onSetLanguage: (String) -> Unit = {},
    onOpenGreenScore: () -> Unit = {},
    onOpenStreak: () -> Unit = {},
    onOpenXp: () -> Unit = {},
    onOpenBuilder: () -> Unit = {},
    onOpenGlossary: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {},
    onStartQuest: (QuestRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredQuests = listOf(
        FeaturedQuest(
            title = l(currentLanguage, "Cà Phê Vợt & Hẻm Di Sản Sài Gòn", "Net Filter Coffee & Heritage Alley", "西贡网滤咖啡与历史胡同", "サイゴン 網フィルターコーヒーと歴史路地", "사이공 그물 필터 커피 & 헤리티지 골목"),
            district = l(currentLanguage, "Phường Sài Gòn", "Saigon Ward", "西贡坊", "サイゴン坊", "사이공동"),
            duration = "45m",
            distance = "1.2 km",
            stopsCount = 4,
            rating = "4.9",
            reviewsCount = 128,
            xp = "+150 XP",
            emoji = "☕",
            tagline = l(currentLanguage, "Hương vị cà phê vợt 70 năm & nét sinh hoạt hẻm xưa", "70-year old net coffee aroma & authentic alley life", "70年老网滤咖啡香与西贡弄堂岁月", "70年の伝統網コーヒーと懐かしい路地の日常", "70년 전통 그물 커피 향과 옛 골목의 정취"),
            tags = listOf(
                l(currentLanguage, "☕ Cà phê vợt", "☕ Net Coffee", "☕ 网滤咖啡", "☕ 網コーヒー", "☕ 그물커피"),
                l(currentLanguage, "🌿 Hẻm râm mát", "🌿 Shaded Alley", "🌿 林荫弄堂", "🌿 木陰の路地", "🌿 그늘진 골목"),
                l(currentLanguage, "🚶 Dễ đi bộ", "🚶 Easy Walk", "🚶 适合步行", "🚶 歩きやすい", "🚶 걷기 편함")
            ),
            bgColors = listOf(Color(0xFF0F5132), Color(0xFF00873D)),
            request = QuestRequest(
                startingLocationName = "Phường Sài Gòn, TP.HCM",
                latitude = 10.774312,
                longitude = 106.703125,
                durationMinutes = 45,
                interests = listOf("Hidden Alleys", "Coffee Culture"),
                freeTextNotes = "Net filter coffee & hidden alleyways",
                language = currentLanguage
            ),
            category = QuestCategory.HIDDEN_ALLEYS
        ),
        FeaturedQuest(
            title = l(currentLanguage, "Bách Khoa Sài Gòn & Hẻm Sinh Viên", "HCMUT Bách Khoa & Student Alleyways", "胡志明市理工大学与青春学生巷弄", "ホーチミン工科大学（BK）＆学生街のヘム", "호치민 공과대학(BK) & 대학가 청춘 골목"),
            district = l(currentLanguage, "Phường Diên Hồng", "Dien Hong Ward", "延洪坊", "ディエンホン坊", "디엔홍동"),
            duration = "45m",
            distance = "1.2 km",
            stopsCount = 5,
            rating = "4.96",
            reviewsCount = 168,
            xp = "+190 XP",
            emoji = "🎓",
            tagline = l(currentLanguage, "Đời sống sinh viên BK, ẩm thực Tô Hiến Thành & cà phê đồ án Lữ Gia", "HCMUT student life, Tô Hiến Thành food alley & maker cafes", "理工大学青春生活、苏宪成小吃街与创客咖啡馆", "工科大の学生街・トーヒエンタイン屋台と設計カフェ", "BK 대학가 청춘, 토히엔탄 먹거리 & 르자 스터디 카페"),
            tags = listOf(
                l(currentLanguage, "🎓 ĐH Bách Khoa", "🎓 HCMUT Campus", "🎓 理工大学", "🎓 工科大", "🎓 공과대학"),
                l(currentLanguage, "🍜 Ẩm thực Tô Hiến Thành", "🍜 Food Alley", "🍜 美食街", "🍜 屋台街", "🍜 먹거리 골목"),
                l(currentLanguage, "☕ Cà phê đồ án", "☕ Maker Cafe", "☕ 创客咖啡", "☕ 設計カフェ", "☕ 스터디 카페")
            ),
            bgColors = listOf(Color(0xFF1E40AF), Color(0xFF3B82F6)),
            request = QuestRequest(
                startingLocationName = "Phường Diên Hồng, TP.HCM",
                latitude = 10.772500,
                longitude = 106.658200,
                durationMinutes = 45,
                interests = listOf("Student Life", "Street Food", "Academic Heritage"),
                freeTextNotes = "Bách Khoa student food, HCMUT campus & Lữ Gia study cafes",
                language = currentLanguage
            ),
            category = QuestCategory.HIDDEN_ALLEYS
        ),
        FeaturedQuest(
            title = l(currentLanguage, "Hẻm Ẩm Thực & Hội Quán Chợ Lớn", "Street Food & Guild Halls (Chợ Lớn)", "堤岸美食寻味与百年会馆", "チョロン 屋台グルメ＆歴史会館", "쩌롱 먹거리 골목 & 유서 깊은 회관"),
            district = l(currentLanguage, "Phường Chợ Lớn", "Cho Lon Ward", "堤岸坊", "チョロン坊", "쩌롱동"),
            duration = "60m",
            distance = "1.8 km",
            stopsCount = 5,
            rating = "4.95",
            reviewsCount = 215,
            xp = "+200 XP",
            emoji = "🍜",
            tagline = l(currentLanguage, "Hội quán cổ kính, dimsum truyền thống & trà thuốc Bắc", "Centuries-old assembly halls, dim sum & herbal tea", "百年会馆古韵、传统点心与老字号青草茶", "歴史ある会館・伝統飲茶と漢方ハーブティー", "유서 깊은 회관, 전통 딤섬과 한방차"),
            tags = listOf(
                l(currentLanguage, "🥟 Dim sum gia truyền", "🥟 Dim Sum", "🥟 传统点心", "🥟 伝統点心", "🥟 전통 딤섬"),
                l(currentLanguage, "🏮 Hội quán cổ", "🏮 Ancient Guild", "🏮 百年会馆", "🏮 歴史会館", "🏮 전통 회관"),
                l(currentLanguage, "🍵 Trà thuốc Bắc", "🍵 Herbal Tea", "🍵 凉茶草药", "🍵 漢方茶", "🍵 한방차")
            ),
            bgColors = listOf(Color(0xFFC2410C), Color(0xFFEA580C)),
            request = QuestRequest(
                startingLocationName = "Phường Chợ Lớn, TP.HCM",
                latitude = 10.753361,
                longitude = 106.661755,
                durationMinutes = 60,
                interests = listOf("Street Food", "Hidden History"),
                freeTextNotes = "Chợ Lớn street food and ancient temples",
                language = currentLanguage
            ),
            category = QuestCategory.STREET_FOOD
        ),
        FeaturedQuest(
            title = l(currentLanguage, "Biệt Động Sài Gòn & Hầm Bí Mật", "Secret Commandos & Bunker Trail", "西贡特工地下军火库寻踪", "サイゴン別動隊と秘密史迹ヘム", "사이공 특공대 비밀 기지"),
            district = l(currentLanguage, "Phường Bàn Cờ", "Ban Co Ward", "棋盘坊", "バンコー坊", "반꺼동"),
            duration = "30m",
            distance = "0.8 km",
            stopsCount = 3,
            rating = "4.88",
            reviewsCount = 94,
            xp = "+120 XP",
            emoji = "🎖️",
            tagline = l(currentLanguage, "Hầm bí mật giấu vũ khí trong lòng nhà phố Sài Gòn", "Secret weapon bunker hidden inside a local shophouse", "民居深处的秘密地下军火库与传奇岁月", "下町民家に隠された秘密武器地下庫を巡る", "도심 주택 아래 숨겨진 비밀 무기 기지"),
            tags = listOf(
                l(currentLanguage, "🎖️ Di tích lịch sử", "🎖️ Historic Site", "🎖️ 历史遗迹", "🎖️ 歴史遺産", "🎖️ 역사 유적"),
                l(currentLanguage, "🧱 Hầm bí mật", "🧱 Secret Bunker", "🧱 地下军火库", "🧱 秘密地下壕", "🧱 비밀 기지"),
                l(currentLanguage, "📸 Check-in độc đáo", "📸 Unique Spot", "📸 独特打卡", "📸 注目スポット", "📸 이색 명소")
            ),
            bgColors = listOf(Color(0xFF6D28D9), Color(0xFF7C3AED)),
            request = QuestRequest(
                startingLocationName = "Phường Bàn Cờ, TP.HCM",
                latitude = 10.782527,
                longitude = 106.695889,
                durationMinutes = 30,
                interests = listOf("History", "Architecture"),
                freeTextNotes = "Secret commandos bunkers & colonial architecture",
                language = currentLanguage
            ),
            category = QuestCategory.HISTORY
        ),
        FeaturedQuest(
            title = l(currentLanguage, "Làng Lồng Đèn Phú Bình & Thủ Công", "Phu Binh Lantern Village & Local Crafts", "富平灯笼传统手艺村", "フービン ランタン作りと伝統工芸街", "푸빈 등불 전통 공예 마을"),
            district = l(currentLanguage, "Phường Hòa Bình", "Hoa Binh Ward", "和平坊", "ホアビン坊", "화빈동"),
            duration = "50m",
            distance = "1.5 km",
            stopsCount = 4,
            rating = "4.92",
            reviewsCount = 142,
            xp = "+180 XP",
            emoji = "🏮",
            tagline = l(currentLanguage, "Làng nghề lồng đèn giấy kiếng & xưởng mộc thủ công", "Traditional cellophane lantern village & artisan ateliers", "玻璃纸传统灯笼手艺村与匠心工坊", "セロファン伝統ランタン工房と職人街", "전통 셀로판 등불 공예 마을과 공방"),
            tags = listOf(
                l(currentLanguage, "🏮 Lồng đèn kiếng", "🏮 Glass Lanterns", "🏮 玻璃纸灯笼", "🏮 伝統ランタン", "🏮 셀로판 등불"),
                l(currentLanguage, "🎨 Làng nghề", "🎨 Artisan Craft", "🎨 传统手艺", "🎨 職人技", "🎨 전통 공예"),
                l(currentLanguage, "✨ Trải nghiệm", "✨ Hands-on", "✨ 深度体验", "✨ 体験", "✨ 체험")
            ),
            bgColors = listOf(Color(0xFFB45309), Color(0xFFD97706)),
            request = QuestRequest(
                startingLocationName = "Phường Hòa Bình, TP.HCM",
                latitude = 10.763812,
                longitude = 106.649231,
                durationMinutes = 50,
                interests = listOf("Local Crafts", "Street Life"),
                freeTextNotes = "Lantern making workshops & artisan alleyways",
                language = currentLanguage
            ),
            category = QuestCategory.LOCAL_CRAFTS
        ),
        FeaturedQuest(
            title = l(currentLanguage, "Hẻm Biệt Thự Cổ & Cà Phê Vườn", "French Colonial Villa Alley & Garden Cafe", "法式古墅与优雅庭院咖啡", "フレンチコロニアル洋館路地＆ガーデンカフェ", "프렌치 빌라 골목 & 가든 카페"),
            district = l(currentLanguage, "Phường Xuân Hòa", "Xuan Hoa Ward", "春和坊", "スアンホア坊", "쑤언호아동"),
            duration = "40m",
            distance = "1.0 km",
            stopsCount = 3,
            rating = "4.85",
            reviewsCount = 88,
            xp = "+140 XP",
            emoji = "🏛️",
            tagline = l(currentLanguage, "Kiến trúc thuộc địa Pháp thế kỷ 20 & sân vườn râm mát", "20th-century colonial villas & green courtyard cafes", "20世纪法式古典别墅与绿荫庭院咖啡馆", "20世紀初頭の仏蘭西洋館と木陰の庭園カフェ", "20세기 초 프랑스식 고택과 정원 카페"),
            tags = listOf(
                l(currentLanguage, "🏛️ Biệt thự cổ", "🏛️ French Villa", "🏛️ 法式建筑", "🏛️ 歴史的洋館", "🏛️ 프랑스 고택"),
                l(currentLanguage, "🪴 Cà phê vườn", "🪴 Garden Cafe", "🪴 庭院咖啡", "🪴 庭園カフェ", "🪴 가든 카페"),
                l(currentLanguage, "📷 Chụp ảnh đẹp", "📷 Photogenic", "📷 绝佳摄影", "📷 映えスポット", "📷 사진 명소")
            ),
            bgColors = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB)),
            request = QuestRequest(
                startingLocationName = "Phường Xuân Hòa, TP.HCM",
                latitude = 10.781211,
                longitude = 106.691422,
                durationMinutes = 40,
                interests = listOf("Architecture", "Photography"),
                freeTextNotes = "French colonial architecture & shaded courtyards",
                language = currentLanguage
            ),
            category = QuestCategory.ARCHITECTURE
        )
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(QuestCategory.ALL) }
    var selectedDistrictFilter by remember { mutableStateOf("") }
    var lastStartClickTime by remember { mutableStateOf(0L) }

    val safeStartQuest: (QuestRequest) -> Unit = { req ->
        val now = System.currentTimeMillis()
        if (now - lastStartClickTime > 800L) {
            lastStartClickTime = now
            onStartQuest(req)
        }
    }

    val districtFilters = listOf(
        "" to l(currentLanguage, "Tất cả phường/khu vực", "All Wards", "全部区域", "全地域", "전체 지역"),
        "Diên Hồng" to l(currentLanguage, "🎓 P. Diên Hồng (Bách Khoa)", "🎓 Dien Hong (HCMUT BK)", "🎓 延洪坊（理工大）", "🎓 ディエンホン（工科大）", "🎓 디엔홍 (공과대)"),
        "Sài Gòn" to l(currentLanguage, "📍 P. Sài Gòn", "📍 Saigon Ward", "📍 西贡坊", "📍 サイゴン坊", "📍 사이공동"),
        "Chợ Lớn" to l(currentLanguage, "📍 P. Chợ Lớn", "📍 Cho Lon Ward", "📍 堤岸坊", "📍 チョロン坊", "📍 쩌롱동"),
        "Bàn Cờ" to l(currentLanguage, "📍 P. Bàn Cờ", "📍 Ban Co Ward", "📍 棋盘坊", "📍 バンコー坊", "📍 반꺼동"),
        "Xuân Hòa" to l(currentLanguage, "📍 P. Xuân Hòa", "📍 Xuan Hoa Ward", "📍 春和坊", "📍 スアンホア坊", "📍 쑤언호아동"),
        "Hòa Bình" to l(currentLanguage, "📍 P. Hòa Bình", "📍 Hoa Binh Ward", "📍 和平坊", "📍 ホアビン坊", "📍 화빈동"),
        "Minh Phụng" to l(currentLanguage, "📍 P. Minh Phụng", "📍 Minh Phung Ward", "📍 明凤坊", "📍 ミンフン坊", "📍 민풍동"),
        "Bình Thới" to l(currentLanguage, "📍 P. Bình Thới", "📍 Binh Thoi Ward", "📍 平泰坊", "📍 ビントイ坊", "📍 빈터이동")
    )

    val filteredQuests = featuredQuests.filter { quest ->
        val matchesDistrict = selectedDistrictFilter.isBlank() ||
            quest.district.contains(selectedDistrictFilter, ignoreCase = true) ||
            quest.request.startingLocationName.contains(selectedDistrictFilter, ignoreCase = true)

        val matchesSearch = searchQuery.isBlank() ||
            quest.title.contains(searchQuery, ignoreCase = true) ||
            quest.district.contains(searchQuery, ignoreCase = true) ||
            quest.request.interests.any { it.contains(searchQuery, ignoreCase = true) } ||
            quest.request.freeTextNotes.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == QuestCategory.ALL ||
            quest.category == selectedCategory ||
            quest.request.interests.any { interest ->
                interest.equals(selectedCategory.interestTag, ignoreCase = true) ||
                interest.contains(selectedCategory.defaultTitleEn, ignoreCase = true)
            }

        matchesDistrict && matchesSearch && matchesCategory
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F4))
            .testTag("explore_screen")
    ) {
        // App Header Bar
        HeaderBar(
            isVi = currentLanguage == "vi",
            greenScore = greenScore,
            currentLanguage = currentLanguage,
            streak = streak,
            xp = xp,
            onSetLanguage = onSetLanguage,
            onOpenGreenScore = onOpenGreenScore,
            onOpenStreak = onOpenStreak,
            onOpenXp = onOpenXp,
            onOpenBuilder = onOpenBuilder,
            onOpenGlossary = onOpenGlossary
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Welcome Greeting
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛵",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Xin chào, Nhà Thám Hiểm!",
                                "Welcome, Explorer!",
                                "你好，胡同探险家！",
                                "ようこそ、探検家さん！",
                                "안녕하세요, 탐험가님!"
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900
                        )
                    }
                    Text(
                        text = l(
                            currentLanguage,
                            "Khám phá câu chuyện ẩn sau từng con hẻm Sài Gòn",
                            "Uncover hidden stories in Saigon's alleyways",
                            "探索西贡深巷中的故事与风情",
                            "サイゴン路地裏の隠れた story を見つけよう",
                            "사이공 골목 속에 숨겨진 이야기를 찾아보세요"
                        ),
                        fontSize = 12.5.sp,
                        color = Ink600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Hero Header Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00B14F), Color(0xFF0F5132))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "GỢI Ý HÔM NAY",
                                            "FEATURED FOR YOU",
                                            "今日精选推荐",
                                            "本日のおすすめ",
                                            "오늘의 추천"
                                        ),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = l(
                                    currentLanguage,
                                    "Khám Phá Sài Gòn Qua Những Con Hẻm",
                                    "Discover Saigon's Hidden Alleyways",
                                    "穿梭胡志明市经典巷弄探索故事",
                                    "細い路地から探るサイゴンの魅力",
                                    "골목길 따라 탐방하는 사이공"
                                ),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = l(
                                    currentLanguage,
                                    "Mỗi con hẻm là một câu chuyện lịch sử, hương vị cà phê và nụ cười địa phương.",
                                    "Every alley holds history, net coffee aromas, and warm local smiles.",
                                    "每一条小巷都蕴藏着风情历史、沉香咖啡与地道的人情味。",
                                    "路地ごとに息づく歴史、珈琲の香り、그리고 人々の温かい笑顔。",
                                    "골목마다 깃든 역사와 정겨운 커피 향, 현지인들의 따뜻한 미소."
                                ),
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        val aiRequest = createAiSurpriseQuestRequest(currentLanguage)
                                        safeStartQuest(aiRequest)
                                    },
                                    color = SunGold,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("✨", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "AI Gợi Ý Tự Động",
                                                "AI Surprise Me!",
                                                "AI 智能生成路线",
                                                "AIサプライズ生成",
                                                "AI 추천 퀘스트"
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Ink900,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        if (featuredQuests.isNotEmpty()) {
                                            val randomQuest = featuredQuests.random()
                                            safeStartQuest(randomQuest.request)
                                        }
                                    },
                                    color = PaperWhite,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("🎲", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Quest Chọn Lọc",
                                                "Featured Quest",
                                                "精选固定路线",
                                                "おすすめランダム",
                                                "추천 퀘스트"
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cultural Glossary Feature Banner
            item {
                CulturalGlossaryBanner(
                    currentLanguage = currentLanguage,
                    onOpenGlossary = onOpenGlossary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Saigon Alleyway Explorers Leaderboard Spotlight Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onOpenLeaderboard() }
                        .testTag("leaderboard_spotlight_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
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
                                Surface(
                                    color = SunGold.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🏆", fontSize = 19.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Bảng Xếp Hạng Hẻm",
                                                "Saigon Leaderboard",
                                                "弄堂探索排行榜",
                                                "路地裏ランキング",
                                                "골목길 리더보드"
                                            ),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink900,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = GrabGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "LIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = ForestGreen,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = GrabGreen,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = l(currentLanguage, "Xem Ranks", "View Ranks", "查看榜单", "ランキング", "순위 보기"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle preview of top explorers
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🥇",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Minh Khôi (14.2 km)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink900
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•",
                                    fontSize = 11.sp,
                                    color = Ink600
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🥈",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lan Anh (7 " + l(currentLanguage, "nhiệm vụ", "quests", "任务", "クエ", "퀘스트") + ")",
                                    fontSize = 11.5.sp,
                                    color = Ink600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Quick Suggestion Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Surface(
                        color = PaperWhite,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (searchQuery.isNotEmpty()) 1.5.dp else 1.dp,
                            color = if (searchQuery.isNotEmpty()) GrabGreen else Color(0xFFE2E8F0)
                        ),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    l(
                                        currentLanguage,
                                        "Tìm theo chủ đề, địa điểm (Bách Khoa, Cà phê vợt, Chợ Lớn)...",
                                        "Search themes or areas (Bách Khoa, Net coffee, Chợ Lớn)...",
                                        "搜索主题或区域（理工大学、网滤咖啡、堤岸）...",
                                        "テーマやエリアを検索（工科大、網珈琲、チョロン）...",
                                        "주제 또는 지역 검색 (공과대, 그물 커피, 쩌롱)..."
                                    ),
                                    fontSize = 13.sp,
                                    color = Ink600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(36.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = GrabGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = Ink600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Ink900,
                                unfocusedTextColor = Ink900
                            ),
                            singleLine = true
                        )
                    }

                    // Quick Search Suggestion Pills
                    val quickSuggestions = listOf(
                        "Bách Khoa" to "🎓 Bách Khoa",
                        "Cà phê vợt" to "☕ Cà phê vợt",
                        "Chợ Lớn" to "🥟 Sủi cảo Chợ Lớn",
                        "Phú Bình" to "🏮 Lồng đèn Phú Bình",
                        "Biệt Động" to "🎖️ Biệt động",
                        "Biệt Thự" to "🏛️ Biệt thự cổ"
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickSuggestions) { (tagKey, tagLabel) ->
                            val isTagActive = searchQuery.contains(tagKey, ignoreCase = true)
                            Surface(
                                onClick = {
                                    searchQuery = if (isTagActive) "" else tagKey
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTagActive) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                                border = if (isTagActive) androidx.compose.foundation.BorderStroke(1.dp, GrabGreen) else null
                            ) {
                                Text(
                                    text = tagLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isTagActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTagActive) GrabGreen else Ink600,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Active Search & Filter Feedback
                    if (searchQuery.isNotBlank() || selectedDistrictFilter.isNotBlank() || selectedCategory != QuestCategory.ALL) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Tìm thấy ${filteredQuests.size} hành trình phù hợp",
                                    "Found ${filteredQuests.size} matching quests",
                                    "找到 ${filteredQuests.size} 个相关路线",
                                    "${filteredQuests.size} 件のクエストが見つかりました",
                                    "${filteredQuests.size}개의 퀘스트를 찾았습니다"
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink600
                            )
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Đặt lại tất cả",
                                    "Reset all",
                                    "重置全部",
                                    "リセット",
                                    "초기화"
                                ),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrabGreen,
                                modifier = Modifier
                                    .clickable {
                                        searchQuery = ""
                                        selectedDistrictFilter = ""
                                        selectedCategory = QuestCategory.ALL
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // District Filter Row
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(districtFilters, key = { it.first }) { (districtKey, districtName) ->
                            val isSelected = selectedDistrictFilter == districtKey
                            Surface(
                                onClick = { selectedDistrictFilter = if (isSelected) "" else districtKey },
                                color = if (isSelected) GrabGreen else PaperWhite,
                                shape = RoundedCornerShape(16.dp),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = if (isSelected) 3.dp else 1.dp
                            ) {
                                Text(
                                    text = districtName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Ink900,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    currentLanguage = currentLanguage,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Interactive Hem Showcase Spotlights
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HemShowcase(
                    currentLanguage = currentLanguage,
                    onStartQuest = safeStartQuest
                )
            }

            // Section Title: Featured Quests Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Hành Trình Gợi Ý (${filteredQuests.size}) 🚀",
                            "Suggested Quests (${filteredQuests.size}) 🚀",
                            "推荐特色路线 (${filteredQuests.size}) 🚀",
                            "おすすめクエスト (${filteredQuests.size}) 🚀",
                            "추천 퀘스트 (${filteredQuests.size}) 🚀"
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink900
                    )
                }
            }

            // Empty Search Results State
            if (filteredQuests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔍",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Không tìm thấy Quest phù hợp!",
                                    "No matching quests found!",
                                    "未找到符合条件的路线！",
                                    "該当するクエストが見つかりません！",
                                    "일치하는 퀘스트를 찾을 수 없습니다!"
                                ),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink900
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Bạn có muốn để AI tự động thiết kế một Quest riêng theo từ khóa này?",
                                    "Would you like AI to generate a custom quest based on your search?",
                                    "是否让 AI 根据您的关键字自动定制独家路线？",
                                    "検索キーワードに基づいて AI にカスタムクエストを作成させますか？",
                                    "검색어 기반으로 AI가 맞춤형 퀘스트를 생성하도록 하시겠습니까?"
                                ),
                                fontSize = 12.sp,
                                color = Ink600,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    val customRequest = QuestRequest(
                                        startingLocationName = if (searchQuery.isNotBlank()) searchQuery else "Ho Chi Minh City",
                                        latitude = 10.7769,
                                        longitude = 106.7009,
                                        durationMinutes = 45,
                                        interests = listOf("Custom Explore", "Hidden Alleys"),
                                        freeTextNotes = searchQuery,
                                        language = currentLanguage
                                    )
                                    safeStartQuest(customRequest)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "✨ AI Tạo Quest Ngay",
                                        "✨ Create Quest with AI",
                                        "✨ AI 立即生成路线",
                                        "✨ AIでクエスト生成",
                                        "✨ AI로 퀘스트 생성"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Featured Quest Cards List
            items(filteredQuests, key = { it.request.startingLocationName + "_" + it.emoji }) { quest ->
                val isBachKhoa = quest.emoji == "🎓" ||
                        quest.district.contains("Diên Hồng", ignoreCase = true) ||
                        quest.district.contains("Dien Hong", ignoreCase = true) ||
                        quest.district.contains("延洪", ignoreCase = true)

                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isBachKhoa) 5.dp else 3.dp,
                        pressedElevation = 7.dp
                    ),
                    border = BorderStroke(
                        width = if (isBachKhoa) 1.5.dp else 1.dp,
                        color = if (isBachKhoa) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = if (isBachKhoa) 10.dp else 7.dp
                        )
                        .clickable { safeStartQuest(quest.request) }
                        .testTag("featured_quest_card_${quest.category.name}")
                ) {
                    Column {
                        // Top Gradient Header Banner with Cultural Theme & Watermark
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 138.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = quest.bgColors,
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            // Cultural Emoji Watermark in Background
                            Text(
                                text = quest.emoji,
                                fontSize = 58.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 8.dp)
                                    .alpha(0.25f)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top row: District Location Pill & XP Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.28f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📍",
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = quest.district,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        color = SunGold,
                                        shape = RoundedCornerShape(10.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "⚡",
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = quest.xp,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Ink900
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Title & Tagline
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = quest.title,
                                        fontSize = 17.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = quest.tagline,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White.copy(alpha = 0.92f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Tags & Highlight Badges Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "📍 ${quest.stopsCount} điểm dừng",
                                        "📍 ${quest.stopsCount} stops",
                                        "📍 ${quest.stopsCount} 个打卡点",
                                        "📍 ${quest.stopsCount} か所",
                                        "📍 ${quest.stopsCount}개 스팟"
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink900,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            quest.tags.take(2).forEach { tag ->
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink600,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        // Card Footer: Rating, Walking Distance & Start Action Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⭐",
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = quest.rating,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink900
                                    )
                                    Text(
                                        text = " (${quest.reviewsCount})",
                                        fontSize = 11.sp,
                                        color = Ink600
                                    )
                                }

                                Text(
                                    text = "•",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⏱️",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = quest.duration,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink600
                                    )
                                }

                                Text(
                                    text = "•",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🚶",
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = quest.distance,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink600
                                    )
                                }
                            }

                            // Start Journey Pill Button
                            Surface(
                                color = GrabGreen,
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 3.dp,
                                modifier = Modifier.clickable { safeStartQuest(quest.request) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "Bắt đầu",
                                            "Start",
                                            "开启任务",
                                            "スタート",
                                            "시작하기"
                                        ),
                                        fontSize = 12.5.sp,
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
}

fun createAiSurpriseQuestRequest(currentLanguage: String): QuestRequest {
    val surpriseThemes = listOf(
        Triple(
            "Phường Sài Gòn, TP.HCM",
            Pair(10.7743, 106.7031),
            Pair(
                listOf("Hidden Alleys", "Coffee Culture", "Vintage Vinyl"),
                "AI Surprise Quest: Secret Net Coffee, Retro Vinyl Records & Art Alleys in Saigon Ward"
            )
        ),
        Triple(
            "Phường Bàn Cờ, TP.HCM",
            Pair(10.7812, 106.6914),
            Pair(
                listOf("Hidden History", "French Architecture", "Secret Bunker"),
                "AI Surprise Quest: Underground Resistance Bunker & Colonial Villa Gardens in Ban Co Ward"
            )
        ),
        Triple(
            "Phường Chợ Lớn, TP.HCM",
            Pair(10.7533, 106.6617),
            Pair(
                listOf("Street Food", "Ancient Temples", "Herbal Medicine"),
                "AI Surprise Quest: Chợ Lớn Dim Sum, Ancient Assembly Halls & Traditional Herbal Tea Guilds in Cho Lon Ward"
            )
        ),
        Triple(
            "Phường Hòa Bình, TP.HCM",
            Pair(10.7638, 106.6492),
            Pair(
                listOf("Traditional Crafts", "Lantern Artisans", "Hidden Heritage"),
                "AI Surprise Quest: Phú Bình Cellophane Lantern Village & Woodcarving Alleys in Hoa Binh Ward"
            )
        ),
        Triple(
            "Phường Đức Nhuận, TP.HCM",
            Pair(10.7981, 106.6815),
            Pair(
                listOf("Alley Food Trail", "Local Life", "Canal Walks"),
                "AI Surprise Quest: Phùng Văn Cung Alley Street Food & Secret Canal Views in Duc Nhuan Ward"
            )
        ),
        Triple(
            "Phường Thanh Đa (Bán đảo Thanh Đa), TP.HCM",
            Pair(10.8258, 106.7242),
            Pair(
                listOf("Old Apartments", "River Views", "Hidden Cafes"),
                "AI Surprise Quest: Thanh Đa Old Apartment Retro Charm & Waterfront Alleys"
            )
        )
    )

    val selected = surpriseThemes.random()
    return QuestRequest(
        startingLocationName = selected.first,
        latitude = selected.second.first,
        longitude = selected.second.second,
        durationMinutes = listOf(30, 45, 60).random(),
        interests = selected.third.first,
        freeTextNotes = selected.third.second,
        language = currentLanguage
    )
}
