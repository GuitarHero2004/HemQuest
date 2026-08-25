package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuestCategory
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite

@Composable
fun CategoryFilterChips(
    selectedCategory: QuestCategory,
    onCategorySelected: (QuestCategory) -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String = "en",
    categories: List<QuestCategory> = QuestCategory.values().toList()
) {
    LazyRow(
        modifier = modifier
            .testTag("category_filter_chips")
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.name }) { category ->
            val isSelected = category == selectedCategory
            
            Surface(
                onClick = {
                    if (!isSelected) {
                        onCategorySelected(category)
                    }
                },
                color = if (isSelected) GrabGreen else PaperWhite,
                shape = RoundedCornerShape(20.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier
                    .testTag("category_chip_${category.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = category.getIcon(),
                        contentDescription = category.getDisplayName(currentLanguage),
                        tint = if (isSelected) Color.White else Ink600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.getDisplayName(currentLanguage),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Ink900
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
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
    }
}
