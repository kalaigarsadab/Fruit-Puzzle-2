package com.example.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.LevelGenerator
import com.example.data.PlayerPrefs
import com.example.data.PreferencesStore
import com.example.model.AppLanguage
import com.example.model.AppScreen
import com.example.model.Basket
import com.example.model.BasketDesign
import com.example.model.Fruit
import com.example.model.FruitTheme
import com.example.model.FruitType
import com.example.model.GameMode
import com.example.model.LevelProgress
import com.example.model.LevelResult
import com.example.model.LevelSpec
import com.example.ui.components.ParticleBurstRequest
import com.example.ui.theme.MintGreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class FloatingPopup(
    val id: Long,
    val text: String,
    val position: Offset
)

@Immutable
data class GameplayUiState(
    val levelSpec: LevelSpec,
    val fruits: List<Fruit>,
    val baskets: List<Basket>,
    val score: Int = 0,
    val mistakes: Int = 0,
    val remainingSeconds: Int? = null,
    val isPaused: Boolean = false,
    val isTimeUp: Boolean = false,
    val isCompleting: Boolean = false,
    val freeHintsLeft: Int = 2,
    val hintedFruitId: Int? = null,
    val hintedFruitType: FruitType? = null,
    val basketPulseCounts: Map<FruitType, Int> = emptyMap(),
    val basketShakeCounts: Map<FruitType, Int> = emptyMap(),
    val dizzyFruitId: Int? = null,
    val burstRequest: ParticleBurstRequest? = null,
    val floatingPopup: FloatingPopup? = null
) {
    val totalFruits: Int get() = fruits.size
    val sortedCount: Int get() = fruits.count { it.sorted }
    val progressFraction: Float get() = if (totalFruits == 0) 0f else sortedCount.toFloat() / totalFruits.toFloat()
    val liveStarsEstimate: Int
        get() = when {
            mistakes <= 1 -> 3
            mistakes <= 3 -> 2
            else -> 1
        }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val prefsStore = PreferencesStore(application)
    private val repository = GameRepository(database.levelProgressDao(), prefsStore)
    val soundEngine = SoundEngine(application)

    val playerPrefs: StateFlow<PlayerPrefs> = repository.playerPrefs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerPrefs()
    )

    val levelProgressMap: StateFlow<Map<Int, LevelProgress>> = repository.levelProgressMap.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedHomeMode = MutableStateFlow<GameMode?>(null)
    val selectedHomeMode: StateFlow<GameMode?> = _selectedHomeMode.asStateFlow()

    private val _gameplayState = MutableStateFlow<GameplayUiState?>(null)
    val gameplayState: StateFlow<GameplayUiState?> = _gameplayState.asStateFlow()

    private val _lastLevelResult = MutableStateFlow<LevelResult?>(null)
    val lastLevelResult: StateFlow<LevelResult?> = _lastLevelResult.asStateFlow()

    private val _showDailyRewardsDialog = MutableStateFlow(false)
    val showDailyRewardsDialog: StateFlow<Boolean> = _showDailyRewardsDialog.asStateFlow()

    private val _showShopDialog = MutableStateFlow(false)
    val showShopDialog: StateFlow<Boolean> = _showShopDialog.asStateFlow()

    private val _lastDailyRewardClaimed = MutableStateFlow<Int?>(null)
    val lastDailyRewardClaimed: StateFlow<Int?> = _lastDailyRewardClaimed.asStateFlow()

    private val _shopStatusMessage = MutableStateFlow<String?>(null)
    val shopStatusMessage: StateFlow<String?> = _shopStatusMessage.asStateFlow()

    private val _settingsRestoredEvent = MutableStateFlow(0)
    val settingsRestoredEvent: StateFlow<Int> = _settingsRestoredEvent.asStateFlow()

    private var timerJob: Job? = null
    private var lastNavActionMs = -1L

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
        viewModelScope.launch {
            playerPrefs.collect { prefs ->
                soundEngine.updateSettings(
                    music = prefs.musicEnabled,
                    sound = prefs.soundEnabled,
                    vibration = prefs.vibrationEnabled
                )
            }
        }
    }

    private fun allowAction(windowMs: Long = 180L): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (lastNavActionMs >= 0L && now - lastNavActionMs < windowMs) return false
        lastNavActionMs = now
        return true
    }

    fun onAppForegroundChanged(inForeground: Boolean) {
        soundEngine.setAppForeground(inForeground)
        if (!inForeground && _currentScreen.value == AppScreen.GAMEPLAY) {
            setPaused(true)
        }
    }

    fun selectHomeMode(mode: GameMode?) {
        soundEngine.playButtonTap()
        _selectedHomeMode.value = mode
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value == screen) return
        if (!allowAction(150L)) return
        soundEngine.playButtonTap()
        if (screen != AppScreen.GAMEPLAY) {
            timerJob?.cancel()
            soundEngine.setGameplayPaused(false)
        }
        _currentScreen.value = screen
    }

    fun startLevel(levelNumber: Int, overrideMode: GameMode? = _selectedHomeMode.value) {
        if (!allowAction(180L)) return
        soundEngine.playButtonTap()
        timerJob?.cancel()
        val spec = LevelGenerator.generateLevel(levelNumber, overrideMode)
        _gameplayState.value = GameplayUiState(
            levelSpec = spec,
            fruits = spec.fruits,
            baskets = spec.baskets,
            remainingSeconds = spec.timeLimitSeconds
        )
        _currentScreen.value = AppScreen.GAMEPLAY
        soundEngine.setGameplayPaused(false)
        startTimerIfNeeded(spec)
    }

    fun restartCurrentLevel() {
        val current = _gameplayState.value ?: return
        if (!allowAction(180L)) return
        soundEngine.playButtonTap()
        timerJob?.cancel()
        val spec = LevelGenerator.generateLevel(current.levelSpec.levelNumber, current.levelSpec.mode)
        _gameplayState.value = GameplayUiState(
            levelSpec = spec,
            fruits = spec.fruits,
            baskets = spec.baskets,
            remainingSeconds = spec.timeLimitSeconds
        )
        soundEngine.setGameplayPaused(false)
        startTimerIfNeeded(spec)
    }

    fun setPaused(paused: Boolean) {
        val current = _gameplayState.value ?: return
        if (current.isPaused == paused) return
        soundEngine.playButtonTap()
        _gameplayState.update { state ->
            state?.copy(isPaused = paused)
        }
        soundEngine.setGameplayPaused(paused)
    }

    private fun startTimerIfNeeded(spec: LevelSpec) {
        if (spec.timeLimitSeconds == null) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _gameplayState.value ?: break
                if (current.isPaused || current.isTimeUp || current.isCompleting) continue
                val sec = current.remainingSeconds ?: break
                if (sec <= 1) {
                    soundEngine.playWrong()
                    _gameplayState.update { it?.copy(remainingSeconds = 0, isTimeUp = true) }
                    break
                } else {
                    _gameplayState.update { it?.copy(remainingSeconds = sec - 1) }
                }
            }
        }
    }

    fun onFruitPickup() {
        soundEngine.playPickup()
    }

    fun onFruitDroppedOnBasket(fruitId: Int, basketType: FruitType, basketCenter: Offset) {
        val state = _gameplayState.value ?: return
        if (state.isPaused || state.isTimeUp || state.isCompleting) return

        val fruit = state.fruits.firstOrNull { it.id == fruitId && !it.sorted } ?: return
        val basket = state.baskets.firstOrNull { it.fruitType == basketType } ?: return

        if (fruit.matches(basket)) {
            soundEngine.playCorrect()
            val multiplier = state.levelSpec.mode.coinMultiplier
            val pointsAdded = 25 * multiplier
            val updatedFruits = state.fruits.map {
                if (it.id == fruitId) it.copy(sorted = true) else it
            }
            val updatedBaskets = state.baskets.map {
                if (it.fruitType == basketType) it.copy(collectedCount = it.collectedCount + 1) else it
            }
            val newPulseMap = state.basketPulseCounts.toMutableMap().apply {
                this[basketType] = (this[basketType] ?: 0) + 1
            }
            val allDone = updatedFruits.all { it.sorted }
            val now = System.nanoTime()

            _gameplayState.update {
                it?.copy(
                    fruits = updatedFruits,
                    baskets = updatedBaskets,
                    score = it.score + pointsAdded,
                    hintedFruitId = if (it.hintedFruitId == fruitId) null else it.hintedFruitId,
                    hintedFruitType = if (it.hintedFruitId == fruitId) null else it.hintedFruitType,
                    basketPulseCounts = newPulseMap,
                    dizzyFruitId = null,
                    isCompleting = allDone,
                    burstRequest = ParticleBurstRequest(
                        id = now,
                        origin = basketCenter,
                        color = if (allDone) MintGreen else basket.color,
                        isCelebration = allDone
                    ),
                    floatingPopup = FloatingPopup(
                        id = now,
                        text = "+$pointsAdded",
                        position = basketCenter.copy(y = basketCenter.y - 40f)
                    )
                )
            }

            if (allDone) {
                completeCurrentLevel()
            }
        } else {
            soundEngine.playWrong()
            val newShakeMap = state.basketShakeCounts.toMutableMap().apply {
                this[basketType] = (this[basketType] ?: 0) + 1
            }
            _gameplayState.update {
                it?.copy(
                    mistakes = it.mistakes + 1,
                    basketShakeCounts = newShakeMap,
                    dizzyFruitId = fruitId
                )
            }
            viewModelScope.launch {
                delay(450L)
                _gameplayState.update { current ->
                    if (current?.dizzyFruitId == fruitId) current.copy(dizzyFruitId = null) else current
                }
            }
        }
    }

    fun triggerHint() {
        val state = _gameplayState.value ?: return
        if (state.isPaused || state.isTimeUp || state.isCompleting) return
        val targetFruit = state.fruits.firstOrNull { !it.sorted } ?: return

        // Synchronous immediate UI response for free hints and available-coin hints!
        if (state.freeHintsLeft > 0) {
            soundEngine.playCoinReward()
            _gameplayState.update {
                it?.copy(
                    freeHintsLeft = it.freeHintsLeft - 1,
                    hintedFruitId = targetFruit.id,
                    hintedFruitType = targetFruit.type
                )
            }
        } else if (playerPrefs.value.coins >= 15) {
            soundEngine.playCoinReward()
            _gameplayState.update {
                it?.copy(
                    hintedFruitId = targetFruit.id,
                    hintedFruitType = targetFruit.type
                )
            }
            viewModelScope.launch {
                repository.spendCoinsForHint(15)
            }
        } else {
            soundEngine.playWrong()
        }
    }

    private fun completeCurrentLevel() {
        timerJob?.cancel()
        viewModelScope.launch {
            delay(480L)
            val finalState = _gameplayState.value ?: return@launch
            val spec = finalState.levelSpec
            val stars = finalState.liveStarsEstimate
            val timeBonus = (finalState.remainingSeconds ?: 0) * 5
            val totalScore = finalState.score + timeBonus + (stars * 30)
            val baseCoins = 12 + (finalState.totalFruits * 2) + (stars * 6)
            val coinsEarned = (baseCoins * spec.mode.coinMultiplier) + (if ((finalState.remainingSeconds ?: 0) > 10) 15 else 0)

            repository.saveLevelCompletion(
                levelNumber = spec.levelNumber,
                starsEarned = stars,
                score = totalScore,
                coinsEarned = coinsEarned
            )

            soundEngine.playLevelComplete()
            _lastLevelResult.value = LevelResult(
                levelNumber = spec.levelNumber,
                mode = spec.mode,
                starsEarned = stars,
                score = totalScore,
                coinsEarned = coinsEarned,
                mistakes = finalState.mistakes,
                timeBonus = timeBonus
            )
            _currentScreen.value = AppScreen.LEVEL_COMPLETE
        }
    }

    fun openDailyRewards() {
        soundEngine.playButtonTap()
        _lastDailyRewardClaimed.value = null
        _showDailyRewardsDialog.value = true
    }

    fun closeDailyRewards() {
        soundEngine.playButtonTap()
        _showDailyRewardsDialog.value = false
    }

    fun canClaimDailyReward(prefs: PlayerPrefs): Boolean {
        val today = currentEpochDay()
        return prefs.lastClaimEpochDay != today
    }

    fun claimDailyReward() {
        val prefs = playerPrefs.value
        val today = currentEpochDay()
        if (prefs.lastClaimEpochDay == today) return
        viewModelScope.launch {
            val coins = repository.claimDailyReward(today, prefs.dailyStreak, prefs.lastClaimEpochDay)
            if (coins != null) {
                soundEngine.playCoinReward()
                _lastDailyRewardClaimed.value = coins
            }
        }
    }

    fun openShop() {
        soundEngine.playButtonTap()
        _shopStatusMessage.value = null
        _showShopDialog.value = true
    }

    fun closeShop() {
        soundEngine.playButtonTap()
        _showShopDialog.value = false
    }

    fun selectOrBuyTheme(theme: FruitTheme, notEnoughCoinsMessage: String) {
        val unlocked = playerPrefs.value.unlockedThemes
        viewModelScope.launch {
            val ok = repository.purchaseOrEquipTheme(theme, unlocked)
            if (ok) {
                soundEngine.playCoinReward()
                _shopStatusMessage.value = null
            } else {
                soundEngine.playWrong()
                _shopStatusMessage.value = notEnoughCoinsMessage
            }
        }
    }

    fun selectOrBuyBasket(basket: BasketDesign, notEnoughCoinsMessage: String) {
        val unlocked = playerPrefs.value.unlockedBaskets
        viewModelScope.launch {
            val ok = repository.purchaseOrEquipBasket(basket, unlocked)
            if (ok) {
                soundEngine.playCoinReward()
                _shopStatusMessage.value = null
            } else {
                soundEngine.playWrong()
                _shopStatusMessage.value = notEnoughCoinsMessage
            }
        }
    }

    fun toggleMusic(enabled: Boolean) {
        soundEngine.playButtonTap()
        viewModelScope.launch { repository.setMusicEnabled(enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
        if (enabled) soundEngine.playButtonTap()
    }

    fun toggleVibration(enabled: Boolean) {
        soundEngine.playButtonTap()
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        soundEngine.playButtonTap()
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun restoreDefaultSettings() {
        soundEngine.playCoinReward()
        _settingsRestoredEvent.update { it + 1 }
        viewModelScope.launch {
            repository.restoreDefaultSettings()
            repository.ensureInitialized()
        }
    }

    private fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundEngine.release()
    }
}
