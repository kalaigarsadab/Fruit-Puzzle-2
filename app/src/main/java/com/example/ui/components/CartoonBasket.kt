package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Basket
import com.example.model.BasketDesign
import com.example.model.FruitExpression
import com.example.model.FruitTheme
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenSoft

@Composable
fun CartoonBasketCard(
    basket: Basket,
    fruitName: String,
    basketDesign: BasketDesign,
    fruitTheme: FruitTheme,
    isHovered: Boolean,
    isHintTarget: Boolean,
    matchPulseTrigger: Int,
    wrongShakeTrigger: Int,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val isComplete = basket.isComplete
    val fruitType = basket.fruitType

    val scaleAnim = remember { Animatable(1f) }
    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(matchPulseTrigger) {
        if (matchPulseTrigger > 0) {
            scaleAnim.animateTo(1.14f, tween(95))
            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    LaunchedEffect(wrongShakeTrigger) {
        if (wrongShakeTrigger > 0) {
            val offsets = floatArrayOf(-15f, 15f, -11f, 11f, -5f, 5f, 0f)
            for (target in offsets) {
                shakeAnim.animateTo(target, tween(40))
            }
        }
    }

    val hoverScale by animateFloatAsState(
        targetValue = when {
            isHovered -> 1.08f
            isHintTarget -> 1.05f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "basket_hover"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isHovered -> MintGreen
            isHintTarget -> Color(0xFFFFC72C)
            isComplete -> MintGreen
            else -> basket.color.copy(alpha = 0.55f)
        },
        animationSpec = tween(120),
        label = "basket_border"
    )

    val cardBg by animateColorAsState(
        targetValue = when {
            isHovered -> MintGreenSoft
            isComplete -> MintGreenSoft.copy(alpha = 0.72f)
            else -> basket.softColor
        },
        animationSpec = tween(120),
        label = "basket_bg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .onGloballyPositioned { coords ->
                onBoundsChanged(coords.boundsInRoot())
            }
            .graphicsLayer {
                translationX = shakeAnim.value
                val totalScale = scaleAnim.value * hoverScale
                scaleX = totalScale
                scaleY = totalScale
            }
            .shadow(
                elevation = if (isHovered || isHintTarget) 10.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = basket.color.copy(alpha = 0.25f),
                spotColor = basket.color.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(
                width = if (isHovered || isHintTarget) 3.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // Top row: Count badge + Check if complete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isComplete) MintGreen else basket.color.copy(alpha = 0.18f)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${basket.collectedCount}/${basket.targetCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isComplete) Color.White else DeepCocoa,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (isComplete) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MintGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Basket Illustration with Mini Fruit Badge sitting inside
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(66.dp)
        ) {
            CartoonFruitIllustration(
                fruitType = fruitType,
                size = 38.dp,
                expression = if (isHovered || matchPulseTrigger > 0) FruitExpression.EXCITED else FruitExpression.HAPPY,
                theme = fruitTheme,
                modifier = Modifier.offset(y = (-13).dp)
            )

            BasketArtwork(
                fruitColor = basket.color,
                design = basketDesign,
                modifier = Modifier
                    .size(width = 78.dp, height = 42.dp)
                    .offset(y = 11.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = fruitName,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            fontWeight = FontWeight.ExtraBold,
            color = DeepCocoa,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BasketArtwork(
    fruitColor: Color,
    design: BasketDesign,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val outline = Color(0xFF4A2E20)
            val outlineStroke = Stroke(width = 2.4f)

            val body = Path().apply {
                moveTo(w * 0.08f, h * 0.24f)
                lineTo(w * 0.92f, h * 0.24f)
                lineTo(w * 0.80f, h * 0.94f)
                quadraticTo(w * 0.50f, h * 1.02f, w * 0.20f, h * 0.94f)
                close()
            }

            val fillColor = when (design) {
                BasketDesign.CLASSIC_WICKER -> Color(0xFFE7B673)
                BasketDesign.BERRY_PICNIC -> Color(0xFFFFD1E1)
                BasketDesign.ROYAL_RIBBON -> Color(0xFFDCD0FF)
                BasketDesign.STARLIGHT_MAGIC -> Color(0xFF5D3FD3)
            }

            val stripeColor = when (design) {
                BasketDesign.CLASSIC_WICKER -> Color(0x669C6324)
                BasketDesign.BERRY_PICNIC -> Color(0x88FF4D8D)
                BasketDesign.ROYAL_RIBBON -> fruitColor.copy(alpha = 0.45f)
                BasketDesign.STARLIGHT_MAGIC -> Color(0xAAFFD54F)
            }

            val rimColor = when (design) {
                BasketDesign.CLASSIC_WICKER -> Color(0xFFC8873E)
                BasketDesign.BERRY_PICNIC -> Color(0xFFFF5E96)
                BasketDesign.ROYAL_RIBBON -> fruitColor
                BasketDesign.STARLIGHT_MAGIC -> Color(0xFFFFC72C)
            }

            val rimCorner = CornerRadius(h * 0.14f, h * 0.14f)
            val rimTopLeft = Offset(w * 0.02f, h * 0.04f)
            val rimSize = Size(w * 0.96f, h * 0.28f)
            val emblemCenter = Offset(w * 0.50f, h * 0.62f)

            onDrawBehind {
                drawPath(body, fillColor)
                drawLine(
                    color = stripeColor,
                    start = Offset(w * 0.14f, h * 0.56f),
                    end = Offset(w * 0.86f, h * 0.56f),
                    strokeWidth = h * 0.09f
                )
                drawLine(stripeColor, Offset(w * 0.30f, h * 0.30f), Offset(w * 0.344f, h * 0.92f), strokeWidth = w * 0.045f)
                drawLine(stripeColor, Offset(w * 0.50f, h * 0.30f), Offset(w * 0.500f, h * 0.92f), strokeWidth = w * 0.045f)
                drawLine(stripeColor, Offset(w * 0.70f, h * 0.30f), Offset(w * 0.656f, h * 0.92f), strokeWidth = w * 0.045f)

                drawPath(body, outline, style = outlineStroke)

                drawRoundRect(
                    color = rimColor,
                    topLeft = rimTopLeft,
                    size = rimSize,
                    cornerRadius = rimCorner
                )
                drawRoundRect(
                    color = outline,
                    topLeft = rimTopLeft,
                    size = rimSize,
                    cornerRadius = rimCorner,
                    style = outlineStroke
                )

                drawCircle(color = Color.White, radius = h * 0.14f, center = emblemCenter)
                drawCircle(color = fruitColor, radius = h * 0.10f, center = emblemCenter)
            }
        }
    )
}
