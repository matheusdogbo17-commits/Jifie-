package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StickerCategory
import com.example.data.model.StickerItem
import com.example.data.model.StickerRepository

@Composable
fun StickerPickerSheet(
    onStickerSelected: (StickerItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var stickerSearchQuery by remember { mutableStateOf("") }
    val categories = remember { StickerCategory.values().toList() }

    val filteredStickers = remember(selectedCategoryIndex, stickerSearchQuery) {
        if (stickerSearchQuery.isNotBlank()) {
            StickerRepository.allStickers.filter {
                it.title.contains(stickerSearchQuery, ignoreCase = true) ||
                    (it.subtitle?.contains(stickerSearchQuery, ignoreCase = true) == true) ||
                    it.emoji.contains(stickerSearchQuery)
            }
        } else {
            val cat = categories[selectedCategoryIndex]
            if (cat == StickerCategory.FAVORITES) {
                StickerRepository.allStickers.take(8)
            } else {
                StickerRepository.allStickers.filter { it.category == cat }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // Header with title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎨", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stickers WhatsApp",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer les stickers",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick search bar for stickers
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = stickerSearchQuery,
                        onValueChange = { stickerSearchQuery = it },
                        placeholder = {
                            Text(
                                "Rechercher un sticker...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 40.dp)
                    )
                }
            }

            // Category Tab Bar
            if (stickerSearchQuery.isBlank()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    edgePadding = 12.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = selectedCategoryIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedCategoryIndex = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = cat.iconEmoji, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Grid of stickers
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                items(filteredStickers, key = { it.id }) { sticker ->
                    StickerGridCard(
                        sticker = sticker,
                        onClick = { onStickerSelected(sticker) }
                    )
                }
            }
        }
    }
}

@Composable
fun StickerGridCard(
    sticker: StickerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp,
        modifier = modifier
            .testTag("sticker_${sticker.id}")
            .clip(RoundedCornerShape(14.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = sticker.emoji,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sticker.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
