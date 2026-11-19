package com.example.data

import com.example.model.Basket
import com.example.model.Fruit
import com.example.model.FruitType
import com.example.model.GameMode
import com.example.model.LevelSpec
import kotlin.random.Random

object LevelGenerator {
    const val TOTAL_LEVELS = 550
    const val LEVELS_PER_WORLD = 50
    const val TOTAL_WORLDS = TOTAL_LEVELS / LEVELS_PER_WORLD // 11 Worlds

    fun defaultModeForLevel(levelNumber: Int): GameMode {
        val clamped = levelNumber.coerceIn(1, TOTAL_LEVELS)
        return when {
            clamped <= 20 -> GameMode.EASY
            clamped % 10 == 0 -> GameMode.CHALLENGE
            clamped % 10 == 5 -> GameMode.RELAX
            clamped <= 120 -> if (clamped % 4 == 1) GameMode.EASY else GameMode.NORMAL
            clamped <= 280 -> when (clamped % 5) {
                1 -> GameMode.EASY
                2, 3 -> GameMode.NORMAL
                else -> GameMode.HARD
            }
            else -> when (clamped % 4) {
                1 -> GameMode.NORMAL
                else -> GameMode.HARD
            }
        }
    }

    fun generateLevel(levelNumber: Int, overrideMode: GameMode? = null): LevelSpec {
        val safeLevel = levelNumber.coerceIn(1, TOTAL_LEVELS)
        val mode = overrideMode ?: defaultModeForLevel(safeLevel)
        val random = Random(safeLevel * 7919 + mode.ordinal * 131)

        val fruitTypeCount = when (mode) {
            GameMode.EASY -> if (safeLevel <= 6 && overrideMode == null) 2 else random.nextInt(2, 4) // 2..3
            GameMode.NORMAL -> random.nextInt(4, 6) // 4..5
            GameMode.HARD -> random.nextInt(6, 9) // 6..8
            GameMode.RELAX -> ((safeLevel % 4) + 3).coerceIn(3, 6)
            GameMode.CHALLENGE -> ((safeLevel % 3) + 4).coerceIn(4, 6)
        }

        val allFruits = FruitType.entries.shuffled(random)
        val chosenTypes = allFruits.take(fruitTypeCount)

        val itemsPerTypeRange = when (mode) {
            GameMode.EASY -> 3..4
            GameMode.NORMAL -> 2..3
            GameMode.HARD -> 2..3
            GameMode.RELAX -> 2..4
            GameMode.CHALLENGE -> 2..3
        }

        val rawItems = mutableListOf<FruitType>()
        chosenTypes.forEach { type ->
            val count = random.nextInt(itemsPerTypeRange.first, itemsPerTypeRange.last + 1)
            repeat(count) { rawItems.add(type) }
        }
        val trimmed = if (rawItems.size > 24) {
            val balanced = mutableListOf<FruitType>()
            var idx = 0
            while (balanced.size < 24) {
                balanced.add(chosenTypes[idx % chosenTypes.size])
                idx++
            }
            balanced
        } else {
            rawItems
        }

        val shuffledFruits = trimmed.shuffled(random).mapIndexed { index, type ->
            Fruit(
                id = index + 1,
                type = type,
                color = type.primaryColor,
                softColor = type.softColor,
                darkAccent = type.darkAccent,
                assetRef = type.assetRef,
                sorted = false,
                bobOffsetPhase = (index * 0.65f) % 6.28f
            )
        }

        val countsByType = shuffledFruits.groupingBy { it.type }.eachCount()
        val baskets = chosenTypes.map { type ->
            Basket(
                id = "basket_${type.name.lowercase()}",
                fruitType = type,
                color = type.primaryColor,
                softColor = type.softColor,
                assetRef = "vector://basket/${type.name.lowercase()}",
                fruitAssetRef = type.assetRef,
                collectedCount = 0,
                targetCount = countsByType[type] ?: 0
            )
        }

        val timeLimit = if (mode.hasTimer) {
            (shuffledFruits.size * 3 + 10).coerceIn(24, 65)
        } else {
            null
        }

        val worldIndex = ((safeLevel - 1) / LEVELS_PER_WORLD) + 1

        return LevelSpec(
            levelNumber = safeLevel,
            mode = mode,
            fruitTypes = chosenTypes,
            fruits = shuffledFruits,
            baskets = baskets,
            timeLimitSeconds = timeLimit,
            worldIndex = worldIndex
        )
    }
}
