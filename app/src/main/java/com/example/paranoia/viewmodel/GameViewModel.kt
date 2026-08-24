package com.example.paranoia.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paranoia.data.CoinSide
import com.example.paranoia.data.FirestoreService
import com.example.paranoia.data.GameData
import com.example.paranoia.data.GameRoundHistory
import com.example.paranoia.data.GameScreen
import com.example.paranoia.data.LocalGameState
import com.example.paranoia.data.OnlineRoomState
import com.example.paranoia.data.Player
import com.example.paranoia.data.QuestionCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class GameViewModel : ViewModel() {
    private val TAG = "GameViewModel"

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

    // Online Category Selection (Strictly for Room Creation - No Custom Questions)
    private val _onlineSelectedCategories = MutableStateFlow<Set<QuestionCategory>>(
        setOf(QuestionCategory.ICEBREAKER, QuestionCategory.FUNNY)
    )
    val onlineSelectedCategories: StateFlow<Set<QuestionCategory>> = _onlineSelectedCategories.asStateFlow()

    private var onlinePollingJob: Job? = null
    private var roomQuestions: List<String> = emptyList()

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
            if (current.size > 1) {
                current.remove(category)
            }
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
    }

    fun toggleOnlineCategory(category: QuestionCategory) {
        if (category == QuestionCategory.CUSTOM) return // Custom questions are strictly for local play
        val current = _onlineSelectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            if (current.size > 1) {
                current.remove(category)
            }
        } else {
            current.add(category)
        }
        _onlineSelectedCategories.value = current
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
        stopOnlinePolling()
        _localGameState.value = null
        _onlineRoomState.value = null
        _showExitDialog.value = false
        _currentScreen.value = GameScreen.MODE_SELECT
    }

    // ==========================================
    // Real Crossplay Online Multiplayer (Firestore)
    // ==========================================

    fun createOnlineRoom(hostName: String, maxRounds: Int = 10) {
        val code = generateRoomCode()
        val trimmedHost = hostName.trim().ifEmpty { "Host" }
        val categories = _onlineSelectedCategories.value
        val questions = GameData.getQuestionsForCategories(categories)
        roomQuestions = questions

        _onlineRoomState.value = OnlineRoomState(
            roomCode = code,
            hostName = trimmedHost,
            hostSessionId = FirestoreService.mySessionId,
            isHost = true,
            myName = trimmedHost,
            players = listOf(Player(id = FirestoreService.mySessionId, name = trimmedHost, order = 0, isHost = true)),
            status = "lobby",
            phase = "question",
            maxRounds = maxRounds,
            selectedCategories = categories,
            isLoading = true
        )
        _currentScreen.value = GameScreen.ONLINE_ROOM

        viewModelScope.launch {
            val result = FirestoreService.createRoom(
                roomCode = code,
                hostName = trimmedHost,
                selectedCategories = categories,
                questions = questions,
                maxRounds = maxRounds
            )

            if (result.isSuccess) {
                _onlineRoomState.value = _onlineRoomState.value?.copy(isLoading = false)
                startOnlinePolling(code)
            } else {
                _onlineRoomState.value = _onlineRoomState.value?.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create room in database"
                )
            }
        }
    }

    fun joinOnlineRoom(code: String, playerName: String) {
        val upperCode = code.trim().uppercase()
        val pName = playerName.trim().ifEmpty { "Player" }

        _onlineRoomState.value = OnlineRoomState(
            roomCode = upperCode,
            hostName = "",
            myName = pName,
            isHost = false,
            players = emptyList(),
            status = "lobby",
            phase = "question",
            isLoading = true
        )
        _currentScreen.value = GameScreen.ONLINE_ROOM

        viewModelScope.launch {
            val result = FirestoreService.joinRoom(upperCode, pName)
            if (result.isSuccess) {
                val joinedRoom = result.getOrNull()!!
                _onlineRoomState.value = joinedRoom.copy(isLoading = false)
                startOnlinePolling(upperCode)
            } else {
                _onlineRoomState.value = _onlineRoomState.value?.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Could not join room"
                )
            }
        }
    }

    private fun startOnlinePolling(roomCode: String) {
        stopOnlinePolling()
        onlinePollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val roomRes = FirestoreService.getRoom(roomCode)
                    if (roomRes.isSuccess && roomRes.getOrNull() != null) {
                        val remoteRoom = roomRes.getOrNull()!!
                        val currentLocal = _onlineRoomState.value

                        // Fetch updated real players from /room_players
                        val realPlayers = FirestoreService.getRoomPlayers(roomCode)
                        val mergedPlayers = if (realPlayers.isNotEmpty()) realPlayers else remoteRoom.players

                        val myName = currentLocal?.myName ?: ""
                        val isHost = remoteRoom.hostSessionId == FirestoreService.mySessionId || currentLocal?.isHost == true

                        val updated = remoteRoom.copy(
                            myName = myName,
                            isHost = isHost,
                            players = mergedPlayers,
                            selectedCategories = if (remoteRoom.selectedCategories.isNotEmpty()) remoteRoom.selectedCategories else currentLocal?.selectedCategories ?: setOf(QuestionCategory.ICEBREAKER, QuestionCategory.FUNNY)
                        )

                        _onlineRoomState.value = updated
                        handleRemoteStateTransition(updated)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Polling error for room $roomCode", e)
                }
                delay(1500) // Poll every 1.5 seconds for snappy sync with web clients
            }
        }
    }

    private fun stopOnlinePolling() {
        onlinePollingJob?.cancel()
        onlinePollingJob = null
    }

    private fun handleRemoteStateTransition(room: OnlineRoomState) {
        when (room.status) {
            "lobby" -> {
                if (_currentScreen.value != GameScreen.ONLINE_ROOM) {
                    _currentScreen.value = GameScreen.ONLINE_ROOM
                }
            }
            "playing" -> {
                val currentAsker = room.players.getOrNull(room.askerIdx)
                val isMyTurn = currentAsker?.name.equals(room.myName, ignoreCase = true)

                if (room.phase == "question") {
                    if (isMyTurn) {
                        if (_currentScreen.value != GameScreen.ONLINE_QUESTION) {
                            _currentScreen.value = GameScreen.ONLINE_QUESTION
                        }
                    } else {
                        if (_currentScreen.value != GameScreen.ONLINE_WAITING) {
                            _currentScreen.value = GameScreen.ONLINE_WAITING
                        }
                    }
                } else if (room.phase == "result") {
                    if (_currentScreen.value != GameScreen.ONLINE_RESULT) {
                        _currentScreen.value = GameScreen.ONLINE_RESULT
                    }
                }
            }
            "ended" -> {
                if (_currentScreen.value != GameScreen.ONLINE_END) {
                    _currentScreen.value = GameScreen.ONLINE_END
                }
            }
        }
    }

    fun startOnlineGame() {
        val room = _onlineRoomState.value ?: return
        if (room.players.size < 2) return

        viewModelScope.launch {
            val questions = if (roomQuestions.isNotEmpty()) roomQuestions else GameData.getQuestionsForCategories(room.selectedCategories)
            val firstQuestion = questions.firstOrNull() ?: "Who in this room is the most mysterious?"
            val playerNames = room.players.map { it.name }

            FirestoreService.updateGameRoom(
                roomCode = room.roomCode,
                hostSessionId = room.hostSessionId,
                hostName = room.hostName,
                status = "playing",
                phase = "question",
                round = 0,
                askerIdx = 0,
                currentQuestion = firstQuestion,
                coinResult = "",
                players = playerNames,
                categories = room.selectedCategories,
                questions = questions,
                maxRounds = room.maxRounds
            )
        }
    }

    fun flipOnlineCoin() {
        val room = _onlineRoomState.value ?: return
        val result = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
        val coinStr = if (result == CoinSide.HEADS) "heads" else "tails"

        viewModelScope.launch {
            val questions = if (roomQuestions.isNotEmpty()) roomQuestions else GameData.getQuestionsForCategories(room.selectedCategories)
            val playerNames = room.players.map { it.name }

            FirestoreService.updateGameRoom(
                roomCode = room.roomCode,
                hostSessionId = room.hostSessionId,
                hostName = room.hostName,
                status = "playing",
                phase = "result",
                round = room.round,
                askerIdx = room.askerIdx,
                currentQuestion = room.currentQuestion,
                coinResult = coinStr,
                players = playerNames,
                categories = room.selectedCategories,
                questions = questions,
                maxRounds = room.maxRounds
            )
        }
    }

    fun nextOnlineRound() {
        val room = _onlineRoomState.value ?: return
        val nextRoundNum = room.round + 1

        viewModelScope.launch {
            val questions = if (roomQuestions.isNotEmpty()) roomQuestions else GameData.getQuestionsForCategories(room.selectedCategories)
            val playerNames = room.players.map { it.name }

            if (nextRoundNum >= room.maxRounds || nextRoundNum >= questions.size) {
                FirestoreService.updateGameRoom(
                    roomCode = room.roomCode,
                    hostSessionId = room.hostSessionId,
                    hostName = room.hostName,
                    status = "ended",
                    phase = "result",
                    round = nextRoundNum,
                    askerIdx = room.askerIdx,
                    currentQuestion = room.currentQuestion,
                    coinResult = "",
                    players = playerNames,
                    categories = room.selectedCategories,
                    questions = questions,
                    maxRounds = room.maxRounds
                )
            } else {
                val nextQ = questions.getOrElse(nextRoundNum % questions.size) { "Who is the biggest gossip?" }
                val nextAskerIdx = nextRoundNum % room.players.size

                FirestoreService.updateGameRoom(
                    roomCode = room.roomCode,
                    hostSessionId = room.hostSessionId,
                    hostName = room.hostName,
                    status = "playing",
                    phase = "question",
                    round = nextRoundNum,
                    askerIdx = nextAskerIdx,
                    currentQuestion = nextQ,
                    coinResult = "",
                    players = playerNames,
                    categories = room.selectedCategories,
                    questions = questions,
                    maxRounds = room.maxRounds
                )
            }
        }
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars.random() }.joinToString("")
    }

    override fun onCleared() {
        super.onCleared()
        stopOnlinePolling()
    }
}
