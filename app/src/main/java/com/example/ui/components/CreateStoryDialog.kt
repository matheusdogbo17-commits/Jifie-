package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class StoryGradientPreset(
    val name: String,
    val colors: List<Long>
)

data class StoryStickerPreset(
    val emoji: String,
    val text: String
)

@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onPublish: (caption: String, bgGradients: List<Long>, stickerText: String?, stickerEmoji: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientPresets = remember {
        listOf(
            StoryGradientPreset("Sunset Instagram", listOf(0xFF833AB4, 0xFFFD1D1D, 0xFFFCB045)),
            StoryGradientPreset("Cyber Neon", listOf(0xFFEC4899, 0xFF8B5CF6, 0xFF3B82F6)),
            StoryGradientPreset("Ocean Breeze", listOf(0xFF0EA5E9, 0xFF10B981)),
            StoryGradientPreset("Tropical Sunrise", listOf(0xFFF59E0B, 0xFFEF4444)),
            StoryGradientPreset("Emerald Mint", listOf(0xFF059669, 0xFF10B981)),
            StoryGradientPreset("Midnight Dark", listOf(0xFF1E1B4B, 0xFF312E81))
        )
    }

    val stickerPresets = remember {
        listOf(
            StoryStickerPreset("😎", "Humeur du jour"),
            StoryStickerPreset("🗼", "Paris, France"),
            StoryStickerPreset("🎵", "Musique: Chill vibes"),
            StoryStickerPreset("🙋‍♂️", "Sondage: Qui est dispo ?"),
            StoryStickerPreset("☕", "Pause café méritée"),
            StoryStickerPreset("🚀", "Nouveau projet !")
        )
    }

    var caption by remember { mutableStateOf("") }
    var selectedGradientIndex by remember { mutableIntStateOf(0) }
    var selectedStickerIndex by remember { mutableStateOf<Int?>(0) }

    val currentGradient = gradientPresets[selectedGradientIndex]
    val selectedSticker = selectedStickerIndex?.let { stickerPresets[it] }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nouvelle Story",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview Card (Instagram story aspect ratio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                currentGradient.colors.map { Color(it) }
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (selectedSticker != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = selectedSticker.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedSticker.text,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (caption.isNotBlank()) caption else "Écrivez votre message ici...",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caption Input
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Votre texte ou légende") },
                    placeholder = { Text("Que voulez-vous partager ?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("story_caption_input"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gradients selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Arrière-plan dégradé",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gradientPresets.forEachIndexed { index, preset ->
                        val isSelected = selectedGradientIndex == index
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(preset.colors.map { Color(it) }))
                                .clickable { selectedGradientIndex = index }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .testTag("story_gradient_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sticker Selector
                Text(
                    text = "Ajouter un sticker interactif",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stickerPresets.forEachIndexed { index, sticker ->
                        val isSelected = selectedStickerIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedStickerIndex = if (isSelected) null else index
                            },
                            leadingIcon = {
                                Text(text = sticker.emoji, fontSize = 14.sp)
                            },
                            label = {
                                Text(text = sticker.text, fontSize = 12.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Publish Button
                Button(
                    onClick = {
                        val finalCaption = if (caption.isNotBlank()) caption else "Nouvelle story ✨"
                        onPublish(
                            finalCaption,
                            currentGradient.colors,
                            selectedSticker?.text,
                            selectedSticker?.emoji
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("publish_story_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE1306C) // Instagram Pink / Magenta
                    )
                ) {
                    Text(
                        text = "📸 Partager dans ma story",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
