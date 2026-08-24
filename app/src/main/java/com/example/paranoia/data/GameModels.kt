package com.example.paranoia.data

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val order: Int = 0,
    val isHost: Boolean = false
)

enum class QuestionCategory(
    val id: String,
    val label: String,
    val description: String,
    val gradientStartHex: Long,
    val gradientEndHex: Long
) {
    ICEBREAKER(
        id = "icebreaker",
        label = "Icebreaker & Chill",
        description = "Fun, wholesome, and easy for any group",
        gradientStartHex = 0xFF0284C7,
        gradientEndHex = 0xFF06B6D4
    ),
    FUNNY(
        id = "funny",
        label = "Funny & Party",
        description = "Laughs, friendly roasts & party chaos",
        gradientStartHex = 0xFFD97706,
        gradientEndHex = 0xFFEA580C
    ),
    SPICY(
        id = "spicy",
        label = "Call-Out & Drama",
        description = "Juicy, dramatic, boundary-pushing",
        gradientStartHex = 0xFFDB2777,
        gradientEndHex = 0xFFE11D48
    ),
    EXTREME(
        id = "extreme",
        label = "Spicy (18+)",
        description = "Wild, uncensored, secret confessions",
        gradientStartHex = 0xFF991B1B,
        gradientEndHex = 0xFF7C2D12
    ),
    CUSTOM(
        id = "custom",
        label = "Custom Questions",
        description = "Your own friend-group roasts and inside jokes",
        gradientStartHex = 0xFF7E22CE,
        gradientEndHex = 0xFF4338CA
    );

    companion object {
        val ONLINE_CATEGORIES = listOf(
            QuestionCategory.ICEBREAKER,
            QuestionCategory.FUNNY,
            QuestionCategory.SPICY,
            QuestionCategory.EXTREME
        )
    }
}

enum class CoinSide {
    HEADS,
    TAILS
}

enum class GameScreen {
    MODE_SELECT,
    LOCAL_SETUP,
    LOCAL_PASS,
    LOCAL_QUESTION,
    LOCAL_COIN,
    LOCAL_RESULT,
    ONLINE_LOBBY,
    ONLINE_ROOM,
    ONLINE_QUESTION,
    ONLINE_WAITING,
    ONLINE_RESULT,
    ONLINE_END,
    CUSTOM_QUESTIONS,
    STATS_HISTORY
}

@Serializable
data class GameRoundHistory(
    val round: Int,
    val askerName: String,
    val question: String,
    val coinResult: CoinSide,
    val accusedPlayer: String? = null
)

data class LocalGameState(
    val players: List<Player> = emptyList(),
    val questions: List<String> = emptyList(),
    val round: Int = 0,
    val askerIdx: Int = 0,
    val currentQuestion: String = "",
    val coinResult: CoinSide? = null,
    val history: List<GameRoundHistory> = emptyList()
)

data class OnlineRoomState(
    val roomCode: String = "",
    val hostName: String = "",
    val hostSessionId: String = "",
    val isHost: Boolean = false,
    val myName: String = "",
    val players: List<Player> = emptyList(),
    val status: String = "lobby", // lobby, playing, ended
    val phase: String = "question", // question, result
    val round: Int = 0,
    val maxRounds: Int = 10,
    val askerIdx: Int = 0,
    val currentQuestion: String = "",
    val coinResult: CoinSide? = null,
    val selectedCategories: Set<QuestionCategory> = setOf(QuestionCategory.ICEBREAKER, QuestionCategory.FUNNY),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
