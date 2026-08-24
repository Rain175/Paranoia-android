package com.example.paranoia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paranoia.data.CoinSide
import com.example.paranoia.data.GameScreen
import com.example.paranoia.ui.screens.CoinFlipScreen
import com.example.paranoia.ui.screens.CustomQuestionsScreen
import com.example.paranoia.ui.screens.ModeSelectScreen
import com.example.paranoia.ui.screens.OnlineGameEndScreen
import com.example.paranoia.ui.screens.OnlineLobbyScreen
import com.example.paranoia.ui.screens.OnlineRoomScreen
import com.example.paranoia.ui.screens.OnlineWaitingScreen
import com.example.paranoia.ui.screens.PassScreen
import com.example.paranoia.ui.screens.QuestionScreen
import com.example.paranoia.ui.screens.ResultScreen
import com.example.paranoia.ui.screens.SetupScreen
import com.example.paranoia.ui.screens.StatsHistoryScreen
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.ParanoiaTheme
import com.example.paranoia.ui.theme.PrimaryPurple
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary
import com.example.paranoia.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParanoiaTheme {
                ParanoiaApp()
            }
        }
    }
}

@Composable
fun ParanoiaApp(
    viewModel: GameViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val localPlayers by viewModel.localPlayers.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val customQuestions by viewModel.customQuestions.collectAsState()
    val localGameState by viewModel.localGameState.collectAsState()
    val onlineRoomState by viewModel.onlineRoomState.collectAsState()
    val isCoinFlipping by viewModel.isCoinFlipping.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                GameScreen.MODE_SELECT -> {
                    ModeSelectScreen(
                        onSelectMode = { target -> viewModel.navigateTo(target) }
                    )
                }

                GameScreen.LOCAL_SETUP -> {
                    SetupScreen(
                        players = localPlayers,
                        selectedCategories = selectedCategories,
                        customQuestionsCount = customQuestions.size,
                        onAddPlayer = { viewModel.addLocalPlayer(it) },
                        onRemovePlayer = { viewModel.removeLocalPlayer(it) },
                        onToggleCategory = { viewModel.toggleCategory(it) },
                        onStartGame = { viewModel.startLocalGame() },
                        onBack = { viewModel.navigateTo(GameScreen.MODE_SELECT) }
                    )
                }

                GameScreen.LOCAL_PASS -> {
                    val game = localGameState
                    if (game != null) {
                        PassScreen(
                            askerName = game.players[game.askerIdx].name,
                            round = game.round + 1,
                            onReady = { viewModel.onPassReady() },
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.LOCAL_QUESTION -> {
                    val game = localGameState
                    if (game != null) {
                        val currentAsker = game.players[game.askerIdx]
                        val others = game.players.filter { it.id != currentAsker.id }.map { it.name }
                        QuestionScreen(
                            question = game.currentQuestion,
                            askerName = currentAsker.name,
                            otherPlayers = others,
                            onFlip = { viewModel.triggerCoinFlip() },
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.LOCAL_COIN -> {
                    val game = localGameState
                    CoinFlipScreen(
                        isFlipping = isCoinFlipping,
                        coinResult = game?.coinResult,
                        onStartFlip = {
                            viewModel.startCoinAnimation(onFinished = {
                                viewModel.onCoinFinished()
                            })
                        },
                        onFlipComplete = { viewModel.onCoinFinished() },
                        onExit = { viewModel.setShowExitDialog(true) }
                    )
                }

                GameScreen.LOCAL_RESULT -> {
                    val game = localGameState
                    if (game != null && game.coinResult != null) {
                        ResultScreen(
                            coinResult = game.coinResult,
                            question = game.currentQuestion,
                            askerName = game.players[game.askerIdx].name,
                            round = game.round + 1,
                            onNextRound = { viewModel.nextLocalRound() },
                            onViewStats = { viewModel.navigateTo(GameScreen.STATS_HISTORY) },
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.ONLINE_LOBBY -> {
                    OnlineLobbyScreen(
                        onCreateRoom = { hostName -> viewModel.createOnlineRoom(hostName) },
                        onJoinRoom = { code, name -> viewModel.joinOnlineRoom(code, name) },
                        onBack = { viewModel.navigateTo(GameScreen.MODE_SELECT) }
                    )
                }

                GameScreen.ONLINE_ROOM -> {
                    val room = onlineRoomState
                    if (room != null) {
                        OnlineRoomScreen(
                            room = room,
                            onAddPlayer = { viewModel.addPlayerToRoom(it) },
                            onRemovePlayer = { viewModel.removePlayerFromRoom(it) },
                            onStartGame = { viewModel.startOnlineGame() },
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.ONLINE_QUESTION -> {
                    val room = onlineRoomState
                    if (room != null) {
                        val currentAsker = room.players.getOrNull(room.askerIdx)
                        val others = room.players.filterIndexed { i, _ -> i != room.askerIdx }.map { it.name }
                        QuestionScreen(
                            question = room.currentQuestion,
                            askerName = currentAsker?.name ?: "You",
                            otherPlayers = others,
                            onFlip = { viewModel.flipOnlineCoin() },
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.ONLINE_WAITING -> {
                    val room = onlineRoomState
                    if (room != null) {
                        val currentAsker = room.players.getOrNull(room.askerIdx)
                        OnlineWaitingScreen(
                            askerName = currentAsker?.name ?: "Player",
                            round = room.round + 1,
                            maxRounds = room.maxRounds,
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.ONLINE_RESULT -> {
                    val room = onlineRoomState
                    if (room != null && room.coinResult != null) {
                        val currentAsker = room.players.getOrNull(room.askerIdx)
                        ResultScreen(
                            coinResult = room.coinResult,
                            question = room.currentQuestion,
                            askerName = currentAsker?.name ?: "Player",
                            round = room.round + 1,
                            onNextRound = { viewModel.nextOnlineRound() },
                            onViewStats = { viewModel.navigateTo(GameScreen.ONLINE_END) },
                            onExit = { viewModel.setShowExitDialog(true) }
                        )
                    }
                }

                GameScreen.ONLINE_END -> {
                    OnlineGameEndScreen(
                        onPlayAgain = { viewModel.navigateTo(GameScreen.ONLINE_LOBBY) },
                        onBackToHome = { viewModel.exitGame() }
                    )
                }

                GameScreen.CUSTOM_QUESTIONS -> {
                    CustomQuestionsScreen(
                        customQuestions = customQuestions,
                        onAddQuestion = { viewModel.addCustomQuestion(it) },
                        onDeleteQuestion = { viewModel.deleteCustomQuestion(it) },
                        onBack = { viewModel.navigateTo(GameScreen.MODE_SELECT) }
                    )
                }

                GameScreen.STATS_HISTORY -> {
                    val history = localGameState?.history ?: emptyList()
                    StatsHistoryScreen(
                        history = history,
                        onPlayAgain = { viewModel.navigateTo(GameScreen.LOCAL_SETUP) },
                        onBack = { viewModel.navigateTo(GameScreen.LOCAL_SETUP) }
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowExitDialog(false) },
            title = {
                Text("Exit Game?", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Are you sure you want to end this game and return to the main menu?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.exitGame() }) {
                    Text("Exit Game", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowExitDialog(false) }) {
                    Text("Stay", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF14141E),
            shape = RoundedCornerShape(18.dp)
        )
    }
}
