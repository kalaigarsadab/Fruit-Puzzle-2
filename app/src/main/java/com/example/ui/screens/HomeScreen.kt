package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerPrefs
import com.example.localization.LocalizedStrings
import com.example.model.FruitExpression
import com.example.model.FruitType
import com.example.model.GameMode
import com.example.ui.components.CandyButton
import com.example.ui.components.CartoonFruitIllustration
import com.example.ui.components.CoinPill
import com.example.ui.components.RoundIconPillButton
import com.example.ui.theme.BerryPink
import com.example.ui.theme.BerryPinkDark
import com.example.ui.theme.BerryPinkSoft
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.MagicPurple
import com.example.ui.theme.MagicPurpleDark
import com.example.ui.theme.MagicPurpleSoft
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.MintGreenSoft
import com.example.ui.theme.SoftCocoa
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.SunnyYellowDark
import com.example.ui.theme.SunnyYellowSoft
import com.example.ui.theme.TangerineOrange
import com.example.ui.theme.TangerineOrangeSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    prefs: PlayerPrefs,
    strings: LocalizedStrings,
    selectedMode: GameMode?,
    canClaimDaily: Boolean,
    onSelectMode: (GameMode?) -> Unit,
    onPlayClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onDailyRewardsClick: () -> Unit,
    onShopClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "home_bob")
    val floatOffset = infinite.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CreamBackground, Color(0xFFFFF1DC), CreamBackground)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Top Utility Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoinPill(
                    coins = prefs.coins,
                    onClick = onShopClick,
                    modifier = Modifier.testTag("home_coins_pill")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoundIconPillButton(
                        icon = Icons.Rounded.CardGiftcard,
                        contentDescription = strings.dailyRewardsButton,
                        backgroundColor = SunnyYellowSoft,
                        iconTint = SunnyYellowDark,
                        badgeDot = canClaimDaily,
                        onClick = onDailyRewardsClick,
                        modifier = Modifier.testTag("daily_rewards_button")
                    )
                    RoundIconPillButton(
                        icon = Icons.Rounded.Palette,
                        contentDescription = strings.themesShopButton,
                        backgroundColor = BerryPinkSoft,
                        iconTint = BerryPinkDark,
                        onClick = onShopClick,
                        modifier = Modifier.testTag("themes_shop_button")
                    )
                    RoundIconPillButton(
                        icon = Icons.Rounded.Settings,
                        contentDescription = strings.settingsButton,
                        backgroundColor = MagicPurpleSoft,
                        iconTint = MagicPurpleDark,
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Magic Sorting Logo + Party Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = MagicPurple.copy(alpha = 0.25f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(CreamSurface)
                    .border(2.5.dp, CreamBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(146.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_party_banner),
                            contentDescription = "Magic Sorting Fruit Party Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xCCFFF8EE))
                                    )
                                )
                        )
                        // Floating cute cartoon mascots on corners
                        CartoonFruitIllustration(
                            fruitType = FruitType.APPLE,
                            size = 48.dp,
                            expression = FruitExpression.EXCITED,
                            theme = prefs.equippedTheme,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .graphicsLayer { translationY = floatOffset.value.dp.toPx() }
                        )
                        CartoonFruitIllustration(
                            fruitType = FruitType.STRAWBERRY,
                            size = 46.dp,
                            expression = FruitExpression.HAPPY,
                            theme = prefs.equippedTheme,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .graphicsLayer { translationY = (-floatOffset.value).dp.toPx() }
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-18).dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(listOf(MagicPurple, BerryPink))
                                )
                                .padding(horizontal = 18.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.appSubtitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                letterSpacing = 1.4.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 31.sp),
                            color = DeepCocoa,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Play CTA
            CandyButton(
                onClick = onPlayClick,
                containerColor = MintGreen,
                shadowColor = MintGreenDark,
                cornerRadius = 28.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .testTag("play_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.playLevel(prefs.highestUnlockedLevel),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Row: 550 Levels + Daily Gifts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CandyButton(
                    onClick = onLevelsClick,
                    containerColor = MagicPurple,
                    shadowColor = MagicPurpleDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("levels_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridView,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.levelsButton,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }

                CandyButton(
                    onClick = onDailyRewardsClick,
                    containerColor = BerryPink,
                    shadowColor = BerryPinkDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("home_daily_cta")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.dailyRewardsButton,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5 Game Modes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.chooseModeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepCocoa
                )
                if (selectedMode != null) {
                    Text(
                        text = "Auto",
                        style = MaterialTheme.typography.labelMedium,
                        color = MagicPurple,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MagicPurpleSoft)
                            .clickable { onSelectMode(null) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                GameMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    ModeCard(
                        mode = mode,
                        title = strings.modeNames[mode] ?: mode.name,
                        description = strings.modeDescriptions[mode] ?: "",
                        isSelected = isSelected,
                        onClick = {
                            onSelectMode(if (isSelected) null else mode)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_card_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeCard(
    mode: GameMode,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) mode.softColor else CreamSurface
    val borderColor = if (isSelected) mode.accentColor else CreamBorder
    val icon = when (mode) {
        GameMode.CHALLENGE -> Icons.Rounded.Timer
        else -> Icons.Rounded.AutoAwesome
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(if (isSelected) 2.5.dp else 1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(mode.accentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = mode.accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DeepCocoa,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = SoftCocoa,
                maxLines = 2
            )
        }
    }
}
