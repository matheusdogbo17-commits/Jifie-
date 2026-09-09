package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SenderType
import com.example.ui.theme.ChatBlueDark
import com.example.ui.theme.ChatBlueLight
import com.example.ui.theme.MessageCheckmarkBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    onReactionSelected: (String) -> Unit,
    onDeleteMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMe = message.senderType == SenderType.ME
    var showContextMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 4.dp,
            bottomEnd = 18.dp
        )
    }

    val bubbleBrush = if (isMe) {
        Brush.linearGradient(listOf(ChatBlueLight, ChatBlueDark))
    } else {
        null
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) {
        timeFormat.format(Date(message.timestamp))
    }

    val reactionsList = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Quick reaction bar if activated
        AnimatedVisibility(
            visible = showReactionPicker,
            enter = fadeIn() + scaleIn()
        ) {
            Surface(
                shape = CircleShape,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .testTag("reaction_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reactionsList.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        onReactionSelected(emoji)
                                        showReactionPicker = false
                                    }
                                )
                                .padding(4.dp)
                                .testTag("reaction_$emoji")
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Box(contentAlignment = if (isMe) Alignment.BottomEnd else Alignment.BottomStart) {
            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clip(bubbleShape)
                    .then(
                        if (bubbleBrush != null) {
                            Modifier.background(bubbleBrush)
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        }
                    )
                    .combinedClickable(
                        onClick = {
                            showReactionPicker = !showReactionPicker
                        },
                        onLongClick = {
                            showContextMenu = true
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag(if (isMe) "my_message_bubble" else "other_message_bubble")
            ) {
                Column {
                    when (message.messageType) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                        MessageType.AUDIO -> {
                            AudioMessageItem(
                                durationSec = message.audioDurationSec,
                                isMe = isMe
                            )
                        }
                        MessageType.IMAGE -> {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Photo",
                                            tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = message.text,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        MessageType.STICKER -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val parts = message.text.split(" ", limit = 2)
                                val emoji = parts.firstOrNull() ?: "⭐"
                                val label = if (parts.size > 1) parts[1] else ""
                                Text(
                                    text = emoji,
                                    fontSize = 52.sp
                                )
                                if (label.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Time & delivery status
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when (message.status) {
                                MessageStatus.SENDING -> {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.6f))
                                    )
                                }
                                MessageStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Envoyé",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                MessageStatus.DELIVERED -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Remis",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                MessageStatus.READ -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Lu",
                                        tint = MessageCheckmarkBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reaction pill badge attached to corner
            if (message.reaction != null) {
                Surface(
                    shape = CircleShape,
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .offset(
                            x = if (isMe) (-8).dp else 8.dp,
                            y = 10.dp
                        )
                        .testTag("reaction_badge")
                ) {
                    Text(
                        text = message.reaction,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Context menu
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Réagir") },
                    onClick = {
                        showContextMenu = false
                        showReactionPicker = true
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    text = {
                        Text(
                            "Supprimer",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        onDeleteMessage()
                    }
                )
            }
        }
    }
}
