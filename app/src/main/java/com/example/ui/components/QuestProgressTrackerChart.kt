package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quest
import com.example.util.l

data class QuestDataPoint(
    val label: String,
    val distanceMeters: Float,
    val culturalXp: Float
)

@Composable
fun QuestProgressTrackerChart(
    activeQuest: Quest?,
    totalWalkedMeters: Int,
    totalXp: Int,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(0) } // 0 = Dual Chart, 1 = Walked Distance, 2 = Cultural Points

    val questPoints = remember(activeQuest, totalWalkedMeters, totalXp) {
        val stops = activeQuest?.stops ?: emptyList()
        val completedStops = stops.filter { it.status.name == "COMPLETED" }
        
        if (stops.isNotEmpty()) {
            val list = mutableListOf<QuestDataPoint>()
            list.add(QuestDataPoint(l(currentLanguage, "Bắt đầu", "Start", "起点", "開始", "시작"), 0f, 0f))
            
            var cumDist = 0f
            var cumXp = 0f
            val distPerStop = if (stops.isNotEmpty()) (totalWalkedMeters.coerceAtLeast(400) / stops.size).toFloat() else 200f
            
            stops.forEachIndexed { index, stop ->
                val isDone = index < completedStops.size
                cumDist += distPerStop
                cumXp += if (isDone) 50f else 30f
                val shortName = "P${index + 1}"
                list.add(QuestDataPoint(shortName, cumDist, cumXp))
            }
            list
        } else {
            listOf(
                QuestDataPoint(l(currentLanguage, "Chặng 1", "Leg 1", "第1站", "第1区間", "1구간"), 350f, 50f),
                QuestDataPoint(l(currentLanguage, "Chặng 2", "Leg 2", "第2站", "第2区間", "2구간"), 720f, 110f),
                QuestDataPoint(l(currentLanguage, "Chặng 3", "Leg 3", "第3站", "第3区間", "3구간"), 1100f, 160f),
                QuestDataPoint(l(currentLanguage, "Chặng 4", "Leg 4", "第4站", "第4区間", "4구간"), 1450f, 210f)
            )
        }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedMetric, questPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quest_progress_tracker_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A1C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E6041))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = l(currentLanguage, "Tiến Trình & Điểm Di Sản", "Walked & Heritage Analytics", "步行与文化积分追踪", "歩行距離＆遺産ポイント", "걸은 거리 및 문화 포인트"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = l(currentLanguage, "Theo dõi khoảng cách & XP qua các chặng", "Distance & XP curve across quest legs", "实时追踪探索足迹与文化成长", "クエスト区間ごとの成長曲線", "퀘스트 구간별 성장 곡선"),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab toggles (All, Walked Distance, Cultural Points)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1D13))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabLabels: List<String> = listOf(
                    l(currentLanguage, "Tổng Hợp", "Combined", "综合", "統合", "통합"),
                    l(currentLanguage, "Khoảng Cách (m)", "Distance (m)", "距离(米)", "距離(m)", "거리(m)"),
                    l(currentLanguage, "Điểm Di Sản (XP)", "Heritage XP", "文化分(XP)", "文化XP", "문화 XP")
                )
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = selectedMetric == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color(0xFF1B4D30) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Color(0xFF34D399) else Color.Transparent,
                                shape = RoundedCornerShape(9.dp)
                            )
                            .padding(vertical = 6.dp)
                            .testTag("chart_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recharts-style Area & Bar Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val paddingLeft = 32f
                    val paddingRight = 16f
                    val paddingTop = 12f
                    val paddingBottom = 24f

                    val chartWidth = w - paddingLeft - paddingRight
                    val chartHeight = h - paddingTop - paddingBottom

                    val maxDist = (questPoints.maxOfOrNull { it.distanceMeters } ?: 1500f).coerceAtLeast(500f)
                    val maxXp = (questPoints.maxOfOrNull { it.culturalXp } ?: 200f).coerceAtLeast(100f)

                    // Draw Horizontal Grid Lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = paddingTop + (chartHeight / gridLines) * i
                        drawLine(
                            color = Color(0xFF1E3A2B),
                            start = Offset(paddingLeft, y),
                            end = Offset(w - paddingRight, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    val stepX = chartWidth / (questPoints.size - 1).coerceAtLeast(1)

                    // 1. Draw Distance (Green Area / Gradient under curve) if 0 or 1
                    if (selectedMetric == 0 || selectedMetric == 1) {
                        val path = Path()
                        val fillPath = Path()

                        questPoints.forEachIndexed { index, pt ->
                            val x = paddingLeft + index * stepX
                            val normalized = (pt.distanceMeters / maxDist) * animProgress.value
                            val y = paddingTop + chartHeight - (normalized * chartHeight)

                            if (index == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, paddingTop + chartHeight)
                                fillPath.lineTo(x, y)
                            } else {
                                val prevX = paddingLeft + (index - 1) * stepX
                                val prevNorm = (questPoints[index - 1].distanceMeters / maxDist) * animProgress.value
                                val prevY = paddingTop + chartHeight - (prevNorm * chartHeight)
                                
                                val cx1 = prevX + (x - prevX) / 2
                                val cy1 = prevY
                                val cx2 = prevX + (x - prevX) / 2
                                val cy2 = y
                                path.cubicTo(cx1, cy1, cx2, cy2, x, y)
                                fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                            }

                            if (index == questPoints.size - 1) {
                                fillPath.lineTo(x, paddingTop + chartHeight)
                                fillPath.close()
                            }
                        }

                        // Gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.35f), Color(0xFF10B981).copy(alpha = 0.0f)),
                                startY = paddingTop,
                                endY = paddingTop + chartHeight
                            )
                        )

                        // Smooth Line
                        drawPath(
                            path = path,
                            color = Color(0xFF34D399),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Data points
                        questPoints.forEachIndexed { index, pt ->
                            val x = paddingLeft + index * stepX
                            val normalized = (pt.distanceMeters / maxDist) * animProgress.value
                            val y = paddingTop + chartHeight - (normalized * chartHeight)

                            drawCircle(
                                color = Color(0xFF064E3B),
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color(0xFF34D399),
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }

                    // 2. Draw Cultural XP (Amber Bar / Line) if 0 or 2
                    if (selectedMetric == 0 || selectedMetric == 2) {
                        val barWidth = 14.dp.toPx()
                        questPoints.forEachIndexed { index, pt ->
                            val x = paddingLeft + index * stepX
                            val normalized = (pt.culturalXp / maxXp) * animProgress.value
                            val barH = normalized * chartHeight
                            val barY = paddingTop + chartHeight - barH

                            if (selectedMetric == 2) {
                                // Draw stylized rounded bars
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706)),
                                        startY = barY,
                                        endY = paddingTop + chartHeight
                                    ),
                                    topLeft = Offset(x - barWidth / 2, barY),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            } else {
                                // In dual mode, draw secondary golden points and dashed track
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(x, barY)
                                )
                            }
                        }
                    }

                    // X-Axis Labels
                    questPoints.forEachIndexed { index, pt ->
                        val x = paddingLeft + index * stepX
                        val textLayout = textMeasurer.measure(
                            text = pt.label,
                            style = TextStyle(color = Color(0xFF64748B), fontSize = 9.sp)
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(x - textLayout.size.width / 2, h - paddingBottom + 4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = l(currentLanguage, "Khoảng cách (m)", "Walked Distance (m)", "已步行距离(米)", "歩行距離(m)", "걸은 거리(m)"),
                        fontSize = 11.sp,
                        color = Color(0xFFA7F3D0)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = l(currentLanguage, "Điểm di sản (XP)", "Cultural XP", "文化积分(XP)", "文化XP", "문화 XP"),
                        fontSize = 11.sp,
                        color = Color(0xFFFDE68A)
                    )
                }
            }
        }
    }
}
