package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GrabGreen
import com.example.util.l

private data class TabEntry(
    val id: Int,
    val icon: ImageVector,
    val vi: String,
    val en: String,
    val zh: String,
    val ja: String,
    val ko: String,
    val isCenterHero: Boolean = false
)

@Composable
fun BottomNavBar(
    selectedTab: Int = 2,
    isVi: Boolean = false,
    currentLanguage: String = if (isVi) "vi" else "en",
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            TabEntry(0, Icons.Default.Explore, "Khám phá", "Explore", "探索", "探索", "탐색"),
            TabEntry(1, Icons.Default.EmojiEvents, "Xếp hạng", "Rank", "排行榜", "順位", "리더보드"),
            TabEntry(2, Icons.Default.Map, "Hành trình", "Quest", "任务", "クエスト", "퀘스트", isCenterHero = true),
            TabEntry(3, Icons.Default.MilitaryTech, "Huy hiệu", "Badges", "徽章", "バッジ", "배지"),
            TabEntry(4, Icons.Default.Person, "Cá nhân", "Profile", "个人", "マイページ", "프로필")
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp, top = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0B5230),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .testTag("app_bottom_nav_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { item ->
                    val selected = selectedTab == item.id
                    val label = l(currentLanguage, item.vi, item.en, item.zh, item.ja, item.ko)
                    val interactionSource = remember { MutableInteractionSource() }

                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onTabSelected(item.id) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = if (item.isCenterHero) 42.dp else 36.dp, height = 28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        selected && item.isCenterHero -> Color(0xFFFFD54F)
                                        selected -> Color.White.copy(alpha = 0.25f)
                                        item.isCenterHero -> Color.White.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label,
                                tint = when {
                                    selected && item.isCenterHero -> Color(0xFF0B5230)
                                    selected -> Color.White
                                    item.isCenterHero -> Color(0xFFFFD54F)
                                    else -> Color.White.copy(alpha = 0.65f)
                                },
                                modifier = Modifier.size(if (item.isCenterHero) 21.dp else 19.dp)
                            )

                            if (item.isCenterHero && !selected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 4.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5252))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = label,
                            color = when {
                                selected && item.isCenterHero -> Color(0xFFFFD54F)
                                selected -> Color.White
                                item.isCenterHero -> Color(0xFFFFE082).copy(alpha = 0.9f)
                                else -> Color.White.copy(alpha = 0.65f)
                            },
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
