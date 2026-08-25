package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.GeminiQuestRepository
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.util.l
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "gemini"
    val text: String,
    var isLiked: Boolean = false
)

@Composable
fun AiGuideScreen(
    currentLanguage: String = "vi",
    isVi: Boolean = currentLanguage == "vi",
    repository: GeminiQuestRepository = remember { GeminiQuestRepository() },
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val langName = when (currentLanguage) {
        "vi" -> "Vietnamese (Tiếng Việt)"
        "zh" -> "Chinese (中文)"
        "ja" -> "Japanese (日本語)"
        "ko" -> "Korean (한국어)"
        else -> "English"
    }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "gemini",
                text = when (currentLanguage) {
                    "vi" -> "Chào bạn! Tôi là Hẻm Navigator AI 🤖✨\nTôi có thể giải đáp mọi thắc mắc về lịch sử con hẻm, quán cà phê vợt, hay nét văn hóa đặc trưng của Sài Gòn!"
                    "zh" -> "您好！我是西贡胡同 AI 导游 🤖✨\n欢迎随时向我询问有关西贡古巷历史、网滤咖啡文化或在地特色景点！"
                    "ja" -> "こんにちは！サイゴン路地裏 AI ナビゲーターです 🤖✨\nサイゴンの路地裏の歴史、網フィルターコーヒー文化、隠れた名所について何でもお聞きください！"
                    "ko" -> "안녕하세요! 사이공 골목 AI 내비게이터입니다 🤖✨\n사이공 골목의 역사, 그물 필터 커피 문화, 현지 명소에 대해 무엇이든 물어보세요!"
                    else -> "Hello! I am your Hẻm Navigator AI 🤖✨\nAsk me anything about Saigon alley history, net coffee culture, or local hidden spots!"
                }
            )
        )
    }

    val categories = when (currentLanguage) {
        "vi" -> listOf("☕ Cà Phê Vợt", "⛩️ Di Tích Hẻm", "📸 Góc Check-in", "🚲 Mẹo Đi Bộ")
        "zh" -> listOf("☕ 网滤咖啡", "⛩️ 胡同古迹", "📸 热门打卡", "🚲 徒步技巧")
        "ja" -> listOf("☕ 網珈琲", "⛩️ 路地史跡", "📸 映えスポット", "🚲 散策コツ")
        "ko" -> listOf("☕ 그물 커피", "⛩️ 골목 유적", "📸 포토 스팟", "🚲 도보 팁")
        else -> listOf("☕ Net Coffee", "⛩️ Alley Heritage", "📸 Photo Spots", "🚲 Walking Tips")
    }

    val categoryPillsMap = mapOf(
        0 to when (currentLanguage) {
            "vi" -> listOf("☕ Cà Phê Vợt Ba Lù Chợ Lớn?", "☕ Tại sao gọi là cà phê vợt?", "☕ Món ăn kèm cà phê hẻm ngon?")
            "zh" -> listOf("☕ 巴炉网滤咖啡的特色？", "☕ 为什么叫网滤咖啡？", "☕ 胡同咖啡馆招牌点心？")
            "ja" -> listOf("☕ バールー網珈琲の特徴？", "☕ なぜ網フィルターと呼ぶ？", "☕ 路地カフェのおすすめスイーツ？")
            "ko" -> listOf("☕ 바루 그물 커피의 특징?", "☕ 왜 그물 커피라고 하나요?", "☕ 골목 카페 추천 디저트?")
            else -> listOf("☕ Story of Ba Lu Net Coffee?", "☕ Why is it called Net Coffee?", "☕ Best snacks at alley cafes?")
        },
        1 to when (currentLanguage) {
            "vi" -> listOf("⛩️ Lịch sử Chùa Bà Thiên Hậu (P. Chợ Lớn)?", "⛩️ Căn hầm Bí Mật Biệt Động (P. Bàn Cờ)?", "⛩️ Làng Lồng Đèn Phú Bình (P. Hòa Bình)?")
            "zh" -> listOf("⛩️ 堤岸天后宫历史（堤岸坊）？", "⛩️ 西贡特工地下军火库遗址（棋盘坊）？", "⛩️ 富平传统提灯村（和平坊）？")
            "ja" -> listOf("⛩️ チョロン天后宮の歴史（チョロン坊）？", "⛩️ サイゴン別動隊の秘密地下壕（バンコー坊）？", "⛩️ フービン伝統ランタン村（ホアビン坊）？")
            "ko" -> listOf("⛩️ 쩌롱 티엔하우 사원 역사 (쩌롱동)?", "⛩️ 사이공 특공대 비밀 지하 기지 (반꺼동)?", "⛩️ 푸빈 등불 전통 마을 (화빈동)?")
            else -> listOf("⛩️ History of Ba Thien Hau Temple (Cho Lon Ward)?", "⛩️ Secret Commando Bunker (Ban Co Ward)?", "⛩️ Phu Binh Lantern Craft Village (Hoa Binh Ward)?")
        },
        2 to when (currentLanguage) {
            "vi" -> listOf("📸 Hẻm 144 Nguyễn Trãi (P. Cầu Ông Lãnh)?", "📸 Hẻm Biệt Thự Cổ (P. Xuân Hòa)?", "📸 Hẻm Hào Sĩ Phường (P. Chợ Quán)?")
            "zh" -> listOf("📸 阮廌街144号提灯小巷（翁领桥坊）？", "📸 法式古墅深巷（春和坊）？", "📸 豪士坊百年老巷（曹关坊）？")
            "ja" -> listOf("📸 阮廌街144番地ランタン路地（カウオンライン坊）？", "📸 フレンチ洋館路地（スアンホア坊）？", "📸 豪士坊（チョークアン坊）？")
            "ko" -> listOf("📸 응우옌짜이 144번지 등불 골목 (까우옹란동)?", "📸 프렌치 빌라 골목 (쑤언호아동)?", "📸 하오시프엉 유서 깊은 골목 (쩌꽌동)?")
            else -> listOf("📸 Alley 144 Nguyen Trai (Cau Ong Lanh Ward)?", "📸 Vintage Villa Alley (Xuan Hoa Ward)?", "📸 Hao Si Phuong Heritage Alley (Cho Quan Ward)?")
        },
        3 to when (currentLanguage) {
            "vi" -> listOf("🚲 Quy tắc ứng xử trong hẻm nhỏ?", "🚲 Giờ nào chụp ảnh hẻm đẹp nhất?", "🚲 Mẹo đi hẻm không bị lạc?")
            "zh" -> listOf("🚲 穿梭狭窄胡同的礼仪？", "🚲 什么时候拍胡同最美？", "🚲 在胡同里不迷路的技巧？")
            "ja" -> listOf("🚲 狭い路地歩きのマナー？", "🚲 路地撮影のベスト時間帯？", "🚲 迷子にならない散策コツ？")
            "ko" -> listOf("🚲 좁은 골목 탐방 에티켓?", "🚲 골목 사진 촬영 최적의 시간?", "🚲 길을 잃지 않는 탐방 팁?")
            else -> listOf("🚲 Alleyway courtesy guidelines?", "🚲 Best lighting time for alley photos?", "🚲 Navigation tips for dense alleys?")
        }
    )

    val currentSuggestionPills = categoryPillsMap[selectedCategoryIndex] ?: categoryPillsMap[0]!!

    fun sendMessage(queryText: String) {
        if (queryText.isBlank() || isLoading) return
        val userMsg = queryText.trim()
        messages.add(ChatMessage("user", userMsg))
        inputText = ""
        isLoading = true

        scope.launch {
            try {
                val prompt = "You are HẻmQuest AI, a friendly, deeply knowledgeable Saigon local cultural guide. Target Language requested: $langName. Reply strictly in $langName. Answer this user question concisely, warmly and engagingly: $userMsg"
                val responseText = repository.askGeminiAssistant(prompt)
                messages.add(ChatMessage("gemini", responseText))
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        "gemini",
                        when (currentLanguage) {
                            "vi" -> "Xin lỗi, hiện tại tôi chưa phản hồi được. Bạn thử lại nhé!"
                            "zh" -> "抱歉，暂时无法回应，请重试！"
                            "ja" -> "申し訳ありません。回答を処理できませんでした。"
                            "ko" -> "죄송합니다, 잠시 후 다시 시도해주세요."
                            else -> "Sorry, I couldn't process that. Please try again!"
                        }
                    )
                )
            } finally {
                isLoading = false
                scope.launch {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F4))
            .padding(top = 16.dp, bottom = 90.dp)
            .testTag("ai_guide_screen")
    ) {
        // AI Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CyberPurple, GrabGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Gemini Hẻm Navigator AI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink900
                    )
                    Text(
                        text = l(
                            currentLanguage,
                            "Trợ lý văn hóa & hẻm phố Sài Gòn",
                            "Saigon Alley Cultural AI Companion",
                            "西贡胡同文化 AI 随身导游",
                            "サイゴン路地裏文化 AI コンパニオン",
                            "사이공 골목 문화 AI 도우미"
                        ),
                        fontSize = 12.sp,
                        color = Ink600
                    )
                }
            }
        }

        // Category Tabs Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                val isSelected = index == selectedCategoryIndex
                Surface(
                    color = if (isSelected) GrabGreen else PaperWhite,
                    shape = CircleShape,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.clickable { selectedCategoryIndex = index }
                ) {
                    Text(
                        text = categories[index],
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Ink900,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Suggestion Pills Row for Active Category
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentSuggestionPills) { pill ->
                Surface(
                    color = PaperWhite,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrabGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { sendMessage(pill) }
                ) {
                    Text(
                        text = pill,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink900,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages.size) { index ->
                val msg = messages[index]
                val isUser = msg.sender == "user"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) GrabGreen else PaperWhite
                        ),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = if (isUser) Color.White else Ink900,
                                lineHeight = 20.sp
                            )

                            if (!isUser) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (msg.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        tint = if (msg.isLiked) ForestGreen else Ink600,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                messages[index] = msg.copy(isLiked = !msg.isLiked)
                                            }
                                    )
                                }
                            }
                        }
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GrabGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CyberPurple,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = l(
                                currentLanguage,
                                "Gemini đang suy nghĩ...",
                                "Gemini is thinking...",
                                "Gemini 正在思考...",
                                "Gemini が考え中...",
                                "Gemini가 생각 중입니다..."
                            ),
                            fontSize = 12.sp,
                            color = Ink600
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = l(
                                currentLanguage,
                                "Hỏi Gemini về hẻm Sài Gòn...",
                                "Ask Gemini about Saigon alleys...",
                                "向 Gemini 咨询西贡胡同...",
                                "サイゴンの路地についてGeminiに質問...",
                                "사이공 골목에 대해 Gemini에게 물어보세요..."
                            ),
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = GrabGreen,
                    shape = CircleShape,
                    modifier = Modifier.clickable { sendMessage(inputText) }
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
