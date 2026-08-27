package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.QuestLevelUtils
import com.example.util.l

@Composable
fun QuestLevelRoadmapDialog(
    totalXp: Int,
    currentLanguage: String = "vi",
    onDismiss: () -> Unit
) {
    val currentLevelInfo = QuestLevelUtils.calculateLevelInfo(totalXp, currentLanguage)
    val allLevels = QuestLevelUtils.getAllLevels()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = PaperWhite,
            border = BorderStroke(1.5.dp, GrabGreen.copy(alpha = 0.4f)),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
                .testTag("level_roadmap_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SunGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "👑", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Hệ Thống Cấp Độ Quest XP",
                                    "Quest XP Level Roadmap",
                                    "Quest XP 等级路线图",
                                    "Quest XP レベルマップ",
                                    "Quest XP 레벨 로드맵"
                                ),
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = Ink900
                            )
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Tích lũy XP từ các chuyến đi bộ hẻm",
                                    "Earn XP from walking quests & stops",
                                    "通过徒步探索积累 XP 提升等级",
                                    "散策クエストでXPを獲得してレベルアップ",
                                    "골목 산책으로 XP를 모아 레벨을 올리세요"
                                ),
                                fontSize = 11.5.sp,
                                color = Ink600
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Ink600)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Level Summary Banner
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentLevelInfo.iconEmoji,
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "CẤP ĐỘ HIỆN TẠI: CẤP ${currentLevelInfo.level}",
                                        "CURRENT LEVEL: LEVEL ${currentLevelInfo.level}",
                                        "当前等级：${currentLevelInfo.level} 级",
                                        "現在のレベル: レベル ${currentLevelInfo.level}",
                                        "현재 레벨: 레벨 ${currentLevelInfo.level}"
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabGreen,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = if (currentLanguage == "vi") currentLevelInfo.titleVi else currentLevelInfo.titleEn,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SunGold.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, SunGold)
                            ) {
                                Text(
                                    text = "$totalXp XP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SunGold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress bar to next level
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${currentLevelInfo.currentLevelXp} / ${currentLevelInfo.requiredLevelXp} XP",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Còn ${currentLevelInfo.requiredLevelXp - currentLevelInfo.currentLevelXp} XP lên Cấp ${currentLevelInfo.level + 1}",
                                    "${currentLevelInfo.requiredLevelXp - currentLevelInfo.currentLevelXp} XP to Level ${currentLevelInfo.level + 1}",
                                    "还需 ${currentLevelInfo.requiredLevelXp - currentLevelInfo.currentLevelXp} XP",
                                    "レベル${currentLevelInfo.level + 1}まであと ${currentLevelInfo.requiredLevelXp - currentLevelInfo.currentLevelXp} XP",
                                    "레벨 ${currentLevelInfo.level + 1}까지 ${currentLevelInfo.requiredLevelXp - currentLevelInfo.currentLevelXp} XP 남음"
                                ),
                                fontSize = 11.5.sp,
                                color = GrabGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { currentLevelInfo.progressFraction },
                            color = GrabGreen,
                            trackColor = Color(0xFF1E3A2B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Level Progression List & XP Guide
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "DANH SÁCH CẤP ĐỘ & ĐẶC QUYỀN",
                            "LEVEL TIERS & UNLOCKED PERKS",
                            "等级阶梯与特权解锁",
                            "レベル一覧と開放特典",
                            "레벨 단계 및 특전 해제"
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        letterSpacing = 0.8.sp
                    )

                    allLevels.forEach { lvl ->
                        val isUnlocked = currentLevelInfo.level >= lvl.level
                        val isCurrent = currentLevelInfo.level == lvl.level

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFFF0FDF4) else if (isUnlocked) PaperWhite else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(
                                1.2.dp,
                                if (isCurrent) GrabGreen else if (isUnlocked) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isUnlocked) SunGold.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = lvl.iconEmoji, fontSize = 22.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Level ${lvl.level} • ",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) ForestGreen else Ink600
                                        )
                                        Text(
                                            text = if (currentLanguage == "vi") lvl.titleVi else lvl.titleEn,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Ink900
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "🎁 " + if (currentLanguage == "vi") lvl.perkVi else lvl.perkEn,
                                        fontSize = 11.sp,
                                        color = Ink600,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (isCurrent) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GrabGreen,
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = l(currentLanguage, "Hiện tại", "Current", "当前", "現在", "현재"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                } else if (isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Unlocked",
                                        tint = ForestGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // How to Earn XP Guide Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, SunGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SunGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "CÁCH TÍCH LŨY QUEST XP NHANH CHÓNG",
                                        "HOW TO EARN QUEST XP FAST",
                                        "如何快速获取 QUEST XP",
                                        "QUEST XPを効率よく獲得する方法",
                                        "QUEST XP 빠르게 모으는 방법"
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink900,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            XpEarnRuleRow(
                                icon = "🚶",
                                title = l(currentLanguage, "Hoàn thành 1 chuyến đi bộ hẻm", "Complete 1 Alley Walk Quest", "完成1次胡同徒步探索", "1回の散策を完了", "골목 산책 1회 완료"),
                                xpText = "+120 ~ 250 XP"
                            )
                            XpEarnRuleRow(
                                icon = "📍",
                                title = l(currentLanguage, "Check-in mỗi điểm dừng di sản", "Check-in at each Heritage Stop", "打卡每个文化景点", "各スポットでチェックイン", "각 문화 지점 체크인"),
                                xpText = "+50 XP"
                            )
                            XpEarnRuleRow(
                                icon = "🏆",
                                title = l(currentLanguage, "Mở khóa huy hiệu văn hóa mới", "Unlock a New Cultural Badge", "解锁1个新文化徽章", "新しい文化バッジを開放", "새로운 문화 배지 해제"),
                                xpText = "+100 XP"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = l(currentLanguage, "Đã Hiểu", "Got It", "知道了", "了解", "확인"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun XpEarnRuleRow(icon: String, title: String, xpText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 12.sp, color = Ink900, fontWeight = FontWeight.Medium)
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = SunGold.copy(alpha = 0.2f)
        ) {
            Text(
                text = xpText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Ink900,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
