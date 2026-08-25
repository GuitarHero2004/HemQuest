package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Quest
import com.example.model.StopStatus
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.util.l

@Composable
fun AlleyScrapbookSection(
    quests: List<Quest>,
    currentLanguage: String
) {
    val completedQuests = quests.filter { q -> q.stops.any { it.status == StopStatus.COMPLETED } }
    if (completedQuests.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("alley_scrapbook_section")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoAlbum,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = l(currentLanguage, "Sổ Tay Ký Ức Hẻm", "Alley Scrapbook", "巷弄记忆集", "路地の思い出帳", "골목 스크랩북"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink900
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(completedQuests) { quest ->
                val completedStops = quest.stops.filter { it.status == StopStatus.COMPLETED }
                completedStops.forEach { stop ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .width(220.dp)
                            .height(180.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = stop.photoUri ?: "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?q=80&w=400&auto=format&fit=crop",
                                contentDescription = stop.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = stop.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stop.category,
                                    fontSize = 11.sp,
                                    color = Ink600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
