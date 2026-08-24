package com.example.paranoia.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.paranoia.data.CoinSide
import com.example.paranoia.ui.components.GameTopBar
import com.example.paranoia.ui.components.GlassCard
import com.example.paranoia.ui.components.NeonPrimaryButton
import com.example.paranoia.ui.theme.AccentCyan
import com.example.paranoia.ui.theme.AccentFuchsia
import com.example.paranoia.ui.theme.CardBorder
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.HeadsCyan
import com.example.paranoia.ui.theme.PrimaryPurple
import com.example.paranoia.ui.theme.TailsFuchsia
import com.example.paranoia.ui.theme.TextMuted
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary

@Composable
fun ResultScreen(
    coinResult: CoinSide,
    question: String,
    askerName: String,
    round: Int,
    onNextRound: () -> Unit,
    onViewStats: () -> Unit,
    onExit: () -> Unit
) {
    val isHeads = coinResult == CoinSide.HEADS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Round $round Result",
                onExit = onExit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Result glowing icon sphere
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (isHeads) {
                                Brush.radialGradient(listOf(HeadsCyan, Color(0xFF0891B2), Color(0xFF0E7490)))
                            } else {
                                Brush.radialGradient(listOf(TailsFuchsia, Color(0xFFBE185D), Color(0xFF701A75)))
                            }
                        )
                        .border(
                            2.dp,
                            if (isHeads) Color(0x8022D3EE) else Color(0x80F43F5E),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHeads) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Heads",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Tails",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Headline
                Text(
                    text = if (isHeads) "HEADS" else "TAILS",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = if (isHeads) HeadsCyan else TailsFuchsia
                )

                Text(
                    text = if (isHeads) "QUESTION REVEALED!" else "FOREVER A SECRET",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Card with explanation
                if (isHeads) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xFF131A24),
                        borderColor = Color(0x4022D3EE)
                    ) {
                        Column {
                            Text(
                                text = "THE SECRET QUESTION WAS:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = HeadsCyan
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = question,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 30.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Now everyone in the room knows exactly why $askerName said what they said!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xFF1A1218),
                        borderColor = Color(0x40F43F5E)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "\"What did they ask...?\"",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "$askerName takes this question to the grave. The player who was named will never know for sure why they were chosen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "STAY PARANOID.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp
                                ),
                                color = TailsFuchsia
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeonPrimaryButton(
                    text = "NEXT ROUND",
                    icon = Icons.Default.ArrowForward,
                    gradientColors = listOf(PrimaryPurple, Color(0xFFBE185D)),
                    onClick = onNextRound
                )
            }
        }
    }
}
