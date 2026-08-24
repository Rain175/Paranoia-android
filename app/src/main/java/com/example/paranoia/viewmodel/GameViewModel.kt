package com.example.paranoia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paranoia.data.CoinSide
import com.example.paranoia.data.GameData
import com.example.paranoia.data.GameRoundHistory
import com.example.paranoia.data.GameScreen
import com.example.paranoia.data.LocalGameState
import com.example.paranoia.data.OnlineRoomState
import com.example.paranoia.data.Player
import com.example.paranoia.data.QuestionCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(GameScreen.MODE_SELECT)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Local Game State
    private val _localPlayers = MutableStateFlow<List<Player>>(emptyList())
    val localPlayers: StateFlow<List<Player>> = _localPlayers.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<QuestionCategory>>(
        setOf(QuestionCategory.ICEBREAKER, QuestionCategory.FUNNY)
    )
    val selectedCategories: StateFlow<Set<QuestionCategory>> = _selectedCategories.asStateFlow()

    private val _customQuestions = MutableStateFlow<List<String>>(listOf(
        "Who would survive the least amount of time on a deserted island?",
        "Who spends the most time looking in the mirror before leaving the house?",
        "Who is most likely to win a reality dating show?"
    ))
    val customQuestions: StateFlow<List<String>> = _customQuestions.asStateFlow()

    private val _localGameState = MutableStateFlow<LocalGameState?>(null)
    val localGameState: StateFlow<LocalGameState?> = _localGameState.asStateFlow()

    // Online Game State
    private val _onlineRoomState = MutableStateFlow<OnlineRoomState?>(null)
    val onlineRoomState: StateFlow<OnlineRoomState?> = _onlineRoomState.asStateFlow()

    // Rules dialog state
    private val _showRulesDialog = MutableStateFlow(false)
    val showRulesDialog: StateFlow<Boolean> = _showRulesDialog.asStateFlow()

    // Exit confirmation dialog state
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    // Coin Animation State
    private val _isCoinFlipping = MutableStateFlow(false)
    val isCoinFlipping: StateFlow<Boolean> = _isCoinFlipping.asStateFlow()

    // Navigation
    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }

    fun setShowRules(show: Boolean) {
        _showRulesDialog.value = show
    }

    fun setShowExitDialog(show: Boolean) {
        _showExitDialog.value = show
    }

    // Local Game Player Management
    fun addLocalPlayer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val current = _localPlayers.value
        if (current.none { it.name.equals(trimmed, ignoreCase = true) }) {
            _localPlayers.value = current + Player(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                order = current.size
            )
        }
    }

    fun removeLocalPlayer(id: String) {
        _localPlayers.value = _localPlayers.value.filter { it.id != id }
    }

    fun toggleCategory(category: QuestionCategory) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            // keep at least 1 category selected
            if (current.size > 1) {
                current.remove(category)
            }
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
    }

    fun addCustomQuestion(questionText: String) {
        val trimmed = questionText.trim()
        if (trimmed.isNotEmpty() && !_customQuestions.value.contains(trimmed)) {
            _customQuestions.value = _customQuestions.value + trimmed
        }
    }

    fun deleteCustomQuestion(questionText: String) {
        _customQuestions.value = _customQuestions.value.filter { it != questionText }
    }

    // Start Local Game
    fun startLocalGame() {
        val players = _localPlayers.value.shuffled()
        if (players.size < 2) return

        val questions = GameData.getQuestionsForCategories(
            categories = _selectedCategories.value,
            customQuestions = _customQuestions.value
        )
        if (questions.isEmpty()) return

        _localGameState.value = LocalGameState(
            players = players,
            questions = questions,
            round = 0,
            askerIdx = 0,
            currentQuestion = questions[0],
            coinResult = null,
            history = emptyList()
        )

        _currentScreen.value = GameScreen.LOCAL_PASS
    }

    fun onPassReady() {
        val game = _localGameState.value ?: return
        val q = game.questions.getOrNull(game.round) ?: "Who in this room is the most mysterious?"
        _localGameState.value = game.copy(currentQuestion = q)
        _currentScreen.value = GameScreen.LOCAL_QUESTION
    }

    fun triggerCoinFlip() {
        val game = _localGameState.value ?: return
        val result = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
        _localGameState.value = game.copy(coinResult = result)
        _currentScreen.value = GameScreen.LOCAL_COIN
    }

    fun startCoinAnimation(onFinished: () -> Unit) {
        _isCoinFlipping.value = true
        viewModelScope.launch {
            delay(1700)
            _isCoinFlipping.value = false
            onFinished()
        }
    }

    fun onCoinFinished() {
        _isCoinFlipping.value = false
        val game = _localGameState.value ?: return
        val coinRes = game.coinResult ?: CoinSide.HEADS

        // Record history
        val newHistory = game.history + GameRoundHistory(
            round = game.round + 1,
            askerName = game.players[game.askerIdx].name,
            question = game.currentQuestion,
            coinResult = coinRes
        )

        _localGameState.value = game.copy(history = newHistory)
        _currentScreen.value = GameScreen.LOCAL_RESULT
    }

    fun nextLocalRound() {
        val game = _localGameState.value ?: return
        val nextRound = game.round + 1

        if (nextRound >= game.questions.size) {
            // End of deck, show stats
            _currentScreen.value = GameScreen.STATS_HISTORY
            return
        }

        val nextAskerIdx = (game.askerIdx + 1) % game.players.size
        _localGameState.value = game.copy(
            round = nextRound,
            askerIdx = nextAskerIdx,
            currentQuestion = game.questions[nextRound],
            coinResult = null
        )

        _currentScreen.value = GameScreen.LOCAL_PASS
    }

    fun exitGame() {
        _localGameState.value = null
        _onlineRoomState.value = null
        _showExitDialog.value = false
        _currentScreen.value = GameScreen.MODE_SELECT
    }

    // Online Multiplayer Simulation & Party Room Logic
    fun createOnlineRoom(hostName: String, maxRounds: Int = 10) {
        val code = generateRoomCode()
        val hostPlayer = Player(
            id = UUID.randomUUID().toString(),
            name = hostName.trim().ifEmpty { "Host" },
            order = 0,
            isHost = true
        )

        _onlineRoomState.value = OnlineRoomState(
            roomCode = code,
            hostName = hostPlayer.name,
            isHost = true,
            myName = hostPlayer.name,
            players = listOf(hostPlayer),
            status = "lobby",
            phase = "question",
            maxRounds = maxRounds,
            selectedCategories = _selectedCategories.value
        )
        _currentScreen.value = GameScreen.ONLINE_ROOM
    }

    fun joinOnlineRoom(code: String, playerName: String) {
        val upperCode = code.trim().uppercase()
        val pName = playerName.trim().ifEmpty { "Player" }

        // If matching current room code or joining a room:
        val currentRoom = _onlineRoomState.value
        if (currentRoom != null && currentRoom.roomCode == upperCode) {
            val newPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = pName,
                order = currentRoom.players.size,
                isHost = false
            )
            _onlineRoomState.value = currentRoom.copy(
                myName = pName,
                isHost = false,
                players = currentRoom.players + newPlayer
            )
        } else {
            // Simulating joined room with bot party members or existing room
            val defaultHost = Player(UUID.randomUUID().toString(), "Party Host", 0, true)
            val joinPlayer = Player(UUID.randomUUID().toString(), pName, 1, false)
            val friend1 = Player(UUID.randomUUID().toString(), "Alex", 2, false)
            val friend2 = Player(UUID.randomUUID().toString(), "Jordan", 3, false)

            _onlineRoomState.value = OnlineRoomState(
                roomCode = upperCode,
                hostName = defaultHost.name,
                isHost = false,
                myName = pName,
                players = listOf(defaultHost, joinPlayer, friend1, friend2),
                status = "lobby",
                phase = "question"
            )
        }
        _currentScreen.value = GameScreen.ONLINE_ROOM
    }

    fun addPlayerToRoom(name: String) {
        val room = _onlineRoomState.value ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val newPlayer = Player(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            order = room.players.size,
            isHost = false
        )
        _onlineRoomState.value = room.copy(players = room.players + newPlayer)
    }

    fun removePlayerFromRoom(playerId: String) {
        val room = _onlineRoomState.value ?: return
        _onlineRoomState.value = room.copy(players = room.players.filter { it.id != playerId })
    }

    fun startOnlineGame() {
        val room = _onlineRoomState.value ?: return
        if (room.players.size < 2) return

        val questions = GameData.getQuestionsForCategories(
            categories = room.selectedCategories,
            customQuestions = _customQuestions.value
        )
        val shuffledPlayers = room.players.shuffled()

        _onlineRoomState.value = room.copy(
            status = "playing",
            phase = "question",
            round = 0,
            askerIdx = 0,
            players = shuffledPlayers,
            currentQuestion = questions.firstOrNull() ?: "Who is most likely to get arrested?",
            coinResult = null
        )

        updateOnlineScreenPhase()
    }

    fun flipOnlineCoin() {
        val room = _onlineRoomState.value ?: return
        val result = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
        _onlineRoomState.value = room.copy(
            phase = "result",
            coinResult = result
        )
        _currentScreen.value = GameScreen.ONLINE_RESULT
    }

    fun nextOnlineRound() {
        val room = _onlineRoomState.value ?: return
        val nextRoundNum = room.round + 1

        if (nextRoundNum >= room.maxRounds) {
            _onlineRoomState.value = room.copy(status = "ended")
            _currentScreen.value = GameScreen.ONLINE_END
            return
        }

        val questions = GameData.getQuestionsForCategories(room.selectedCategories)
        val nextQ = questions.getOrElse(nextRoundNum % questions.size) { "Who is the biggest gossip?" }
        val nextAskerIdx = nextRoundNum % room.players.size

        _onlineRoomState.value = room.copy(
            round = nextRoundNum,
            askerIdx = nextAskerIdx,
            phase = "question",
            currentQuestion = nextQ,
            coinResult = null
        )

        updateOnlineScreenPhase()
    }

    private fun updateOnlineScreenPhase() {
        val room = _onlineRoomState.value ?: return
        val currentAsker = room.players.getOrNull(room.askerIdx)
        val isMyTurn = currentAsker?.name.equals(room.myName, ignoreCase = true)

        if (room.phase == "question") {
            if (isMyTurn) {
                _currentScreen.value = GameScreen.ONLINE_QUESTION
            } else {
                _currentScreen.value = GameScreen.ONLINE_WAITING
            }
        } else if (room.phase == "result") {
            _currentScreen.value = GameScreen.ONLINE_RESULT
        }
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars.random() }.joinToString("")
    }
}
