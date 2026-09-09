package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Story
import com.example.data.model.UserStoryGroup
import kotlinx.coroutines.delay

@Composable
fun StoryViewerDialog(
    storyGroup: UserStoryGroup,
    onClose: () -> Unit,
    onReply: (Story, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (storyGroup.stories.isEmpty()) {
        onClose()
        return
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        StoryViewerContent(
            storyGroup = storyGroup,
            onClose = onClose,
            onReply = onReply,
            modifier = modifier
        )
    }
}

@Composable
fun StoryViewerContent(
    storyGroup: UserStoryGroup,
    onClose: () -> Unit,
    onReply: (Story, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showReactionCelebration by remember { mutableStateOf<String?>(null) }

    val currentStory = storyGroup.stories.getOrNull(currentIndex) ?: storyGroup.stories.first()
    val totalStories = storyGroup.stories.size

    // Story progress auto-advance timer
    LaunchedEffect(currentIndex, isPaused) {
        if (!isPaused) {
            val stepMs = 50L
            val totalDurationMs = 5000L
            val increment = stepMs.toFloat() / totalDurationMs.toFloat()

            while (progress < 1f) {
                delay(stepMs)
                if (!isPaused) {
                    progress += increment
                }
            }

            // Move to next story or finish
            if (currentIndex < totalStories - 1) {
                currentIndex++
                progress = 0f
            } else {
                onClose()
            }
        }
    }

    // Reaction burst animation dismisser
    LaunchedEffect(showReactionCelebration) {
        if (showReactionCelebration != null) {
            delay(1200)
            showReactionCelebration = null
        }
    }

    val gradientColors = remember(currentStory.bgGradients) {
        if (currentStory.bgGradients.size >= 2) {
            currentStory.bgGradients.map { Color(it) }
        } else {
            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("story_viewer_screen")
    ) {
        // Tap gesture areas: Left 30% for prev, Right 70% for next, Long press to pause
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentIndex, totalStories) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.35f) {
                                // Previous story
                                if (currentIndex > 0) {
                                    currentIndex--
                                    progress = 0f
                                }
                            } else {
                                // Next story
                                if (currentIndex < totalStories - 1) {
                                    currentIndex++
                                    progress = 0f
                                } else {
                                    onClose()
                                }
                            }
                        }
                    )
                }
        )

        // Main Column layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Segmented Progress Bars & Header
            Column {
                // Segmented Progress Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0 until totalStories) {
                        val segmentProgress = when {
                            i < currentIndex -> 1f
                            i == currentIndex -> progress.coerceIn(0f, 1f)
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Author Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(storyGroup.userAvatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = storyGroup.userName.firstOrNull()?.toString() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = storyGroup.userName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Story • il y a 2h",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f))
                            .testTag("close_story_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Center Content: Story Caption & Stickers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive Sticker if any (Instagram style)
                if (!currentStory.stickerText.isNullOrBlank() || !currentStory.stickerEmoji.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 6.dp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!currentStory.stickerEmoji.isNullOrBlank()) {
                                Text(text = currentStory.stickerEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (!currentStory.stickerText.isNullOrBlank()) {
                                Text(
                                    text = currentStory.stickerText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }

                // Story Main Caption
                Text(
                    text = currentStory.caption,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Bottom Section: Reply Input & Quick Reactions (WhatsApp / Instagram style)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Quick emoji reactions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojis = listOf("❤️", "😂", "🔥", "👏", "😮", "😢")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .clickable {
                                    showReactionCelebration = emoji
                                    onReply(currentStory, emoji)
                                }
                                .padding(4.dp)
                                .testTag("story_react_$emoji")
                        )
                    }
                }

                // Text Reply Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                "Envoyer un message...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("story_reply_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onReply(currentStory, replyText)
                                replyText = ""
                                showReactionCelebration = "💌"
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (replyText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.3f))
                            .testTag("story_reply_send")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Envoyer réponse",
                            tint = if (replyText.isNotBlank()) Color(0xFF2563EB) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Floating reaction burst
        AnimatedVisibility(
            visible = showReactionCelebration != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = showReactionCelebration ?: "",
                        fontSize = 52.sp
                    )
                }
            }
        }
    }
}
