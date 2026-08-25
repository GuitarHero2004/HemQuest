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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthViewModel
import com.example.ui.UserStatsViewModel
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

data class CulturalCardItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: String
)

data class CompletedQuestRecord(
    val id: String,
    val title: String,
    val district: String,
    val date: String,
    val distance: String,
    val duration: String,
    val rating: Int,
    val comment: String,
    val isSubmitted: Boolean
)

@Composable
fun BadgesAndReviewsView(
    currentLanguage: String = "vi",
    userStatsViewModel: UserStatsViewModel? = null,
    authViewModel: AuthViewModel? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Badges, 2: Reviews
    var selectedBadgeDetail by remember { mutableStateOf<CulturalCardItem?>(null) }

    val userStatsState = userStatsViewModel?.userStats?.collectAsStateWithLifecycle()
    val userStats = userStatsState?.value ?: com.example.data.UserStatsEntity()
    val syncState by (userStatsViewModel?.syncState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.example.ui.FirestoreSyncState()) })
    val authUiState by (authViewModel?.uiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.example.auth.AuthUiState()) })
    val currentUid = authUiState.userProfile?.uid

    val unlockedBadgeIds = remember(userStats.unlockedBadgeIds) {
        userStats.unlockedBadgeIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
    val completedQuestsCount = userStats.completedQuestsCount

    fun isBadgeUnlocked(id: String): Boolean {
        return when (id) {
            "first_step" -> completedQuestsCount >= 1 || unlockedBadgeIds.contains(id)
            "alley_walker" -> completedQuestsCount >= 3 || unlockedBadgeIds.contains(id)
            "heritage_master" -> completedQuestsCount >= 5 || unlockedBadgeIds.contains(id)
            "saigon_expert" -> completedQuestsCount >= 10 || unlockedBadgeIds.contains(id)
            else -> unlockedBadgeIds.contains(id)
        }
    }

    val culturalBadges = remember(currentLanguage, unlockedBadgeIds, completedQuestsCount) {
        val milestones = listOf(
            CulturalCardItem(
                "first_step",
                l(currentLanguage, "Bước Chân Đầu Tiên", "First Step", "初入胡同", "最初の一歩", "첫 번째 발걸음"),
                l(currentLanguage, "Thành Tựu Cột Mốc", "Milestone Achievement", "里程碑成就", "マイルストーン", "마일스톤"),
                l(currentLanguage, "Hoàn thành chuyến phiêu lưu ngõ hẻm đầu tiên.", "Completed the first alley exploration adventure.", "完成首次西贡胡同探险任务。", "初めての路地裏探検クエストを達成。", "첫 번째 골목 탐험 퀘스트 완료."),
                "🏃"
            ),
            CulturalCardItem(
                "alley_walker",
                l(currentLanguage, "Kẻ Lang Thang Ngõ Hẻm", "Alley Wanderer", "胡同漫步者", "路地裏ウォーカー", "골목 방랑자"),
                l(currentLanguage, "Thành Tựu Cột Mốc", "Milestone Achievement", "里程碑成就", "マイルストーン", "마일스톤"),
                l(currentLanguage, "Chinh phục thành công 3 tuyến hành trình khám phá đô thị.", "Successfully completed 3 urban exploration journeys.", "成功探索3条城市特色胡同路线。", "3つの都市探検ルートを踏破。", "3개 도시 탐험 루트 완료."),
                "🧭"
            ),
            CulturalCardItem(
                "heritage_master",
                l(currentLanguage, "Bậc Thầy Di Sản", "Heritage Master", "文化遗迹导师", "ヘリテージマスター", "헤리티지 마스터"),
                l(currentLanguage, "Thành Tựu Cột Mốc", "Milestone Achievement", "里程碑成就", "マイルストーン", "마일스톤"),
                l(currentLanguage, "Khám phá 5 tuyến di sản văn hóa và ngõ hẻm truyền thống.", "Discovered 5 traditional cultural heritage alley routes.", "探索5条传统文化遗产胡同路线。", "5つの伝統文化遺産路地を探索。", "5개 전통 문화유산 골목 탐험."),
                "🏛️"
            ),
            CulturalCardItem(
                "saigon_expert",
                l(currentLanguage, "Thổ Địa Sài Gòn", "Saigon Urban Legend", "西贡通达人", "サイゴン達人", "사이공 골목 대가"),
                l(currentLanguage, "Thành Tựu Đỉnh Cao", "Mastery Achievement", "殿堂级成就", "最高位マスター", "마스터리 성취"),
                l(currentLanguage, "Chinh phục xuất sắc 10 nhiệm vụ khám phá Sài Gòn.", "Mastered 10 alley discovery quests across Saigon.", "卓越达成10项西贡全城探索任务。", "サイゴン全域で10のクエストを達成。", "사이공 전역 10개 퀘스트 마스터."),
                "👑"
            )
        )

        val builtInCards = listOf(
            CulturalCardItem("coffee", l(currentLanguage, "Cà Phê Vợt 70 Năm", "70-Year Net Coffee", "70年网滤老咖啡", "70年伝統の網フィルター珈琲", "70년 전통 그물 필터 커피"), "P. Cầu Ông Lãnh", l(currentLanguage, "Thưởng thức cà phê pha bằng vợt vải truyền thống trong hẻm sâu.", "Tasting coffee brewed through traditional fabric nets in deep alleys.", "在幽深巷弄品尝传统布袋网滤咖啡。", "奥深い路地裏で伝統の布フィルター珈琲を味わう。", "골목 깊은 곳에서 전통 그물 필터 커피 즐기기."), "☕"),
            CulturalCardItem("lantern", l(currentLanguage, "Hẻm Đèn Lồng Phú Bình", "Phu Binh Lantern Alley", "提灯高挂隐秘胡同", "ランタン広がる路地裏", "등불 골목"), "P. Hòa Bình", l(currentLanguage, "Khám phá làng nghề thủ công lồng đèn giấy kính hơn 60 năm lịch sử.", "Discover traditional cellophane lantern craft village over 60 years old.", "探访传承60载历史的传统手工玻璃纸提灯街巷。", "60年以上の歴史を持つ伝統手作りランタンの里を探索。", "60년 전통의 수제 셀로판 등불 공예 마을 탐험."), "🏮"),
            CulturalCardItem("commando", l(currentLanguage, "Biệt Động Sài Gòn", "Secret Commandos", "西贡特工秘密地下军火库", "サイゴン別動隊の遺迹", "사이공 특공대"), "P. Bàn Cờ", l(currentLanguage, "Ghé thăm căn hầm chứa vũ khí bí mật giấu dưới sàn nhà gỗ.", "Visit secret bunker hidden beneath wooden flooring.", "走进隐藏在木地板下方的秘密军火地下掩体。", "木造の床下に隠された秘密の武器庫を見学。", "나무 바닥 아래 숨겨진 비밀 기지 방문."), "🏛️"),
            CulturalCardItem("temple", l(currentLanguage, "Chùa Bà Thiên Hậu", "Ba Thien Hau Temple", "堤岸天后古庙", "天后宮古寺廟", "바티엔하우 사원"), "P. Chợ Lớn", l(currentLanguage, "Chiêm ngưỡng kiến trúc chạm khắc phù điêu gốm sứ tinh xảo.", "Admire elaborate ceramic relief sculptures and ancestral architecture.", "领略岭南建筑风格的精美陶瓷浮雕与香火圣地。", "精巧な陶器のレリーフ彫刻と歴史ある寺院建築を鑑賞。", "정교한 도자기 부조 조각과 고대 사원 건축 감상."), "⛩️"),
            CulturalCardItem("market", l(currentLanguage, "Chợ Cũ Tôn Thất Đạm", "Old Ton That Dam Market", "尊室淡百年老集市", "トンタットダム旧集市", "톤탓담 Old 마켓"), "P. Sài Gòn", l(currentLanguage, "Khu chợ ngoài trời xưa nhất Sài Gòn nép mình giữa các tòa nhà chọc trời.", "Saigon's oldest outdoor market nestled amidst modern skyscrapers.", "穿梭于摩天大楼掩映下的西贡最悠久露天老市集。", "超高層ビル群の足元に広がるサイゴン最古の青空市場。", "초고층 빌딩 사이에 자리잡은 사이공에서 가장 오래된 야외 시장."), "🍜"),
            CulturalCardItem("mural", l(currentLanguage, "Góc Tranh Tường Hẻm", "Alley Mural Corner", "胡同涂鸦艺术角", "路地裏の壁画アート角", "골목 벽화 모퉁이"), "P. Sài Gòn", l(currentLanguage, "Chiêm ngưỡng các bức bích họa khắc họa nếp sống Sài Gòn xưa.", "Admire colorful murals depicting old Saigon daily life.", "欣赏生动描摹昔日西贡市井生活的彩色胡同壁画。", "昔のサイゴンの暮らしを描いた鮮やかな壁画を鑑賞。", "옛 사이공의 일상을 묘사한 다채로운 벽화 감상."), "🎨"),
            CulturalCardItem("wood", l(currentLanguage, "Xóm Mộc Thủ Công", "Carpentry Craft Alley", "传统木雕手艺街", "木工職人の路地", "전통 목공예 골목"), "P. Xuân Hòa", l(currentLanguage, "Lắng nghe tiếng đục đẽo gỗ mộc mạc từ các nghệ nhân hẻm.", "Listen to the rhythmic chisel sounds from master woodworkers.", "聆听胡同老手艺工匠敲凿木器的淳朴韵律。", "路地裏の熟練職人による木彫りの音に耳を澄ませる。", "골목 장인들의 전통 목공예 손길 감상."), "🪵"),
            CulturalCardItem("pagoda", l(currentLanguage, "Chùa Phụng Sơn Cổ Tự", "Phung Son Pagoda", "奉山古刹遗址", "フンソン古寺", "풍손 사원"), "P. Minh Phụng", l(currentLanguage, "Di tích khảo cổ và ngôi cổ tự thanh tịnh nép trong lòng đô thị.", "Archaeological landmark and tranquil ancient pagoda tucked inside urban alleyways.", "隐匿于喧嚣都市胡同中的宁静古刹与考古遗址。", "都会の路地裏に佇む静寂な考古遺跡と古寺院。", "도시 골목 속에 고요히 자리한 고대 사찰 유적지."), "🪷")
        )

        val dynamicAiQuests = unlockedBadgeIds.filter { it.startsWith("quest_") }.mapIndexed { idx, qId ->
            val icon = com.example.util.IdGenerator.getBadgeIconForQuest(qId)
            val readableTitle = qId
                .replace("quest_badge_", "")
                .replace("quest_", "")
                .split("_")
                .filter { it.isNotEmpty() && !it.all { char -> char.isDigit() } }
                .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() } }
                .ifBlank { "Nhiệm Vụ Di Sản #${idx + 1}" }

            CulturalCardItem(
                id = qId,
                title = readableTitle,
                category = l(currentLanguage, "Hành Trình Văn Hóa Đã Mở", "Cultural Quest Master", "已解锁文化路线", "解除された文化ルート", "해제된 문화 여정"),
                description = l(
                    currentLanguage,
                    "Huy hiệu độc bản vinh danh người thám hiểm đã hoàn thành xuất sắc tuyến hẻm di sản và đồng bộ thành công lên Firestore.",
                    "Unique cultural badge honoring explorer who conquered this heritage alleyway route, verified and synced to Firestore.",
                    "专属文化探索徽章，已成功同步至 Firestore 档案馆。",
                    "Firestoreに同期された特別な路地裏文化探索達成バッジ。",
                    "Firestore에 안전하게 동기화된 독창적인 문화 탐방 완주 배지."
                ),
                icon = icon
            )
        }

        milestones + builtInCards + dynamicAiQuests
    }

    val completedQuests = remember {
        mutableStateListOf<CompletedQuestRecord>()
    }

    val unlockedCount = culturalBadges.count { isBadgeUnlocked(it.id) }
    val totalQuestsGoal = 10
    val questProgressFraction = (completedQuestsCount.toFloat() / totalQuestsGoal.toFloat()).coerceIn(0f, 1f)

    // Badge Lore Detail Dialog
    selectedBadgeDetail?.let { badge ->
        val isUnlocked = isBadgeUnlocked(badge.id)
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            confirmButton = {
                if (!isUnlocked) {
                    Button(
                        onClick = {
                            userStatsViewModel?.unlockBadge(badge.id, currentUid)
                            selectedBadgeDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = l(currentLanguage, "Mở Khóa Ngay (+100 XP)", "Unlock Now (+100 XP)", "立即解锁 (+100 XP)", "今すぐ解除 (+100 XP)", "지금 해제 (+100 XP)"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { selectedBadgeDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isUnlocked) {
                    OutlinedButton(
                        onClick = { selectedBadgeDetail = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = l(currentLanguage, "Để sau", "Later", "稍后再说", "後で", "나중에"), color = Ink600)
                    }
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = badge.icon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = badge.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink900)
                        Text(text = badge.category, fontSize = 12.sp, color = Ink600)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = badge.description, fontSize = 13.sp, color = Ink900, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if (isUnlocked) GrabGreen.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) ForestGreen else Ink600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUnlocked) {
                                    l(currentLanguage, "Đã lưu vào Sổ Tay & Đồng bộ Firestore", "Saved in Journal & Synced with Firestore", "已保存至手册并同步到 Firestore", "ジャーナルに保存＆Firestore同期完了", "저널 저장 및 Firestore 동기화 완료")
                                } else {
                                    l(currentLanguage, "Chưa mở khóa • Hoàn thành khám phá để nhận thẻ", "Locked • Complete exploration to unlock card", "未解锁 • 完成相应胡同探索即可获取", "未解除 • 対象の路地探索を完了してカードを獲得", "잠김 • 해당 골목 탐험 완료 시 획득 가능")
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) ForestGreen else Ink600
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = PaperWhite
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F4))
            .testTag("badges_reviews_view")
    ) {
        HeaderBar(
            title = l(currentLanguage, "Huy Hiệu & Sổ Tay", "Badges & Reviews", "勋章与见闻", "バッジ＆レビュー", "배지 및 리뷰"),
            currentLanguage = currentLanguage,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp)
        ) {
            // Quest Progress Tracker Hero Card
            item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "🗺️", fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = l(currentLanguage, "Tiến Độ Nhiệm Vụ Hẻm", "Quest Progress Tracker", "胡同任务完成进度", "路地裏クエスト達成度", "골목 퀘스트 진행 현황"),
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$completedQuestsCount / $totalQuestsGoal ${l(currentLanguage, "nhiệm vụ hoàn thành", "quests completed", "个任务已完成", "個のクエスト完了", "개 퀘스트 완료")} (${(questProgressFraction * 100).toInt()}%)",
                                    fontSize = 11.5.sp,
                                    color = Ink600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Firestore Sync Status Pill
                        Surface(
                            color = if (syncState.isSynced) GrabGreen.copy(alpha = 0.12f) else SunGold.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (syncState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = GrabGreen
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firestore",
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

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { questProgressFraction },
                        color = GrabGreen,
                        trackColor = GrabGreen.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Milestones row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌱 " + l(currentLanguage, "Tập sự (0/3)", "Beginner (0/3)", "初探者 (0/3)", "ビギナー (0/3)", "초심자 (0/3)"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedQuestsCount >= 1) GrabGreen else Ink600
                        )
                        Text(
                            text = "🧭 " + l(currentLanguage, "Thổ địa (5/10)", "Local Guide (5/10)", "地道达人 (5/10)", "ローカル達人 (5/10)", "골목 가이드 (5/10)"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedQuestsCount >= 5) GrabGreen else Ink600
                        )
                        Text(
                            text = "👑 " + l(currentLanguage, "Bậc thầy (10/10)", "Master (10/10)", "西贡通 (10/10)", "マスター (10/10)", "마스터 (10/10)"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedQuestsCount >= 10) SunGold else Ink600
                        )
                    }

                    // Firestore Manual Refresh Row
                    if (currentUid != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    userStatsViewModel?.fetchFromFirestore(currentUid)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = GrabGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(currentLanguage, "Đồng bộ đám mây Firestore", "Sync with Firestore Cloud", "同步 Firestore 云端数据", "Firestore クラウド同期", "Firestore 클라우드 동기화"),
                                    fontSize = 11.sp,
                                    color = Ink900,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = GrabGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Top Filter Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = {
                            Text(
                                text = l(currentLanguage, "Tất cả", "All Items", "全部", "すべて", "전체"),
                                fontWeight = if (selectedFilter == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrabGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = {
                            Text(
                                text = "${l(currentLanguage, "Huy Hiệu Di Sản", "Cultural Badges", "文化徽章", "文化バッジ", "문화 배지")} ($unlockedCount/${culturalBadges.size})",
                                fontWeight = if (selectedFilter == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrabGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = {
                            Text(
                                text = "${l(currentLanguage, "Nhận Xét Đã Lưu", "Saved Reviews", "已存评价", "保存済み評価", "저장된 후기")} (${completedQuests.size})",
                                fontWeight = if (selectedFilter == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrabGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Section 1: Cultural Badges Grid / List
        if (selectedFilter == 0 || selectedFilter == 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SunGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Bộ Sưu Tập Thẻ Văn Hóa Hẻm",
                                "Cultural Alley Cards Collection",
                                "胡同文化徽章收藏",
                                "路地裏文化カードコレクション",
                                "골목 문화 카드 컬렉션"
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                    }

                    Surface(
                        color = GrabGreen.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$unlockedCount/${culturalBadges.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(culturalBadges, key = { it.id }) { badge ->
                val isUnlocked = isBadgeUnlocked(badge.id)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) PaperWhite else Color(0xFFEBEFEA)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .clickable { selectedBadgeDetail = badge }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isUnlocked) GrabGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isUnlocked) badge.icon else "🔒",
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Ink900 else Ink600
                            )
                            Text(
                                text = badge.category,
                                fontSize = 12.sp,
                                color = Ink600
                            )
                        }

                        Surface(
                            color = if (isUnlocked) GrabGreen else Color.Gray,
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (isUnlocked) {
                                    l(currentLanguage, "Đã Mở", "Unlocked", "已解锁", "解除済み", "해제됨")
                                } else {
                                    l(currentLanguage, "Chưa Mở", "Locked", "未解锁", "未解除", "잠김")
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Completed Quest Reviews & Comments
        if (selectedFilter == 0 || selectedFilter == 2) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Nhận Xét & Đánh Giá Quest",
                                "Completed Quest Feedback",
                                "已完成探索与评价",
                                "完了したクエストの評価",
                                "완료된 퀘스트 후기"
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                    }

                    Surface(
                        color = ForestGreen.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${completedQuests.size} ${l(currentLanguage, "bài", "reviews", "篇", "件", "개")}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            itemsIndexed(completedQuests, key = { _, questRecord -> questRecord.id }) { index, questRecord ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = questRecord.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink900
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = PaperSecondary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = questRecord.district,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink600,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${questRecord.distance} • ${questRecord.duration} • ${questRecord.date}",
                                        fontSize = 11.sp,
                                        color = Ink600
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Star Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Đánh giá của bạn:",
                                    "Your Rating:",
                                    "您的评分：",
                                    "あなたの評価:",
                                    "내 평가:"
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink600
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= questRecord.rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Star $star",
                                        tint = SunGold,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                completedQuests[index] = questRecord.copy(
                                                    rating = star,
                                                    isSubmitted = false
                                                )
                                            }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Comment OutlinedTextField
                        OutlinedTextField(
                            value = questRecord.comment,
                            onValueChange = { newComment ->
                                completedQuests[index] = questRecord.copy(
                                    comment = newComment,
                                    isSubmitted = false
                                )
                            },
                            placeholder = {
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Viết nhận xét của bạn về hành trình này...",
                                        "Write your experience or notes...",
                                        "写下您对本次探索的感受...",
                                        "このクエストの感想を記入...",
                                        "이 퀘스트에 대한 후기를 적어주세요..."
                                    ),
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PaperSecondary,
                                unfocusedContainerColor = PaperSecondary,
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            maxLines = 3,
                            singleLine = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Save / Submitted Status Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (questRecord.isSubmitted) {
                                Surface(
                                    color = ForestGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Đã lưu nhận xét",
                                                "Review Saved",
                                                "评价已保存",
                                                "レビュー保存済み",
                                                "후기 저장됨"
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        completedQuests[index] = questRecord.copy(isSubmitted = true)
                                        if (currentUid != null) {
                                            userStatsViewModel?.syncToFirestore(currentUid)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClayOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = l(
                                            currentLanguage,
                                            "Lưu Đánh Giá",
                                            "Save Review",
                                            "保存评价",
                                            "レビューを保存",
                                            "후기 저장"
                                        ),
                                        fontSize = 11.sp,
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
}
