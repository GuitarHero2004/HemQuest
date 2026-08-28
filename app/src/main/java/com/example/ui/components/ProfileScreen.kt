package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthUiState
import com.example.auth.AuthViewModel
import com.example.ui.components.shimmerEffect
import com.example.ui.theme.DuoLime
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.SunGold
import com.example.util.l
import kotlinx.coroutines.delay

data class LanguageOption(
    val code: String,
    val flag: String,
    val nativeName: String,
    val englishName: String
)

@Composable
fun ProfileScreen(
    isVi: Boolean,
    greenScore: Int,
    currentLanguage: String = "vi",
    onSetLanguage: (String) -> Unit = {},
    onOpenGlossary: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    userStatsViewModel: com.example.ui.UserStatsViewModel? = null,
    onResetAllUserData: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authUiState by (authViewModel?.uiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(AuthUiState()) })
    val userStatsState = userStatsViewModel?.userStats?.collectAsStateWithLifecycle()
    val userStats = userStatsState?.value ?: com.example.data.UserStatsEntity()
    val locationState by (userStatsViewModel?.locationState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.example.ui.LiveLocationState()) })
    val syncState by (userStatsViewModel?.syncState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.example.ui.FirestoreSyncState()) })
    val allQuests by (userStatsViewModel?.allQuestsFlow?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList<com.example.model.Quest>()) })
    val passportPhotos by (userStatsViewModel?.passportPhotos?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList<com.example.data.PassportPhotoEntity>()) })
    val isAuthenticated = authUiState.userProfile != null
    val userProfile = authUiState.userProfile

    val languages = listOf(
        LanguageOption("vi", "🇻🇳", "Tiếng Việt", "Vietnamese"),
        LanguageOption("en", "🇬🇧", "English", "English"),
        LanguageOption("zh", "🇨🇳", "中文", "Chinese"),
        LanguageOption("ja", "🇯🇵", "日本語", "Japanese"),
        LanguageOption("ko", "🇰🇷", "한국어", "Korean")
    )

    var showLevelRoadmapDialog by remember { mutableStateOf(false) }
    var offlineModeEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var pendingLanguage by remember { mutableStateOf<LanguageOption?>(null) }
    var isApplyingLanguage by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showResetDataConfirmation by remember { mutableStateOf(false) }

    // Success / Notification feedback banner
    if (authUiState.successMessage != null) {
        LaunchedEffect(authUiState.successMessage) {
            delay(3500)
            authViewModel?.clearMessages()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F4))
            .testTag("profile_screen")
    ) {
        HeaderBar(
            title = l(currentLanguage, "Hồ Sơ & Cài Đặt", "Profile & Settings", "个人资料与设置", "プロフィールと設定", "프로필 및 설정"),
            currentLanguage = currentLanguage,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentPadding = PaddingValues(top = 10.dp)
        ) {
                // Notification / Feedback Banner if present
                authUiState.successMessage?.let { msg ->
                    item {
                        Surface(
                            color = GrabGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = msg,
                                    color = ForestGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // User Profile Header Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(26.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(GrabGreen, DuoLime)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (userProfile?.isGoogleLinked == true) "🌐" else "🇻🇳",
                                    fontSize = 36.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = userProfile?.displayName?.ifBlank { "Saigon Walker" } ?: "Saigon Walker",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink900
                            )

                            if (!userProfile?.email.isNullOrBlank()) {
                                Text(
                                    text = userProfile?.email ?: "",
                                    fontSize = 13.sp,
                                    color = Ink600,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Level Badge & XP System
                            val levelInfo = com.example.util.QuestLevelUtils.calculateLevelInfo(userStats.totalXp, currentLanguage)

                            Surface(
                                color = SunGold.copy(alpha = 0.2f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, SunGold),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { showLevelRoadmapDialog = true }
                                    .testTag("level_badge_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = levelInfo.iconEmoji,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${l(currentLanguage, "Cấp ${levelInfo.level}", "Level ${levelInfo.level}", "${levelInfo.level} 级", "レベル ${levelInfo.level}", "레벨 ${levelInfo.level}")} • ${if (currentLanguage == "vi") levelInfo.titleVi else levelInfo.titleEn}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink900
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = Ink600,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // XP Visual Progress Bar Card
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showLevelRoadmapDialog = true }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "⚡", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${levelInfo.currentLevelXp} / ${levelInfo.requiredLevelXp} XP",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Ink900
                                            )
                                            Text(
                                                text = " (${userStats.totalXp} XP ${l(currentLanguage, "tổng", "total", "总计", "合計", "총")})",
                                                fontSize = 11.5.sp,
                                                color = Ink600
                                            )
                                        }

                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Còn ${levelInfo.requiredLevelXp - levelInfo.currentLevelXp} XP lên Cấp ${levelInfo.level + 1}",
                                                "${levelInfo.requiredLevelXp - levelInfo.currentLevelXp} XP to Level ${levelInfo.level + 1}",
                                                "还需 ${levelInfo.requiredLevelXp - levelInfo.currentLevelXp} XP",
                                                "レベル${levelInfo.level + 1}まであと ${levelInfo.requiredLevelXp - levelInfo.currentLevelXp} XP",
                                                "레벨 ${levelInfo.level + 1}까지 ${levelInfo.requiredLevelXp - levelInfo.currentLevelXp} XP 남음"
                                            ),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrabGreen
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Visual Progress Track
                                    LinearProgressIndicator(
                                        progress = { levelInfo.progressFraction },
                                        color = GrabGreen,
                                        trackColor = GrabGreen.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🎁 " + if (currentLanguage == "vi") levelInfo.perkVi else levelInfo.perkEn,
                                            fontSize = 11.sp,
                                            color = Ink600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GrabGreen.copy(alpha = 0.15f),
                                            border = BorderStroke(0.8.dp, GrabGreen.copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = l(currentLanguage, "Xem Lộ Trình", "Roadmap", "查看路线图", "マップを見る", "로드맵 보기"),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForestGreen
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                                    contentDescription = null,
                                                    tint = ForestGreen,
                                                    modifier = Modifier.size(9.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Authentication Section
                            if (!isAuthenticated) {
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Đăng nhập để đồng bộ thành tích & tài khoản Google",
                                        "Sign in to sync stats & link Google Account",
                                        "登录以同步成就并关联谷歌账号",
                                        "ログインして実績とGoogleアカウントを同期",
                                        "Google 계정 연동 및 업적 동기화를 위해 로그인하세요"
                                    ),
                                    fontSize = 12.sp,
                                    color = Ink600,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { authViewModel?.openAuthDialog(isSignUp = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("open_signin_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = l(currentLanguage, "Đăng Nhập", "Sign In", "登录", "ログイン", "로그인"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { authViewModel?.openAuthDialog(isSignUp = true) },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.2.dp, GrabGreen),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("open_signup_button")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = l(currentLanguage, "Đăng Ký", "Sign Up", "注册", "新規登録", "회원가입"),
                                            color = GrabGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                // Authenticated State: Provider Badges + Google Linking Support
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = l(currentLanguage, "Phương thức:", "Methods:", "登录方式：", "認証方法:", "인증 방식:"),
                                        fontSize = 12.sp,
                                        color = Ink600,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (userProfile?.isEmailLinked == true) {
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "✉️ Email/Pass",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Ink900,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    if (userProfile?.isGoogleLinked == true) {
                                        Surface(
                                            color = Color(0xFFE0F2FE),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "🌐 Google",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Google Account Linking Section (Supporting linking Google after Email/Database login)
                                if (userProfile?.isGoogleLinked != true) {
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "🌐", fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = l(
                                                            currentLanguage,
                                                            "Liên kết tài khoản Google",
                                                            "Link Google Account",
                                                            "关联 Google 账号",
                                                            "Google アカウントを連携",
                                                            "Google 계정 연동하기"
                                                        ),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Ink900
                                                    )
                                                    Text(
                                                        text = l(
                                                            currentLanguage,
                                                            "Đăng nhập linh hoạt bằng Email hoặc Google.",
                                                            "Sign in flexibly with either Email or Google.",
                                                            "支持随时使用邮箱或谷歌账号登录。",
                                                            "メールまたはGoogleで柔軟にログイン可能。",
                                                            "이메일 또는 Google로 간편하게 로그인."
                                                        ),
                                                        fontSize = 11.sp,
                                                        color = Ink600
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Button(
                                                onClick = { authViewModel?.linkGoogleAccount(context) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp)
                                                    .testTag("link_google_button")
                                            ) {
                                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = l(
                                                        currentLanguage,
                                                        "Liên Kết Google Ngay",
                                                        "Link Google Now",
                                                        "立即关联 Google",
                                                        "今すぐ Google 連携",
                                                        "지금 Google 연동"
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Google is already linked
                                    Surface(
                                        color = Color(0xFFF0FDF4),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = l(
                                                        currentLanguage,
                                                        "Đã liên kết Google Account",
                                                        "Google Account linked",
                                                        "已关联 Google 账号",
                                                        "Google アカウント連携済み",
                                                        "Google 계정 연동 완료"
                                                    ),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForestGreen
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = { authViewModel?.unlinkGoogleAccount() },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(
                                                    text = l(currentLanguage, "Hủy liên kết", "Unlink", "取消关联", "解除", "해제"),
                                                    fontSize = 11.sp,
                                                    color = Ink600
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Sign Out Button
                                OutlinedButton(
                                    onClick = { showLogoutConfirmation = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = l(currentLanguage, "Đăng Xuất", "Sign Out", "退出登录", "ログアウト", "로그아웃"),
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Quest Progress Tracker Hero Card
                item {
                    val totalQuestsGoal = 10
                    val completedQuestsCount = userStats.completedQuestsCount
                    val questProgressFraction = (completedQuestsCount.toFloat() / totalQuestsGoal.toFloat()).coerceIn(0f, 1f)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = SunGold.copy(alpha = 0.2f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = "🎯", fontSize = 22.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = l(currentLanguage, "Tiến Độ Nhiệm Vụ Hẻm", "Quest Progress Tracker", "胡同任务完成进度", "路地裏クエスト達成度", "골목 퀘스트 진행 현황"),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Ink900
                                        )
                                        Text(
                                            text = "$completedQuestsCount / $totalQuestsGoal ${l(currentLanguage, "nhiệm vụ hoàn thành", "quests completed", "个任务已完成", "個のクエスト完了", "개 퀘스트 완료")} (${(questProgressFraction * 100).toInt()}%)",
                                            fontSize = 12.sp,
                                            color = Ink600
                                        )
                                    }
                                }

                                Surface(
                                    color = GrabGreen.copy(alpha = 0.12f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${(questProgressFraction * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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
                        }
                    }
                }

                item { AlleyScrapbookSection(allQuests, currentLanguage, passportPhotos = passportPhotos) }

                // Real-Time User Location & Live Session Walk Tracker Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF0288D1).copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF0288D1),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = l(currentLanguage, "Vị Trí & Hành Trình Trực Tiếp", "Live GPS Location & Walk", "实时定位与行程追踪", "リアルタイム位置＆歩行", "실시간 위치 및 도보 추적"),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Ink900
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(GrabGreen)
                                            )
                                        }
                                        Text(
                                            text = locationState.districtName,
                                            fontSize = 12.sp,
                                            color = ForestGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFF0288D1).copy(alpha = 0.1f),
                                    shape = CircleShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GpsFixed,
                                            contentDescription = null,
                                            tint = Color(0xFF0288D1),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "GPS ±${locationState.accuracyMeters.toInt()}m",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0288D1)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Coordinates and Real-time Walk Metrics Box
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = l(currentLanguage, "Tọa độ vệ tinh:", "GPS Coordinates:", "卫星坐标：", "衛星座標:", "위성 좌표:"),
                                            fontSize = 11.sp,
                                            color = Ink600
                                        )
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.4f", locationState.latitude)}°N, ${String.format(java.util.Locale.US, "%.4f", locationState.longitude)}°E",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink900
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = l(currentLanguage, "Quãng đường phiên này:", "Session Walked:", "本次行走：", "セッション歩行:", "이번 세션 도보:"),
                                            fontSize = 11.sp,
                                            color = Ink600
                                        )
                                        Text(
                                            text = "${locationState.sessionDistanceMeters.toInt()} m • ${locationState.sessionSteps} " + l(currentLanguage, "bước", "steps", "步", "歩", "걸음"),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Stats Grid Cards (Steps Count, Distance Count, Streak)
                item {
                    val totalDistanceKm = String.format(java.util.Locale.US, "%.2f km", (userStats.totalDistanceMeters.coerceAtLeast(userStats.totalSteps * 0.75).toDouble()) / 1000.0)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Total Steps
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PaperWhite),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = null,
                                    tint = GrabGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%,d".format(userStats.totalSteps),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink900
                                )
                                Text(
                                    text = l(currentLanguage, "Tổng bước chân", "Total Steps", "总步数", "合計歩数", "총 걸음 수"),
                                    fontSize = 10.sp,
                                    color = Ink600
                                )
                            }
                        }

                        // Stat 2: Total Distance Count
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PaperWhite),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = totalDistanceKm,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink900
                                )
                                Text(
                                    text = l(currentLanguage, "Tổng quãng đường", "Total Distance", "总里程", "総距離", "총 이동 거리"),
                                    fontSize = 10.sp,
                                    color = Ink600
                                )
                            }
                        }

                        // Stat 3: Streak & Checkpoints
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PaperWhite),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = StreakFlame,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${userStats.currentStreak} " + l(currentLanguage, "ngày", "Days", "天", "日間", "일"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink900
                                )
                                Text(
                                    text = l(currentLanguage, "Chuỗi khám phá", "Streak", "连续探索", "ストリーク", "연속 탐험"),
                                    fontSize = 10.sp,
                                    color = Ink600
                                )
                            }
                        }
                    }
                }

                // Visual Progress Tracker (Distance & Cultural Points Analytics)
                item {
                    QuestProgressTrackerChart(
                        activeQuest = null,
                        totalWalkedMeters = userStats.totalDistanceMeters.toInt().coerceAtLeast(userStats.totalSteps * 7 / 10),
                        totalXp = userStats.totalXp,
                        currentLanguage = currentLanguage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Cloud Sync & Firestore Status Card
                val currentUid = userProfile?.uid
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = GrabGreen.copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = null,
                                                tint = ForestGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = l(currentLanguage, "Đồng Bộ Firestore Cloud", "Firestore Cloud Sync", "Firestore 云端同步", "Firestore クラウド同期", "Firestore 클라우드 동기화"),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink900
                                        )
                                        Text(
                                            text = if (syncState.isSyncing) {
                                                l(currentLanguage, "Đang đồng bộ dữ liệu...", "Syncing in progress...", "正在同步数据...", "同期中...", "동기화 진행 중...")
                                            } else {
                                                l(currentLanguage, "Huy hiệu & nhiệm vụ được lưu an toàn", "Badges & quests securely synced", "徽章与任务已安全备份", "バッジとクエストが安全に同期済み", "배지 및 퀘스트 안전 동기화됨")
                                            },
                                            fontSize = 11.sp,
                                            color = if (syncState.isSyncing) GrabGreen else Ink600
                                        )
                                    }
                                }

                                if (syncState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = GrabGreen
                                    )
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                if (currentUid != null) {
                                                    userStatsViewModel?.fetchFromFirestore(currentUid)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = GrabGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = l(currentLanguage, "Tải", "Fetch", "获取", "取得", "가져오기"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GrabGreen
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (currentUid != null) {
                                                    userStatsViewModel?.syncToFirestore(currentUid)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudSync,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = l(currentLanguage, "Đồng bộ", "Sync", "同步", "同期", "동기화"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            if (syncState.isSyncing) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .shimmerEffect(
                                            shape = RoundedCornerShape(2.dp),
                                            shimmerColors = listOf(
                                                GrabGreen.copy(alpha = 0.2f),
                                                GrabGreen,
                                                GrabGreen.copy(alpha = 0.2f)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }



                // Settings Section Title: Language Selection
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = GrabGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Ngôn ngữ ứng dụng (Language)",
                                "App Language",
                                "应用语言 (Language)",
                                "アプリの言語 (Language)",
                                "앱 언어 (Language)"
                            ),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900
                        )
                    }
                }

                // Language Selector Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            languages.forEachIndexed { index, lang ->
                                val isSelected = currentLanguage == lang.code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) GrabGreen.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            if (!isSelected) {
                                                pendingLanguage = lang
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.flag,
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = lang.nativeName,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) GrabGreen else Ink900
                                            )
                                            Text(
                                                text = lang.englishName,
                                                fontSize = 11.sp,
                                                color = Ink600
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            color = GrabGreen,
                                            shape = CircleShape,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (index < languages.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = Color.LightGray.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }

                // General Preferences Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = GrabGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Cài đặt & Tùy chọn",
                                "Preferences & System",
                                "系统设置",
                                "設定とシステム",
                                "설정 및 시스템"
                            ),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Offline Maps Cache
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = Ink600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Lưu bản đồ Offline hẻm",
                                                "Offline Alley Map Cache",
                                                "离线胡同地图缓存",
                                                "オフライン路地マップキャッシュ",
                                                "오프라인 골목 지도 캐시"
                                            ),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink900
                                        )
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Tự động tải trước dữ liệu khi mất mạng",
                                                "Pre-cache alley routes for low connection",
                                                "弱网自动预加载离线路线",
                                                "オフライン時もルートを自動キャッシュ",
                                                "오프라인 상태를 위한 데이터 자동 pre-cache"
                                            ),
                                            fontSize = 11.sp,
                                            color = Ink600
                                        )
                                    }
                                }
                                Switch(
                                    checked = offlineModeEnabled,
                                    onCheckedChange = { offlineModeEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GrabGreen
                                    )
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color.LightGray.copy(alpha = 0.2f)
                            )

                            // Notifications
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Ink600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Thông báo Quest & Chuỗi",
                                                "Quest & Streak Alerts",
                                                "任务与连续探索提醒",
                                                "クエスト・ストリーク通知",
                                                "퀘스트 및 연속 탐험 알림"
                                            ),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink900
                                        )
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Nhắc nhở duy trì chuỗi khám phá",
                                                "Daily reminders to keep your streak",
                                                "每日提醒保持连续探索纪录",
                                                "連続記録維持のための毎日のリマインダー",
                                                "연속 탐험 기록을 유지하기 위한 알림"
                                            ),
                                            fontSize = 11.sp,
                                            color = Ink600
                                        )
                                    }
                                }
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GrabGreen
                                    )
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color.LightGray.copy(alpha = 0.2f)
                            )

                            // Seed / Re-initialize Mock Quests & Bách Khoa to Firestore
                            var isSeedingToFirestore by remember { mutableStateOf(false) }
                            var seedStatusMessage by remember { mutableStateOf<String?>(null) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSeedingToFirestore) {
                                        isSeedingToFirestore = true
                                        seedStatusMessage = null
                                        userStatsViewModel?.seedMockQuestsAndGlossary { success ->
                                            isSeedingToFirestore = false
                                            seedStatusMessage = if (success) {
                                                l(currentLanguage, "Đã khởi tạo 'mock_quests' & 'cultural_glossary' lên Firestore!", "Initialized 'mock_quests' & 'cultural_glossary' on Firestore!", "已成功在Firestore初始化mock_quests与cultural_glossary！", "Firestoreにmock_questsとcultural_glossaryを初期化しました！", "Firestore에 mock_quests 및 cultural_glossary 초기화 완료!")
                                            } else {
                                                l(currentLanguage, "Lỗi khởi tạo. Vui lòng kiểm tra mạng/Firestore.", "Failed to seed. Please check connection.", "初始化失败，请检查网络。", "初期化に失敗しました。", "초기화 실패. 네트워크를 확인하세요.")
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = GrabGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Khởi tạo Mock Quests lên Firestore",
                                                "Seed Mock Quests to Firestore",
                                                "向Firestore写入Mock Quests",
                                                "FirestoreにMock Questsを初期化",
                                                "Firestore에 Mock Quests 시딩"
                                            ),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink900
                                        )
                                        Text(
                                            text = seedStatusMessage ?: l(
                                                currentLanguage,
                                                "Tạo collection 'mock_quests' & 'cultural_glossary' trên Cloud",
                                                "Create 'mock_quests' & 'cultural_glossary' collections on Cloud",
                                                "在云端创建mock_quests和cultural_glossary集合",
                                                "クラウド上にmock_questsとcultural_glossaryを作成",
                                                "클라우드에 mock_quests 및 cultural_glossary 생성"
                                            ),
                                            fontSize = 11.sp,
                                            color = if (seedStatusMessage != null) GrabGreen else Ink600
                                        )
                                    }
                                }
                                if (isSeedingToFirestore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = GrabGreen
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = Ink600.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color.LightGray.copy(alpha = 0.2f)
                            )

                            // Clear / Reset All User Data
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showResetDataConfirmation = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Đặt lại toàn bộ dữ liệu (Default)",
                                                "Reset All Data (Default State)",
                                                "重置所有数据（恢复默认）",
                                                "すべてのデータをリセット（初期状態）",
                                                "모든 데이터 초기화 (기본값)"
                                            ),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE53935)
                                        )
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "Xóa dữ liệu khám phá và trả về mặc định để test",
                                                "Clear local progress & reset stats for testing",
                                                "清除本地探索进度并重置为初始测试状态",
                                                "探索の進行状況をクリアして初期テスト状態に戻す",
                                                "로컬 진행 상황을 초기화하여 테스트 상태로 복원"
                                            ),
                                            fontSize = 11.sp,
                                            color = Ink600
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935).copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // App Footer Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HẻmQuest Saigon v1.0.4",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink600
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Môi trường AI Studio • Du lịch di sản xanh",
                                "AI Studio Platform • Eco Cultural Tourism",
                                "AI Studio Platform • 绿色文化旅游",
                                "AI Studio Platform • エコ文化観光",
                                "AI Studio Platform • 에코 문화 관광"
                            ),
                            fontSize = 11.sp,
                            color = Ink600
                        )
                    }
                }
            }
        }

    // Quest Level Roadmap & Perks Modal
    if (showLevelRoadmapDialog) {
        QuestLevelRoadmapDialog(
            totalXp = userStats.totalXp,
            currentLanguage = currentLanguage,
            onDismiss = { showLevelRoadmapDialog = false }
        )
    }

    // Confirmation Modal for Logout
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = l(
                        currentLanguage,
                        "Xác nhận đăng xuất",
                        "Confirm Sign Out",
                        "确认退出登录",
                        "ログアウトの確認",
                        "로그아웃 확인"
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink900
                )
            },
            text = {
                Text(
                    text = l(
                        currentLanguage,
                        "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?\n\nTiến trình khám phá và điểm xanh đã được lưu trên thiết bị an toàn.",
                        "Are you sure you want to sign out of your account?\n\nYour quest progress and Green Points are safely saved.",
                        "您确定要退出当前账号吗？\n\n您的任务进度和绿分已安全保存在设备中。",
                        "アカウントからログアウトしてもよろしいですか？\n\nクエストの進行状況とグリーンポイントは安全に保存されています。",
                        "계정에서 로그아웃하시겠습니까?\n\n퀘스트 진행 상황과 그린 포인트는 안전하게 저장되어 있습니다."
                    ),
                    fontSize = 14.sp,
                    color = Ink600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        authViewModel?.signOut()
                        onSignOut?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Đăng Xuất",
                            "Sign Out",
                            "退出",
                            "ログアウト",
                            "로그아웃"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutConfirmation = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Hủy",
                            "Cancel",
                            "取消",
                            "キャンセル",
                            "취소"
                        ),
                        color = Ink900
                    )
                }
            },
            containerColor = PaperWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Confirmation Modal for Resetting All Data
    if (showResetDataConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetDataConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = l(
                        currentLanguage,
                        "Đặt lại toàn bộ dữ liệu?",
                        "Reset All User Data?",
                        "重置所有用户数据？",
                        "すべてのデータをリセットしますか？",
                        "모든 사용자 데이터를 초기화하시겠습니까?"
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink900
                )
            },
            text = {
                Text(
                    text = l(
                        currentLanguage,
                        "Tất cả số bước, điểm XP, danh hiệu, huy hiệu và dữ liệu vị trí sẽ được đưa về giá trị mặc định ban đầu (0) để sẵn sàng kiểm thử trên Android Studio.",
                        "All steps, XP, titles, unlocked badges, and location tracking data will be restored to clean default values (0) for fresh testing in Android Studio.",
                        "所有步数、XP积分、称号、徽章及位置数据将恢复为默认初始状态 (0)，以便在 Android Studio 中进行测试。",
                        "すべての歩数、XP、バッジ、位置情報は初期状態 (0) にリセットされ、Android Studioでのテスト準備が整います。",
                        "모든 걸음 수, XP, 배지 및 위치 데이터가 초기 상태(0)로 재설정되어 Android Studio 테스트 준비가 완료됩니다."
                    ),
                    fontSize = 14.sp,
                    color = Ink600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDataConfirmation = false
                        userStatsViewModel?.resetAllUserData {
                            authViewModel?.signOut()
                            android.widget.Toast.makeText(
                                context,
                                l(
                                    currentLanguage,
                                    "✨ Đã khôi phục cài đặt gốc và đăng xuất thành công!",
                                    "✨ Factory reset complete! All account data removed & logged out.",
                                    "✨ 恢复出厂设置成功！已清空所有数据并退出登录。",
                                    "✨ 初期化完了！すべてのデータを削除してログアウトしました。",
                                    "✨ 초기화 완료! 모든 데이터 삭제 및 로그아웃 되었습니다."
                                ),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        onResetAllUserData?.invoke()
                        onSignOut?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Xóa & Đặt Lại",
                            "Clear & Reset",
                            "清空并重置",
                            "リセット実行",
                            "초기화 실행"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDataConfirmation = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Hủy",
                            "Cancel",
                            "取消",
                            "キャンセル",
                            "취소"
                        ),
                        color = Ink900
                    )
                }
            },
            containerColor = PaperWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Auth Dialog Popup
    if (authViewModel != null && authUiState.showAuthDialog) {
        AuthDialog(
            authViewModel = authViewModel,
            authUiState = authUiState,
            currentLanguage = currentLanguage,
            onDismiss = { authViewModel.closeAuthDialog() }
        )
    }

    // Confirmation Modal for Language Change
    pendingLanguage?.let { targetLang ->
        val dialogTitle = l(
            currentLanguage,
            "Xác nhận đổi ngôn ngữ",
            "Confirm Language Change",
            "确认更改语言",
            "言語変更の確認",
            "언어 변경 확인"
        )
        val dialogMsg = l(
            currentLanguage,
            "Bạn có chắc chắn muốn đổi ngôn ngữ ứng dụng sang ${targetLang.nativeName} (${targetLang.flag})?\n\nGiao diện và dữ liệu Quest sẽ được cập nhật.",
            "Do you want to change app language to ${targetLang.nativeName} (${targetLang.flag})?\n\nThe interface and Quest content will be updated.",
            "确定要将应用语言更改为 ${targetLang.nativeName} (${targetLang.flag}) 吗？\n\n应用界面及任务内容将会更新。",
            "アプリの言語を ${targetLang.nativeName} (${targetLang.flag}) に変更しますか？\n\nUIとクエストコンテンツが更新されます。",
            "앱 언어를 ${targetLang.nativeName} (${targetLang.flag}) (으)로 변경하시겠습니까?\n\n인터페이스 및 퀘스트 내용이 업데이트됩니다."
        )

        AlertDialog(
            onDismissRequest = { pendingLanguage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = GrabGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink900
                )
            },
            text = {
                Text(
                    text = dialogMsg,
                    fontSize = 14.sp,
                    color = Ink600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val langCode = targetLang.code
                        pendingLanguage = null
                        isApplyingLanguage = true
                        onSetLanguage(langCode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Xác nhận",
                            "Confirm",
                            "确认",
                            "確認",
                            "확인"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingLanguage = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = l(
                            currentLanguage,
                            "Hủy",
                            "Cancel",
                            "取消",
                            "キャンセル",
                            "취소"
                        ),
                        color = Ink900
                    )
                }
            },
            containerColor = PaperWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Loading Overlay Dialog when switching language
    if (isApplyingLanguage) {
        LaunchedEffect(Unit) {
            delay(1200)
            isApplyingLanguage = false
        }

        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = GrabGreen,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = l(
                            currentLanguage,
                            "Đang áp dụng ngôn ngữ mới...",
                            "Applying new language...",
                            "正在应用新语言...",
                            "新しい言語を適用中...",
                            "새 언어 적용 중..."
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = l(
                            currentLanguage,
                            "Đang làm mới Quest & giao diện",
                            "Refreshing Quests & UI interface",
                            "正在刷新任务与界面内容",
                            "クエストとUI画面を更新中",
                            "퀘스트 및 UI 인터페이스 새로고침 중"
                        ),
                        fontSize = 12.sp,
                        color = Ink600
                    )
                }
            }
        }
    }
}
