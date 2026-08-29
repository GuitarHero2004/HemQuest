package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CulturalGlossaryItem
import com.example.model.GlossaryCategory
import com.example.repository.CulturalGlossaryRepository
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperSecondary
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.l

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CulturalGlossaryBottomSheet(
    currentLanguage: String = "vi",
    initialSearchQuery: String = "",
    initialTermId: String? = null,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var selectedCategory by remember { mutableStateOf(GlossaryCategory.ALL) }
    var expandedTermId by remember { mutableStateOf<String?>(initialTermId) }
    var bookmarkedTermIds by remember { mutableStateOf(setOf<String>()) }
    var triviaIndex by remember { mutableStateOf(0) }

    val filteredItems = remember(searchQuery, selectedCategory, currentLanguage) {
        CulturalGlossaryRepository.search(searchQuery, selectedCategory, currentLanguage)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF4F7F4),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
                .testTag("cultural_glossary_sheet")
        ) {
            // Drag handle indicator
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(GrabGreen.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Row: Back/Close Button, Title & Subtitle, Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("glossary_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = l(currentLanguage, "Quay lại", "Back", "返回", "戻る", "뒤로"),
                            tint = Ink900,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GrabGreen, ForestGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Glossary Icon",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = l(
                                currentLanguage,
                                "Bách Khoa Hẻm Sài Gòn",
                                "Saigon Cultural Encyclopedia",
                                "西贡深巷文化百科",
                                "サイゴン路地裏文化大辞典",
                                "사이공 골목 문화 대백과"
                            ),
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Ink900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = l(
                                currentLanguage,
                                "Giải mã 15+ thuật ngữ & nếp sống hẻm",
                                "Decode 15+ terms & alleyway heritage",
                                "解密15+地道表达与市井生活",
                                "15+の専門用語・暮らしの知恵を解説",
                                "15+개의 골목 용어 및 삶의 지혜 해설"
                            ),
                            fontSize = 11.sp,
                            color = Ink600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = GrabGreen.copy(alpha = 0.12f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${filteredItems.size} ${l(currentLanguage, "Mục", "Terms", "词条", "項目", "항목")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Cultural Spotlight / Did You Know? Card
            val currentTrivia = CulturalGlossaryRepository.triviaSpotlights.getOrNull(triviaIndex % CulturalGlossaryRepository.triviaSpotlights.size)
            if (currentTrivia != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { triviaIndex++ }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrivia.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = currentTrivia.second,
                                fontSize = 11.5.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 16.sp
                            )
                        }
                        Text(
                            text = "➔",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = l(
                            currentLanguage,
                            "Tìm từ ngữ (Hẻm cụt, Hủ tiếu gõ, Cà phê vợt)...",
                            "Search terms (Cul-de-sac, Noodle cart, Net coffee)...",
                            "搜索词条（死胡同、笃笃面、网滤咖啡等）...",
                            "用語を検索（袋小路、フーティウ屋台、網珈琲など）...",
                            "용어 검색 (막다른 골목, 국수 수레, 그물 커피 등)..."
                        ),
                        fontSize = 12.sp,
                        color = Ink600
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GrabGreen
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Ink600
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PaperWhite,
                    unfocusedContainerColor = PaperWhite,
                    focusedBorderColor = GrabGreen,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedTextColor = Ink900,
                    unfocusedTextColor = Ink900
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(GlossaryCategory.values(), key = { it.name }) { category ->
                    val isSelected = selectedCategory == category
                    val title = when (currentLanguage) {
                        "vi" -> category.titleVi
                        "en" -> category.titleEn
                        "zh" -> category.titleZh
                        "ja" -> category.titleJa
                        "ko" -> category.titleKo
                        else -> category.titleVi
                    }
                    val count = if (category == GlossaryCategory.ALL) {
                        CulturalGlossaryRepository.items.size
                    } else {
                        CulturalGlossaryRepository.items.count { it.category == category }
                    }

                    Surface(
                        onClick = { selectedCategory = category },
                        color = if (isSelected) GrabGreen else PaperWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = if (isSelected) 3.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$title ($count)",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink900
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Glossary Term Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PaperWhite),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔎", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Không tìm thấy thuật ngữ phù hợp!",
                                        "No terms found!",
                                        "未找到匹配的文汇词条！",
                                        "該当する用語が見つかりません！",
                                        "일치하는 용어를 찾을 수 없습니다!"
                                    ),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink900
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = l(
                                        currentLanguage,
                                        "Thử tìm bằng từ khóa khác hoặc chuyển danh mục.",
                                        "Try searching with another keyword or category.",
                                        "请尝试使用其他关键字或切换分类。",
                                        "別のキーワードやカテゴリーでお試しください。",
                                        "다른 검색어나 카테고리로 검색해 보세요."
                                    ),
                                    fontSize = 12.sp,
                                    color = Ink600
                                )
                            }
                        }
                    }
                } else {
                    items(filteredItems, key = { it.id }) { item ->
                        val isExpanded = expandedTermId == item.id
                        val isBookmarked = bookmarkedTermIds.contains(item.id)
                        GlossaryTermCard(
                            item = item,
                            isExpanded = isExpanded,
                            isBookmarked = isBookmarked,
                            currentLanguage = currentLanguage,
                            onToggleExpand = {
                                expandedTermId = if (isExpanded) null else item.id
                            },
                            onToggleBookmark = {
                                bookmarkedTermIds = if (isBookmarked) {
                                    bookmarkedTermIds - item.id
                                } else {
                                    bookmarkedTermIds + item.id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlossaryTermCard(
    item: CulturalGlossaryItem,
    isExpanded: Boolean,
    isBookmarked: Boolean,
    currentLanguage: String,
    onToggleExpand: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedNotice by remember { mutableStateOf(false) }

    val shortDef = when (currentLanguage) {
        "vi" -> item.shortDefinitionVi
        "en" -> item.shortDefinitionEn
        "zh" -> item.shortDefinitionZh
        "ja" -> item.shortDefinitionJa
        "ko" -> item.shortDefinitionKo
        else -> item.shortDefinitionEn
    }

    val fullDesc = when (currentLanguage) {
        "vi" -> item.fullDescriptionVi
        "en" -> item.fullDescriptionEn
        "zh" -> item.fullDescriptionZh
        "ja" -> item.fullDescriptionJa
        "ko" -> item.fullDescriptionKo
        else -> item.fullDescriptionEn
    }

    val whyItMatters = when (currentLanguage) {
        "vi" -> item.whyItMattersVi
        "en" -> item.whyItMattersEn
        "zh" -> item.whyItMattersZh
        "ja" -> item.whyItMattersJa
        "ko" -> item.whyItMattersKo
        else -> item.whyItMattersEn
    }

    val exampleLocations = if (currentLanguage == "vi") item.exampleLocationsVi else item.exampleLocationsEn

    val isBachKhoaItem = item.id.contains("bk", ignoreCase = true) || item.term.contains("Bách Khoa", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 6.dp else if (isBachKhoaItem) 4.dp else 2.dp),
        border = BorderStroke(
            width = if (isBachKhoaItem) 1.5.dp else 1.dp,
            color = if (isBachKhoaItem) item.accentColor.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 2.dp,
                vertical = if (isBachKhoaItem) 6.dp else 2.dp
            )
            .clickable { onToggleExpand() }
            .animateContentSize()
            .testTag("glossary_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Term + Phonetic + Category + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(item.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.icon, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.term,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink900
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Phonetic",
                                tint = ForestGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.phonetic,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) ClayOrange else Ink600,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        color = item.accentColor.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = item.category.tag,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.accentColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Ink600,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Tone Guide (if present)
            if (item.toneGuide.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Ink600,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.toneGuide,
                            fontSize = 10.5.sp,
                            color = Ink600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Short Definition Preview
            Text(
                text = shortDef,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Ink900,
                lineHeight = 18.5.sp
            )

            // Expanded Detailed Educational Section
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Full Cultural Origin Description
                Card(
                    colors = CardDefaults.cardColors(containerColor = PaperSecondary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "NGUỒN GỐC & CÂU CHUYỆN VĂN HÓA",
                                    "CULTURAL ORIGIN & STORY",
                                    "文化起源与风情故事",
                                    "文化の由来とストーリー",
                                    "문화적 유래 및 이야기"
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = fullDesc,
                            fontSize = 12.5.sp,
                            color = Ink900,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Trivia box if available
                val triviaText = if (currentLanguage == "vi") item.triviaVi else item.triviaEn
                if (triviaText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = triviaText,
                                fontSize = 11.5.sp,
                                color = Color(0xFF1E40AF),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Why It Matters / Practical Value Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = ClayOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = l(
                                    currentLanguage,
                                    "VÌ SAO ĐIỀU NÀY QUAN TRỌNG?",
                                    "WHY THIS MATTERS FOR EXPLORERS",
                                    "为何探险家应当了解？",
                                    "なぜこれを知ると探索が楽しくなる？",
                                    "탐험가에게 왜 중요한가요?"
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayOrange,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = whyItMatters,
                                fontSize = 12.sp,
                                color = Ink900,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                if (exampleLocations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(
                                    currentLanguage,
                                    "ĐIỂM TRẢI NGHIỆM TRONG HẺMQUEST:",
                                    "SAMPLE LOCATIONS IN HEMQUEST:",
                                    "HẻmQuest 中的对应体验点：",
                                    "HẻmQuest 内の体験スポット:",
                                    "HẻmQuest 내 관련 장소:"
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(exampleLocations) { loc ->
                                Surface(
                                    color = GrabGreen.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = loc,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForestGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom card toolbar: Copy & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showCopiedNotice) {
                        Text(
                            text = l(currentLanguage, "✓ Đã sao chép!", "✓ Copied to clipboard!", "✓ 已复制！", "✓ コピーしました！", "✓ 복사됨!"),
                            color = ForestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        onClick = {
                            val textToCopy = "${item.term} (${item.phonetic}): $shortDef\n\n$fullDesc"
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            showCopiedNotice = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Ink600,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = l(currentLanguage, "Sao chép thẻ", "Copy Note", "复制卡片", "カードをコピー", "카드 복사"),
                                fontSize = 11.sp,
                                color = Ink600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
