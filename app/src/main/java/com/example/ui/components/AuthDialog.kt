package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.auth.AuthUiState
import com.example.auth.AuthViewModel
import com.example.ui.theme.DuoLime
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

private fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun AuthDialog(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState,
    currentLanguage: String = "vi",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isSignUp = authUiState.isSignUpMode

    // Password strength calculation
    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> 0
            password.length < 6 -> 1
            password.length < 8 || !password.any { it.isDigit() } -> 2
            else -> 3
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("auth_dialog"),
            shape = RoundedCornerShape(32.dp),
            color = PaperWhite,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar with App Icon + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(GrabGreen, DuoLime)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏮", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "HẻmQuest Saigon",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink900
                            )
                            Text(
                                text = l(
                                    currentLanguage,
                                    "Khám phá văn hóa & hẻm phố",
                                    "Cultural & Alley Explorations",
                                    "文化与胡同探索",
                                    "路地裏と文化探索",
                                    "골목 및 문화 탐험"
                                ),
                                fontSize = 11.sp,
                                color = Ink600
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Ink900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle for Current Auth Mode
                Text(
                    text = if (isSignUp) {
                        l(currentLanguage, "Tạo tài khoản mới", "Create New Account", "创建新账号", "新規アカウント作成", "새 계정 만들기")
                    } else {
                        l(currentLanguage, "Đăng nhập HẻmQuest", "Sign In to HẻmQuest", "登录 HẻmQuest", "HẻmQuest にログイン", "HẻmQuest 로그인")
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink900,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                Text(
                    text = if (isSignUp) {
                        l(currentLanguage, "Tham gia hành trình khám phá hẻm phố Sài Gòn", "Join the journey to explore Saigon's alleys", "加入探索西贡胡同的旅程", "サイゴンの路地裏探索に参加しよう", "사이공 골목 탐험에 참여하세요")
                    } else {
                        l(currentLanguage, "Chào mừng bạn quay lại với hành trình", "Welcome back to your adventure", "欢迎回到您的探索之旅", "冒険へのおかえりなさい", "탐험으로 돌아오신 것을 환영합니다")
                    },
                    fontSize = 12.sp,
                    color = Ink600,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Custom high-contrast color styling for auth inputs
                val authTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Ink900,
                    unfocusedTextColor = Ink900,
                    focusedLabelColor = ForestGreen,
                    unfocusedLabelColor = Ink600,
                    focusedPlaceholderColor = Ink600.copy(alpha = 0.55f),
                    unfocusedPlaceholderColor = Ink600.copy(alpha = 0.55f),
                    focusedBorderColor = GrabGreen,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    cursorColor = GrabGreen
                )

                // Google Native Sign-In Action Button
                Surface(
                    onClick = {
                        val activityContext = context.findActivity() ?: context
                        authViewModel.signInWithGoogle(activityContext)
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_signin_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Official Google 4-color Vector Brand Logo
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Tiếp tục bằng tài khoản Google",
                                "Continue with Google Account",
                                "使用 Google 账号继续",
                                "Google アカウントで続ける",
                                "Google 계정으로 계속"
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Divider: OR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    Text(
                        text = l(currentLanguage, "HOẶC DÙNG EMAIL", "OR WITH EMAIL", "或使用邮箱", "またはメールで", "또는 이메일"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink600,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error Message Alert
                AnimatedVisibility(
                    visible = authUiState.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    authUiState.errorMessage?.let { err ->
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚠️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = err,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // If Sign-Up mode: Display Name Field
                AnimatedVisibility(
                    visible = isSignUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = {
                                Text(
                                    text = l(currentLanguage, "Họ và tên / Biệt danh", "Full Name / Nickname", "姓名 / 昵称", "氏名 / ニックネーム", "이름 / 닉네임"),
                                    color = Ink600
                                )
                            },
                            placeholder = { 
                                Text(
                                    text = "VD: Minh Anh, Saigon Walker...",
                                    color = Ink600.copy(alpha = 0.5f)
                                ) 
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = GrabGreen)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = authTextFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_display_name_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Email Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(
                            text = l(currentLanguage, "Địa chỉ Email", "Email Address", "电子邮箱", "メールアドレス", "이메일 주소"),
                            color = Ink600
                        )
                    },
                    placeholder = { 
                        Text(
                            text = "tenban@gmail.com",
                            color = Ink600.copy(alpha = 0.5f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = GrabGreen)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = authTextFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = l(currentLanguage, "Mật khẩu", "Password", "密码", "パスワード", "비밀번호"),
                            color = Ink600
                        )
                    },
                    placeholder = { 
                        Text(
                            text = "••••••••",
                            color = Ink600.copy(alpha = 0.5f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GrabGreen)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = Ink600
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    colors = authTextFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                // Password Strength Indicator in Sign-Up Mode
                if (isSignUp && password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            repeat(3) { step ->
                                val active = step < passwordStrength
                                val stepColor = when (passwordStrength) {
                                    1 -> Color(0xFFEF4444)
                                    2 -> SunGold
                                    else -> ForestGreen
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (active) stepColor else Color(0xFFE2E8F0))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (passwordStrength) {
                                1 -> l(currentLanguage, "Mật khẩu yếu", "Weak", "弱", "弱い", "취약함")
                                2 -> l(currentLanguage, "Trung bình", "Medium", "中等", "普通", "보통")
                                else -> l(currentLanguage, "Mạnh & an toàn", "Strong", "强", "強力", "안전함")
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (passwordStrength) {
                                1 -> Color(0xFFEF4444)
                                2 -> SunGold
                                else -> ForestGreen
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (isSignUp) {
                            authViewModel.signUpWithEmail(email, password, displayName)
                        } else {
                            authViewModel.signInWithEmail(email, password)
                        }
                    },
                    enabled = !authUiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (authUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSignUp) {
                                    l(currentLanguage, "Đăng Ký & Khám Phá", "Sign Up & Explore", "立即注册并探索", "登録して探索を開始", "가입하고 탐험 시작")
                                } else {
                                    l(currentLanguage, "Đăng Nhập Vào HẻmQuest", "Sign In to HẻmQuest", "登录 HẻmQuest", "HẻmQuest にログイン", "HẻmQuest 로그인")
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Switch Sign In / Sign Up Link Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSignUp) {
                            l(currentLanguage, "Đã có tài khoản?", "Already have an account?", "已有账号？", "アカウントをお持ちですか？", "이미 계정이 있으신가요?")
                        } else {
                            l(currentLanguage, "Chưa có tài khoản?", "Don't have an account?", "还没有账号？", "アカウントをお持ちでないですか？", "계정이 없으신가요?")
                        },
                        fontSize = 13.sp,
                        color = Ink600
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSignUp) {
                            l(currentLanguage, "Đăng nhập ngay", "Sign in now", "立即登录", "ログインする", "지금 로그인")
                        } else {
                            l(currentLanguage, "Tạo tài khoản mới", "Create account", "创建新账号", "アカウント作成", "새 계정 만들기")
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreen,
                        modifier = Modifier.clickable {
                            authViewModel.toggleSignUpMode(!isSignUp)
                        }
                    )
                }
            }
        }
    }
}

