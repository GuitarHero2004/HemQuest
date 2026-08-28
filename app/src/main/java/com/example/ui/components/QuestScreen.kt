package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.auth.AuthViewModel
import com.example.model.Quest
import com.example.model.QuestStop
import com.example.model.StopStatus
import com.example.ui.QuestUiState
import com.example.ui.QuestViewModel
import com.example.ui.UserStatsViewModel
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun QuestScreen(
    viewModel: QuestViewModel,
    userStatsViewModel: UserStatsViewModel,
    authViewModel: AuthViewModel? = null,
    onBack: (() -> Unit)? = null,
    onOpenGlossary: ((String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userStats by userStatsViewModel.userStats.collectAsStateWithLifecycle()
    
    val currentLanguage = uiState.questRequest.language
    val isVi = currentLanguage == "vi"
    val context = LocalContext.current

    var showCameraModal by remember { mutableStateOf(false) }
    var showExitQuestDialog by remember { mutableStateOf(false) }
    val activeStop = uiState.selectedStop ?: uiState.currentQuest?.stops?.firstOrNull { it.status != StopStatus.COMPLETED }
    
    Box(modifier = modifier.fillMaxSize()) {
        GoongMapView(
            quest = uiState.currentQuest,
            selectedStop = uiState.selectedStop,
            onSelectStop = { stop -> viewModel.selectStop(stop) },
            userLatitude = uiState.userLocationLat,
            userLongitude = uiState.userLocationLng,
            onSimulateStep = { 
                viewModel.simulateStepTowardsOngoingStop()
                userStatsViewModel.addSteps(45)
            },
            isJourneyStarted = uiState.isJourneyStarted,
            currentLanguage = currentLanguage
        )

        // Top Quest Walking Sequence HUD
        if (uiState.currentQuest != null) {
            QuestSequenceProgressHUD(
                quest = uiState.currentQuest!!,
                uiState = uiState,
                currentLanguage = currentLanguage,
                onSelectStop = { stop -> viewModel.selectStop(stop) },
                onSimulateWalkStep = {
                    viewModel.simulateStepTowardsOngoingStop()
                    userStatsViewModel.addSteps(45)
                },
                onToggleExpand = { viewModel.toggleSequenceHudExpanded() },
                onExitQuest = {
                    viewModel.exitAndResetQuest()
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp)
            )
        }

        // Free Roam Mode Floating Card when no quest is active
        if (uiState.currentQuest == null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .testTag("free_roam_map_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ForestGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(GrabGreen, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(currentLanguage, "CHẾ ĐỘ TỰ DO", "FREE ROAM MODE", "自由漫游模式", "自由探索モード", "자유 탐색 모드"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }

                        Text(
                            text = "📍 " + l(currentLanguage, "Hẻm Sài Gòn", "Saigon Alleys", "西贡胡同", "サイゴンの路地", "사이공 골목"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink600
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = l(
                            currentLanguage,
                            "Bản Đồ Khám Phá Hẻm Phố",
                            "Saigon Alleyway Map Explorer",
                            "西贡胡同街区探索地图",
                            "サイゴン路地裏マップ探索",
                            "사이공 골목길 탐색 지도"
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink900
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = l(
                            currentLanguage,
                            "Hiện không có quest đang chạy. Bạn có thể tự do dạo ngõ hẻm hoặc tạo một nhiệm vụ mới với AI.",
                            "No active quest on map. Wander freely around city alleys or create a personalized AI quest.",
                            "当前没有进行中的任务。您可以自由漫游街巷或使用 AI 生成新任务。",
                            "アクティブなクエストはありません。路地裏を自由に散策するか、AIで新しいクエストを作成できます。",
                            "진행 중인 퀘스트가 없습니다. 자유롭게 골목을 거닐거나 AI로 새 퀘스트를 만들어 보세요."
                        ),
                        fontSize = 12.5.sp,
                        color = Ink600,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onBack?.invoke() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.42f)
                                .height(44.dp)
                                .testTag("back_to_explore_list_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Ink900
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Khám phá", "Explore", "探索列表", "リスト", "목록"),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink900
                            )
                        }

                        Button(
                            onClick = { viewModel.openQuestBuilder() },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.58f)
                                .height(44.dp)
                                .testTag("open_builder_from_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Tạo Quest AI", "Create Quest", "创建任务", "クエスト作成", "퀘스트 생성"),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Floating Bottom Active Checkpoint Card (above bottom nav bar)
        if (uiState.currentQuest != null && activeStop != null) {
            val totalStops = uiState.currentQuest?.stops?.size ?: 1
            val currentIndex = (uiState.currentQuest?.stops?.indexOfFirst { it.id == activeStop.id } ?: 0) + 1
            val isCurrentCompleted = activeStop.status == StopStatus.COMPLETED

            if (!uiState.isJourneyStarted) {
                // Ready to explore preview card before starting journey
                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 84.dp, start = 14.dp, end = 14.dp)
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .testTag("start_journey_preview_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = GrabGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = l(currentLanguage, "SẴN SÀNG KHÁM PHÁ", "READY TO EXPLORE", "准备探索", "探索の準備完了", "탐색 준비 완료"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "🚩 $totalStops ${l(currentLanguage, "chặng", "stops", "站", "スポット", "지점")}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink600
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.currentQuest?.title ?: activeStop.name,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "📍 ${l(currentLanguage, "Chặng 1:", "Stop 1:", "第1站:", "スポット1:", "지점 1:")} ${activeStop.name}",
                            fontSize = 12.sp,
                            color = Ink600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.exitAndResetQuest()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF4444)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(0.38f)
                                    .height(46.dp)
                                    .testTag("cancel_quest_preview_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = l(currentLanguage, "Thoát", "Exit", "退出", "終了", "나가기"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.startJourney()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(0.62f)
                                    .height(46.dp)
                                    .testTag("start_journey_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = l(currentLanguage, "Bắt đầu", "Start", "开始旅程", "開始", "시작"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 84.dp, start = 14.dp, end = 14.dp)
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .testTag("active_checkpoint_card")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        // Header Row: Progress Pill + Shortcuts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isCurrentCompleted) ForestGreen.copy(alpha = 0.12f) else ClayOrange.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, if (isCurrentCompleted) ForestGreen.copy(alpha = 0.3f) else ClayOrange.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrentCompleted) ForestGreen else ClayOrange)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCurrentCompleted) {
                                            l(currentLanguage, "Đã xong", "Completed", "已完成", "完了", "완료")
                                        } else {
                                            l(currentLanguage, "Chặng $currentIndex / $totalStops", "Stop $currentIndex / $totalStops", "第 $currentIndex / $totalStops 站", "スポット $currentIndex / $totalStops", "스팟 $currentIndex / $totalStops")
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentCompleted) ForestGreen else ClayOrange
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Google Maps Shortcut with official icon
                                Surface(
                                    onClick = {
                                        val gmmIntentUri = Uri.parse("google.navigation:q=${activeStop.latitude},${activeStop.longitude}&mode=w")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            val fallbackUri = Uri.parse("geo:${activeStop.latitude},${activeStop.longitude}?q=${activeStop.latitude},${activeStop.longitude}(${Uri.encode(activeStop.name)})")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                                        }
                                    },
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("open_google_maps_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_google_maps),
                                            contentDescription = "Google Maps",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Maps",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Ink900
                                        )
                                    }
                                }

                                // Stop Details Button
                                Surface(
                                    onClick = {
                                        viewModel.selectStop(activeStop)
                                    },
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("view_stop_detail_button")
                                 ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = l(currentLanguage, "Chi tiết", "Details", "详情", "詳細", "상세"),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ForestGreen
                                        )
                                    }
                                }

                                // Exit Quest Button
                                Surface(
                                    onClick = { showExitQuestDialog = true },
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("exit_quest_card_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Exit Quest",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = l(currentLanguage, "Thoát", "Exit", "退出", "終了", "나가기"),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stop Title
                        Text(
                            text = activeStop.name,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Distance & Walking Estimation Row
                        if (!isCurrentCompleted && uiState.distanceToOngoingMeters > 0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            val formattedDist = if (uiState.distanceToOngoingMeters < 1000) {
                                "${uiState.distanceToOngoingMeters}m"
                            } else if (uiState.distanceToOngoingMeters < 50_000) {
                                String.format(java.util.Locale.US, "%.1f km", uiState.distanceToOngoingMeters / 1000.0)
                            } else {
                                "${uiState.distanceToOngoingMeters / 1000} km"
                            }

                            val walkTime = if (uiState.estimatedMinutesWalk in 1..59) {
                                "~${uiState.estimatedMinutesWalk} " + l(currentLanguage, "phút đi bộ", "min walk", "分钟步行", "分歩行", "분 도보")
                            } else if (uiState.estimatedMinutesWalk in 60..1439) {
                                "~${uiState.estimatedMinutesWalk / 60}h ${uiState.estimatedMinutesWalk % 60}m"
                            } else {
                                ""
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = buildString {
                                            append(l(currentLanguage, "Cách bạn:", "Distance:", "距离:", "現在地から:", "거리:"))
                                            append(" ")
                                            append(formattedDist)
                                            if (walkTime.isNotEmpty()) {
                                                append(" • ")
                                                append(walkTime)
                                            }
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForestGreen
                                    )
                                }

                                if (uiState.cardinalDirection.isNotBlank()) {
                                    Text(
                                        text = uiState.cardinalDirection,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink600
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        // Challenge Prompt in a clean minimal pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = ClayOrange,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeStop.challenge.prompt,
                                    fontSize = 11.5.sp,
                                    color = Ink900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(9.dp))

                        // Actions Row: AI Camera Verification + Direct Stop Confirmation
                        if (!isCurrentCompleted) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // AI Camera Verification Button
                                Button(
                                    onClick = {
                                        showCameraModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClayOrange),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .weight(1.15f)
                                        .height(44.dp)
                                        .testTag("open_camera_modal_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Chụp ảnh AI (+50 XP)",
                                                "AI Camera (+50 XP)",
                                                "AI 拍照 (+50 XP)",
                                                "AI 写真 (+50 XP)",
                                                "AI 사진 (+50 XP)"
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Direct Confirm Stop Completion Button (Check-in)
                                Button(
                                    onClick = {
                                        viewModel.confirmStopCompletion()
                                        userStatsViewModel.incrementCheckpoints()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .height(44.dp)
                                        .testTag("confirm_stop_completion_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Đã đến chặng",
                                                "Check-in Stop",
                                                "已到达本站",
                                                "到着確認",
                                                "도착 확인"
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                color = ForestGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = l(currentLanguage, "Đã hoàn thành chặng này!", "Stop completed!", "此站已完成！", "このスポットは完了しました！", "이 지점을 완료했습니다!"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // In-App Camera Modal
        if (showCameraModal && activeStop != null) {
            InAppCameraModal(
                stop = activeStop,
                currentLanguage = currentLanguage,
                isVerifying = uiState.isVerifyingPhoto,
                onDismiss = {
                    if (!uiState.isVerifyingPhoto) {
                        showCameraModal = false
                    }
                },
                onVerifyWithAi = { bitmap ->
                    // Keep modal open so the loading state & scanning animation is shown to user
                    viewModel.verifyPhotoChallenge(bitmap)
                },
                onDirectConfirmCompletion = { bitmap ->
                    showCameraModal = false
                    viewModel.confirmStopCompletion()
                    userStatsViewModel.incrementCheckpoints()
                    if (bitmap != null) {
                        val currentQuest = uiState.currentQuest
                        userStatsViewModel.savePassportPhoto(
                            activeStop.id,
                            activeStop.name,
                            currentQuest?.id ?: "",
                            currentQuest?.title ?: "",
                            bitmap,
                            verificationType = "MANUAL_VERIFIED"
                        )
                    }
                }
            )
        }

        // Auto close camera modal when Gemini verification sheet appears
        LaunchedEffect(uiState.showPhotoVerificationSheet) {
            if (uiState.showPhotoVerificationSheet) {
                showCameraModal = false
            }
        }

        // Floating AI Vision Analysis Progress Indicator over Map if modal is not active
        if (uiState.isVerifyingPhoto && !showCameraModal) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 220.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.5.dp, GrabGreen),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = GrabGreen,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = l(
                                currentLanguage,
                                "Gemini AI Vision đang đối chiếu ảnh...",
                                "Gemini AI Vision analyzing photo...",
                                "Gemini AI 视觉正在比对照片...",
                                "Gemini AI Visionが画像を照合中...",
                                "Gemini AI Vision이 사진을 대조 중..."
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = l(
                                currentLanguage,
                                "Đang nhận diện kiến trúc & di sản hẻm phố",
                                "Recognizing alleyway heritage & architecture",
                                "正在识别胡同遗产与建筑特征",
                                "路地裏の建築・遺産要素を認識しています",
                                "골목 유산 및 건축 양식을 분석하고 있습니다"
                            ),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Photo Verification Bottom Sheet (Displays Gemini multimodal analysis result)
        if (uiState.showPhotoVerificationSheet && uiState.verificationResult != null) {
            PhotoVerificationBottomSheet(
                result = uiState.verificationResult,
                capturedBitmap = uiState.lastCapturedBitmap,
                currentLanguage = currentLanguage,
                onConfirmComplete = {
                    viewModel.confirmStopCompletion()
                    userStatsViewModel.incrementCheckpoints()
                    val capturedBitmap = uiState.lastCapturedBitmap
                    val currentQuest = uiState.currentQuest
                    if (activeStop != null && capturedBitmap != null) {
                        userStatsViewModel.savePassportPhoto(
                            activeStop.id,
                            activeStop.name,
                            currentQuest?.id ?: "",
                            currentQuest?.title ?: "",
                            capturedBitmap,
                            verificationType = "AI_VERIFIED"
                        )
                    }
                },
                onDismiss = {
                    viewModel.closePhotoVerificationSheet()
                }
            )
        }

        // Sheets & Dialogs
        if (uiState.showCheckpointDiscoveryDialog && uiState.discoveryStop != null) {
            CheckpointDiscoveryDialog(
                stop = uiState.discoveryStop,
                currentLanguage = currentLanguage,
                onDismiss = { viewModel.closeCheckpointDiscovery() },
                onStartChallenge = {
                    viewModel.closeCheckpointDiscovery()
                    showCameraModal = true
                },
                onConfirmCompletion = {
                    viewModel.confirmStopCompletion()
                    userStatsViewModel.incrementCheckpoints()
                }
            )
        }

        if (uiState.showCheckpointDetailSheet && uiState.selectedStop != null) {
            CheckpointDetailSheet(
                stop = uiState.selectedStop!!,
                stopIndex = uiState.currentQuest?.stops?.indexOfFirst { it.id == uiState.selectedStop?.id } ?: 0,
                totalStops = uiState.currentQuest?.stops?.size ?: 1,
                isVi = isVi,
                currentLanguage = currentLanguage,
                onDismiss = { viewModel.closeCheckpointDetail() },
                onStartCameraChallenge = { 
                    viewModel.closeCheckpointDetail()
                    showCameraModal = true
                },
                onConfirmCompletion = {
                    viewModel.confirmStopCompletion()
                    userStatsViewModel.incrementCheckpoints()
                },
                onSkipStop = { viewModel.skipCurrentStop() },
                onOpenGlossaryForTerm = { termId ->
                    viewModel.closeCheckpointDetail()
                    onOpenGlossary?.invoke(termId)
                }
            )
        }

        if (showExitQuestDialog) {
            AlertDialog(
                onDismissRequest = { showExitQuestDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = l(
                            currentLanguage,
                            "Dừng hành trình này?",
                            "Exit Active Quest?",
                            "退出当前任务？",
                            "探索を終了しますか？",
                            "퀘스트를 종료하시겠습니까?"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Ink900
                    )
                },
                text = {
                    Text(
                        text = l(
                            currentLanguage,
                            "Bạn có muốn thoát khỏi nhiệm vụ này không? Tiến trình các chặng đã hoàn thành vẫn sẽ được lưu vào lịch sử khám phá của bạn.",
                            "Do you want to exit this quest? Completed checkpoints and XP will still be saved to your travel history.",
                            "您确定要退出此任务吗？已完成的打卡点与XP将保留在您的探索历史中。",
                            "このクエストを終了しますか？完了したスポットとXPは旅行履歴に保存されます。",
                            "이 퀘스트를 종료하시겠습니까? 이미 완료한 체크포인트와 XP는 여행 기록에 저장됩니다."
                        ),
                        fontSize = 13.5.sp,
                        color = Ink600,
                        lineHeight = 19.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitQuestDialog = false
                            viewModel.exitAndResetQuest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = l(currentLanguage, "Thoát Hành Trình", "Exit Quest", "确认退出", "終了する", "퀘스트 종료"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showExitQuestDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = l(currentLanguage, "Tiếp tục khám phá", "Keep Exploring", "继续探索", "探索を続ける", "계속 탐험"),
                            color = Ink900
                        )
                    }
                },
                containerColor = PaperWhite,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (uiState.showStreakInfoDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.toggleStreakInfoDialog(false) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.toggleStreakInfoDialog(false) }) {
                        androidx.compose.material3.Text(l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"))
                    }
                },
                title = { androidx.compose.material3.Text(l(currentLanguage, "Chuỗi Khám Phá", "Discovery Streak", "探索连胜", "連続探索記録", "연속 탐험 기록")) },
                text = { androidx.compose.material3.Text(l(currentLanguage, "Bạn đã khám phá hẻm liên tục trong ${userStats.currentStreak} ngày! Tiếp tục để nhận thêm điểm XP.", "You've been exploring alleys for ${userStats.currentStreak} days in a row! Keep it up for bonus XP.", "您已连续${userStats.currentStreak}天探索胡同！继续保持以获取更多经验值。", "${userStats.currentStreak}日連続で路地裏を探索中！ボーナスXPを獲得しよう。", "${userStats.currentStreak}일 연속으로 골목을 탐험 중입니다! 계속해서 보너스 XP를 획득하세요.")) },
                containerColor = com.example.ui.theme.PaperWhite
            )
        }
        
        if (uiState.errorMessage != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.clearError() }) {
                        androidx.compose.material3.Text("OK")
                    }
                },
                title = { androidx.compose.material3.Text("Error") },
                text = { androidx.compose.material3.Text(uiState.errorMessage ?: "") },
                containerColor = com.example.ui.theme.PaperWhite
            )
        }

        if (uiState.showXpInfoDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.toggleXpInfoDialog(false) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.toggleXpInfoDialog(false) }) {
                        androidx.compose.material3.Text(l(currentLanguage, "Đóng", "Close", "关闭", "閉じる", "닫기"))
                    }
                },
                title = { androidx.compose.material3.Text("XP") },
                text = { androidx.compose.material3.Text(l(currentLanguage, "Bạn đang có ${userStats.totalXp} XP. Điểm kinh nghiệm giúp bạn lên cấp và mở khóa phần thưởng.", "You have ${userStats.totalXp} XP. Experience points help you level up and unlock rewards.", "您拥有 ${userStats.totalXp} 经验值。经验值可帮助您升级并解锁奖励。", "現在 ${userStats.totalXp} XPを保有しています。経験値を獲得してレベルアップや報酬のアンロックをしよう。", "현재 ${userStats.totalXp} XP를 보유하고 있습니다. 경험치는 레벨업과 보상 해제에 도움이 됩니다.")) },
                containerColor = com.example.ui.theme.PaperWhite
            )
        }
        
        if (uiState.isLoading || uiState.isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.example.ui.theme.PaperWhite.copy(alpha = 0.8f))
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.CircularProgressIndicator(color = com.example.ui.theme.GrabGreen)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                    androidx.compose.material3.Text(
                        l(currentLanguage, "Đang cập nhật...", "Updating...", "正在更新...", "更新中...", "업데이트 중..."),
                        color = com.example.ui.theme.Ink900,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.showGreenScoreDialog) {
            GreenScoreDialog(
                greenScore = uiState.currentQuest?.greenScore,
                isVi = isVi,
                currentLanguage = currentLanguage,
                onDismiss = { viewModel.toggleGreenScoreDialog(false) }
            )
        }

        if (uiState.showRecapDialog && uiState.currentQuest != null) {
            JourneyRecapDialog(
                quest = uiState.currentQuest,
                isVi = isVi,
                currentLanguage = currentLanguage,
                onSaveAndExit = {
                    val currentQuest = uiState.currentQuest
                    if (currentQuest != null) {
                        userStatsViewModel.saveCompletedQuest(currentQuest)
                    }
                    val dist = uiState.questDistanceMeters.takeIf { it > 0 } ?: 1200.0
                    val xp = if (dist > 2000) 250 else 150
                    userStatsViewModel.completeQuest(
                        xpReward = xp,
                        distanceMeters = dist,
                        questTitle = uiState.currentQuest?.title,
                        questCategory = uiState.currentQuest?.theme,
                        questId = uiState.currentQuest?.id,
                        currentUid = authViewModel?.uiState?.value?.userProfile?.uid
                    )
                    viewModel.saveAndCompleteQuest {
                        onBack?.invoke()
                    }
                },
                onNewQuest = { 
                    val currentQuest = uiState.currentQuest
                    if (currentQuest != null) {
                        userStatsViewModel.saveCompletedQuest(currentQuest)
                    }
                    val dist = uiState.questDistanceMeters.takeIf { it > 0 } ?: 1200.0
                    val xp = if (dist > 2000) 250 else 150
                    userStatsViewModel.completeQuest(
                        xpReward = xp,
                        distanceMeters = dist,
                        questTitle = uiState.currentQuest?.title,
                        questCategory = uiState.currentQuest?.theme,
                        questId = uiState.currentQuest?.id,
                        currentUid = authViewModel?.uiState?.value?.userProfile?.uid
                    )
                    viewModel.exitAndResetQuest()
                    onBack?.invoke() ?: viewModel.openQuestBuilder()
                },
                onDismiss = { viewModel.toggleRecapDialog(false) }
            )
        }

        if (uiState.currentQuest == null && !uiState.isLoading && !uiState.isGenerating) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                color = com.example.ui.theme.PaperWhite,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = l(
                            currentLanguage,
                            "Bản Đồ Vị Trí Sài Gòn",
                            "Saigon Map View",
                            "西贡地图视图",
                            "サイゴンマップビュー",
                            "사이공 지도 보기"
                        ),
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        color = com.example.ui.theme.Ink900
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                    androidx.compose.material3.Text(
                        text = l(
                            currentLanguage,
                            "Chưa có hành trình active. Bạn có thể mở lại Quest gần đây hoặc chọn một hành trình mới từ trang Khám Phá.",
                            "No active quest. You can reload your last saved quest or explore new quests from Discover.",
                            "没有活动任务。您可以重新加载上次保存的任务或从“探索”中选择新任务。",
                            "アクティブなクエストがありません。保存した最新のクエストを再読み込みするか、「発見」から選択してください。",
                            "활성 퀘스트가 없습니다. 최근 저장된 퀘스트를 다시 불러오거나 '탐색'에서 새 퀘스트를 선택하세요."
                        ),
                        fontSize = 13.sp,
                        color = com.example.ui.theme.Ink600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.loadLastSavedQuest() },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.GrabGreen),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            androidx.compose.material3.Text(
                                l(currentLanguage, "Quest Gần Đây", "Recent Quest", "最近任务", "最近のクエスト", "최근 퀘스트"),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 13.sp,
                                color = com.example.ui.theme.GrabGreen
                            )
                        }

                        androidx.compose.material3.Button(
                            onClick = { onBack?.invoke() ?: viewModel.openQuestBuilder() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.GrabGreen
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            androidx.compose.material3.Text(
                                l(currentLanguage, "Khám Phá Mới", "Explore Quests", "探索新任务", "新しい探索", "새 탐색"),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 13.sp,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Connected Quest Walking Sequence & Live Step Counter HUD
 */
@Composable
fun QuestSequenceProgressHUD(
    quest: Quest,
    uiState: QuestUiState,
    currentLanguage: String,
    onSelectStop: (QuestStop) -> Unit,
    onSimulateWalkStep: () -> Unit,
    onToggleExpand: () -> Unit,
    onExitQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStops = quest.stops.size
    val completedStops = quest.stops.count { it.status == StopStatus.COMPLETED }
    val progressPct = if (totalStops > 0) (completedStops * 100) / totalStops else 0

    val infiniteTransition = rememberInfiniteTransition(label = "active_pulse")
    val pulseRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("quest_sequence_progress_hud")
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Header Row: Progress Summary & Collapse Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                Brush.linearGradient(listOf(GrabGreen, ForestGreen)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = l(
                                currentLanguage,
                                "Tiến trình hành trình",
                                "Quest Walking Progress",
                                "探索任务进度",
                                "探索シーケンス進行度",
                                "퀘스트 진행 상태"
                            ),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900
                        )
                        Text(
                            text = "$completedStops/$totalStops ${l(currentLanguage, "chặng", "stops", "站", "スポット", "지점")} • $progressPct%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick step pulse button
                    Surface(
                        onClick = onSimulateWalkStep,
                        color = GrabGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("simulate_step_hud_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👟", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "+35m", "+35m", "+35米", "+35m", "+35m"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isSequenceHudExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Sequence",
                            tint = Ink600,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onExitQuest,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("exit_quest_hud_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Quest",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Smooth Animated Linear Progress Bar
            LinearProgressIndicator(
                progress = { (progressPct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = ForestGreen,
                trackColor = Color(0xFFE2E8F0)
            )

            // Real-time Steps, Distance, Calories, and CO2 Environmental Bar
            Spacer(modifier = Modifier.height(8.dp))
            LiveWalkingMetricsBar(
                stepCount = uiState.questStepCount,
                distanceMeters = uiState.questDistanceMeters,
                caloriesBurned = uiState.questCaloriesBurned,
                co2SavedKg = uiState.questCo2SavedKg,
                currentLanguage = currentLanguage,
                isCompact = !uiState.isSequenceHudExpanded
            )

            // Expanded Sequence Node Stepper
            if (uiState.isSequenceHudExpanded) {
                Spacer(modifier = Modifier.height(10.dp))

                // Connected Checkpoint Stepper Node Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    quest.stops.forEachIndexed { index, stop ->
                        val isDone = stop.status == StopStatus.COMPLETED
                        val isCurrent = stop.id == (uiState.ongoingStop?.id ?: uiState.selectedStop?.id)
                        val isSelected = stop.id == uiState.selectedStop?.id

                        // Step Node Circle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onSelectStop(stop) }
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(36.dp)
                            ) {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .scale(pulseRingScale)
                                            .border(2.dp, ClayOrange.copy(alpha = 0.6f), CircleShape)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isDone -> ForestGreen
                                                isCurrent -> ClayOrange
                                                isSelected -> GrabGreen
                                                else -> Color(0xFFCBD5E1)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = stop.name.take(10) + if (stop.name.length > 10) ".." else "",
                                fontSize = 9.5.sp,
                                fontWeight = if (isCurrent || isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) ClayOrange else if (isDone) ForestGreen else Ink600,
                                maxLines = 1
                            )
                        }

                        // Connecting Line between nodes
                        if (index < totalStops - 1) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(
                                        if (isDone) ForestGreen else Color(0xFFCBD5E1)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWalkingMetricsBar(
    stepCount: Int,
    distanceMeters: Double,
    caloriesBurned: Int,
    co2SavedKg: Double,
    currentLanguage: String,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Formatted metrics
    val formattedDist = if (distanceMeters >= 1000.0) {
        String.format(java.util.Locale.US, "%.2f km", distanceMeters / 1000.0)
    } else {
        "${distanceMeters.toInt()}m"
    }

    val formattedCo2 = if (co2SavedKg < 1.0) {
        val grams = (co2SavedKg * 1000.0).toInt().coerceAtLeast(0)
        "${grams}g"
    } else {
        String.format(java.util.Locale.US, "%.2f kg", co2SavedKg)
    }

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = if (isCompact) 4.dp else 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Footsteps Counter
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👟", fontSize = if (isCompact) 11.sp else 12.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$stepCount ${l(currentLanguage, "bước", "steps", "步", "歩", "걸음")}",
                    fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink900
                )
            }

            // Real-time Distance Traveled
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formattedDist,
                    fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )
            }

            // Real-time Calories Burned
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = ClayOrange,
                    modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "$caloriesBurned kcal",
                    fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClayOrange
                )
            }

            // Real-time CO2 Emission Saved
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = GrabGreen,
                    modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formattedCo2,
                    fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )
            }
        }
    }
}
