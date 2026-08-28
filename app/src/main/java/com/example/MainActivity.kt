package com.example

import android.os.Bundle
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.QuestViewModel
import com.example.ui.QuestViewModelFactory
import com.example.ui.UserStatsViewModel
import com.example.ui.UserStatsViewModelFactory
import com.example.ui.components.BadgesAndReviewsView
import com.example.ui.components.ExploreScreen
import com.example.ui.components.GlobalSyncStatusBar
import com.example.ui.components.LeaderboardView
import com.example.ui.components.ProfileScreen
import com.example.ui.components.QuestScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.HemQuestTheme
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.data.AppDatabase
import com.example.repository.GeminiQuestRepository
import com.example.repository.OfflineQuestRepository

import com.example.util.l

import com.example.auth.AuthManager
import com.example.auth.AuthViewModel
import com.example.auth.AuthViewModelFactory
import com.example.repository.UserAuthRepository

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { OfflineQuestRepository(GeminiQuestRepository(), database.questDao()) }
    private val prefs by lazy { getSharedPreferences("hemquest_prefs", MODE_PRIVATE) }
    private val userAuthRepository by lazy { UserAuthRepository(this, database.userStatsDao()) }
    private val authManager by lazy { AuthManager(this, database.userStatsDao(), database.questDao(), database.passportPhotoDao()) }
    private val notificationManager by lazy { com.example.util.AppNotificationManager.getInstance(this) }
    private val viewModel: QuestViewModel by viewModels { QuestViewModelFactory(repository, prefs) }
    private val userStatsViewModel: UserStatsViewModel by viewModels { UserStatsViewModelFactory(database.userStatsDao(), database.questDao(), authManager, notificationManager, database.passportPhotoDao()) }
    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(authManager) }

    @androidx.compose.material3.ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Trigger auth repository observer check
        userAuthRepository.triggerUserInitialization()
        enableEdgeToEdge()
        setContent {
            HemQuestTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Permissions granted */ }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.CAMERA
                    )
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        permissionsToRequest.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())

                    // Start background/foreground PedometerService for continuous tracking
                    try {
                        com.example.service.PedometerService.startService(context)
                    } catch (e: Exception) {
                        // Fallback handling
                    }

                    // Observe real-time footsteps, calories, and CO2 emissions from PedometerService
                    launch {
                        var lastRecordedSteps = 0
                        com.example.service.PedometerService.pedometerState.collect { pedometerData ->
                            val currentSteps = pedometerData.liveSteps
                            if (currentSteps > lastRecordedSteps) {
                                val delta = currentSteps - lastRecordedSteps
                                lastRecordedSteps = currentSteps
                                viewModel.onStepDetected(delta)
                                userStatsViewModel.onStepDetected(delta)
                            }
                        }
                    }

                    // Seed initial curated mock quests (e.g. Bách Khoa Hẻm, Thanh Đa) to Firestore if empty
                    launch {
                        try {
                            com.example.repository.MockQuestSeeder.seedIfNeeded()
                        } catch (e: Exception) {
                            // Offline or permissions handling
                        }
                    }

                    // Real-time GPS location & distance tracker
                    launch {
                        try {
                            val tracker = com.example.util.LocationTracker(context)
                            tracker.getLocationFlow().collect { loc ->
                                if (loc != null) {
                                    viewModel.updateUserLocation(loc.latitude, loc.longitude)
                                    userStatsViewModel.onLocationUpdate(loc)
                                }
                            }
                        } catch (e: Exception) {
                            // Location permission might be pending
                        }
                    }
                }
                HemQuestApp(viewModel, userStatsViewModel, authViewModel)
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun HemQuestApp(
    viewModel: QuestViewModel,
    userStatsViewModel: com.example.ui.UserStatsViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "splash"
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLanguage = uiState.questRequest.language
    val syncState by userStatsViewModel.syncState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUid = authUiState.userProfile?.uid
    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationManager = remember { com.example.util.AppNotificationManager.getInstance(context) }
    val currentNotification by notificationManager.currentInAppNotification.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        NavigationGraph(
            navController = navController,
            viewModel = viewModel,
            userStatsViewModel = userStatsViewModel,
            authViewModel = authViewModel
        )

        // In-App Notification Banner Overlay for XP & Level Ups
        com.example.ui.components.InAppNotificationBanner(
            notification = currentNotification,
            onDismiss = { notificationManager.dismissInAppNotification() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Global Synchronization & Shimmer Feedback Overlay
        GlobalSyncStatusBar(
            syncState = syncState,
            currentLanguage = currentLanguage,
            modifier = Modifier.align(Alignment.TopCenter),
            onRetry = {
                if (currentUid != null) {
                    userStatsViewModel.syncToFirestore(currentUid)
                }
            }
        )

        // Floating Pill Bottom Navigation Bar Overlay
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomNavigationBar(
                    navController = navController,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

private data class NavDestination(
    val route: String,
    val icon: ImageVector,
    val vi: String,
    val en: String,
    val zh: String,
    val ja: String,
    val ko: String,
    val isCenterHero: Boolean = false
)

@Composable
fun BottomNavigationBar(navController: NavHostController, currentLanguage: String) {
    val destinations = remember {
        listOf(
            NavDestination("explore", Icons.Default.Explore, "Khám phá", "Explore", "探索", "探索", "탐색"),
            NavDestination("leaderboard", Icons.Default.EmojiEvents, "Xếp hạng", "Rank", "排行榜", "順位", "리더보드"),
            NavDestination("quest", Icons.Default.Map, "Hành trình", "Quest", "任务", "クエスト", "퀘스트", isCenterHero = true),
            NavDestination("badges", Icons.Default.MilitaryTech, "Huy hiệu", "Badges", "徽章", "バッジ", "배지"),
            NavDestination("profile", Icons.Default.Person, "Cá nhân", "Profile", "个人", "マイページ", "프로필")
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "explore"

    Box(
        modifier = Modifier
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
                destinations.forEach { item ->
                    val selected = currentRoute == item.route
                    val label = l(currentLanguage, item.vi, item.en, item.zh, item.ja, item.ko)
                    val interactionSource = remember { MutableInteractionSource() }
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.06f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "tab_scale"
                    )

                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon container with active highlight or center hero highlight
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

                            // Live activity dot for center Discover
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

                        // Text permanently under logo for all tabs
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

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun NavigationGraph(
    navController: NavHostController,
    viewModel: QuestViewModel,
    userStatsViewModel: com.example.ui.UserStatsViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isVi = uiState.questRequest.language == "vi"

    var showGlossary by remember { mutableStateOf(false) }
    var glossaryTermId by remember { mutableStateOf<String?>(null) }

    val openGlossaryAction: (String?) -> Unit = { termId ->
        glossaryTermId = termId
        showGlossary = true
    }

    if (showGlossary) {
        com.example.ui.components.CulturalGlossaryBottomSheet(
            currentLanguage = uiState.questRequest.language,
            initialTermId = glossaryTermId,
            onDismiss = {
                showGlossary = false
                glossaryTermId = null
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) },
        popExitTransition = { fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) }
    ) {
        composable("splash") {
            com.example.ui.components.SplashScreen(
                onTimeout = { navController.navigate("explore") { popUpTo("splash") { inclusive = true } } }
            )
        }
        composable("explore") {
            val userStats by userStatsViewModel.userStats.collectAsStateWithLifecycle()
            ExploreScreen(
                currentLanguage = uiState.questRequest.language,
                greenScore = uiState.currentQuest?.greenScore?.score ?: 120,
                streak = userStats.currentStreak,
                xp = userStats.totalXp,
                onSetLanguage = { viewModel.setLanguage(it) },
                onOpenGreenScore = { viewModel.toggleGreenScoreDialog(true) },
                onOpenStreak = { viewModel.toggleStreakInfoDialog(true) },
                onOpenXp = { viewModel.toggleXpInfoDialog(true) },
                onOpenBuilder = { viewModel.openQuestBuilder() },
                onOpenGlossary = { openGlossaryAction(null) },
                onOpenLeaderboard = {
                    navController.navigate("leaderboard") {
                        launchSingleTop = true
                    }
                },
                onStartQuest = { request -> 
                    viewModel.generateNewQuest(request)
                    navController.navigate("quest") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("quest") {
            QuestScreen(
                viewModel = viewModel,
                userStatsViewModel = userStatsViewModel,
                authViewModel = authViewModel,
                onBack = {
                    navController.navigate("explore") {
                        popUpTo("explore") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onOpenGlossary = { termId -> openGlossaryAction(termId) }
            )
        }
        composable("leaderboard") {
            LeaderboardView(
                currentLanguage = uiState.questRequest.language,
                userStatsViewModel = userStatsViewModel,
                authViewModel = authViewModel,
                onBack = null
            )
        }
        composable("badges") {
            BadgesAndReviewsView(
                currentLanguage = uiState.questRequest.language,
                userStatsViewModel = userStatsViewModel,
                authViewModel = authViewModel,
                onBack = null
            )
        }
        composable("profile") {
            ProfileScreen(
                isVi = isVi,
                greenScore = uiState.currentQuest?.greenScore?.score ?: 120,
                currentLanguage = uiState.questRequest.language,
                onSetLanguage = { viewModel.setLanguage(it) },
                onOpenGlossary = { openGlossaryAction(null) },
                onNavigateToBadges = {
                    navController.navigate("badges") {
                        launchSingleTop = true
                    }
                },
                onNavigateToLeaderboard = {
                    navController.navigate("leaderboard") {
                        launchSingleTop = true
                    }
                },
                authViewModel = authViewModel,
                userStatsViewModel = userStatsViewModel,
                onResetAllUserData = {
                    viewModel.exitAndResetQuest()
                },
                onSignOut = {
                    viewModel.exitAndResetQuest()
                    userStatsViewModel.clearSessionOnSignOut()
                    navController.navigate("splash") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = null
            )
        }
    }
}
