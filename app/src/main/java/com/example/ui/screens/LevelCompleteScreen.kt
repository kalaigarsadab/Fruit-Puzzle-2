package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.LevelGenerator
import com.example.data.PlayerPrefs
import com.example.localization.LocalizedStrings
import com.example.model.FruitExpression
import com.example.model.FruitType
import com.example.model.LevelResult
import com.example.ui.components.CandyButton
import com.example.ui.components.CartoonFruitIllustration
import com.example.ui.components.ParticleBurstRequest
import com.example.ui.components.ParticleOverlay
import com.example.ui.theme.BerryPink
import com.example.ui.theme.BerryPinkDark
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.MagicPurple
import com.example.ui.theme.MagicPurpleDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.MintGreenSoft
import com.example.ui.theme.SoftCocoa
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.SunnyYellowSoft
import kotlinx.coroutines.delay

@Composable
fun LevelCompleteScreen(
    result: LevelResult,
    prefs: PlayerPrefs,
    strings: LocalizedStrings,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    BackHandler(onBack = onHome)

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var burst by remember { mutableStateOf<ParticleBurstRequest?>(null) }
    var revealedStars by remember { mutableIntStateOf(0) }
    var targetCoins by remember { mutableIntStateOf(0) }

    val animatedCoins by animateIntAsState(
        targetValue = targetCoins,
        animationSpec = tween(850),
        label = "coins_count_up"
    )

    LaunchedEffect(result.levelNumber, containerSize) {
        if (containerSize.width > 0) {
            burst = ParticleBurstRequest(
                id = System.nanoTime(),
                origin = Offset(containerSize.width / 2f, containerSize.height * 0.28f),
                color = SunnyYellow,
                isCelebration = true
            )
        }
        targetCoins = result.coinsEarned
        for (i in 1..result.starsEarned) {
            delay(220L)
            revealedStars = i
        }
    }

    val infinite = rememberInfiniteTransition(label = "complete_bounce")
    val bounce = infinite.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fruit_party_bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .background(
                Brush.verticalGradient(
                    colors = listOf(CreamBackground, Color(0xFFFFEED9), CreamBackground)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            // Cheering Trio of Cartoon Fruits
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                CartoonFruitIllustration(
                    fruitType = FruitType.WATERMELON,
                    size = 64.dp,
                    expression = FruitExpression.EXCITED,
                    theme = prefs.equippedTheme,
                    modifier = Modifier.graphicsLayer { translationY = bounce.value.dp.toPx() }
                )
                CartoonFruitIllustration(
                    fruitType = FruitType.PINEAPPLE,
                    size = 80.dp,
                    expression = FruitExpression.EXCITED,
                    theme = prefs.equippedTheme,
                    modifier = Modifier.graphicsLayer { translationY = (-bounce.value).dp.toPx() }
                )
                CartoonFruitIllustration(
                    fruitType = FruitType.STRAWBERRY,
                    size = 64.dp,
                    expression = FruitExpression.EXCITED,
                    theme = prefs.equippedTheme,
                    modifier = Modifier.graphicsLayer { translationY = bounce.value.dp.toPx() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Victory Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(30.dp))
                    .clip(RoundedCornerShape(30.dp))
                    .background(CreamSurface)
                    .border(3.dp, CreamBorder, RoundedCornerShape(30.dp))
                    .padding(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MintGreenSoft)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${strings.levelLabel(result.levelNumber)} • ${strings.modeNames[result.mode] ?: ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = strings.levelCompleteTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = DeepCocoa,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animated 3-Star Showcase
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val active = i <= revealedStars
                        val starScale = remember { Animatable(0.6f) }
                        LaunchedEffect(active) {
                            if (active) {
                                starScale.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                starScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (active) SunnyYellow else CreamBorder,
                            modifier = Modifier
                                .size(if (i == 2) 60.dp else 48.dp)
                                .scale(if (active) starScale.value else 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reward Breakdown Rows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SunnyYellowSoft)
                        .border(1.5.dp, SunnyYellow, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.coinsEarnedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCocoa
                    )
                    Text(
                        text = "+$animatedCoins",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFD98200),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMiniTile(
                        label = strings.scoreLabel,
                        value = result.score.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    StatMiniTile(
                        label = if (result.timeBonus > 0) strings.timeBonusLabel else strings.accuracyLabel,
                        value = if (result.timeBonus > 0) "+${result.timeBonus}" else if (result.mistakes == 0) "100%" else "${(100 - result.mistakes * 8).coerceAtLeast(60)}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Next Level CTA
            if (result.levelNumber < LevelGenerator.TOTAL_LEVELS) {
                CandyButton(
                    onClick = onNextLevel,
                    containerColor = MintGreen,
                    shadowColor = MintGreenDark,
                    cornerRadius = 26.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .testTag("next_level_button")
                ) {
                    Text(
                        text = strings.nextLevelButton,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CandyButton(
                    onClick = onReplay,
                    containerColor = MagicPurple,
                    shadowColor = MagicPurpleDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("replay_button")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.replayButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                }

                CandyButton(
                    onClick = onHome,
                    containerColor = BerryPink,
                    shadowColor = BerryPinkDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("complete_home_button")
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.homeButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }

        ParticleOverlay(burstRequest = burst)
    }
}

@Composable
private fun StatMiniTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CreamBackground)
            .border(1.5.dp, CreamBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SoftCocoa
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = DeepCocoa,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
