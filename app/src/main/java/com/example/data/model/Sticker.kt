package com.example.data.model

enum class StickerCategory(val label: String, val iconEmoji: String) {
    FAVORITES("Favoris", "⭐"),
    LOVE("Amour", "❤️"),
    FUN("Humour", "😂"),
    QUICK("Réponses", "👍"),
    PARTY("Fête", "🎉"),
    MOOD("Humeur", "☕")
}

data class StickerItem(
    val id: String,
    val category: StickerCategory,
    val emoji: String,
    val title: String,
    val subtitle: String? = null,
    val backgroundHue: Long = 0xFFF1F5F9
)

object StickerRepository {
    val allStickers: List<StickerItem> = listOf(
        // LOVE
        StickerItem("s_love_1", StickerCategory.LOVE, "💖", "Trop d'amour", "Love you"),
        StickerItem("s_love_2", StickerCategory.LOVE, "🥰", "Trop chou", "Adoration"),
        StickerItem("s_love_3", StickerCategory.LOVE, "😘", "Gros bisous", "Bisous"),
        StickerItem("s_love_4", StickerCategory.LOVE, "💌", "Lettre d'amour", "Pour toi"),
        StickerItem("s_love_5", StickerCategory.LOVE, "🌹", "Une rose", "Romantique"),
        StickerItem("s_love_6", StickerCategory.LOVE, "🫶", "Heart hands", "Merci"),

        // FUN
        StickerItem("s_fun_1", StickerCategory.FUN, "🤣", "MDR", "Trop drôle"),
        StickerItem("s_fun_2", StickerCategory.FUN, "💀", "Je suis mort", "Hilarant"),
        StickerItem("s_fun_3", StickerCategory.FUN, "🤦‍♂️", "Facepalm", "Oups"),
        StickerItem("s_fun_4", StickerCategory.FUN, "🤪", "Totalement fou", "Crazy mode"),
        StickerItem("s_fun_5", StickerCategory.FUN, "🤡", "Le clown", "Non mais sérieux"),
        StickerItem("s_fun_6", StickerCategory.FUN, "🍿", "Je prends le pop-corn", "Dramatique"),

        // QUICK REPLIES
        StickerItem("s_quick_1", StickerCategory.QUICK, "👍", "C'est validé !", "Pouce en l'air"),
        StickerItem("s_quick_2", StickerCategory.QUICK, "👌", "Parfait", "C'est noté"),
        StickerItem("s_quick_3", StickerCategory.QUICK, "🙏", "Merci infiniment", "Reconnaissance"),
        StickerItem("s_quick_4", StickerCategory.QUICK, "🤝", "Marché conclu !", "Deal"),
        StickerItem("s_quick_5", StickerCategory.QUICK, "⏳", "J'arrive dans 5 min", "En route"),
        StickerItem("s_quick_6", StickerCategory.QUICK, "👀", "Vu et approuvé", "Je surveille"),

        // PARTY
        StickerItem("s_party_1", StickerCategory.PARTY, "🎉", "Félicitations !", "Bravo !"),
        StickerItem("s_party_2", StickerCategory.PARTY, "🚀", "Let's Go !", "Au sommet"),
        StickerItem("s_party_3", StickerCategory.PARTY, "🔥", "C'est le feu !", "Trop fort"),
        StickerItem("s_party_4", StickerCategory.PARTY, "🎂", "Joyeux anniversaire !", "Fête"),
        StickerItem("s_party_5", StickerCategory.PARTY, "🥳", "Party time !", "Ambiance"),
        StickerItem("s_party_6", StickerCategory.PARTY, "🍾", "Champagne !", "Victoire"),

        // MOOD
        StickerItem("s_mood_1", StickerCategory.MOOD, "☕", "Pause café", "Besoin d'énergie"),
        StickerItem("s_mood_2", StickerCategory.MOOD, "💻", "En plein boulot", "Focus"),
        StickerItem("s_mood_3", StickerCategory.MOOD, "😴", "K.O. total", "Dodo time"),
        StickerItem("s_mood_4", StickerCategory.MOOD, "🍕", "L'heure de manger", "Faim !"),
        StickerItem("s_mood_5", StickerCategory.MOOD, "🏖️", "Mode vacances", "Chill"),
        StickerItem("s_mood_6", StickerCategory.MOOD, "💪", "Motivation max !", "Gym time")
    )
}
