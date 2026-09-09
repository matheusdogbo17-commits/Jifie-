package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserStoryGroup

// Iconic Instagram Story Gradient
val InstagramStoryBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF833AB4), // Purple
        Color(0xFFFD1D1D), // Crimson Red
        Color(0xFFFCB045)  // Warm Orange/Yellow
    )
)

val ViewedStoryBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF9CA3AF),
        Color(0xFFD1D5DB)
    )
)

@Composable
fun StoriesRow(
    storyGroups: List<UserStoryGroup>,
    onStoryClick: (UserStoryGroup) -> Unit,
    onCreateStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myStoryGroup = remember(storyGroups) { storyGroups.firstOrNull { it.isMe } }
    val otherStoryGroups = remember(storyGroups) { storyGroups.filter { !it.isMe } }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag("stories_row")
    ) {
        // "Ma story" / Add Story Item
        item(key = "my_story") {
            MyStoryItem(
                myStoryGroup = myStoryGroup,
                onViewMyStory = {
                    if (myStoryGroup != null && myStoryGroup.stories.isNotEmpty()) {
                        onStoryClick(myStoryGroup)
                    } else {
                        onCreateStoryClick()
                    }
                },
                onAddStory = onCreateStoryClick
            )
        }

        // Friends' Stories
        items(otherStoryGroups, key = { it.userId }) { group ->
            StoryAvatarItem(
                group = group,
                onClick = { onStoryClick(group) }
            )
        }
    }
}

@Composable
fun MyStoryItem(
    myStoryGroup: UserStoryGroup?,
    onViewMyStory: () -> Unit,
    onAddStory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasStories = myStoryGroup != null && myStoryGroup.stories.isNotEmpty()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(68.dp)
            .clickable(onClick = onViewMyStory)
            .testTag("my_story_item")
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // Story Ring if user has stories
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .then(
                        if (hasStories) {
                            Modifier.border(2.5.dp, InstagramStoryBrush, CircleShape)
                        } else {
                            Modifier.border(1.5.dp, Color(0xFFE5E7EB), CircleShape)
                        }
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Moi",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Small "+" Badge on bottom-right to add a story
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clickable(onClick = onAddStory)
                    .testTag("add_story_badge"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une story",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Votre story",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StoryAvatarItem(
    group: UserStoryGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ringBrush = if (group.hasUnviewed) InstagramStoryBrush else ViewedStoryBrush
    val initial = group.userName.firstOrNull()?.toString() ?: "?"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(68.dp)
            .clickable(onClick = onClick)
            .testTag("story_item_${group.userId}")
    ) {
        // Outer ring with Instagram gradient or gray for viewed
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.5.dp, ringBrush, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(group.userAvatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = group.userName.split(" ").firstOrNull() ?: group.userName,
            fontSize = 11.sp,
            fontWeight = if (group.hasUnviewed) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
