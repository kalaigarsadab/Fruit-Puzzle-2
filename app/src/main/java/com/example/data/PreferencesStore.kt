package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.BasketDesign
import com.example.model.FruitTheme
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "magic_sorting_prefs")

@Immutable
data class PlayerPrefs(
    val coins: Int = 100,
    val highestUnlockedLevel: Int = 1,
    val musicEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.EN,
    val dailyStreak: Int = 0,
    val lastClaimEpochDay: Long = -1L,
    val unlockedThemes: Set<String> = setOf(FruitTheme.SUNNY_ORCHARD.id),
    val equippedTheme: FruitTheme = FruitTheme.SUNNY_ORCHARD,
    val unlockedBaskets: Set<String> = setOf(BasketDesign.CLASSIC_WICKER.id),
    val equippedBasket: BasketDesign = BasketDesign.CLASSIC_WICKER
)

class PreferencesStore(private val context: Context) {
    private object Keys {
        val COINS = intPreferencesKey("coins")
        val HIGHEST_LEVEL = intPreferencesKey("highest_unlocked_level")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val LANGUAGE = stringPreferencesKey("language_code")
        val DAILY_STREAK = intPreferencesKey("daily_streak")
        val LAST_CLAIM_DAY = longPreferencesKey("last_claim_epoch_day")
        val UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
        val EQUIPPED_THEME = stringPreferencesKey("equipped_theme")
        val UNLOCKED_BASKETS = stringSetPreferencesKey("unlocked_baskets")
        val EQUIPPED_BASKET = stringPreferencesKey("equipped_basket")
    }

    val prefsFlow: Flow<PlayerPrefs> = context.dataStore.data.map { prefs ->
        val themeId = prefs[Keys.EQUIPPED_THEME] ?: FruitTheme.SUNNY_ORCHARD.id
        val basketId = prefs[Keys.EQUIPPED_BASKET] ?: BasketDesign.CLASSIC_WICKER.id
        PlayerPrefs(
            coins = prefs[Keys.COINS] ?: 100,
            highestUnlockedLevel = prefs[Keys.HIGHEST_LEVEL] ?: 1,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: "en"),
            dailyStreak = prefs[Keys.DAILY_STREAK] ?: 0,
            lastClaimEpochDay = prefs[Keys.LAST_CLAIM_DAY] ?: -1L,
            unlockedThemes = prefs[Keys.UNLOCKED_THEMES] ?: setOf(FruitTheme.SUNNY_ORCHARD.id),
            equippedTheme = FruitTheme.entries.firstOrNull { it.id == themeId } ?: FruitTheme.SUNNY_ORCHARD,
            unlockedBaskets = prefs[Keys.UNLOCKED_BASKETS] ?: setOf(BasketDesign.CLASSIC_WICKER.id),
            equippedBasket = BasketDesign.entries.firstOrNull { it.id == basketId } ?: BasketDesign.CLASSIC_WICKER
        )
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: 100
            prefs[Keys.COINS] = (current + amount).coerceAtLeast(0)
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: 100
            if (current >= amount) {
                prefs[Keys.COINS] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun updateHighestLevel(level: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIGHEST_LEVEL] ?: 1
            if (level > current) {
                prefs[Keys.HIGHEST_LEVEL] = level.coerceAtMost(LevelGenerator.TOTAL_LEVELS)
            }
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun recordDailyClaim(epochDay: Long, newStreak: Int, rewardCoins: Int) {
        context.dataStore.edit { prefs ->
            val currentCoins = prefs[Keys.COINS] ?: 100
            prefs[Keys.COINS] = currentCoins + rewardCoins
            prefs[Keys.DAILY_STREAK] = newStreak
            prefs[Keys.LAST_CLAIM_DAY] = epochDay
            if (newStreak >= 7) {
                val existingThemes = prefs[Keys.UNLOCKED_THEMES] ?: setOf(FruitTheme.SUNNY_ORCHARD.id)
                prefs[Keys.UNLOCKED_THEMES] = existingThemes + FruitTheme.SPARKLE_PARTY.id
            }
        }
    }

    suspend fun unlockAndEquipTheme(theme: FruitTheme) {
        context.dataStore.edit { prefs ->
            val existing = prefs[Keys.UNLOCKED_THEMES] ?: setOf(FruitTheme.SUNNY_ORCHARD.id)
            prefs[Keys.UNLOCKED_THEMES] = existing + theme.id
            prefs[Keys.EQUIPPED_THEME] = theme.id
        }
    }

    suspend fun equipTheme(theme: FruitTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_THEME] = theme.id
        }
    }

    suspend fun unlockAndEquipBasket(basket: BasketDesign) {
        context.dataStore.edit { prefs ->
            val existing = prefs[Keys.UNLOCKED_BASKETS] ?: setOf(BasketDesign.CLASSIC_WICKER.id)
            prefs[Keys.UNLOCKED_BASKETS] = existing + basket.id
            prefs[Keys.EQUIPPED_BASKET] = basket.id
        }
    }

    suspend fun equipBasket(basket: BasketDesign) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_BASKET] = basket.id
        }
    }

    suspend fun restoreDefaultSettings() {
        context.dataStore.edit { prefs ->
            prefs[Keys.MUSIC_ENABLED] = true
            prefs[Keys.SOUND_ENABLED] = true
            prefs[Keys.VIBRATION_ENABLED] = true
            prefs[Keys.LANGUAGE] = AppLanguage.EN.code
            prefs[Keys.EQUIPPED_THEME] = FruitTheme.SUNNY_ORCHARD.id
            prefs[Keys.EQUIPPED_BASKET] = BasketDesign.CLASSIC_WICKER.id
        }
    }
}
