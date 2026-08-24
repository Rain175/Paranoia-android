package com.example.paranoia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paranoia.data.GameData
import com.example.paranoia.data.OnlineRoomState
import com.example.paranoia.data.QuestionCategory
import com.example.paranoia.ui.components.GameTopBar
import com.example.paranoia.ui.components.GlassCard
import com.example.paranoia.ui.components.NeonPrimaryButton
import com.example.paranoia.ui.theme.AccentCyan
import com.example.paranoia.ui.theme.AccentPink
import com.example.paranoia.ui.theme.CardBorder
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.PrimaryPurple
import com.example.paranoia.ui.theme.TextMuted
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary

@Composable
fun OnlineLobbyScreen(
    selectedCategories: Set<QuestionCategory>,
    onToggleCategory: (QuestionCategory) -> Unit,
    onCreateRoom: (hostName: String) -> Unit,
    onJoinRoom: (code: String, playerName: String) -> Unit,
    onBack: () -> Unit
) {
    var hostNameInput by remember { mutableStateOf("") }
    var joinNameInput by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("create") } // "create" or "join"

    val onlineCategories = QuestionCategory.ONLINE_CATEGORIES

    val totalOnlineQuestions = remember(selectedCategories) {
        var count = 0
        if (selectedCategories.contains(QuestionCategory.ICEBREAKER)) count += GameData.ICEBREAKER_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.FUNNY)) count += GameData.FUNNY_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.SPICY)) count += GameData.SPICY_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.EXTREME)) count += GameData.EXTREME_QUESTIONS.size
        count
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Party Room (Crossplay)",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Crossplay Banner Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1806B6D4))
                        .border(1.dp, Color(0x4006B6D4), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Crossplay",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Crossplay active with Web & Mobile browsers!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14141E))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (activeTab == "create") PrimaryPurple else Color.Transparent)
                            .clickable { activeTab = "create" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CREATE ROOM",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (activeTab == "create") Color.White else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (activeTab == "join") AccentCyan else Color.Transparent)
                            .clickable { activeTab = "join" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JOIN ROOM",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (activeTab == "join") Color.Black else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (activeTab == "create") {
                    // Create Room Section
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF12121A)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "HOST A NEW PARTY",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            OutlinedTextField(
                                value = hostNameInput,
                                onValueChange = { hostNameInput = it },
                                label = { Text("Your Name (Host)") },
                                placeholder = { Text("e.g. Alex", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF181822),
                                    unfocusedContainerColor = Color(0xFF101016),
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            // Category Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SELECT ROOM CATEGORIES",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$totalOnlineQuestions Qs in deck",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                onlineCategories.forEach { cat ->
                                    val isSelected = selectedCategories.contains(cat)
                                    val catCount = when (cat) {
                                        QuestionCategory.ICEBREAKER -> GameData.ICEBREAKER_QUESTIONS.size
                                        QuestionCategory.FUNNY -> GameData.FUNNY_QUESTIONS.size
                                        QuestionCategory.SPICY -> GameData.SPICY_QUESTIONS.size
                                        QuestionCategory.EXTREME -> GameData.EXTREME_QUESTIONS.size
                                        QuestionCategory.CUSTOM -> 0
                                    }

                                    val bgBrush = if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(Color(cat.gradientStartHex), Color(cat.gradientEndHex))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(Color(0xFF181824), Color(0xFF181824))
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(bgBrush)
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0x60FFFFFF) else CardBorder,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable { onToggleCategory(cat) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = cat.label,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0x33000000))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "$catCount",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White.copy(alpha = 0.85f)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = cat.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextMuted
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else Color.Transparent)
                                                    .border(2.dp, if (isSelected) Color.White else CardBorder, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Note: Custom questions are reserved for Local Play.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            NeonPrimaryButton(
                                text = "CREATE PARTY ROOM",
                                enabled = hostNameInput.isNotBlank() && selectedCategories.isNotEmpty(),
                                icon = Icons.Default.Wifi,
                                onClick = { onCreateRoom(hostNameInput) }
                            )
                        }
                    }
                } else {
                    // Join Room Section
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF12121A)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "ENTER ROOM CODE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            OutlinedTextField(
                                value = roomCodeInput.uppercase(),
                                onValueChange = { if (it.length <= 4) roomCodeInput = it.uppercase() },
                                label = { Text("4-Letter Room Code") },
                                placeholder = { Text("e.g. WXYZ", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF181822),
                                    unfocusedContainerColor = Color(0xFF101016),
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = joinNameInput,
                                onValueChange = { joinNameInput = it },
                                label = { Text("Your Player Name (1 per device)") },
                                placeholder = { Text("e.g. Jordan", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF181822),
                                    unfocusedContainerColor = Color(0xFF101016),
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            NeonPrimaryButton(
                                text = "JOIN PARTY",
                                enabled = roomCodeInput.length >= 4 && joinNameInput.isNotBlank(),
                                icon = Icons.Default.ChevronRight,
                                gradientColors = listOf(AccentCyan, Color(0xFF0284C7)),
                                onClick = { onJoinRoom(roomCodeInput, joinNameInput) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OnlineRoomScreen(
    room: OnlineRoomState,
    onStartGame: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Room: ${room.roomCode}",
                trailingBadge = if (room.isHost) "HOST" else "PLAYER",
                onExit = onExit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Big Code Display
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xFF141420),
                    borderColor = Color(0x6006B6D4)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SHARE THIS ROOM CODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = room.roomCode,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Friends can join from web or mobile browsers!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Single Device Policy Notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Devices",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1 player per device. Everyone joins on their own screen!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (room.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x30DC2626))
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = room.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Connected Players List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLAYERS IN LOBBY (${room.players.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    if (room.players.size < 2) {
                        Text(
                            text = "Need at least 2 players",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF87171)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    room.players.forEachIndexed { index, player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF161622))
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (player.isHost) PrimaryPurple else Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = player.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = player.name + if (player.name.equals(room.myName, ignoreCase = true)) " (You)" else "",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (player.isHost) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x309333EA))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "HOST",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFC084FC)
                                    )
                                }
                            }
                        }
                    }
                }

                if (room.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = AccentCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Start Button (or Waiting for Host notice)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (room.isHost) {
                    NeonPrimaryButton(
                        text = if (room.players.size >= 2) "START PARTY GAME" else "WAITING FOR PLAYERS (MIN 2)",
                        enabled = room.players.size >= 2,
                        icon = Icons.Default.PlayArrow,
                        onClick = onStartGame
                    )
                } else {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF14141E)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Waiting for host to start the game...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineWaitingScreen(
    askerName: String,
    round: Int,
    maxRounds: Int,
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Round $round of $maxRounds",
                onExit = onExit
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(Color(0x209333EA))
                        .border(2.dp, PrimaryPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Waiting",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "LISTEN CLOSELY...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$askerName is reading their secret question",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "They are about to say a player's name OUT LOUD in the room!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun OnlineGameEndScreen(
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GAME OVER!",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hope your friendships survived the paranoia.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(36.dp))

            NeonPrimaryButton(
                text = "PLAY AGAIN",
                onClick = onPlayAgain
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E28)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Back to Main Menu", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
