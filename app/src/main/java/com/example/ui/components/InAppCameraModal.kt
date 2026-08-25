package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.QuestStop
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.SunGold
import com.example.util.l
import java.util.concurrent.Executors

/**
 * 2-Phase Camera Modal:
 * Phase 1: Live Hardware Camera Viewfinder (CameraX), Shutter, Gallery, Sample.
 * Phase 2: Review captured photo and trigger Gemini AI Multimodal verification or direct confirm.
 */
@Composable
fun InAppCameraModal(
    stop: QuestStop,
    currentLanguage: String = "vi",
    isVerifying: Boolean = false,
    onDismiss: () -> Unit,
    onVerifyWithAi: (Bitmap) -> Unit,
    onDirectConfirmCompletion: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGridVisible by remember { mutableStateOf(true) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // System camera fallback launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
        }
    }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val decoded = BitmapFactory.decodeStream(stream, null, options)
                    if (decoded != null) {
                        capturedBitmap = decoded
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraModal", "Error opening gallery image", e)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "camera_fx")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B11))
                .testTag("in_app_camera_modal"),
            color = Color(0xFF070B11)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ==========================================
                // 1. TOP HEADER & HUD CONTROLS
                // ==========================================
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (capturedBitmap != null) {
                                    capturedBitmap = null
                                } else {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFF1E293B).copy(alpha = 0.85f), CircleShape)
                                .testTag("camera_modal_back_button")
                        ) {
                            Icon(
                                imageVector = if (capturedBitmap != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                                contentDescription = l(currentLanguage, "Quay lại", "Back", "返回", "戻る", "뒤로"),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = if (capturedBitmap == null) {
                                    l(currentLanguage, "CHỤP ẢNH CHECK-IN", "PHOTO CHECK-IN", "拍照打卡", "チェックイン撮影", "체크인 사진 촬영")
                                } else {
                                    l(currentLanguage, "KIỂM TRA ẢNH & AI", "PHOTO REVIEW & AI", "AI 智能验证", "AIで確認", "AI 인증")
                                },
                                color = SunGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = stop.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Grid Toggle
                        IconButton(
                            onClick = { isGridVisible = !isGridVisible },
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isGridVisible) GrabGreen.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.85f),
                                    CircleShape
                                )
                                .testTag("camera_grid_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = "Toggle Grid",
                                tint = if (isGridVisible) GrabGreen else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mission / Challenge Target Card
                    Surface(
                        color = Color(0xFF111928).copy(alpha = 0.92f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, SunGold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ClayOrange.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, ClayOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = ClayOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = l(currentLanguage, "NHIỆM VỤ TẠI ĐIỂM NÀY:", "MISSION AT THIS STOP:", "此站任务:", "このスポットのミッション:", "이 스팟의 미션:"),
                                    color = SunGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = stop.challenge.prompt,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // 2. MAIN VIEWFINDER (Live CameraX View / Captured Review)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0A0F1A))
                        .border(
                            2.dp,
                            if (capturedBitmap != null) GrabGreen else Color(0xFF243247),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedBitmap != null) {
                        // PHASE 2: Captured Photo Preview
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Challenge Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top indicator badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, GrabGreen),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GrabGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(currentLanguage, "✓ Ảnh đã sẵn sàng", "✓ Photo Ready", "✓ 照片已就绪", "✓ 写真準備完了", "✓ 사진 준비 완료"),
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // PHASE 1: Viewfinder frame with Live CameraX
                        if (hasCameraPermission) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx).apply {
                                        scaleType = PreviewView.ScaleType.FILL_CENTER
                                    }
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }
                                            val imgCap = ImageCapture.Builder()
                                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                                .build()
                                            imageCapture = imgCap

                                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imgCap
                                            )
                                        } catch (exc: Exception) {
                                            Log.e("CameraModal", "CameraX bind error", exc)
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = SunGold,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Cần quyền máy ảnh",
                                        "Camera Access Required",
                                        "需要相机权限",
                                        "カメラの許可が必要です",
                                        "카메라 권한이 필요합니다"
                                    ),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Cho phép ứng dụng sử dụng máy ảnh để ghi lại khoảnh khắc khám phá di sản hẻm Sài Gòn.",
                                        "Allow camera access to capture moments along your journey.",
                                        "允许相机权限以拍摄探索照片。",
                                        "路地探訪の瞬間を撮影するためにカメラのアクセスを許可してください。",
                                        "골목 탐방 순간을 촬영하기 위해 카메라 권한을 허용해주세요."
                                    ),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = l(currentLanguage, "Cấp quyền máy ảnh", "Grant Permission", "授予权限", "許可する", "권한 허용"),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 3x3 Grid Overlay
                        if (isGridVisible) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.14f)))
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.14f)))
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.14f)))
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.14f)))
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        // Viewfinder Gold Frame Corners
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                                .size(24.dp)
                                .border(BorderStroke(2.5.dp, SunGold), RoundedCornerShape(topStart = 6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp)
                                .size(24.dp)
                                .border(BorderStroke(2.5.dp, SunGold), RoundedCornerShape(topEnd = 6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                                .size(24.dp)
                                .border(BorderStroke(2.5.dp, SunGold), RoundedCornerShape(bottomStart = 6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp)
                                .size(24.dp)
                                .border(BorderStroke(2.5.dp, SunGold), RoundedCornerShape(bottomEnd = 6.dp))
                        )

                        // Focus Reticle in Center
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(110.dp)
                                .scale(pulseScale)
                                .border(2.dp, GrabGreen.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SunGold, CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = GrabGreen.copy(alpha = 0.6f),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Hint message in Viewfinder
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "📸 Canh góc & bấm nút chụp để ghi lại khoảnh khắc",
                                    "📸 Frame shot & tap shutter to capture",
                                    "📸 对准目标，按下快门记录美好瞬间",
                                    "📸 構図を合わせてシャッターを押してください",
                                    "📸 구도를 맞추고 셔터 버튼을 누르세요"
                                ),
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Scanning & Analyzing Animation Overlay
                    if (isVerifying) {
                        val infiniteScan = rememberInfiniteTransition(label = "scan_trans")
                        val scanYOffset by infiniteScan.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scan_laser"
                        )

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val beamHeight = maxHeight
                            // Moving Laser Beam
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .align(Alignment.TopCenter)
                                    .offset(y = beamHeight * scanYOffset)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                GrabGreen.copy(alpha = 0f),
                                                GrabGreen.copy(alpha = 0.9f),
                                                SunGold,
                                                GrabGreen.copy(alpha = 0.9f),
                                                GrabGreen.copy(alpha = 0f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                                    border = BorderStroke(2.dp, GrabGreen),
                                    shadowElevation = 10.dp,
                                    modifier = Modifier.size(76.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            color = GrabGreen,
                                            strokeWidth = 3.5.dp,
                                            modifier = Modifier.size(54.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = SunGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Gemini AI Vision Đang Phân Tích...",
                                        "Gemini AI Vision Analyzing Photo...",
                                        "Gemini AI 视觉正在深度比对...",
                                        "Gemini AI Visionが画像を照合中...",
                                        "Gemini AI Vision이 사진을 분석 중..."
                                    ),
                                    color = Color.White,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Đang đối chiếu kiến trúc hẻm & hoa văn văn hóa",
                                        "Matching alley architecture & cultural motifs",
                                        "正在比对胡同建筑与文化特色要素",
                                        "路地裏の建築様式と文化要素を照合しています",
                                        "골목 건축 양식과 문화 요소를 대조 중입니다"
                                    ),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // 3. BOTTOM CONTROL PANEL
                // ==========================================
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (capturedBitmap == null) {
                            // Phase 1: Camera Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Gallery Picker
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(68.dp)
                                ) {
                                    IconButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(Color(0xFF1E293B), CircleShape)
                                            .border(1.5.dp, Color(0xFF334155), CircleShape)
                                            .testTag("camera_gallery_picker_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = l(currentLanguage, "Thư viện ảnh", "Gallery", "相册", "ギャラリー", "갤러리"),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = l(currentLanguage, "Thư viện", "Gallery", "相册", "ギャラリー", "갤러리"),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Center: Shutter Button
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .padding(4.dp)
                                        .border(3.dp, Color.White, CircleShape)
                                        .clickable {
                                            if (!hasCameraPermission) {
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            } else {
                                                val imgCap = imageCapture
                                                if (imgCap != null) {
                                                    try {
                                                        imgCap.takePicture(
                                                            cameraExecutor,
                                                            object : ImageCapture.OnImageCapturedCallback() {
                                                                override fun onCaptureSuccess(image: ImageProxy) {
                                                                    val bmp = decodeImageProxy(image)
                                                                    image.close()
                                                                    (context as? android.app.Activity)?.runOnUiThread {
                                                                        capturedBitmap = bmp
                                                                    }
                                                                }

                                                                override fun onError(exception: ImageCaptureException) {
                                                                    Log.e("CameraModal", "CameraX capture error", exception)
                                                                    (context as? android.app.Activity)?.runOnUiThread {
                                                                        takePictureLauncher.launch(null)
                                                                    }
                                                                }
                                                            }
                                                        )
                                                    } catch (e: Exception) {
                                                        takePictureLauncher.launch(null)
                                                    }
                                                } else {
                                                    takePictureLauncher.launch(null)
                                                }
                                            }
                                        }
                                        .testTag("camera_shutter_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(GrabGreen, ForestGreen)
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Capture Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                // Right: Quick Sample / AI Snapshot
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(68.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            capturedBitmap = generateSampleAlleyBitmap(stop.name)
                                        },
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(ClayOrange, CircleShape)
                                            .testTag("camera_sample_photo_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = l(currentLanguage, "Ảnh mẫu", "Sample Photo", "示例照片", "サンプル写真", "샘플 사진"),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = l(currentLanguage, "Ảnh mẫu", "Sample", "示例", "サンプル", "샘플"),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Quick Direct Check-in Text Button
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = onDirectConfirmCompletion,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("camera_direct_confirm_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GrabGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Hoặc xác nhận đã đến chặng này (+50 XP)",
                                        "Or direct check-in this stop (+50 XP)",
                                        "或直接确认到达本站 (+50 XP)",
                                        "またはこのスポットの到着を直接確認 (+50 XP)",
                                        "또는 이 지점 도착 직접 확인 (+50 XP)"
                                    ),
                                    color = GrabGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            // Phase 2: Review & AI Verify Controls
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val b = capturedBitmap
                                        if (b != null) {
                                            onVerifyWithAi(b)
                                        }
                                    },
                                    enabled = !isVerifying,
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("verify_photo_with_gemini_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = l(
                                                currentLanguage,
                                                "GỬI ẢNH CHO GEMINI AI XÁC THỰC (+50 XP)",
                                                "VERIFY PHOTO WITH GEMINI AI (+50 XP)",
                                                "提交照片供 GEMINI AI 验证 (+50 XP)",
                                                "GEMINI AIで写真を検証 (+50 XP)",
                                                "GEMINI AI로 사진 검증 요청 (+50 XP)"
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { capturedBitmap = null },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFF64748B)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("camera_retake_button")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = l(currentLanguage, "Chụp lại", "Retake", "重拍", "再撮影", "다시 촬영"),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onDirectConfirmCompletion,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("camera_review_direct_confirm_button")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GrabGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = l(currentLanguage, "Đã đến", "Check-in", "已到达", "到着確認", "도착 확인"),
                                                color = GrabGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
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
    }
}

/**
 * Converts CameraX ImageProxy to Bitmap with correct orientation
 */
private fun decodeImageProxy(image: ImageProxy): Bitmap {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val rotation = image.imageInfo.rotationDegrees
    return if (rotation != 0) {
        val matrix = android.graphics.Matrix().apply {
            postRotate(rotation.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

/**
 * Generates a realistic simulated cultural snapshot bitmap with heritage texture
 */
private fun generateSampleAlleyBitmap(landmarkName: String): Bitmap {
    val width = 640
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background Heritage Gradient / Alley ambience
    val bgPaint = Paint().apply {
        color = AndroidColor.rgb(38, 54, 44)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Decorative wall texture
    val wallPaint = Paint().apply {
        color = AndroidColor.rgb(65, 85, 72)
        strokeWidth = 3f
    }
    for (i in 0..height step 30) {
        canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), wallPaint)
    }

    // Architectural landmark banner
    val textPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 30f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    canvas.drawText("🏮 SÀI GÒN HẺM QUEST 📸", width / 2f, 150f, textPaint)

    textPaint.apply {
        color = AndroidColor.rgb(255, 215, 0)
        textSize = 26f
    }
    canvas.drawText(landmarkName, width / 2f, 230f, textPaint)

    textPaint.apply {
        color = AndroidColor.rgb(220, 230, 225)
        textSize = 19f
        isFakeBoldText = false
    }
    canvas.drawText("Cultural Detail & Heritage Check-in Point", width / 2f, 290f, textPaint)

    // Stamp badge
    val stampPaint = Paint().apply {
        color = AndroidColor.rgb(0, 137, 62)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawCircle(width / 2f, 370f, 38f, stampPaint)

    val stampText = Paint().apply {
        color = AndroidColor.rgb(0, 220, 100)
        textSize = 13f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    canvas.drawText("VERIFIED", width / 2f, 375f, stampText)

    return bitmap
}
