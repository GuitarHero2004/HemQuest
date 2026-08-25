package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.DuoLime
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.SunGold
import com.example.util.l

@Composable
fun HeaderBar(
    isVi: Boolean,
    greenScore: Int,
    currentLanguage: String,
    streak: Int,
    xp: Int,
    onSetLanguage: (String) -> Unit,
    onOpenGreenScore: () -> Unit,
    onOpenStreak: () -> Unit,
    onOpenXp: () -> Unit,
    onOpenBuilder: () -> Unit = {},
    onOpenGlossary: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showLanguagePicker by remember { mutableStateOf(false) }

    // Outer Surface covers the full top edge (status bar & camera cutout) in seamless PaperWhite
    Surface(
        color = PaperWhite,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_header_bar")
    ) {
        // Inner Column pushes all content below status bar and punch-hole camera cutouts
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 10.dp)
        ) {
            // Top Row: Brand Header & Interactive Language Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & App Title
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = l(currentLanguage, "Quay lại", "Back", "返回", "戻る", "뒤로"),
                                tint = Ink900,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GrabGreen, DuoLime)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nature,
                            contentDescription = "HẻmQuest Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HẻmQuest",
                                fontWeight = FontWeight.Black,
                                fontSize = 21.sp,
                                color = Ink900,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ClayOrange.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "SAIGON",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ClayOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = l(
                                currentLanguage,
                                "Khám phá hẻm phố • Tích điểm Xanh",
                                "Discover Saigon Alleys • Eco Quest",
                                "探索西贡弄堂 • 积累低碳积分",
                                "サイゴン路地裏探索 • エコポイント",
                                "사이공 골목 탐방 • 에코 포인트"
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Interactive Language Switcher Pill
                Surface(
                    color = GrabGreen.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .clickable { showLanguagePicker = true }
                        .testTag("header_language_switcher")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                "vi" -> "🇻🇳 VI"
                                "en" -> "🇬🇧 EN"
                                "zh" -> "🇨🇳 中文"
                                "ja" -> "🇯🇵 日本語"
                                "ko" -> "🇰🇷 한국어"
                                else -> "🌐"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gamification Bar: Streaks, XP, Green Points, Glossary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Flame Pill (Duolingo style)
                Surface(
                    color = StreakFlame.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier
                        .clickable { onOpenStreak() }
                        .testTag("header_streak_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = StreakFlame,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "$streak ngày",
                                "$streak days",
                                "$streak 天",
                                "$streak 日間",
                                "$streak 일 연속"
                            ),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = StreakFlame
                        )
                    }
                }

                // XP Points Badge
                Surface(
                    color = SunGold.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier
                        .clickable { onOpenXp() }
                        .testTag("header_xp_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP",
                            tint = SunGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$xp XP",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900
                        )
                    }
                }

                // Green Eco Score Pill
                Surface(
                    color = GrabGreen.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier
                        .clickable { onOpenGreenScore() }
                        .testTag("header_greenscore_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Green Score",
                            tint = GrabGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "$greenScore Điểm Xanh",
                                "$greenScore Eco Pts",
                                "$greenScore 环保积分",
                                "$greenScore エコpt",
                                "$greenScore 에코P"
                            ),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = ForestGreen
                        )
                    }
                }

                // Glossary Pill (if provided)
                if (onOpenGlossary != null) {
                    Surface(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier
                            .clickable { onOpenGlossary() }
                            .testTag("header_glossary_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📚", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Bách Khoa",
                                    "Glossary",
                                    "百科",
                                    "辞典",
                                    "용어집"
                                ),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D28D9)
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Language Picker Dialog
    if (showLanguagePicker) {
        Dialog(onDismissRequest = { showLanguagePicker = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = l(currentLanguage, "Chọn Ngôn Ngữ", "Select Language", "选择语言", "言語を選択", "언어 선택"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink900
                            )
                        }
                        IconButton(
                            onClick = { showLanguagePicker = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Ink600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val languages = listOf(
                        Triple("vi", "Tiếng Việt", "🇻🇳"),
                        Triple("en", "English", "🇬🇧"),
                        Triple("zh", "中文 (Chinese)", "🇨🇳"),
                        Triple("ja", "日本語 (Japanese)", "🇯🇵"),
                        Triple("ko", "한국어 (Korean)", "🇰🇷")
                    )

                    languages.forEach { (code, name, flag) ->
                        val isSelected = currentLanguage == code
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) GrabGreen.copy(alpha = 0.12f) else Color(0xFFF8FAF8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSetLanguage(code)
                                    showLanguagePicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ForestGreen else Ink900
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ForestGreen,
                                        modifier = Modifier.size(18.dp)
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

@Composable
fun HeaderBar(
    title: String,
    currentLanguage: String = "vi",
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PaperWhite,
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("screen_header_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F2))
                            .testTag("screen_header_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = l(currentLanguage, "Quay lại", "Back", "返回", "戻る", "뒤로"),
                            tint = Ink900,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (actions != null) {
                actions()
            }
        }
    }
}


