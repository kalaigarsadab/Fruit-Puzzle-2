package com.example.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.SunnyYellowDark
import com.example.ui.theme.SunnyYellowSoft

/**
 * Executes [onClick] immediately on the first tap and suppresses accidental
 * duplicate taps within [debounceWindowMs] milliseconds.
 */
@Composable
fun rememberInstantDebouncedClick(
    debounceWindowMs: Long = 220L,
    onClick: () -> Unit
): () -> Unit {
    var lastTapMs by remember { mutableLongStateOf(-1L) }
    return remember(onClick, debounceWindowMs) {
        {
            val now = SystemClock.elapsedRealtime()
            if (lastTapMs < 0L || now - lastTapMs >= debounceWindowMs) {
                lastTapMs = now
                onClick()
            }
        }
    }
}

@Composable
fun CandyButton(
    onClick: () -> Unit,
    containerColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 22.dp,
    contentColor: Color = Color.White,
    debounceMs: Long = 200L,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(stiffness = 1200f),
        label = "candy_btn_scale"
    )
    val debouncedClick = rememberInstantDebouncedClick(debounceMs, onClick)

    val effectiveContainer = if (enabled) containerColor else Color(0xFFD6CEC4)
    val effectiveShadow = if (enabled) shadowColor else Color(0xFFB5ABA0)

    // Use graphicsLayer instead of changing padding during press so there is ZERO relayout lag!
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (isPressed && enabled) 3.dp.toPx() else 0f
            }
            .defaultMinSize(minWidth = 48.dp, minHeight = 52.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(effectiveShadow)
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(effectiveContainer)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = debouncedClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun CoinPill(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val debouncedClick = onClick?.let { rememberInstantDebouncedClick(220L, it) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(CircleShape)
            .background(SunnyYellowSoft)
            .border(2.dp, SunnyYellow, CircleShape)
            .then(
                if (debouncedClick != null) {
                    Modifier.clickable(onClick = debouncedClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawCircle(SunnyYellowDark, radius = size.minDimension / 2f)
            drawCircle(SunnyYellow, radius = size.minDimension * 0.42f)
            drawCircle(Color(0xFFFFF59D), radius = size.minDimension * 0.16f, center = Offset(size.width * 0.36f, size.height * 0.35f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = DeepCocoa
        )
    }
}

@Composable
fun StarRow(
    earnedStars: Int,
    maxStars: Int = 3,
    starSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val active = i <= earnedStars
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = if (active) SunnyYellow else CreamBorder,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun RoundIconPillButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeDot: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = 1200f),
        label = "pill_btn_scale"
    )
    val debouncedClick = rememberInstantDebouncedClick(180L, onClick)

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = debouncedClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        if (badgeDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF385C))
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}
