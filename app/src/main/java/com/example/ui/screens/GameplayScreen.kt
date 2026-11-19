package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerPrefs
import com.example.localization.LocalizedStrings
import com.example.model.Fruit
import com.example.model.FruitExpression
import com.example.model.FruitTheme
import com.example.model.FruitType
import com.example.ui.components.CandyButton
import com.example.ui.components.CartoonBasketCard
import com.example.ui.components.CartoonFruitIllustration
import com.example.ui.components.CoinPill
import com.example.ui.components.ParticleOverlay
import com.example.ui.components.RoundIconPillButton
import com.example.ui.components.StarRow
import com.example.ui.theme.AppleRed
import com.example.ui.theme.BerryPink
import com.example.ui.theme.BerryPinkDark
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.MagicPurple
import com.example.ui.theme.MagicPurpleDark
import com.example.ui.theme.MagicPurpleSoft
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SoftCocoa
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.SunnyYellowDark
import com.example.ui.theme.SunnyYellowSoft
import com.example.ui.theme.TangerineOrange
import com.example.ui.theme.TangerineOrangeSoft
import com.example.viewmodel.FloatingPopup
import com.example.viewmodel.GameplayUiState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Stable
private class FruitDragController {
    var draggedFruit by mutableStateOf<Fruit?>(null)
    var pointerRootOffset by mutableStateOf(Offset.Zero)

    fun start(fruit: Fruit, startRoot: Offset) {
        draggedFruit = fruit
        pointerRootOffset = startRoot
    }

    fun move(delta: Offset) {
        pointerRootOffset += delta
    }

    fun end(): Pair<Fruit, Offset>? {
        val f = draggedFruit ?: return null
        val pos = pointerRootOffset
        draggedFruit = null
        return f to pos
    }

    fun cancel() {
        draggedFruit = null
    }
}

@Composable
fun GameplayScreen(
    state: GameplayUiState,
    prefs: PlayerPrefs,
    strings: LocalizedStrings,
    onFruitPickup: () -> Unit,
    onFruitDroppedOnBasket: (fruitId: Int, basketType: FruitType, basketCenter: Offset) -> Unit,
    onHintClick: () -> Unit,
    onRestartClick: () -> Unit,
    onPauseToggle: (Boolean) -> Unit,
    onGoHome: () -> Unit
) {
    BackHandler {
        if (!state.isTimeUp && !state.isCompleting) {
            onPauseToggle(!state.isPaused)
        } else {
            onGoHome()
        }
    }

    val spec = state.levelSpec
    val basketBounds = remember(spec.levelNumber, spec.mode) { mutableStateMapOf<FruitType, Rect>() }
    val fruitCenters = remember(spec.levelNumber, spec.mode) { mutableStateMapOf<Int, Offset>() }
    var rootOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    val dragController = remember(spec.levelNumber, spec.mode) { FruitDragController() }

    // ONE shared infinite phase instead of 24 per-fruit InfiniteTransitions!
    val sharedTransition = rememberInfiniteTransition(label = "shared_fruit_bob")
    val sharedBobPhase = sharedTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bob_phase"
    )

    // Blink timer for lively cartoon fruits
    var blinkActive by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2800L)
            blinkActive = true
            delay(140L)
            blinkActive = false
        }
    }

    // Derived state so pointer movement only updates hover when crossing basket boundaries
    val hoveredBasketType by remember(dragController, basketBounds) {
        derivedStateOf {
            if (dragController.draggedFruit == null) return@derivedStateOf null
            val pos = dragController.pointerRootOffset
            basketBounds.entries.firstOrNull { (_, rect) ->
                rect.inflate(28f).contains(pos)
            }?.key
        }
    }

    val density = LocalDensity.current
    val dragFruitHalfPx = with(density) { 40.dp.toPx() }
    val bobAmplitudePx = with(density) { 3.5.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CreamBackground, Color(0xFFFFF2DF), Color(0xFFFFF8EE))
                )
            )
            .onGloballyPositioned { coords ->
                rootOriginInWindow = coords.positionInRoot()
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconPillButton(
                    icon = Icons.Rounded.Pause,
                    contentDescription = strings.pauseTitle,
                    backgroundColor = MagicPurpleSoft,
                    iconTint = MagicPurpleDark,
                    onClick = { onPauseToggle(true) },
                    modifier = Modifier.testTag("pause_button")
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.levelLabel(spec.levelNumber),
                            style = MaterialTheme.typography.titleLarge,
                            color = DeepCocoa
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(spec.mode.softColor)
                                .border(1.dp, spec.mode.accentColor, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.modeNames[spec.mode] ?: spec.mode.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepCocoa
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StarRow(earnedStars = state.liveStarsEstimate, starSize = 16.dp)
                        Text(
                            text = "${strings.scoreLabel}: ${state.score}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftCocoa
                        )
                    }
                }

                if (state.remainingSeconds != null) {
                    val lowTime = state.remainingSeconds <= 10
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (lowTime) Color(0xFFFFE0E6) else TangerineOrangeSoft)
                            .border(
                                2.dp,
                                if (lowTime) AppleRed else TangerineOrange,
                                CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("timer_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = if (lowTime) AppleRed else TangerineOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.timeLeftLabel(state.remainingSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (lowTime) AppleRed else DeepCocoa
                        )
                    }
                } else {
                    CoinPill(coins = prefs.coins)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Animated Progress Bar
            val progressAnim by animateFloatAsState(
                targetValue = state.progressFraction,
                animationSpec = spring(stiffness = 380f),
                label = "progress_bar"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(CreamBorder.copy(alpha = 0.65f))
                        .border(1.5.dp, CreamBorder, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressAnim.coerceIn(0f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(listOf(MintGreen, MagicPurple))
                            )
                    )
                }
                Text(
                    text = "${state.sortedCount}/${state.totalFruits}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepCocoa
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Friendly Tip / Hint Banner
            val hintName = state.hintedFruitType?.let { strings.fruitNames[it] }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (hintName != null) SunnyYellowSoft else Color.White.copy(alpha = 0.75f))
                    .border(
                        1.dp,
                        if (hintName != null) SunnyYellow else CreamBorder,
                        CircleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (hintName != null) strings.hintTip(hintName) else strings.dragTip,
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepCocoa,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fruit Sorting Area (Meadow Tray)
            val cols = when {
                state.totalFruits <= 6 -> 3
                state.totalFruits <= 16 -> 4
                else -> 5
            }
            val fruitSize = when (cols) {
                3 -> 72.dp
                4 -> 62.dp
                else -> 54.dp
            }
            val rows = remember(state.fruits, cols) { state.fruits.chunked(cols) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.05f)
                    .shadow(4.dp, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(CreamSurface.copy(alpha = 0.92f))
                    .border(2.dp, CreamBorder, RoundedCornerShape(26.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowItems.forEach { item ->
                                val isBeingDragged = dragController.draggedFruit?.id == item.id
                                val isHinted = state.hintedFruitId == item.id
                                val isDizzy = state.dizzyFruitId == item.id

                                DraggableFruitSlot(
                                    item = item,
                                    fruitSize = fruitSize,
                                    theme = prefs.equippedTheme,
                                    sharedBobPhase = sharedBobPhase,
                                    bobAmplitudePx = bobAmplitudePx,
                                    isBeingDragged = isBeingDragged,
                                    isHinted = isHinted,
                                    isDizzy = isDizzy,
                                    blinkActive = blinkActive,
                                    enabled = !state.isPaused && !state.isTimeUp && !state.isCompleting,
                                    onCenterPositioned = { center ->
                                        fruitCenters[item.id] = center
                                    },
                                    onDragStart = { startRootOffset ->
                                        if (dragController.draggedFruit == null) {
                                            onFruitPickup()
                                            dragController.start(item, startRootOffset)
                                        }
                                    },
                                    onDragMove = { delta ->
                                        if (dragController.draggedFruit?.id == item.id) {
                                            dragController.move(delta)
                                        }
                                    },
                                    onDragEnd = {
                                        val completed = dragController.end()
                                        if (completed != null) {
                                            val (fruit, dropOffset) = completed
                                            val matchedEntry = basketBounds.entries.firstOrNull { (_, rect) ->
                                                rect.inflate(28f).contains(dropOffset)
                                            }
                                            if (matchedEntry != null) {
                                                onFruitDroppedOnBasket(
                                                    fruit.id,
                                                    matchedEntry.key,
                                                    matchedEntry.value.center
                                                )
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        dragController.cancel()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Matching Baskets Area
            val basketCols = when {
                state.baskets.size <= 3 -> state.baskets.size
                state.baskets.size == 4 -> 2
                state.baskets.size <= 6 -> 3
                else -> 4
            }
            val basketRows = remember(state.baskets, basketCols) { state.baskets.chunked(basketCols) }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                basketRows.forEach { rowBaskets ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowBaskets.forEach { basket ->
                            val fruitType = basket.fruitType
                            CartoonBasketCard(
                                basket = basket,
                                fruitName = strings.fruitNames[fruitType] ?: fruitType.name,
                                basketDesign = prefs.equippedBasket,
                                fruitTheme = prefs.equippedTheme,
                                isHovered = hoveredBasketType == fruitType,
                                isHintTarget = state.hintedFruitType == fruitType,
                                matchPulseTrigger = state.basketPulseCounts[fruitType] ?: 0,
                                wrongShakeTrigger = state.basketShakeCounts[fruitType] ?: 0,
                                onBoundsChanged = { rect ->
                                    basketBounds[fruitType] = rect
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("basket_${fruitType.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Hint & Restart Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CandyButton(
                    onClick = onHintClick,
                    containerColor = SunnyYellow,
                    shadowColor = SunnyYellowDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("hint_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = DeepCocoa,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val badge = if (state.freeHintsLeft > 0) "${state.freeHintsLeft}" else "15c"
                    Text(
                        text = "${strings.hintButton} ($badge)",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepCocoa
                    )
                }

                CandyButton(
                    onClick = onRestartClick,
                    containerColor = BerryPink,
                    shadowColor = BerryPinkDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.restartButton,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }

        // Hint trajectory line overlay
        val hintedFruitPos = state.hintedFruitId?.let { fruitCenters[it] }
        val hintedBasketPos = state.hintedFruitType?.let { basketBounds[it]?.center }
        if (hintedFruitPos != null && hintedBasketPos != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = SunnyYellow,
                    start = hintedFruitPos - rootOriginInWindow,
                    end = hintedBasketPos - rootOriginInWindow,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                )
            }
        }

        // Isolated Floating Drag Overlay (reads drag state inside graphicsLayer -> zero screen recomposition!)
        FloatingDraggedFruitLayer(
            dragController = dragController,
            rootOriginProvider = { rootOriginInWindow },
            dragFruitHalfPx = dragFruitHalfPx,
            theme = prefs.equippedTheme
        )

        // Particle celebration & match burst overlay
        ParticleOverlay(
            burstRequest = state.burstRequest?.let {
                it.copy(origin = it.origin - rootOriginInWindow)
            }
        )

        // Floating "+25" score popup
        state.floatingPopup?.let { popup ->
            FloatingScoreTag(
                popup = popup,
                rootOrigin = rootOriginInWindow
            )
        }

        // Pause Overlay
        AnimatedVisibility(
            visible = state.isPaused && !state.isTimeUp,
            enter = fadeIn(tween(110)),
            exit = fadeOut(tween(90))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x882E1E1A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = 330.dp)
                        .fillMaxWidth(0.84f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(CreamSurface)
                        .border(3.dp, CreamBorder, RoundedCornerShape(30.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = strings.pauseTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepCocoa
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    CandyButton(
                        onClick = { onPauseToggle(false) },
                        containerColor = MintGreen,
                        shadowColor = MintGreenDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_button")
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.resumeButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    CandyButton(
                        onClick = onRestartClick,
                        containerColor = MagicPurple,
                        shadowColor = MagicPurpleDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pause_restart_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.restartButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    CandyButton(
                        onClick = onGoHome,
                        containerColor = BerryPink,
                        shadowColor = BerryPinkDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pause_home_button")
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.homeButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }

        // Time's Up Overlay (Challenge Mode)
        AnimatedVisibility(
            visible = state.isTimeUp,
            enter = fadeIn(tween(110)),
            exit = fadeOut(tween(90))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x992E1E1A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = 330.dp)
                        .fillMaxWidth(0.84f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(CreamSurface)
                        .border(3.dp, CreamBorder, RoundedCornerShape(30.dp))
                        .padding(24.dp)
                ) {
                    CartoonFruitIllustration(
                        fruitType = spec.fruitTypes.first(),
                        size = 76.dp,
                        expression = FruitExpression.DIZZY,
                        theme = prefs.equippedTheme
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.timeUpTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppleRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.timeUpSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftCocoa,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    CandyButton(
                        onClick = onRestartClick,
                        containerColor = MintGreen,
                        shadowColor = MintGreenDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("try_again_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.tryAgainButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    CandyButton(
                        onClick = onGoHome,
                        containerColor = MagicPurple,
                        shadowColor = MagicPurpleDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timeup_home_button")
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.homeButton, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingDraggedFruitLayer(
    dragController: FruitDragController,
    rootOriginProvider: () -> Offset,
    dragFruitHalfPx: Float,
    theme: FruitTheme
) {
    val fruit = dragController.draggedFruit ?: return
    Box(
        modifier = Modifier.graphicsLayer {
            val localPos = dragController.pointerRootOffset - rootOriginProvider()
            translationX = localPos.x - dragFruitHalfPx
            translationY = localPos.y - dragFruitHalfPx
            scaleX = 1.20f
            scaleY = 1.20f
        }
    ) {
        CartoonFruitIllustration(
            fruitType = fruit.type,
            size = 80.dp,
            expression = FruitExpression.EXCITED,
            theme = theme
        )
    }
}

@Composable
private fun DraggableFruitSlot(
    item: Fruit,
    fruitSize: Dp,
    theme: FruitTheme,
    sharedBobPhase: State<Float>,
    bobAmplitudePx: Float,
    isBeingDragged: Boolean,
    isHinted: Boolean,
    isDizzy: Boolean,
    blinkActive: Boolean,
    enabled: Boolean,
    onCenterPositioned: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = when {
            item.sorted -> 0f
            isHinted -> 1.15f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "fruit_slot_scale"
    )

    var slotCenter by remember { mutableStateOf(Offset.Zero) }
    val enabledState by rememberUpdatedState(enabled)
    val sortedState by rememberUpdatedState(item.sorted)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(fruitSize + 10.dp)
            .onGloballyPositioned { coords ->
                val center = coords.boundsInRoot().center
                slotCenter = center
                onCenterPositioned(center)
            }
            // Instant zero-touch-slop pickup on pointer down!
            .pointerInput(item.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (sortedState || !enabledState) return@awaitEachGesture
                    down.consume()

                    val halfW = size.width / 2f
                    val halfH = size.height / 2f
                    val startRoot = Offset(
                        slotCenter.x - halfW + down.position.x,
                        slotCenter.y - halfH + down.position.y
                    )
                    onDragStart(startRoot)

                    val pointerId = down.id
                    var finished = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == pointerId }
                            if (change == null || change.changedToUpIgnoreConsumed()) {
                                change?.consume()
                                finished = true
                                onDragEnd()
                                break
                            }
                            val delta = change.positionChange()
                            if (delta != Offset.Zero) {
                                change.consume()
                                onDragMove(delta)
                            }
                        }
                    } finally {
                        if (!finished) {
                            onDragCancel()
                        }
                    }
                }
            }
            .testTag("fruit_item_${item.id}")
    ) {
        if (!item.sorted && !isBeingDragged) {
            if (isHinted) {
                Box(
                    modifier = Modifier
                        .size(fruitSize + 8.dp)
                        .clip(CircleShape)
                        .background(SunnyYellow.copy(alpha = 0.28f))
                        .border(2.5.dp, SunnyYellow, CircleShape)
                )
            }
            val expr = when {
                isDizzy -> FruitExpression.DIZZY
                blinkActive -> FruitExpression.BLINK
                else -> FruitExpression.HAPPY
            }
            // Pure GPU transform: reads sharedBobPhase ONLY inside graphicsLayer!
            CartoonFruitIllustration(
                fruitType = item.type,
                size = fruitSize,
                expression = expr,
                theme = theme,
                modifier = Modifier.graphicsLayer {
                    translationY = sin(sharedBobPhase.value + item.bobOffsetPhase) * bobAmplitudePx
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
    }
}

@Composable
private fun FloatingScoreTag(
    popup: FloatingPopup,
    rootOrigin: Offset
) {
    val yAnim = remember(popup.id) { Animatable(0f) }
    val alphaAnim = remember(popup.id) { Animatable(1f) }

    LaunchedEffect(popup.id) {
        yAnim.animateTo(-70f, tween(650))
    }
    LaunchedEffect(popup.id) {
        delay(220L)
        alphaAnim.animateTo(0f, tween(430))
    }

    val local = popup.position - rootOrigin
    Text(
        text = popup.text,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
        fontWeight = FontWeight.ExtraBold,
        color = MintGreenDark,
        modifier = Modifier.graphicsLayer {
            translationX = local.x - 36f
            translationY = local.y + yAnim.value
            alpha = alphaAnim.value
        }
    )
}
