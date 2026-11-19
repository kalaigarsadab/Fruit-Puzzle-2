package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.core.app.ApplicationProvider
import com.example.data.LevelGenerator
import com.example.model.AppLanguage
import com.example.model.AppScreen
import com.example.model.GameMode
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun advanceSystemClock(ms: Long = 300L) {
        ShadowSystemClock.advanceBy(Duration.ofMillis(ms))
    }

    @Test
    fun readStringFromContext_matchesMagicSorting() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Magic Sorting", appName)
    }

    @Test
    fun gameplayButtons_pauseResumeRestartHintHome_respondImmediately() {
        composeRule.waitForIdle()

        // 1. Tap Play on Home Screen
        composeRule.onNodeWithTag("play_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // Gameplay HUD & controls appear immediately
        composeRule.onNodeWithTag("pause_button").assertIsDisplayed()
        composeRule.onNodeWithTag("hint_button").assertIsDisplayed()
        composeRule.onNodeWithTag("restart_button").assertIsDisplayed()

        // 2. Tap Hint -> free hints decrement from 2 to 1 immediately
        composeRule.onNodeWithTag("hint_button").assertTextContains("2", substring = true)
        composeRule.onNodeWithTag("hint_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hint_button").assertTextContains("1", substring = true)

        // Rapid duplicate tap within debounce window (<180ms) must NOT trigger duplicate hint
        composeRule.onNodeWithTag("hint_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hint_button").assertTextContains("1", substring = true)

        // 3. Tap Pause -> Pause overlay appears immediately
        advanceSystemClock()
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("resume_button").assertIsDisplayed()
        composeRule.onNodeWithTag("pause_restart_button").assertIsDisplayed()
        composeRule.onNodeWithTag("pause_home_button").assertIsDisplayed()

        // 4. Tap Resume -> Pause overlay closes immediately
        advanceSystemClock()
        composeRule.onNodeWithTag("resume_button").performClick()
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithTag("resume_button").fetchSemanticsNodes().isEmpty())

        // 5. Tap Restart -> resets hints back to 2 immediately
        advanceSystemClock()
        composeRule.onNodeWithTag("restart_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hint_button").assertTextContains("2", substring = true)

        // 6. Pause again & tap Home -> returns to Home Screen immediately
        advanceSystemClock()
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.waitForIdle()
        advanceSystemClock()
        composeRule.onNodeWithTag("pause_home_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("play_button").assertIsDisplayed()
    }

    @Test
    fun settingsAndRestoreButton_togglesAndRestoresDefaultsImmediately() {
        composeRule.waitForIdle()

        // Open Settings
        composeRule.onNodeWithTag("settings_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // Switch language to Spanish and toggle music off
        composeRule.onNodeWithTag("language_option_es").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("music_switch").performClick()
        composeRule.waitForIdle()

        // Tap Restore Defaults -> resets language to English & shows confirmation
        advanceSystemClock()
        composeRule.onNodeWithTag("restore_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Default settings restored!").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_back").performScrollTo()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()

        // Return Home via settings back button
        advanceSystemClock()
        composeRule.onNodeWithTag("settings_back").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("play_button").assertIsDisplayed()
    }

    @Test
    fun dragAndDropFruitOntoMatchingBasket_updatesScoreAndSortsFruit() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("play_button").performClick()
        composeRule.waitForIdle()

        val spec = LevelGenerator.generateLevel(1)
        val firstFruit = spec.fruits.first()
        val fruitNode = composeRule.onNodeWithTag("fruit_item_${firstFruit.id}")
        val basketNode = composeRule.onNodeWithTag("basket_${firstFruit.type.name.lowercase()}")

        val fruitBounds = fruitNode.fetchSemanticsNode().boundsInRoot
        val basketBounds = basketNode.fetchSemanticsNode().boundsInRoot
        val startLocal = Offset(fruitBounds.width / 2f, fruitBounds.height / 2f)
        val endLocal = Offset(
            basketBounds.center.x - fruitBounds.left,
            basketBounds.center.y - fruitBounds.top
        )

        fruitNode.performTouchInput {
            swipe(start = startLocal, end = endLocal, durationMillis = 150L)
        }
        composeRule.waitForIdle()

        // Level 1 starts with 0 score and adds +25 on correct sort
        composeRule.onNodeWithText("Score: 25", substring = true).assertIsDisplayed()
    }

    @Test
    fun viewModel_guardsRapidDuplicateActionsAndSupportsAllCoreTransitions() {
        val vm = GameViewModel(ApplicationProvider.getApplicationContext())

        advanceSystemClock(300L)
        vm.startLevel(1, GameMode.EASY)
        assertEquals(AppScreen.GAMEPLAY, vm.currentScreen.value)
        val initialState = vm.gameplayState.value
        assertNotNull(initialState)

        // Rapid duplicate startLevel within 50ms is suppressed
        advanceSystemClock(20L)
        vm.startLevel(2, GameMode.HARD)
        assertEquals(1, vm.gameplayState.value?.levelSpec?.levelNumber)

        // Pause & Resume work immediately
        vm.setPaused(true)
        assertTrue(vm.gameplayState.value?.isPaused == true)
        vm.setPaused(false)
        assertFalse(vm.gameplayState.value?.isPaused == true)

        // Hint works immediately
        vm.triggerHint()
        assertEquals(1, vm.gameplayState.value?.freeHintsLeft)
        assertNotNull(vm.gameplayState.value?.hintedFruitId)

        // Restart works after action window
        advanceSystemClock(250L)
        vm.restartCurrentLevel()
        assertEquals(2, vm.gameplayState.value?.freeHintsLeft)

        // Restore resets settings event counter and switches language back to EN
        vm.setLanguage(AppLanguage.FR)
        vm.restoreDefaultSettings()
        assertEquals(1, vm.settingsRestoredEvent.value)

        // Navigate Home
        advanceSystemClock(250L)
        vm.navigateTo(AppScreen.HOME)
        assertEquals(AppScreen.HOME, vm.currentScreen.value)

        vm.soundEngine.release()
    }
}
