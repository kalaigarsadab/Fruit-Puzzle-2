package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localization.AppStrings
import com.example.model.AppScreen
import com.example.ui.screens.DailyRewardsDialog
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelCompleteScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.PartyShopDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MagicSortingTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicSortingTheme {
                MagicSortingApp()
            }
        }
    }
}

@Composable
fun MagicSortingApp(viewModel: GameViewModel = viewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onAppForegroundChanged(true)
                Lifecycle.Event.ON_PAUSE -> viewModel.onAppForegroundChanged(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val prefs by viewModel.playerPrefs.collectAsStateWithLifecycle()
    val progressMap by viewModel.levelProgressMap.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedHomeMode by viewModel.selectedHomeMode.collectAsStateWithLifecycle()
    val gameplayState by viewModel.gameplayState.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastLevelResult.collectAsStateWithLifecycle()
    val showDailyRewards by viewModel.showDailyRewardsDialog.collectAsStateWithLifecycle()
    val showShop by viewModel.showShopDialog.collectAsStateWithLifecycle()
    val lastDailyClaimed by viewModel.lastDailyRewardClaimed.collectAsStateWithLifecycle()
    val shopStatus by viewModel.shopStatusMessage.collectAsStateWithLifecycle()
    val restoredCounter by viewModel.settingsRestoredEvent.collectAsStateWithLifecycle()

    val strings = AppStrings.get(prefs.language)
    val canClaimDaily = viewModel.canClaimDailyReward(prefs)

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(90)) },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(
                prefs = prefs,
                strings = strings,
                selectedMode = selectedHomeMode,
                canClaimDaily = canClaimDaily,
                onSelectMode = viewModel::selectHomeMode,
                onPlayClick = { viewModel.startLevel(prefs.highestUnlockedLevel, selectedHomeMode) },
                onLevelsClick = { viewModel.navigateTo(AppScreen.LEVEL_SELECT) },
                onDailyRewardsClick = viewModel::openDailyRewards,
                onShopClick = viewModel::openShop,
                onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )

            AppScreen.LEVEL_SELECT -> LevelSelectScreen(
                highestUnlockedLevel = prefs.highestUnlockedLevel,
                progressMap = progressMap,
                strings = strings,
                onSelectLevel = { level -> viewModel.startLevel(level, selectedHomeMode) },
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )

            AppScreen.GAMEPLAY -> {
                val gameState = gameplayState
                if (gameState != null) {
                    GameplayScreen(
                        state = gameState,
                        prefs = prefs,
                        strings = strings,
                        onFruitPickup = viewModel::onFruitPickup,
                        onFruitDroppedOnBasket = viewModel::onFruitDroppedOnBasket,
                        onHintClick = viewModel::triggerHint,
                        onRestartClick = viewModel::restartCurrentLevel,
                        onPauseToggle = viewModel::setPaused,
                        onGoHome = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }

            AppScreen.LEVEL_COMPLETE -> {
                val result = lastResult
                if (result != null) {
                    LevelCompleteScreen(
                        result = result,
                        prefs = prefs,
                        strings = strings,
                        onNextLevel = {
                            viewModel.startLevel(
                                levelNumber = (result.levelNumber + 1).coerceAtMost(550),
                                overrideMode = selectedHomeMode
                            )
                        },
                        onReplay = {
                            viewModel.startLevel(result.levelNumber, result.mode)
                        },
                        onHome = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }

            AppScreen.SETTINGS -> SettingsScreen(
                prefs = prefs,
                strings = strings,
                restoredEventCounter = restoredCounter,
                onToggleMusic = viewModel::toggleMusic,
                onToggleSound = viewModel::toggleSound,
                onToggleVibration = viewModel::toggleVibration,
                onSelectLanguage = viewModel::setLanguage,
                onRestoreDefaults = viewModel::restoreDefaultSettings,
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }
    }

    if (showDailyRewards) {
        DailyRewardsDialog(
            prefs = prefs,
            strings = strings,
            canClaimToday = canClaimDaily,
            lastClaimedCoins = lastDailyClaimed,
            onClaim = viewModel::claimDailyReward,
            onDismiss = viewModel::closeDailyRewards
        )
    }

    if (showShop) {
        PartyShopDialog(
            prefs = prefs,
            strings = strings,
            statusMessage = shopStatus,
            onSelectTheme = { theme -> viewModel.selectOrBuyTheme(theme, strings.notEnoughCoins) },
            onSelectBasket = { basket -> viewModel.selectOrBuyBasket(basket, strings.notEnoughCoins) },
            onDismiss = viewModel::closeShop
        )
    }
}
