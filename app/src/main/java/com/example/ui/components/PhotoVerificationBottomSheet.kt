package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhotoVerificationResult
import com.example.model.VerificationStatus
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold

import com.example.util.l

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoVerificationBottomSheet(
    result: PhotoVerificationResult?,
    capturedBitmap: Bitmap?,
    isVi: Boolean = false,
    currentLanguage: String = if (isVi) "vi" else "en",
    onConfirmComplete: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (result == null) return

    val lang = currentLanguage

    val statusBg = when (result.status) {
        VerificationStatus.LIKELY_MATCH -> ForestGreen
        VerificationStatus.UNCERTAIN -> SunGold
        VerificationStatus.NOT_ENOUGH_INFORMATION -> ClayOrange
        VerificationStatus.REJECTED -> Color(0xFFE53935)
    }

    val statusText = when (result.status) {
        VerificationStatus.LIKELY_MATCH -> l(lang, "XÁC NHẬN PHÙ HỢP VĂN HÓA", "MATCHES CULTURAL DETAIL", "符合文化特色细节", "文化・歴史要素に合致", "문화적 특징과 일치")
        VerificationStatus.UNCERTAIN -> l(lang, "GÓC CHỤP THÚ VỊ / CHƯA RÕ", "UNCERTAIN / INTERESTING ANGLE", "视角独特 / 待确认", "ユニークな構図 / 要確認", "흥미로운 촬영 각도 / 확인 필요")
        VerificationStatus.NOT_ENOUGH_INFORMATION -> l(lang, "ẢNH CẦN THÊM CHI TIẾT", "PHOTO NEEDS MORE DETAIL", "照片需要更多细节", "写真の詳細が不足しています", "사진 상세 정보 필요")
        VerificationStatus.REJECTED -> l(lang, "ẢNH CHƯA KHỚP THỬ THÁCH", "CHALLENGE NOT MATCHED", "照片未符合挑战要求", "課題と一致しません", "챌린지와 일치하지 않음")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .testTag("photo_verification_sheet")
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(ForestGreen.copy(alpha = 0.15f))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = l(lang, "Phân Tích AI Gemini Multimodal", "Gemini AI Vision Analysis", "Gemini AI 视觉智能分析", "Gemini AI ビジョン解析", "Gemini AI 비전 분석"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink900
                    )
                }

                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color(0xFFF1F5F2),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = l(lang, "Đóng", "Close", "关闭", "閉じる", "닫기"),
                            tint = Ink900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Captured Image Preview
            if (capturedBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperSecondary)
                        .border(1.dp, Color(0xFFE4DDD0), RoundedCornerShape(18.dp))
                ) {
                    Image(
                        bitmap = capturedBitmap.asImageBitmap(),
                        contentDescription = "Captured Challenge Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Overlay AI Status Badge
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (result.status) {
                                    VerificationStatus.LIKELY_MATCH -> Icons.Default.CheckCircle
                                    VerificationStatus.REJECTED -> Icons.Default.Close
                                    else -> Icons.Default.HelpOutline
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Observation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = l(lang, "NHẬN XÉT CỦA GEMINI AI:", "GEMINI OBSERVATION:", "GEMINI AI 观察点评:", "GEMINI AIの考察:", "GEMINI AI의 관찰 소견:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ForestGreen,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.observation,
                        fontSize = 14.sp,
                        color = Ink900,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = l(lang, "GÓC NHÌN DI SẢN:", "HERITAGE INSIGHT:", "文化遗产视角:", "文化遺産インサイト:", "문화 유산 인사이트:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ClayOrange,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.detailNotes,
                        fontSize = 13.sp,
                        color = Ink600,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (result.status) {
                VerificationStatus.LIKELY_MATCH -> {
                    // Match Success: Primary button confirms and collects the card
                    Button(
                        onClick = onConfirmComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_stop_completion_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = l(lang, "XÁC NHẬN HOÀN THÀNH & THU THẬP THẺ", "COLLECT CARD & COMPLETE STOP", "确认完成并收集卡片", "カードを獲得してポイント完了", "카드 수집 및 완료 Confirmation"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = l(lang, "Chụp lại ảnh khác", "Take Another Photo", "重新拍照", "写真を撮り直す", "다른 사진 찍기"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink600
                        )
                    }
                }
                VerificationStatus.REJECTED -> {
                    // Mismatch/Rejected: Must retake photo of the correct landmark/object
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("retake_photo_after_reject_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = l(lang, "CHỤP LẠI ĐÚNG ĐỊA ĐIỂM / ĐỐI TƯỢNG", "RETAKE PHOTO WITH CORRECT SUBJECT", "重新拍摄正确的目标", "対象を正しく撮り直す", "올바른 대상 다시 촬영"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                else -> {
                    // Uncertain / Need more info
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("retake_photo_uncertain_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = l(lang, "CHỤP LẠI RÕ NÉT HƠN", "RETAKE CLEARER PHOTO", "拍摄更清晰的照片", "より鮮明に撮り直す", "더 선명하게 다시 찍기"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
