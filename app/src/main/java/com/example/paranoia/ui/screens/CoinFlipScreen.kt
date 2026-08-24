package com.example.paranoia.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paranoia.data.CoinSide
import com.example.paranoia.ui.components.GameTopBar
import com.example.paranoia.ui.components.InteractiveCoin
import com.example.paranoia.ui.components.NeonPrimaryButton
import com.example.paranoia.ui.theme.AccentAmber
import com.example.paranoia.ui.theme.AccentCyan
import com.example.paranoia.ui.theme.AccentFuchsia
import com.example.paranoia.ui.theme.DarkBackground
import com.example.paranoia.ui.theme.TextMuted
import com.example.paranoia.ui.theme.TextPrimary
import com.example.paranoia.ui.theme.TextSecondary

@Composable
fun CoinFlipScreen(
    isFlipping: Boolean,
    coinResult: CoinSide?,
    onStartFlip: () -> Unit,
    onFlipComplete: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameTopBar(
                title = "Coin Flip",
                onExit = onExit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "THE MOMENT OF TRUTH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = AccentAmber
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEADS = ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = TextPrimary
                    )
                    Text(
                        text = "REVEAL",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = AccentCyan
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAILS = ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = TextPrimary
                    )
                    Text(
                        text = "SECRET",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = AccentFuchsia
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Interactive 3D Coin
                Box(
                    modifier = Modifier.clickable(enabled = !isFlipping) {
                        onStartFlip()
                    },
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveCoin(
                        isFlipping = isFlipping,
                        targetResult = coinResult,
                        onFlipComplete = onFlipComplete,
                        size = 190
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = if (isFlipping) "Flipping in the air..." else "Tap the coin or button below to flip!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Flip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                NeonPrimaryButton(
                    text = if (isFlipping) "FLIPPING..." else "FLIP IT!",
                    enabled = !isFlipping,
                    icon = Icons.Default.Casino,
                    gradientColors = listOf(AccentAmber, Color(0xFFD97706)),
                    onClick = onStartFlip
                )
            }
        }
    }
}
