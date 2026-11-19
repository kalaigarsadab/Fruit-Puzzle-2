package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.GameRepository
import com.example.data.PlayerPrefs
import com.example.localization.LocalizedStrings
import com.example.model.BasketDesign
import com.example.model.FruitExpression
import com.example.model.FruitTheme
import com.example.model.FruitType
import com.example.ui.components.BasketArtwork
import com.example.ui.components.CandyButton
import com.example.ui.components.CartoonFruitIllustration
import com.example.ui.components.CoinPill
import com.example.ui.theme.BerryPink
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyRewardsDialog(
    prefs: PlayerPrefs,
    strings: LocalizedStrings,
    canClaimToday: Boolean,
    lastClaimedCoins: Int?,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(CreamSurface)
                .border(3.dp, CreamBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = BerryPink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.dailyRewardsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepCocoa
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = SoftCocoa,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.dailyRewardsSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftCocoa,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                GameRepository.DAILY_REWARDS.forEachIndexed { idx, reward ->
                    val dayNum = idx + 1
                    val claimedCount = prefs.dailyStreak
                    val isClaimed = dayNum <= claimedCount && !canClaimToday || (canClaimToday && dayNum <= claimedCount)
                    val isCurrent = canClaimToday && dayNum == (claimedCount % 7) + 1

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isCurrent -> SunnyYellowSoft
                                    isClaimed -> MintGreenSoft
                                    else -> CreamBackground
                                }
                            )
                            .border(
                                width = if (isCurrent) 2.5.dp else 1.5.dp,
                                color = when {
                                    isCurrent -> SunnyYellow
                                    isClaimed -> MintGreen
                                    else -> CreamBorder
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = strings.dayLabel(dayNum),
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepCocoa
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isClaimed) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MintGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "+$reward",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SunnyYellowDark
                            )
                        }
                    }
                }
            }

            if (lastClaimedCoins != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MintGreenSoft)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+$lastClaimedCoins ${strings.coinsLabel}!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MintGreenDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CandyButton(
                onClick = onClaim,
                enabled = canClaimToday,
                containerColor = MintGreen,
                shadowColor = MintGreenDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("claim_daily_button")
            ) {
                Text(
                    text = if (canClaimToday) strings.claimReward else strings.claimedToday,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PartyShopDialog(
    prefs: PlayerPrefs,
    strings: LocalizedStrings,
    statusMessage: String?,
    onSelectTheme: (FruitTheme) -> Unit,
    onSelectBasket: (BasketDesign) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(CreamSurface)
                .border(3.dp, CreamBorder, RoundedCornerShape(28.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.shopTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepCocoa
                )
                CoinPill(coins = prefs.coins)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(CreamBackground)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(strings.fruitThemesTab, strings.basketDesignsTab).forEachIndexed { idx, label ->
                    val active = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (active) MagicPurple else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) Color.White else SoftCocoa
                        )
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = BerryPink,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedTab == 0) {
                    FruitTheme.entries.forEach { theme ->
                        val unlocked = prefs.unlockedThemes.contains(theme.id)
                        val equipped = prefs.equippedTheme == theme
                        ShopItemRow(
                            title = strings.fruitThemeNames[theme] ?: theme.name,
                            unlocked = unlocked,
                            equipped = equipped,
                            cost = theme.costCoins,
                            strings = strings,
                            preview = {
                                CartoonFruitIllustration(
                                    fruitType = FruitType.APPLE,
                                    size = 48.dp,
                                    expression = FruitExpression.EXCITED,
                                    theme = theme
                                )
                            },
                            onClick = { onSelectTheme(theme) }
                        )
                    }
                } else {
                    BasketDesign.entries.forEach { basket ->
                        val unlocked = prefs.unlockedBaskets.contains(basket.id)
                        val equipped = prefs.equippedBasket == basket
                        ShopItemRow(
                            title = strings.basketDesignNames[basket] ?: basket.name,
                            unlocked = unlocked,
                            equipped = equipped,
                            cost = basket.costCoins,
                            strings = strings,
                            preview = {
                                BasketArtwork(
                                    fruitColor = BerryPink,
                                    design = basket,
                                    modifier = Modifier.size(width = 56.dp, height = 34.dp)
                                )
                            },
                            onClick = { onSelectBasket(basket) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CandyButton(
                onClick = onDismiss,
                containerColor = MagicPurple,
                shadowColor = MagicPurpleDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.homeButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

@Composable
private fun ShopItemRow(
    title: String,
    unlocked: Boolean,
    equipped: Boolean,
    cost: Int,
    strings: LocalizedStrings,
    preview: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (equipped) MintGreenSoft else CreamBackground)
            .border(
                width = if (equipped) 2.dp else 1.5.dp,
                color = if (equipped) MintGreen else CreamBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                preview()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DeepCocoa
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    when {
                        equipped -> MintGreen
                        unlocked -> MagicPurpleSoft
                        else -> SunnyYellowSoft
                    }
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    equipped -> strings.equippedLabel
                    unlocked -> strings.equipLabel
                    else -> strings.unlockFor(cost)
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    equipped -> Color.White
                    unlocked -> MagicPurpleDark
                    else -> DeepCocoa
                }
            )
        }
    }
}
