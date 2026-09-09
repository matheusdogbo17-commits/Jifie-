package com.example.data.repository

import com.example.data.local.ConversationDao
import com.example.data.local.MessageDao
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SenderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val conversations: Flow<List<Conversation>> = conversationDao.getAllConversations()

    fun getConversation(id: String): Flow<Conversation?> = conversationDao.getConversationById(id)

    fun getMessages(conversationId: String): Flow<List<Message>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun checkAndSeedInitialData() {
        val count = conversationDao.getConversationsCount()
        if (count == 0) {
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val now = System.currentTimeMillis()
        val minute = 60 * 1000L
        val hour = 60 * minute
        val day = 24 * hour

        val seedConversations = listOf(
            Conversation(
                id = "conv_sarah",
                participantName = "Sarah Martin",
                participantPhone = "+33 6 12 34 56 78",
                participantAvatarColor = 0xFFEC4899, // Pink
                participantStatus = "En ligne",
                isOnline = true,
                isPinned = true,
                isGroup = false,
                lastMessageText = "Tu as pu regarder la présentation pour la réunion de demain ?",
                lastMessageTimestamp = now - 5 * minute,
                unreadCount = 2
            ),
            Conversation(
                id = "conv_thomas",
                participantName = "Thomas Dubois",
                participantPhone = "+33 6 98 76 54 32",
                participantAvatarColor = 0xFF3B82F6, // Blue
                participantStatus = "Vu il y a 10 min",
                isOnline = false,
                isPinned = true,
                isGroup = false,
                lastMessageText = "Parfait pour le déjeuner à 12h30, je réserve la table !",
                lastMessageTimestamp = now - 28 * minute,
                unreadCount = 0
            ),
            Conversation(
                id = "conv_projet",
                participantName = "Équipe Projet App",
                participantPhone = "",
                participantAvatarColor = 0xFF8B5CF6, // Purple
                participantStatus = "5 participants",
                isOnline = true,
                isPinned = false,
                isGroup = true,
                groupMembersCount = 5,
                lastMessageText = "Lucas: La nouvelle version est prête pour les tests !",
                lastMessageTimestamp = now - 2 * hour,
                unreadCount = 1
            ),
            Conversation(
                id = "conv_sophie",
                participantName = "Sophie Laurent",
                participantPhone = "+33 6 45 23 89 10",
                participantAvatarColor = 0xFF10B981, // Emerald
                participantStatus = "En ligne",
                isOnline = true,
                isPinned = false,
                isGroup = false,
                lastMessageText = "Merci beaucoup pour ton aide hier soir, ça m'a sauvé la mise ✨",
                lastMessageTimestamp = now - 5 * hour,
                unreadCount = 0
            ),
            Conversation(
                id = "conv_lucas",
                participantName = "Lucas Bernard",
                participantPhone = "+33 7 81 90 23 45",
                participantAvatarColor = 0xFFF59E0B, // Amber
                participantStatus = "En ligne",
                isOnline = true,
                isPinned = false,
                isGroup = false,
                lastMessageText = "Message vocal (0:14)",
                lastMessageTimestamp = now - 1 * day,
                unreadCount = 0
            ),
            Conversation(
                id = "conv_elodie",
                participantName = "Élodie Moreau",
                participantPhone = "+33 6 77 88 99 00",
                participantAvatarColor = 0xFF06B6D4, // Cyan
                participantStatus = "Vu hier",
                isOnline = false,
                isPinned = false,
                isGroup = false,
                lastMessageText = "Super ! On se voit ce week-end ?",
                lastMessageTimestamp = now - 2 * day,
                unreadCount = 0
            )
        )

        conversationDao.insertConversations(seedConversations)

        // Seed messages for Sarah
        val sarahMessages = listOf(
            Message(
                conversationId = "conv_sarah",
                text = "Coucou ! Comment vas-tu ?",
                senderType = SenderType.OTHER,
                timestamp = now - 30 * minute,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_sarah",
                text = "Ça va super bien et toi ? En pleine préparation.",
                senderType = SenderType.ME,
                timestamp = now - 25 * minute,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_sarah",
                text = "J'ai finalisé le premier jet des maquettes ce matin 🎨",
                senderType = SenderType.OTHER,
                timestamp = now - 15 * minute,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_sarah",
                text = "Tu as pu regarder la présentation pour la réunion de demain ?",
                senderType = SenderType.OTHER,
                timestamp = now - 5 * minute,
                status = MessageStatus.DELIVERED
            )
        )

        // Seed messages for Thomas
        val thomasMessages = listOf(
            Message(
                conversationId = "conv_thomas",
                text = "Salut ! Tu es libre pour déjeuner ensemble ce midi ?",
                senderType = SenderType.OTHER,
                timestamp = now - 45 * minute,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_thomas",
                text = "Carrément ! Vers 12h30 au bistro italien ?",
                senderType = SenderType.ME,
                timestamp = now - 35 * minute,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_thomas",
                text = "Parfait pour le déjeuner à 12h30, je réserve la table !",
                senderType = SenderType.OTHER,
                timestamp = now - 28 * minute,
                status = MessageStatus.READ,
                reaction = "🍕"
            )
        )

        // Seed messages for Group
        val groupMessages = listOf(
            Message(
                conversationId = "conv_projet",
                text = "Bonjour l'équipe ! On fait le point de sprint à 14h.",
                senderType = SenderType.OTHER,
                timestamp = now - 4 * hour,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_projet",
                text = "Tous les tests unitaires et d'interface passent au vert !",
                senderType = SenderType.ME,
                timestamp = now - 3 * hour,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_projet",
                text = "Lucas: La nouvelle version est prête pour les tests !",
                senderType = SenderType.OTHER,
                timestamp = now - 2 * hour,
                status = MessageStatus.READ,
                reaction = "🚀"
            )
        )

        // Seed messages for Lucas (with audio message)
        val lucasMessages = listOf(
            Message(
                conversationId = "conv_lucas",
                text = "Écoute ça quand tu as 2 minutes !",
                senderType = SenderType.OTHER,
                timestamp = now - 1 * day - 10 * minute,
                status = MessageStatus.READ
            ),
            Message(
                conversationId = "conv_lucas",
                text = "Message vocal",
                senderType = SenderType.OTHER,
                timestamp = now - 1 * day,
                status = MessageStatus.READ,
                messageType = MessageType.AUDIO,
                audioDurationSec = 14
            )
        )

        messageDao.insertMessages(sarahMessages + thomasMessages + groupMessages + lucasMessages)
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        messageType: MessageType = MessageType.TEXT,
        mediaUri: String? = null,
        audioDurationSec: Int = 0
    ) {
        val now = System.currentTimeMillis()
        val msg = Message(
            conversationId = conversationId,
            text = text,
            senderType = SenderType.ME,
            timestamp = now,
            status = MessageStatus.SENT,
            messageType = messageType,
            mediaUri = mediaUri,
            audioDurationSec = audioDurationSec
        )
        messageDao.insertMessage(msg)

        val preview = when (messageType) {
            MessageType.TEXT -> text
            MessageType.AUDIO -> "Message vocal ($audioDurationSec s)"
            MessageType.IMAGE -> "Photo"
        }
        conversationDao.updateLastMessage(conversationId, preview, now)

        // Simulate contact reply after a natural pause
        triggerSimulatedReply(conversationId, text)
    }

    private fun triggerSimulatedReply(conversationId: String, userMessage: String) {
        externalScope.launch {
            delay(1000)
            conversationDao.setTyping(conversationId, true)
            delay(1500)
            conversationDao.setTyping(conversationId, false)

            val replyText = generateContextualReply(userMessage)
            val replyMsg = Message(
                conversationId = conversationId,
                text = replyText,
                senderType = SenderType.OTHER,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.READ,
                messageType = MessageType.TEXT
            )
            messageDao.insertMessage(replyMsg)
            conversationDao.updateLastMessage(conversationId, replyText, System.currentTimeMillis())
        }
    }

    private fun generateContextualReply(userMessage: String): String {
        val lower = userMessage.lowercase().trim()
        return when {
            lower.contains("bonjour") || lower.contains("salut") || lower.contains("coucou") ->
                "Salut ! Comment vas-tu aujourd'hui ? 😊"
            lower.contains("merci") ->
                "De rien avec grand plaisir ! N'hésite pas si besoin."
            lower.contains("oui") || lower.contains("d'accord") || lower.contains("ok") ->
                "Super, c'est noté ! On fait comme ça 👍"
            lower.contains("réunion") || lower.contains("rdv") ->
                "Parfait, c'est calé dans mon agenda. À tout à l'heure !"
            lower.contains("?") ->
                "Absolument, je regarde ça en détail et je te confirme très vite !"
            lower.contains("photo") || lower.contains("image") ->
                "Super cliché ! J'adore le rendu 👌"
            else -> listOf(
                "Super, merci pour l'info !",
                "Parfait, ça me va très bien !",
                "Bien reçu ! Je m'en occupe tout de suite.",
                "Génial ! On se tient au courant.",
                "D'accord, à plus tard ! 👋"
            ).random()
        }
    }

    suspend fun markAsRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    suspend fun togglePin(conversationId: String, isPinned: Boolean) {
        conversationDao.setPinned(conversationId, !isPinned)
    }

    suspend fun deleteConversation(conversationId: String) {
        messageDao.deleteAllMessagesForConversation(conversationId)
        conversationDao.deleteConversationById(conversationId)
    }

    suspend fun clearMessages(conversationId: String) {
        messageDao.deleteAllMessagesForConversation(conversationId)
        conversationDao.updateLastMessage(conversationId, "Discussion effacée", System.currentTimeMillis())
    }

    suspend fun toggleReaction(messageId: Long, currentReaction: String?, newReaction: String) {
        val finalReaction = if (currentReaction == newReaction) null else newReaction
        messageDao.updateReaction(messageId, finalReaction)
    }

    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessageById(messageId)
    }

    suspend fun createNewConversation(
        name: String,
        phone: String,
        initialMessage: String?
    ): String {
        val convId = "conv_" + UUID.randomUUID().toString().take(8)
        val colors = listOf(
            0xFF2563EB, 0xFF7C3AED, 0xFFDB2777, 0xFF059669,
            0xFFD97706, 0xFF0284C7, 0xFFDC2626
        )
        val randomColor = colors.random()
        val now = System.currentTimeMillis()

        val conv = Conversation(
            id = convId,
            participantName = name,
            participantPhone = phone,
            participantAvatarColor = randomColor,
            participantStatus = "En ligne",
            isOnline = true,
            isPinned = false,
            isGroup = false,
            lastMessageText = initialMessage ?: "Nouvelle conversation",
            lastMessageTimestamp = now,
            unreadCount = 0
        )
        conversationDao.insertConversation(conv)

        if (!initialMessage.isNullOrBlank()) {
            sendMessage(convId, initialMessage)
        }

        return convId
    }

    fun getAvailableContacts(): List<Contact> {
        return listOf(
            Contact("c1", "Sarah Martin", "+33 6 12 34 56 78", "Disponible pour discuter", 0xFFEC4899, true),
            Contact("c2", "Thomas Dubois", "+33 6 98 76 54 32", "Au travail 💻", 0xFF3B82F6, false),
            Contact("c3", "Sophie Laurent", "+33 6 45 23 89 10", "La vie est belle ☀️", 0xFF10B981, true),
            Contact("c4", "Lucas Bernard", "+33 7 81 90 23 45", "En voyage ✈️", 0xFFF59E0B, true),
            Contact("c5", "Élodie Moreau", "+33 6 77 88 99 00", "Ne pas déranger", 0xFF06B6D4, false),
            Contact("c6", "Alexandre Petit", "+33 6 33 44 55 66", "Salut ! J'utilise Messagerie.", 0xFF8B5CF6, true),
            Contact("c7", "Camille Richard", "+33 7 11 22 33 44", "En réunion 📅", 0xFFEF4444, false),
            Contact("c8", "Julien Faure", "+33 6 55 66 77 88", "Toujours motivé !", 0xFF14B8A6, true)
        )
    }
}
