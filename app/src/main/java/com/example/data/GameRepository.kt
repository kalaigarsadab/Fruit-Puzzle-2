package com.example.data

import com.example.model.AppLanguage
import com.example.model.BasketDesign
import com.example.model.FruitTheme
import com.example.model.LevelProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(
    private val levelDao: LevelProgressDao,
    private val preferencesStore: PreferencesStore
) {
    val playerPrefs: Flow<PlayerPrefs> = preferencesStore.prefsFlow

    val levelProgressMap: Flow<Map<Int, LevelProgress>> = levelDao.observeAllProgress().map { rows ->
        rows.associate { row ->
            row.levelNumber to LevelProgress(
                levelNumber = row.levelNumber,
                unlocked = row.unlocked,
                stars = row.stars,
                bestScore = row.bestScore
            )
        }
    }

    suspend fun ensureInitialized() {
        val level1 = levelDao.getLevelProgress(1)
        if (level1 == null) {
            levelDao.upsert(
                LevelProgressEntity(
                    levelNumber = 1,
                    unlocked = true,
                    stars = 0,
                    bestScore = 0
                )
            )
        }
    }

    suspend fun saveLevelCompletion(
        levelNumber: Int,
        starsEarned: Int,
        score: Int,
        coinsEarned: Int
    ) {
        val existing = levelDao.getLevelProgress(levelNumber)
        val bestStars = maxOf(existing?.stars ?: 0, starsEarned)
        val bestScore = maxOf(existing?.bestScore ?: 0, score)

        levelDao.upsert(
            LevelProgressEntity(
                levelNumber = levelNumber,
                unlocked = true,
                stars = bestStars,
                bestScore = bestScore
            )
        )

        val nextLevel = (levelNumber + 1).coerceAtMost(LevelGenerator.TOTAL_LEVELS)
        if (nextLevel > levelNumber) {
            val nextExisting = levelDao.getLevelProgress(nextLevel)
            if (nextExisting == null || !nextExisting.unlocked) {
                levelDao.upsert(
                    LevelProgressEntity(
                        levelNumber = nextLevel,
                        unlocked = true,
                        stars = nextExisting?.stars ?: 0,
                        bestScore = nextExisting?.bestScore ?: 0
                    )
                )
            }
        }

        preferencesStore.updateHighestLevel(nextLevel)
        preferencesStore.addCoins(coinsEarned)
    }

    suspend fun spendCoinsForHint(cost: Int): Boolean = preferencesStore.spendCoins(cost)

    suspend fun setMusicEnabled(enabled: Boolean) = preferencesStore.setMusicEnabled(enabled)
    suspend fun setSoundEnabled(enabled: Boolean) = preferencesStore.setSoundEnabled(enabled)
    suspend fun setVibrationEnabled(enabled: Boolean) = preferencesStore.setVibrationEnabled(enabled)
    suspend fun setLanguage(language: AppLanguage) = preferencesStore.setLanguage(language)
    suspend fun restoreDefaultSettings() = preferencesStore.restoreDefaultSettings()

    suspend fun claimDailyReward(currentEpochDay: Long, currentStreak: Int, lastClaimDay: Long): Int? {
        if (lastClaimDay == currentEpochDay) return null
        val newStreak = if (lastClaimDay == currentEpochDay - 1L) {
            (currentStreak % 7) + 1
        } else {
            1
        }
        val rewardAmount = dailyRewardCoinsForDay(newStreak)
        preferencesStore.recordDailyClaim(currentEpochDay, newStreak, rewardAmount)
        return rewardAmount
    }

    suspend fun purchaseOrEquipTheme(theme: FruitTheme, unlockedThemes: Set<String>): Boolean {
        if (unlockedThemes.contains(theme.id)) {
            preferencesStore.equipTheme(theme)
            return true
        }
        return if (preferencesStore.spendCoins(theme.costCoins)) {
            preferencesStore.unlockAndEquipTheme(theme)
            true
        } else {
            false
        }
    }

    suspend fun purchaseOrEquipBasket(basket: BasketDesign, unlockedBaskets: Set<String>): Boolean {
        if (unlockedBaskets.contains(basket.id)) {
            preferencesStore.equipBasket(basket)
            return true
        }
        return if (preferencesStore.spendCoins(basket.costCoins)) {
            preferencesStore.unlockAndEquipBasket(basket)
            true
        } else {
            false
        }
    }

    companion object {
        val DAILY_REWARDS = listOf(50, 75, 100, 140, 180, 230, 320)

        fun dailyRewardCoinsForDay(dayIndex1Based: Int): Int {
            return DAILY_REWARDS[(dayIndex1Based - 1).coerceIn(0, DAILY_REWARDS.lastIndex)]
        }
    }
}
