package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatDatabase
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.StickerItem
import com.example.data.model.Story
import com.example.data.model.UserStoryGroup
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ConversationFilter(val label: String) {
    ALL("Toutes"),
    UNREAD("Non lues"),
    PINNED("Épinglées"),
    GROUPS("Groupes")
}

data class CallInfo(
    val participantName: String,
    val isVideo: Boolean,
    val durationSeconds: Int = 0,
    val isConnected: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ConversationFilter.ALL)
    val selectedFilter: StateFlow<ConversationFilter> = _selectedFilter.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private var recordingJob: Job? = null

    private val _activeCall = MutableStateFlow<CallInfo?>(null)
    val activeCall: StateFlow<CallInfo?> = _activeCall.asStateFlow()

    private var callTimerJob: Job? = null

    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog.asStateFlow()

    private val _availableContacts = MutableStateFlow<List<Contact>>(emptyList())
    val availableContacts: StateFlow<List<Contact>> = _availableContacts.asStateFlow()

    // Floating chat bubble states
    private val _isFloatingBubbleEnabled = MutableStateFlow(true)
    val isFloatingBubbleEnabled: StateFlow<Boolean> = _isFloatingBubbleEnabled.asStateFlow()

    private val _isFloatingBubbleExpanded = MutableStateFlow(false)
    val isFloatingBubbleExpanded: StateFlow<Boolean> = _isFloatingBubbleExpanded.asStateFlow()

    private val _bubbleConversationId = MutableStateFlow<String?>("conv_sarah")
    val bubbleConversationId: StateFlow<String?> = _bubbleConversationId.asStateFlow()

    private val _bubbleInputText = MutableStateFlow("")
    val bubbleInputText: StateFlow<String> = _bubbleInputText.asStateFlow()

    // Instagram-like Stories State
    private val _userStories = MutableStateFlow<List<UserStoryGroup>>(
        listOf(
            UserStoryGroup(
                userId = "me",
                userName = "Ma story",
                userAvatarColor = 0xFF2563EB,
                isMe = true,
                hasUnviewed = false,
                stories = listOf(
                    Story(
                        id = "story_me_1",
                        userId = "me",
                        userName = "Moi",
                        userAvatarColor = 0xFF2563EB,
                        bgGradients = listOf(0xFF833AB4, 0xFFFD1D1D, 0xFFFCB045),
                        caption = "Prêt pour le week-end ! 🚀✨",
                        stickerText = "Humeur du jour",
                        stickerEmoji = "😎"
                    )
                )
            ),
            UserStoryGroup(
                userId = "sarah",
                userName = "Sarah Martin",
                userAvatarColor = 0xFF2563EB,
                hasUnviewed = true,
                stories = listOf(
                    Story(
                        id = "story_sarah_1",
                        userId = "sarah",
                        userName = "Sarah Martin",
                        userAvatarColor = 0xFF2563EB,
                        bgGradients = listOf(0xFFF97316, 0xFFEC4899),
                        caption = "Tournage à Paris aujourd'hui 🎬☕",
                        stickerText = "Paris, France",
                        stickerEmoji = "📍"
                    ),
                    Story(
                        id = "story_sarah_2",
                        userId = "sarah",
                        userName = "Sarah Martin",
                        userAvatarColor = 0xFF2563EB,
                        bgGradients = listOf(0xFF8B5CF6, 0xFF3B82F6),
                        caption = "Magnifique coucher de soleil sur les toits 🌅",
                        stickerText = "Chill vibes 🎵",
                        stickerEmoji = "🎧"
                    )
                )
            ),
            UserStoryGroup(
                userId = "thomas",
                userName = "Thomas Dubois",
                userAvatarColor = 0xFF0D9488,
                hasUnviewed = true,
                stories = listOf(
                    Story(
                        id = "story_thomas_1",
                        userId = "thomas",
                        userName = "Thomas Dubois",
                        userAvatarColor = 0xFF0D9488,
                        bgGradients = listOf(0xFF0F172A, 0xFF0EA5E9),
                        caption = "Nouveau setup triple écran enfin prêt 💻🔥",
                        stickerText = "Focus mode",
                        stickerEmoji = "⚡"
                    )
                )
            ),
            UserStoryGroup(
                userId = "julie",
                userName = "Julie Lefebvre",
                userAvatarColor = 0xFFDB2777,
                hasUnviewed = true,
                stories = listOf(
                    Story(
                        id = "story_julie_1",
                        userId = "julie",
                        userName = "Julie Lefebvre",
                        userAvatarColor = 0xFFDB2777,
                        bgGradients = listOf(0xFFEC4899, 0xFFF43F5E),
                        caption = "Brunch du dimanche en terrasse 🥐☕✨",
                        stickerText = "Gourmandise",
                        stickerEmoji = "🍰"
                    ),
                    Story(
                        id = "story_julie_2",
                        userId = "julie",
                        userName = "Julie Lefebvre",
                        userAvatarColor = 0xFFDB2777,
                        bgGradients = listOf(0xFF10B981, 0xFF06B6D4),
                        caption = "Qui est chaud pour un ciné ce soir ? 🍿",
                        stickerText = "Sondage: Oui / Non",
                        stickerEmoji = "🙋‍♀️"
                    )
                )
            ),
            UserStoryGroup(
                userId = "alexandre",
                userName = "Alexandre V.",
                userAvatarColor = 0xFF7C3AED,
                hasUnviewed = true,
                stories = listOf(
                    Story(
                        id = "story_alex_1",
                        userId = "alexandre",
                        userName = "Alexandre V.",
                        userAvatarColor = 0xFF7C3AED,
                        bgGradients = listOf(0xFF4F46E5, 0xFF9333EA),
                        caption = "Release 2.0 déployée avec succès en prod 🚀🎉",
                        stickerText = "Victoire !",
                        stickerEmoji = "🍾"
                    )
                )
            )
        )
    )
    val userStories: StateFlow<List<UserStoryGroup>> = _userStories.asStateFlow()

    private val _activeStoryGroup = MutableStateFlow<UserStoryGroup?>(null)
    val activeStoryGroup: StateFlow<UserStoryGroup?> = _activeStoryGroup.asStateFlow()

    private val _showCreateStoryDialog = MutableStateFlow(false)
    val showCreateStoryDialog: StateFlow<Boolean> = _showCreateStoryDialog.asStateFlow()

    init {
        val database = ChatDatabase.getDatabase(application)
        repository = ChatRepository(database.conversationDao(), database.messageDao())
        _availableContacts.value = repository.getAvailableContacts()

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val filteredConversations: StateFlow<List<Conversation>> = combine(
        repository.conversations,
        _searchQuery,
        _selectedFilter
    ) { conversations, query, filter ->
        conversations.filter { conv ->
            val matchesQuery = query.isBlank() ||
                conv.participantName.contains(query, ignoreCase = true) ||
                conv.lastMessageText.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                ConversationFilter.ALL -> true
                ConversationFilter.UNREAD -> conv.unreadCount > 0
                ConversationFilter.PINNED -> conv.isPinned
                ConversationFilter.GROUPS -> conv.isGroup
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeConversation: StateFlow<Conversation?> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getConversation(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activeMessages: StateFlow<List<Message>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessages(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bubbleConversation: StateFlow<Conversation?> = _bubbleConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getConversation(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val bubbleMessages: StateFlow<List<Message>> = _bubbleConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessages(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFloatingBubble() {
        _isFloatingBubbleEnabled.value = !_isFloatingBubbleEnabled.value
        if (_isFloatingBubbleEnabled.value && _bubbleConversationId.value == null) {
            _bubbleConversationId.value = filteredConversations.value.firstOrNull()?.id ?: "conv_sarah"
        }
    }

    fun openFloatingBubbleFor(conversationId: String) {
        _bubbleConversationId.value = conversationId
        _isFloatingBubbleEnabled.value = true
        _isFloatingBubbleExpanded.value = true
    }

    fun closeFloatingBubble() {
        _isFloatingBubbleEnabled.value = false
        _isFloatingBubbleExpanded.value = false
    }

    fun toggleFloatingBubbleExpanded() {
        _isFloatingBubbleExpanded.value = !_isFloatingBubbleExpanded.value
    }

    fun setFloatingBubbleExpanded(expanded: Boolean) {
        _isFloatingBubbleExpanded.value = expanded
    }

    fun switchBubbleConversation(convId: String) {
        _bubbleConversationId.value = convId
    }

    fun onBubbleInputTextChanged(text: String) {
        _bubbleInputText.value = text
    }

    fun sendBubbleMessage() {
        val text = _bubbleInputText.value.trim()
        val convId = _bubbleConversationId.value ?: return
        if (text.isBlank()) return

        _bubbleInputText.value = ""
        viewModelScope.launch {
            repository.sendMessage(convId, text, MessageType.TEXT)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: ConversationFilter) {
        _selectedFilter.value = filter
    }

    fun selectConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
        _inputText.value = ""
        if (conversationId != null) {
            viewModelScope.launch {
                repository.markAsRead(conversationId)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendTextMessage() {
        val text = _inputText.value.trim()
        val convId = _activeConversationId.value ?: return
        if (text.isBlank()) return

        _inputText.value = ""
        viewModelScope.launch {
            repository.sendMessage(convId, text, MessageType.TEXT)
        }
    }

    fun startAudioRecording() {
        _isRecordingAudio.value = true
        _recordingDurationSec.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_isRecordingAudio.value) {
                delay(1000)
                _recordingDurationSec.value += 1
            }
        }
    }

    fun stopAndSendAudioRecording() {
        val duration = _recordingDurationSec.value
        _isRecordingAudio.value = false
        recordingJob?.cancel()
        val convId = _activeConversationId.value ?: return

        val finalDuration = if (duration < 1) 2 else duration
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "Message vocal ($finalDuration s)",
                messageType = MessageType.AUDIO,
                audioDurationSec = finalDuration
            )
        }
    }

    fun cancelAudioRecording() {
        _isRecordingAudio.value = false
        recordingJob?.cancel()
        _recordingDurationSec.value = 0
    }

    fun sendImageMessage(mediaUri: String? = null) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "Photo partagée 📷",
                messageType = MessageType.IMAGE,
                mediaUri = mediaUri
            )
        }
    }

    fun toggleReaction(messageId: Long, newReaction: String) {
        viewModelScope.launch {
            val current = activeMessages.value.firstOrNull { it.id == messageId }?.reaction
            repository.toggleReaction(messageId, current, newReaction)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun clearActiveConversation() {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            repository.clearMessages(convId)
        }
    }

    fun togglePin(conversation: Conversation) {
        viewModelScope.launch {
            repository.togglePin(conversation.id, conversation.isPinned)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            if (_activeConversationId.value == conversationId) {
                _activeConversationId.value = null
            }
            repository.deleteConversation(conversationId)
        }
    }

    fun openNewChatDialog() {
        _showNewChatDialog.value = true
    }

    fun closeNewChatDialog() {
        _showNewChatDialog.value = false
    }

    fun startChatWithContact(contact: Contact) {
        viewModelScope.launch {
            _showNewChatDialog.value = false
            // Check if conversation already exists
            val existing = filteredConversations.value.firstOrNull { it.participantPhone == contact.phone }
            if (existing != null) {
                selectConversation(existing.id)
            } else {
                val newId = repository.createNewConversation(
                    name = contact.name,
                    phone = contact.phone,
                    initialMessage = null
                )
                selectConversation(newId)
            }
        }
    }

    fun createCustomConversation(name: String, phone: String, message: String) {
        viewModelScope.launch {
            _showNewChatDialog.value = false
            val newId = repository.createNewConversation(
                name = name.ifBlank { "Contact" },
                phone = phone.ifBlank { "+33 6 00 00 00 00" },
                initialMessage = message.ifBlank { null }
            )
            selectConversation(newId)
        }
    }

    fun startCall(participantName: String, isVideo: Boolean) {
        _activeCall.value = CallInfo(participantName = participantName, isVideo = isVideo)
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            delay(1500)
            _activeCall.value = _activeCall.value?.copy(isConnected = true)
            while (_activeCall.value != null) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        _activeCall.value = null
    }

    // Story Management Methods
    fun openStoryViewer(group: UserStoryGroup) {
        _activeStoryGroup.value = group
        // Mark stories as viewed in group
        val updatedList = _userStories.value.map { g ->
            if (g.userId == group.userId) {
                g.copy(
                    hasUnviewed = false,
                    stories = g.stories.map { it.copy(isViewed = true) }
                )
            } else {
                g
            }
        }
        _userStories.value = updatedList
    }

    fun closeStoryViewer() {
        _activeStoryGroup.value = null
    }

    fun openCreateStoryDialog() {
        _showCreateStoryDialog.value = true
    }

    fun closeCreateStoryDialog() {
        _showCreateStoryDialog.value = false
    }

    fun publishStory(
        caption: String,
        bgGradients: List<Long>,
        stickerText: String? = null,
        stickerEmoji: String? = null
    ) {
        val newStory = Story(
            id = "story_me_${System.currentTimeMillis()}",
            userId = "me",
            userName = "Moi",
            userAvatarColor = 0xFF2563EB,
            bgGradients = bgGradients,
            caption = caption,
            stickerText = stickerText,
            stickerEmoji = stickerEmoji,
            timestamp = System.currentTimeMillis(),
            isViewed = false
        )

        val currentList = _userStories.value.toMutableList()
        val meIndex = currentList.indexOfFirst { it.isMe }
        if (meIndex >= 0) {
            val meGroup = currentList[meIndex]
            currentList[meIndex] = meGroup.copy(
                stories = listOf(newStory) + meGroup.stories,
                hasUnviewed = false
            )
        } else {
            currentList.add(
                0,
                UserStoryGroup(
                    userId = "me",
                    userName = "Ma story",
                    userAvatarColor = 0xFF2563EB,
                    isMe = true,
                    stories = listOf(newStory),
                    hasUnviewed = false
                )
            )
        }
        _userStories.value = currentList
        _showCreateStoryDialog.value = false
    }

    fun replyToStory(story: Story, replyText: String) {
        viewModelScope.launch {
            // Find corresponding conversation or create one
            val allConvs = repository.conversations.first()
            val targetConv = allConvs.firstOrNull { conv ->
                conv.participantName.contains(story.userName, ignoreCase = true) ||
                    story.userName.contains(conv.participantName, ignoreCase = true)
            }

            val convId = targetConv?.id ?: repository.createNewConversation(
                name = story.userName,
                phone = "+33 6 12 34 56 78",
                initialMessage = null
            )

            val fullMessage = "Story de ${story.userName} : \"${story.caption.take(25)}...\"\n➜ $replyText"
            repository.sendMessage(
                conversationId = convId,
                text = fullMessage,
                messageType = MessageType.TEXT
            )
            _activeStoryGroup.value = null
        }
    }

    // Stickers Sending Methods
    fun sendBubbleSticker(sticker: StickerItem) {
        val convId = _bubbleConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "${sticker.emoji} ${sticker.title}",
                messageType = MessageType.STICKER
            )
        }
    }

    fun sendActiveChatSticker(sticker: StickerItem) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "${sticker.emoji} ${sticker.title}",
                messageType = MessageType.STICKER
            )
        }
    }
}
