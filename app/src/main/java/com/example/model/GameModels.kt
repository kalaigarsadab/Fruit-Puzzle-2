package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleSoft
import com.example.ui.theme.BananaSoft
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.BerryPink
import com.example.ui.theme.BerryPinkSoft
import com.example.ui.theme.CherryCrimson
import com.example.ui.theme.CherrySoft
import com.example.ui.theme.GrapesPurple
import com.example.ui.theme.GrapesSoft
import com.example.ui.theme.KiwiLime
import com.example.ui.theme.KiwiSoft
import com.example.ui.theme.MagicPurple
import com.example.ui.theme.MagicPurpleSoft
import com.example.ui.theme.MangoCoral
import com.example.ui.theme.MangoSoft
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenSoft
import com.example.ui.theme.OrangeFruit
import com.example.ui.theme.OrangeSoft
import com.example.ui.theme.PineappleGold
import com.example.ui.theme.PineappleSoft
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoft
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.SunnyYellowSoft
import com.example.ui.theme.TangerineOrange
import com.example.ui.theme.TangerineOrangeSoft
import com.example.ui.theme.WatermelonPink
import com.example.ui.theme.WatermelonSoft

@Immutable
enum class FruitType(
    val primaryColor: Color,
    val softColor: Color,
    val darkAccent: Color,
    val assetRef: String
) {
    APPLE(AppleRed, AppleSoft, Color(0xFFD62839), "vector://fruit/apple"),
    BANANA(BananaYellow, BananaSoft, Color(0xFFD99B00), "vector://fruit/banana"),
    ORANGE(OrangeFruit, OrangeSoft, Color(0xFFD65E00), "vector://fruit/orange"),
    GRAPES(GrapesPurple, GrapesSoft, Color(0xFF632BD9), "vector://fruit/grapes"),
    WATERMELON(WatermelonPink, WatermelonSoft, Color(0xFF28B463), "vector://fruit/watermelon"),
    STRAWBERRY(StrawberryRed, StrawberrySoft, Color(0xFFD11F4B), "vector://fruit/strawberry"),
    PINEAPPLE(PineappleGold, PineappleSoft, Color(0xFFD98700), "vector://fruit/pineapple"),
    MANGO(MangoCoral, MangoSoft, Color(0xFFD85225), "vector://fruit/mango"),
    CHERRY(CherryCrimson, CherrySoft, Color(0xFFB3193F), "vector://fruit/cherry"),
    KIWI(KiwiLime, KiwiSoft, Color(0xFF8C5A3C), "vector://fruit/kiwi")
}

@Immutable
enum class GameMode(
    val accentColor: Color,
    val softColor: Color,
    val coinMultiplier: Int,
    val hasTimer: Boolean
) {
    EASY(MintGreen, MintGreenSoft, 1, false),
    NORMAL(SkyBlue, SkyBlueSoft, 1, false),
    HARD(MagicPurple, MagicPurpleSoft, 2, false),
    RELAX(BerryPink, BerryPinkSoft, 1, false),
    CHALLENGE(TangerineOrange, TangerineOrangeSoft, 2, true)
}

@Immutable
enum class FruitTheme(
    val id: String,
    val costCoins: Int,
    val accentColor: Color,
    val softColor: Color
) {
    SUNNY_ORCHARD("sunny_orchard", 0, SunnyYellow, SunnyYellowSoft),
    SPARKLE_PARTY("sparkle_party", 120, BerryPink, BerryPinkSoft),
    PASTEL_DREAM("pastel_dream", 220, MintGreen, MintGreenSoft),
    ROYAL_CROWN("royal_crown", 360, MagicPurple, MagicPurpleSoft)
}

@Immutable
enum class BasketDesign(
    val id: String,
    val costCoins: Int,
    val trimColor: Color,
    val weaveColor: Color
) {
    CLASSIC_WICKER("classic_wicker", 0, Color(0xFFD79C56), Color(0xFFB87935)),
    BERRY_PICNIC("berry_picnic", 150, BerryPink, Color(0xFFFF89B5)),
    ROYAL_RIBBON("royal_ribbon", 260, MagicPurple, Color(0xFFB094FF)),
    STARLIGHT_MAGIC("starlight_magic", 400, SunnyYellow, Color(0xFF6A48D7))
}

enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    ES("es", "Español"),
    FR("fr", "Français"),
    HI("hi", "हिन्दी"),
    TA("ta", "தமிழ்");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: EN
    }
}

enum class FruitExpression {
    HAPPY,
    EXCITED,
    BLINK,
    DIZZY
}

/**
 * Core Fruit game object containing fruit type, color palette, asset reference,
 * and sorting state for matching mechanics.
 */
@Immutable
data class Fruit(
    val id: Int,
    val type: FruitType,
    val color: Color = type.primaryColor,
    val softColor: Color = type.softColor,
    val darkAccent: Color = type.darkAccent,
    val assetRef: String = type.assetRef,
    val sorted: Boolean = false,
    val bobOffsetPhase: Float = 0f
) {
    fun matches(basket: Basket): Boolean = type == basket.fruitType
}

typealias FruitItem = Fruit

/**
 * Core Basket game object containing target fruit type, theme colors,
 * asset reference, and live collection counts.
 */
@Immutable
data class Basket(
    val id: String,
    val fruitType: FruitType,
    val color: Color = fruitType.primaryColor,
    val softColor: Color = fruitType.softColor,
    val assetRef: String = "vector://basket/${fruitType.name.lowercase()}",
    val fruitAssetRef: String = fruitType.assetRef,
    val design: BasketDesign = BasketDesign.CLASSIC_WICKER,
    val collectedCount: Int = 0,
    val targetCount: Int = 0
) {
    val isComplete: Boolean get() = collectedCount >= targetCount
}

@Immutable
data class LevelSpec(
    val levelNumber: Int,
    val mode: GameMode,
    val fruitTypes: List<FruitType>,
    val fruits: List<Fruit>,
    val baskets: List<Basket>,
    val timeLimitSeconds: Int?,
    val worldIndex: Int
)

@Immutable
data class LevelProgress(
    val levelNumber: Int,
    val unlocked: Boolean,
    val stars: Int,
    val bestScore: Int
)

@Immutable
data class LevelResult(
    val levelNumber: Int,
    val mode: GameMode,
    val starsEarned: Int,
    val score: Int,
    val coinsEarned: Int,
    val mistakes: Int,
    val timeBonus: Int
)

enum class AppScreen {
    HOME,
    LEVEL_SELECT,
    GAMEPLAY,
    LEVEL_COMPLETE,
    SETTINGS
}
