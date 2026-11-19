package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelGenerator
import com.example.localization.LocalizedStrings
import com.example.model.LevelProgress
import com.example.ui.components.RoundIconPillButton
import com.example.ui.components.StarRow
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.MagicPurple
import com.example.ui.theme.MagicPurpleDark
import com.example.ui.theme.MagicPurpleSoft
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.SoftCocoa
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.SunnyYellowSoft

@Composable
fun LevelSelectScreen(
    highestUnlockedLevel: Int,
    progressMap: Map<Int, LevelProgress>,
    strings: LocalizedStrings,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val initialWorld = ((highestUnlockedLevel - 1) / LevelGenerator.LEVELS_PER_WORLD).coerceIn(0, LevelGenerator.TOTAL_WORLDS - 1)
    var selectedWorldIndex by remember { mutableIntStateOf(initialWorld) }

    val totalStarsEarned = remember(progressMap) {
        progressMap.values.sumOf { it.stars }
    }
    val maxTotalStars = LevelGenerator.TOTAL_LEVELS * 3

    val startLevel = selectedWorldIndex * LevelGenerator.LEVELS_PER_WORLD + 1
    val endLevel = (startLevel + LevelGenerator.LEVELS_PER_WORLD - 1).coerceAtMost(LevelGenerator.TOTAL_LEVELS)
    val levelRange = remember(selectedWorldIndex) { (startLevel..endLevel).toList() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconPillButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.homeButton,
                    backgroundColor = MagicPurpleSoft,
                    iconTint = MagicPurpleDark,
                    onClick = onBack,
                    modifier = Modifier.testTag("level_select_back")
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.levelSelectTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepCocoa
                    )
                    Text(
                        text = strings.totalLevelsSubtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftCocoa
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SunnyYellowSoft)
                        .border(2.dp, SunnyYellow, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = SunnyYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalStarsEarned/$maxTotalStars",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCocoa
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 11 Worlds / Chapters Selector (covering all 550 levels)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(LevelGenerator.TOTAL_WORLDS) { worldIdx ->
                    val first = worldIdx * LevelGenerator.LEVELS_PER_WORLD + 1
                    val last = first + LevelGenerator.LEVELS_PER_WORLD - 1
                    val isSelected = worldIdx == selectedWorldIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MagicPurple else CreamSurface)
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) MagicPurpleDark else CreamBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedWorldIndex = worldIdx }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("world_tab_${worldIdx + 1}")
                    ) {
                        Text(
                            text = strings.worldLabel(worldIdx + 1),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else DeepCocoa
                        )
                        Text(
                            text = "$first–$last",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MutedTaupe
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 50-Level Grid for selected World
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 68.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(levelRange, key = { it }) { levelNum ->
                    val progress = progressMap[levelNum]
                    val unlocked = (progress?.unlocked == true) || levelNum <= highestUnlockedLevel
                    val stars = progress?.stars ?: 0
                    val mode = LevelGenerator.defaultModeForLevel(levelNum)

                    LevelTile(
                        levelNumber = levelNum,
                        unlocked = unlocked,
                        stars = stars,
                        modeColor = mode.accentColor,
                        onClick = { if (unlocked) onSelectLevel(levelNum) },
                        modifier = Modifier.testTag("level_tile_$levelNum")
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelTile(
    levelNumber: Int,
    unlocked: Boolean,
    stars: Int,
    modeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (unlocked) CreamSurface else Color(0xFFF3EAE0)
    val borderColor = if (unlocked) modeColor.copy(alpha = 0.7f) else CreamBorder

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .aspectRatio(0.9f)
            .shadow(if (unlocked) 4.dp else 0.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(6.dp)
    ) {
        if (unlocked) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(modeColor)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = levelNumber.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                fontWeight = FontWeight.ExtraBold,
                color = DeepCocoa
            )
            Spacer(modifier = Modifier.height(2.dp))
            StarRow(earnedStars = stars, starSize = 13.dp)
        } else {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Locked level $levelNumber",
                tint = MutedTaupe,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = levelNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MutedTaupe
            )
        }
    }
}
