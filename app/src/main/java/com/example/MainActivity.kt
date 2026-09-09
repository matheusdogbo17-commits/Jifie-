package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CallOverlay
import com.example.ui.components.CreateStoryDialog
import com.example.ui.components.FloatingChatBubbleHost
import com.example.ui.components.StoryViewerDialog
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ConversationsScreen
import com.example.ui.screens.NewChatBottomSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ChatApp()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatApp(chatViewModel: ChatViewModel = viewModel()) {
  val conversations by chatViewModel.filteredConversations.collectAsStateWithLifecycle()
  val activeConversationId by chatViewModel.activeConversationId.collectAsStateWithLifecycle()
  val activeConversation by chatViewModel.activeConversation.collectAsStateWithLifecycle()
  val activeMessages by chatViewModel.activeMessages.collectAsStateWithLifecycle()
  val searchQuery by chatViewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedFilter by chatViewModel.selectedFilter.collectAsStateWithLifecycle()
  val inputText by chatViewModel.inputText.collectAsStateWithLifecycle()
  val isRecordingAudio by chatViewModel.isRecordingAudio.collectAsStateWithLifecycle()
  val recordingDurationSec by chatViewModel.recordingDurationSec.collectAsStateWithLifecycle()
  val activeCall by chatViewModel.activeCall.collectAsStateWithLifecycle()
  val showNewChatDialog by chatViewModel.showNewChatDialog.collectAsStateWithLifecycle()
  val contacts by chatViewModel.availableContacts.collectAsStateWithLifecycle()

  // Instagram-like Stories states
  val userStories by chatViewModel.userStories.collectAsStateWithLifecycle()
  val activeStoryGroup by chatViewModel.activeStoryGroup.collectAsStateWithLifecycle()
  val showCreateStoryDialog by chatViewModel.showCreateStoryDialog.collectAsStateWithLifecycle()

  // Floating chat bubble states
  val isFloatingBubbleEnabled by chatViewModel.isFloatingBubbleEnabled.collectAsStateWithLifecycle()
  val isFloatingBubbleExpanded by chatViewModel.isFloatingBubbleExpanded.collectAsStateWithLifecycle()
  val bubbleConversation by chatViewModel.bubbleConversation.collectAsStateWithLifecycle()
  val bubbleMessages by chatViewModel.bubbleMessages.collectAsStateWithLifecycle()
  val bubbleInputText by chatViewModel.bubbleInputText.collectAsStateWithLifecycle()

  BackHandler(enabled = activeConversationId != null) {
    chatViewModel.selectConversation(null)
  }

  Crossfade(
    targetState = activeConversationId != null,
    label = "screen_transition"
  ) { isChatOpen ->
    if (isChatOpen) {
      ChatDetailScreen(
        conversation = activeConversation,
        messages = activeMessages,
        inputText = inputText,
        isRecordingAudio = isRecordingAudio,
        recordingDurationSec = recordingDurationSec,
        onInputTextChanged = chatViewModel::onInputTextChanged,
        onSendMessage = chatViewModel::sendTextMessage,
        onStartRecordingAudio = chatViewModel::startAudioRecording,
        onStopRecordingAudio = chatViewModel::stopAndSendAudioRecording,
        onCancelRecordingAudio = chatViewModel::cancelAudioRecording,
        onSendImage = { chatViewModel.sendImageMessage() },
        onReactionSelected = chatViewModel::toggleReaction,
        onDeleteMessage = chatViewModel::deleteMessage,
        onClearChat = chatViewModel::clearActiveConversation,
        onStartAudioCall = {
          activeConversation?.let {
            chatViewModel.startCall(it.participantName, isVideo = false)
          }
        },
        onStartVideoCall = {
          activeConversation?.let {
            chatViewModel.startCall(it.participantName, isVideo = true)
          }
        },
        onMinimizeToBubble = {
          activeConversation?.let {
            chatViewModel.openFloatingBubbleFor(it.id)
            chatViewModel.setFloatingBubbleExpanded(false)
          }
          chatViewModel.selectConversation(null)
        },
        onBackClick = { chatViewModel.selectConversation(null) }
      )
    } else {
      ConversationsScreen(
        conversations = conversations,
        contacts = contacts,
        storyGroups = userStories,
        searchQuery = searchQuery,
        selectedFilter = selectedFilter,
        onSearchQueryChanged = chatViewModel::onSearchQueryChanged,
        onFilterSelected = chatViewModel::onFilterSelected,
        onConversationClick = chatViewModel::selectConversation,
        onStartNewChat = chatViewModel::openNewChatDialog,
        onContactQuickClick = chatViewModel::startChatWithContact,
        onStoryClick = chatViewModel::openStoryViewer,
        onCreateStoryClick = chatViewModel::openCreateStoryDialog,
        onTogglePin = chatViewModel::togglePin,
        onDeleteConversation = chatViewModel::deleteConversation,
        onToggleFloatingBubble = chatViewModel::toggleFloatingBubble,
        onOpenBubbleFor = chatViewModel::openFloatingBubbleFor
      )
    }
  }

  // Floating discussion bubble host (accessible from anywhere in the app)
  FloatingChatBubbleHost(
    isVisible = isFloatingBubbleEnabled && (activeConversationId == null || activeConversationId != (bubbleConversation?.id ?: "")),
    isExpanded = isFloatingBubbleExpanded,
    conversation = bubbleConversation ?: conversations.firstOrNull(),
    allConversations = conversations,
    messages = bubbleMessages,
    inputText = bubbleInputText,
    onInputTextChanged = chatViewModel::onBubbleInputTextChanged,
    onSendMessage = chatViewModel::sendBubbleMessage,
    onSendSticker = chatViewModel::sendBubbleSticker,
    onToggleExpanded = chatViewModel::toggleFloatingBubbleExpanded,
    onCloseBubble = chatViewModel::closeFloatingBubble,
    onOpenFullScreen = { convId ->
      chatViewModel.setFloatingBubbleExpanded(false)
      chatViewModel.selectConversation(convId)
    },
    onSwitchConversation = chatViewModel::switchBubbleConversation
  )

  // Instagram-style Story Viewer Modal
  activeStoryGroup?.let { storyGroup ->
    StoryViewerDialog(
      storyGroup = storyGroup,
      onClose = chatViewModel::closeStoryViewer,
      onReply = chatViewModel::replyToStory
    )
  }

  // Instagram-style Story Creator Dialog
  if (showCreateStoryDialog) {
    CreateStoryDialog(
      onDismiss = chatViewModel::closeCreateStoryDialog,
      onPublish = chatViewModel::publishStory
    )
  }

  if (showNewChatDialog) {
    NewChatBottomSheet(
      contacts = contacts,
      onSelectContact = chatViewModel::startChatWithContact,
      onCreateCustomChat = chatViewModel::createCustomConversation,
      onDismiss = chatViewModel::closeNewChatDialog
    )
  }

  CallOverlay(
    callInfo = activeCall,
    onEndCall = chatViewModel::endCall
  )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
