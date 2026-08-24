package com.example.paranoia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paranoia.data.CoinSide
import com.example.paranoia.data.GameRoundHistory
import com.example.paranoia.ui.components.GameTopBar
import com.example.paranoia.ui.components.GlassCard
import com.example.paranoia.ui.components.NeonPrimaryButton
import com.example.paranoia.ui.theme.AccentCyan
import com.example.paranoia.ui.theme.AccentFuchsia
import com.example.paranoia.ui.theme.CardBorder
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.HeadsCyan
import com.example.paranoia.ui.theme.TailsFuchsia
import com.example.paranoia.ui.theme.TextMuted
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary

@Composable
fun StatsHistoryScreen(
    history: List<GameRoundHistory>,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    val headsCount = history.count { it.coinResult == CoinSide.HEADS }
    val tailsCount = history.count { it.coinResult == CoinSide.TAILS }
    val paranoiaQuotient = if (history.isNotEmpty()) {
        (tailsCount.toFloat() / history.size.toFloat() * 100).toInt()
    } else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Game Recap & Stats",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Stats Highlights Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = Color(0xFF101924),
                            borderColor = Color(0x4022D3EE)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$headsCount",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                    color = HeadsCyan
                                )
                                Text(
                                    text = "REVEALED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = Color(0xFF201018),
                            borderColor = Color(0x40F43F5E)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$tailsCount",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                    color = TailsFuchsia
                                )
                                Text(
                                    text = "SECRETS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color(0xFF14141E)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "PARANOIA QUOTIENT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = AccentCyan
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Percentage of questions kept secret",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "$paranoiaQuotient%",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "ROUND LOG (${history.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "No rounds completed yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                } else {
                    items(history) { round ->
                        val isHeads = round.coinResult == CoinSide.HEADS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF14141E))
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isHeads) Color(0x2022D3EE) else Color(0x20F43F5E))
                                    .border(1.dp, if (isHeads) HeadsCyan else TailsFuchsia, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isHeads) Icons.Default.Visibility else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isHeads) HeadsCyan else TailsFuchsia,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Round ${round.round} • ${round.askerName}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = if (isHeads) "REVEALED" else "SECRET",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isHeads) HeadsCyan else TailsFuchsia
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHeads) round.question else "[Kept Secret by ${round.askerName}]",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = if (isHeads) androidx.compose.ui.text.font.FontStyle.Normal else androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = if (isHeads) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                NeonPrimaryButton(
                    text = "PLAY AGAIN",
                    onClick = onPlayAgain
                )
            }
        }
    }
}
