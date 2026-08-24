package com.example.paranoia.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paranoia.data.GameData
import com.example.paranoia.data.Player
import com.example.paranoia.data.QuestionCategory
import com.example.paranoia.ui.components.GameTopBar
import com.example.paranoia.ui.components.GlassCard
import com.example.paranoia.ui.components.NeonPrimaryButton
import com.example.paranoia.ui.theme.CardBorder
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.PrimaryPurple
import com.example.paranoia.ui.theme.TextMuted
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary

@Composable
fun SetupScreen(
    players: List<Player>,
    selectedCategories: Set<QuestionCategory>,
    customQuestionsCount: Int,
    onAddPlayer: (String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onToggleCategory: (QuestionCategory) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }

    val handleAdd = {
        if (nameInput.isNotBlank()) {
            onAddPlayer(nameInput)
            nameInput = ""
        }
    }

    val canStart = players.size >= 2 && selectedCategories.isNotEmpty()

    val totalQuestions = remember(selectedCategories, customQuestionsCount) {
        var count = 0
        if (selectedCategories.contains(QuestionCategory.ICEBREAKER)) count += GameData.ICEBREAKER_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.FUNNY)) count += GameData.FUNNY_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.SPICY)) count += GameData.SPICY_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.EXTREME)) count += GameData.EXTREME_QUESTIONS.size
        if (selectedCategories.contains(QuestionCategory.CUSTOM)) count += customQuestionsCount
        count
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Game Setup",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Players Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLAYERS (${players.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    if (players.size < 2) {
                        Text(
                            text = "Need at least 2",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF87171)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add player input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("Enter player name...", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { handleAdd() }),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF161620),
                            unfocusedContainerColor = Color(0xFF101016),
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = handleAdd,
                        enabled = nameInput.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            disabledContainerColor = Color(0xFF27272A)
                        ),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Player List Container
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0xFF121218)
                ) {
                    if (players.isEmpty()) {
                        Text(
                            text = "No players added yet. Add your friends to start!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            players.forEachIndexed { index, player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1A1A24))
                                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E1065)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFC084FC)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onRemovePlayer(player.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick preset add suggestions
                if (players.isEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Sarah", "Dave", "Chloe", "Sam").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x18FFFFFF))
                                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                    .clickable { onAddPlayer(preset) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ $preset",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Categories Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTION DECKS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Text(
                        text = "$totalQuestions questions ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuestionCategory.values().forEach { cat ->
                        val isSelected = selectedCategories.contains(cat)
                        val catCount = when (cat) {
                            QuestionCategory.ICEBREAKER -> GameData.ICEBREAKER_QUESTIONS.size
                            QuestionCategory.FUNNY -> GameData.FUNNY_QUESTIONS.size
                            QuestionCategory.SPICY -> GameData.SPICY_QUESTIONS.size
                            QuestionCategory.EXTREME -> GameData.EXTREME_QUESTIONS.size
                            QuestionCategory.CUSTOM -> customQuestionsCount
                        }

                        val brush = if (isSelected) {
                            Brush.linearGradient(
                                listOf(Color(cat.gradientStartHex), Color(cat.gradientEndHex))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color(0xFF14141C), Color(0xFF14141C))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(brush)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0x60FFFFFF) else CardBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { onToggleCategory(cat) }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat.label,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x33000000))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$catCount Qs",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = cat.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Start Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                NeonPrimaryButton(
                    text = if (canStart) "START GAME" else "ADD PLAYERS TO START",
                    enabled = canStart,
                    icon = Icons.Default.PlayArrow,
                    onClick = onStartGame
                )
            }
        }
    }
}
