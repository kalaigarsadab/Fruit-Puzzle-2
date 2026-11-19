package com.example

import com.example.data.LevelGenerator
import com.example.model.Basket
import com.example.model.BasketDesign
import com.example.model.Fruit
import com.example.model.FruitType
import com.example.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun fruitAndBasket_dataClassesExposeMatchingMechanicsAndAssets() {
        val apple = Fruit(id = 1, type = FruitType.APPLE)
        val appleBasket = Basket(id = "basket_apple", fruitType = FruitType.APPLE, targetCount = 2)
        val bananaBasket = Basket(
            id = "basket_banana",
            fruitType = FruitType.BANANA,
            design = BasketDesign.BERRY_PICNIC,
            collectedCount = 1,
            targetCount = 1
        )

        assertEquals(FruitType.APPLE.primaryColor, apple.color)
        assertEquals(FruitType.APPLE.softColor, apple.softColor)
        assertEquals("vector://fruit/apple", apple.assetRef)
        assertEquals("vector://basket/apple", appleBasket.assetRef)
        assertEquals("vector://fruit/apple", appleBasket.fruitAssetRef)

        assertTrue(apple.matches(appleBasket))
        assertFalse(apple.matches(bananaBasket))
        assertFalse(appleBasket.isComplete)
        assertTrue(bananaBasket.isComplete)
    }

    @Test
    fun levelGenerator_produces550ValidLevelsWithConsistentFruitsAndBaskets() {
        val seenFruits = mutableSetOf<FruitType>()
        for (level in 1..LevelGenerator.TOTAL_LEVELS) {
            val spec = LevelGenerator.generateLevel(level)
            assertEquals(level, spec.levelNumber)
            assertTrue(spec.fruitTypes.size in 2..8)
            assertTrue(spec.fruits.isNotEmpty())
            assertTrue(spec.fruits.all { it.type in spec.fruitTypes })
            assertEquals(spec.fruitTypes.size, spec.baskets.size)
            assertEquals(spec.fruits.size, spec.baskets.sumOf { it.targetCount })
            spec.baskets.forEach { basket ->
                val expectedCount = spec.fruits.count { it.matches(basket) }
                assertEquals(expectedCount, basket.targetCount)
            }
            seenFruits.addAll(spec.fruitTypes)
        }
        assertEquals(FruitType.entries.toSet(), seenFruits)
    }

    @Test
    fun levelGenerator_respectsGameModeRules() {
        val easy = LevelGenerator.generateLevel(10, GameMode.EASY)
        assertTrue(easy.fruitTypes.size in 2..3)
        assertNull(easy.timeLimitSeconds)

        val normal = LevelGenerator.generateLevel(10, GameMode.NORMAL)
        assertTrue(normal.fruitTypes.size in 4..5)
        assertNull(normal.timeLimitSeconds)

        val hard = LevelGenerator.generateLevel(10, GameMode.HARD)
        assertTrue(hard.fruitTypes.size in 6..8)
        assertNull(hard.timeLimitSeconds)

        val relax = LevelGenerator.generateLevel(10, GameMode.RELAX)
        assertNull(relax.timeLimitSeconds)

        val challenge = LevelGenerator.generateLevel(10, GameMode.CHALLENGE)
        assertNotNull(challenge.timeLimitSeconds)
    }
}
