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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paranoia.data.GameScreen
import com.example.paranoia.ui.components.ShimmerHeader
import com.example.paranoia.ui.theme.AccentAmber
import com.example.paranoia.ui.theme.AccentCyan
import com.example.paranoia.ui.theme.AccentFuchsia
import com.example.paranoia.ui.theme.AccentPink
import com.example.paranoia.ui.theme.CardBorder
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.PrimaryPurple
import com.example.paranoia.ui.theme.TextMuted
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary

@Composable
fun ModeSelectScreen(
    onSelectMode: (GameScreen) -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            ShimmerHeader(
                title = "PARANOIA",
                subtitle = "CHOOSE YOUR GAME MODE"
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Local Mode
            ModeSelectionCard(
                title = "Local Play",
                subtitle = "Pass one device around the room",
                icon = Icons.Default.People,
                gradient = listOf(PrimaryPurple, AccentPink),
                onClick = { onSelectMode(GameScreen.LOCAL_SETUP) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Online Party Room
            ModeSelectionCard(
                title = "Party Room (Online)",
                subtitle = "Create a room or join with 4-letter code",
                icon = Icons.Default.Wifi,
                gradient = listOf(AccentCyan, PrimaryPurple),
                onClick = { onSelectMode(GameScreen.ONLINE_LOBBY) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Questions Deck
            ModeSelectionCard(
                title = "Custom Questions",
                subtitle = "Write your own roasts & inside jokes",
                icon = Icons.Default.EditNote,
                gradient = listOf(AccentAmber, Color(0xFFEA580C)),
                onClick = { onSelectMode(GameScreen.CUSTOM_QUESTIONS) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom utility actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .clickable { showHowToPlay = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "How to play",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOW TO PLAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }
}

@Composable
fun ModeSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "HOW PARANOIA WORKS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = AccentCyan
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RuleItem(
                    number = "1",
                    title = "Secret Question",
                    description = "The current Asker receives a spicy secret question that only they can see."
                )
                RuleItem(
                    number = "2",
                    title = "Answer Out Loud",
                    description = "The Asker MUST say a player's name out loud to everyone in the room — without giving away what the question was!"
                )
                RuleItem(
                    number = "3",
                    title = "Flip the Coin",
                    description = "Flip the coin of fate: \n• HEADS: The secret question is revealed to everyone!\n• TAILS: The question stays a secret forever. Stay paranoid!"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "GOT IT",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
        },
        containerColor = Color(0xFF14141C),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun RuleItem(number: String, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E1065)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFC084FC)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
