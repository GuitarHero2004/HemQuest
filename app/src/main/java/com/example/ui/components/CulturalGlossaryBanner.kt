package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

@Composable
fun CulturalGlossaryBanner(
    currentLanguage: String = "vi",
    onOpenGlossary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenGlossary() }
            .testTag("cultural_glossary_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GrabGreen, ForestGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📚", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Bách Khoa Hẻm Sài Gòn",
                                    "Cultural Glossary",
                                    "西贡深巷文化百科",
                                    "サイゴン路地裏文化辞典",
                                    "사이공 골목 문화 용어집"
                                ),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SunGold.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = l(currentLanguage, "10 TỪ", "10 TERMS", "10词条", "10項目", "10용어"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink900,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = l(
                                currentLanguage,
                                "Giải thích 'Hẻm', 'Xẹt', 'Biệt Động', 'Cà Phê Vợt'...",
                                "Explain 'Hẻm', 'Xẹt', 'Biệt Động', 'Net Coffee'...",
                                "通俗解读“Hẻm”、“Xẹt”、“西贡特工”等...",
                                "「Hẻm」「Xẹt」「網フィルター珈琲」の解説...",
                                "'Hẻm', 'Xẹt', '특공대', '그물 커피' 해설..."
                            ),
                            fontSize = 12.sp,
                            color = Ink600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = GrabGreen.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Glossary",
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
