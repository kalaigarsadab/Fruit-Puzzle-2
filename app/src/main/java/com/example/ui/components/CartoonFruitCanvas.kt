package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.FruitExpression
import com.example.model.FruitTheme
import com.example.model.FruitType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CartoonFruitIllustration(
    fruitType: FruitType,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    expression: FruitExpression = FruitExpression.HAPPY,
    theme: FruitTheme = FruitTheme.SUNNY_ORCHARD
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Theme Aura
        when (theme) {
            FruitTheme.SPARKLE_PARTY -> {
                drawSparkle(Offset(w * 0.16f, h * 0.22f), w * 0.08f, Color(0xFFFFD54F))
                drawSparkle(Offset(w * 0.85f, h * 0.28f), w * 0.07f, Color(0xFFFF80AB))
                drawSparkle(Offset(w * 0.80f, h * 0.82f), w * 0.06f, Color(0xFF80D8FF))
            }
            FruitTheme.PASTEL_DREAM -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x66B2F7EF), Color(0x33F7D6E0), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = w * 0.52f
                    ),
                    radius = w * 0.52f,
                    center = Offset(cx, cy)
                )
            }
            FruitTheme.SUNNY_ORCHARD, FruitTheme.ROYAL_CROWN -> Unit
        }

        // Soft drop shadow underneath fruit
        drawOval(
            color = Color(0x22000000),
            topLeft = Offset(w * 0.22f, h * 0.85f),
            size = Size(w * 0.56f, h * 0.10f)
        )

        val outlineColor = Color(0xFF4A2E20)
        val strokeWidth = (w * 0.035f).coerceAtLeast(1.8f)

        when (fruitType) {
            FruitType.APPLE -> drawApple(w, h, outlineColor, strokeWidth, expression)
            FruitType.BANANA -> drawBanana(w, h, outlineColor, strokeWidth, expression)
            FruitType.ORANGE -> drawOrange(w, h, outlineColor, strokeWidth, expression)
            FruitType.GRAPES -> drawGrapes(w, h, outlineColor, strokeWidth, expression)
            FruitType.WATERMELON -> drawWatermelon(w, h, outlineColor, strokeWidth, expression)
            FruitType.STRAWBERRY -> drawStrawberry(w, h, outlineColor, strokeWidth, expression)
            FruitType.PINEAPPLE -> drawPineapple(w, h, outlineColor, strokeWidth, expression)
            FruitType.MANGO -> drawMango(w, h, outlineColor, strokeWidth, expression)
            FruitType.CHERRY -> drawCherry(w, h, outlineColor, strokeWidth, expression)
            FruitType.KIWI -> drawKiwi(w, h, outlineColor, strokeWidth, expression)
        }

        if (theme == FruitTheme.ROYAL_CROWN) {
            drawMiniCrown(cx, h * 0.14f, w * 0.28f, outlineColor)
        }
    }
}

private fun DrawScope.drawApple(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Stem
    val stemPath = Path().apply {
        moveTo(w * 0.49f, h * 0.26f)
        quadraticTo(w * 0.50f, h * 0.14f, w * 0.56f, h * 0.11f)
    }
    drawPath(stemPath, Color(0xFF795548), style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))

    // Leaf
    val leafPath = Path().apply {
        moveTo(w * 0.51f, h * 0.21f)
        cubicTo(w * 0.62f, h * 0.10f, w * 0.77f, h * 0.14f, w * 0.72f, h * 0.26f)
        cubicTo(w * 0.63f, h * 0.29f, w * 0.55f, h * 0.26f, w * 0.51f, h * 0.21f)
    }
    drawPath(leafPath, Color(0xFF4CD97B))
    drawPath(leafPath, outline, style = Stroke(width = strokeW * 0.8f, join = StrokeJoin.Round))

    // Apple body
    val bodyPath = Path().apply {
        moveTo(w * 0.50f, h * 0.28f)
        cubicTo(w * 0.34f, h * 0.19f, w * 0.15f, h * 0.30f, w * 0.16f, h * 0.53f)
        cubicTo(w * 0.17f, h * 0.77f, w * 0.34f, h * 0.89f, w * 0.48f, h * 0.85f)
        quadraticTo(w * 0.50f, h * 0.84f, w * 0.52f, h * 0.85f)
        cubicTo(w * 0.66f, h * 0.89f, w * 0.83f, h * 0.77f, w * 0.84f, h * 0.53f)
        cubicTo(w * 0.85f, h * 0.30f, w * 0.66f, h * 0.19f, w * 0.50f, h * 0.28f)
        close()
    }
    drawPath(
        bodyPath,
        Brush.radialGradient(
            colors = listOf(Color(0xFFFF758F), Color(0xFFFF384F), Color(0xFFD91E36)),
            center = Offset(w * 0.38f, h * 0.40f),
            radius = w * 0.48f
        )
    )
    drawPath(bodyPath, outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // Glossy highlight
    rotate(-22f, pivot = Offset(w * 0.30f, h * 0.40f)) {
        drawOval(
            color = Color(0xAAFFFFFF),
            topLeft = Offset(w * 0.25f, h * 0.34f),
            size = Size(w * 0.10f, h * 0.16f)
        )
    }

    drawKawaiiFace(Offset(w * 0.50f, h * 0.56f), w * 0.28f, expression, outline)
}

private fun DrawScope.drawBanana(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    val bodyPath = Path().apply {
        moveTo(w * 0.22f, h * 0.24f)
        cubicTo(w * 0.14f, h * 0.56f, w * 0.36f, h * 0.86f, w * 0.82f, h * 0.77f)
        cubicTo(w * 0.89f, h * 0.75f, w * 0.88f, h * 0.66f, w * 0.80f, h * 0.64f)
        cubicTo(w * 0.52f, h * 0.63f, w * 0.37f, h * 0.46f, w * 0.34f, h * 0.22f)
        cubicTo(w * 0.32f, h * 0.14f, w * 0.24f, h * 0.15f, w * 0.22f, h * 0.24f)
        close()
    }
    drawPath(
        bodyPath,
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFF066), Color(0xFFFFD32A), Color(0xFFF5B014)),
            start = Offset(w * 0.20f, h * 0.20f),
            end = Offset(w * 0.82f, h * 0.78f)
        )
    )
    drawPath(bodyPath, outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // Stem tip
    drawCircle(Color(0xFF795548), radius = w * 0.042f, center = Offset(w * 0.26f, h * 0.17f))
    drawCircle(Color(0xFF795548), radius = w * 0.032f, center = Offset(w * 0.85f, h * 0.72f))

    // Gloss highlight
    drawLine(
        color = Color(0x99FFFFFF),
        start = Offset(w * 0.29f, h * 0.34f),
        end = Offset(w * 0.44f, h * 0.58f),
        strokeWidth = w * 0.045f,
        cap = StrokeCap.Round
    )

    drawKawaiiFace(Offset(w * 0.47f, h * 0.61f), w * 0.22f, expression, outline)
}

private fun DrawScope.drawOrange(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Leaf
    val leaf = Path().apply {
        moveTo(w * 0.50f, h * 0.22f)
        cubicTo(w * 0.58f, h * 0.10f, w * 0.74f, h * 0.13f, w * 0.69f, h * 0.24f)
        cubicTo(w * 0.61f, h * 0.27f, w * 0.54f, h * 0.25f, w * 0.50f, h * 0.22f)
    }
    drawPath(leaf, Color(0xFF4CD97B))
    drawPath(leaf, outline, style = Stroke(width = strokeW * 0.8f))

    val center = Offset(w * 0.50f, h * 0.54f)
    val radius = w * 0.33f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFB74D), Color(0xFFFF8C24), Color(0xFFE66300)),
            center = Offset(w * 0.40f, h * 0.42f),
            radius = radius * 1.3f
        ),
        radius = radius,
        center = center
    )
    drawCircle(outline, radius = radius, center = center, style = Stroke(width = strokeW))

    // Citrus pores
    val poreColor = Color(0x55C85000)
    drawCircle(poreColor, radius = w * 0.015f, center = Offset(w * 0.28f, h * 0.45f))
    drawCircle(poreColor, radius = w * 0.015f, center = Offset(w * 0.72f, h * 0.43f))
    drawCircle(poreColor, radius = w * 0.015f, center = Offset(w * 0.68f, h * 0.70f))

    // Highlight
    drawOval(
        color = Color(0x99FFFFFF),
        topLeft = Offset(w * 0.28f, h * 0.32f),
        size = Size(w * 0.14f, h * 0.08f)
    )

    drawKawaiiFace(Offset(w * 0.50f, h * 0.56f), w * 0.28f, expression, outline)
}

private fun DrawScope.drawGrapes(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Stem & curled vine
    drawLine(
        color = Color(0xFF6D4C41),
        start = Offset(w * 0.50f, h * 0.25f),
        end = Offset(w * 0.50f, h * 0.12f),
        strokeWidth = w * 0.055f,
        cap = StrokeCap.Round
    )
    val leaf = Path().apply {
        moveTo(w * 0.50f, h * 0.20f)
        cubicTo(w * 0.62f, h * 0.09f, w * 0.75f, h * 0.14f, w * 0.68f, h * 0.25f)
        close()
    }
    drawPath(leaf, Color(0xFF52D681))
    drawPath(leaf, outline, style = Stroke(width = strokeW * 0.75f))

    // Berry cluster (back to front)
    val berryPositions = listOf(
        Offset(0.32f, 0.38f), Offset(0.50f, 0.36f), Offset(0.68f, 0.38f),
        Offset(0.28f, 0.54f), Offset(0.72f, 0.54f),
        Offset(0.38f, 0.70f), Offset(0.62f, 0.70f),
        Offset(0.50f, 0.80f),
        Offset(0.50f, 0.54f) // hero center grape with face
    )
    berryPositions.forEachIndexed { idx, pos ->
        val c = Offset(w * pos.x, h * pos.y)
        val r = if (idx == berryPositions.lastIndex) w * 0.165f else w * 0.135f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB388FF), Color(0xFF8C52FF), Color(0xFF5B23CC)),
                center = Offset(c.x - r * 0.25f, c.y - r * 0.25f),
                radius = r * 1.4f
            ),
            radius = r,
            center = c
        )
        drawCircle(outline, radius = r, center = c, style = Stroke(width = strokeW * 0.85f))
        drawCircle(Color(0x88FFFFFF), radius = r * 0.22f, center = Offset(c.x - r * 0.35f, c.y - r * 0.35f))
    }

    drawKawaiiFace(Offset(w * 0.50f, h * 0.55f), w * 0.20f, expression, outline)
}

private fun DrawScope.drawWatermelon(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Outer green rind arc
    val rindPath = Path().apply {
        moveTo(w * 0.14f, h * 0.42f)
        quadraticTo(w * 0.50f, h * 1.02f, w * 0.86f, h * 0.42f)
        close()
    }
    drawPath(rindPath, Color(0xFF28B463))
    drawPath(rindPath, outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // Crisp white pith layer
    val pithPath = Path().apply {
        moveTo(w * 0.18f, h * 0.42f)
        quadraticTo(w * 0.50f, h * 0.93f, w * 0.82f, h * 0.42f)
        close()
    }
    drawPath(pithPath, Color(0xFFEAFCEB))

    // Juicy ruby flesh
    val fleshPath = Path().apply {
        moveTo(w * 0.22f, h * 0.42f)
        quadraticTo(w * 0.50f, h * 0.86f, w * 0.78f, h * 0.42f)
        close()
    }
    drawPath(
        fleshPath,
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFF6B8B), Color(0xFFFF3E66)),
            startY = h * 0.40f,
            endY = h * 0.78f
        )
    )
    drawPath(fleshPath, outline, style = Stroke(width = strokeW * 0.7f, join = StrokeJoin.Round))

    // Seeds
    val seedColor = Color(0xFF3E2723)
    drawOval(seedColor, Offset(w * 0.31f, h * 0.48f), Size(w * 0.032f, w * 0.048f))
    drawOval(seedColor, Offset(w * 0.66f, h * 0.48f), Size(w * 0.032f, w * 0.048f))
    drawOval(seedColor, Offset(w * 0.48f, h * 0.69f), Size(w * 0.030f, w * 0.045f))

    drawKawaiiFace(Offset(w * 0.50f, h * 0.54f), w * 0.22f, expression, outline)
}

private fun DrawScope.drawStrawberry(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Heart-berry body
    val bodyPath = Path().apply {
        moveTo(w * 0.50f, h * 0.86f)
        cubicTo(w * 0.24f, h * 0.76f, w * 0.16f, h * 0.46f, w * 0.28f, h * 0.30f)
        quadraticTo(w * 0.50f, h * 0.20f, w * 0.72f, h * 0.30f)
        cubicTo(w * 0.84f, h * 0.46f, w * 0.76f, h * 0.76f, w * 0.50f, h * 0.86f)
        close()
    }
    drawPath(
        bodyPath,
        Brush.radialGradient(
            colors = listOf(Color(0xFFFF6E91), Color(0xFFFF3666), Color(0xFFD61A48)),
            center = Offset(w * 0.42f, h * 0.42f),
            radius = w * 0.45f
        )
    )
    drawPath(bodyPath, outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // Golden seeds
    val seedColor = Color(0xFFFFE57F)
    listOf(
        Offset(0.32f, 0.44f), Offset(0.68f, 0.44f),
        Offset(0.38f, 0.70f), Offset(0.62f, 0.70f),
        Offset(0.50f, 0.78f)
    ).forEach { p ->
        drawOval(seedColor, Offset(w * p.x - w * 0.014f, h * p.y), Size(w * 0.028f, w * 0.042f))
    }

    // Leafy green crown
    val crown = Path().apply {
        moveTo(w * 0.25f, h * 0.31f)
        lineTo(w * 0.38f, h * 0.22f)
        lineTo(w * 0.50f, h * 0.14f)
        lineTo(w * 0.62f, h * 0.22f)
        lineTo(w * 0.75f, h * 0.31f)
        lineTo(w * 0.59f, h * 0.33f)
        lineTo(w * 0.50f, h * 0.38f)
        lineTo(w * 0.41f, h * 0.33f)
        close()
    }
    drawPath(crown, Color(0xFF4CD97B))
    drawPath(crown, outline, style = Stroke(width = strokeW * 0.8f, join = StrokeJoin.Round))

    drawKawaiiFace(Offset(w * 0.50f, h * 0.55f), w * 0.25f, expression, outline)
}

private fun DrawScope.drawPineapple(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Spiky green crown
    val crown = Path().apply {
        moveTo(w * 0.34f, h * 0.36f)
        lineTo(w * 0.26f, h * 0.16f)
        lineTo(w * 0.43f, h * 0.24f)
        lineTo(w * 0.50f, h * 0.09f)
        lineTo(w * 0.57f, h * 0.24f)
        lineTo(w * 0.74f, h * 0.16f)
        lineTo(w * 0.66f, h * 0.36f)
        close()
    }
    drawPath(crown, Color(0xFF39D170))
    drawPath(crown, outline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))

    // Golden body
    val bodyLeft = w * 0.23f
    val bodyTop = h * 0.32f
    val bodyW = w * 0.54f
    val bodyH = h * 0.54f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFDC5E), Color(0xFFFFB61D), Color(0xFFE58E00)),
            startY = bodyTop,
            endY = bodyTop + bodyH
        ),
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(w * 0.24f, w * 0.24f),
        style = Stroke(width = strokeW)
    )

    // Subtle diamond lines
    val gridColor = Color(0x44A05A00)
    drawLine(gridColor, Offset(w * 0.29f, h * 0.42f), Offset(w * 0.71f, h * 0.76f), strokeWidth = 2f)
    drawLine(gridColor, Offset(w * 0.71f, h * 0.42f), Offset(w * 0.29f, h * 0.76f), strokeWidth = 2f)

    drawKawaiiFace(Offset(w * 0.50f, h * 0.60f), w * 0.24f, expression, outline)
}

private fun DrawScope.drawMango(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Leaf
    val leaf = Path().apply {
        moveTo(w * 0.50f, h * 0.21f)
        cubicTo(w * 0.60f, h * 0.10f, w * 0.75f, h * 0.14f, w * 0.69f, h * 0.25f)
        close()
    }
    drawPath(leaf, Color(0xFF4CD97B))
    drawPath(leaf, outline, style = Stroke(width = strokeW * 0.8f))

    // Curved mango silhouette
    val body = Path().apply {
        moveTo(w * 0.48f, h * 0.21f)
        cubicTo(w * 0.77f, h * 0.20f, w * 0.85f, h * 0.50f, w * 0.68f, h * 0.76f)
        cubicTo(w * 0.55f, h * 0.90f, w * 0.28f, h * 0.85f, w * 0.23f, h * 0.65f)
        cubicTo(w * 0.18f, h * 0.40f, w * 0.29f, h * 0.22f, w * 0.48f, h * 0.21f)
        close()
    }
    drawPath(
        body,
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF6D59), Color(0xFFFFA73B), Color(0xFFFFD84D)),
            start = Offset(w * 0.30f, h * 0.24f),
            end = Offset(w * 0.70f, h * 0.82f)
        )
    )
    drawPath(body, outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    drawOval(
        color = Color(0x88FFFFFF),
        topLeft = Offset(w * 0.31f, h * 0.30f),
        size = Size(w * 0.10f, h * 0.15f)
    )

    drawKawaiiFace(Offset(w * 0.50f, h * 0.56f), w * 0.24f, expression, outline)
}

private fun DrawScope.drawCherry(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    // Stems
    val leftStem = Path().apply {
        moveTo(w * 0.33f, h * 0.52f)
        quadraticTo(w * 0.38f, h * 0.25f, w * 0.54f, h * 0.15f)
    }
    val rightStem = Path().apply {
        moveTo(w * 0.68f, h * 0.55f)
        quadraticTo(w * 0.63f, h * 0.28f, w * 0.54f, h * 0.15f)
    }
    drawPath(leftStem, Color(0xFF4CAF50), style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))
    drawPath(rightStem, Color(0xFF4CAF50), style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))

    // Top leaf
    val leaf = Path().apply {
        moveTo(w * 0.54f, h * 0.15f)
        cubicTo(w * 0.67f, h * 0.09f, w * 0.79f, h * 0.17f, w * 0.68f, h * 0.25f)
        close()
    }
    drawPath(leaf, Color(0xFF4CD97B))
    drawPath(leaf, outline, style = Stroke(width = strokeW * 0.75f))

    // Twin cherries
    val c1 = Offset(w * 0.33f, h * 0.66f)
    val c2 = Offset(w * 0.67f, h * 0.68f)
    val r = w * 0.20f

    listOf(c1, c2).forEach { c ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF6B8B), Color(0xFFE62E5C), Color(0xFFB31237)),
                center = Offset(c.x - r * 0.25f, c.y - r * 0.25f),
                radius = r * 1.4f
            ),
            radius = r,
            center = c
        )
        drawCircle(outline, radius = r, center = c, style = Stroke(width = strokeW * 0.9f))
        drawCircle(Color(0xAAFFFFFF), radius = r * 0.22f, center = Offset(c.x - r * 0.38f, c.y - r * 0.38f))
    }

    drawKawaiiFace(c1.copy(y = c1.y + h * 0.02f), w * 0.16f, expression, outline)
    drawKawaiiFace(c2.copy(y = c2.y + h * 0.02f), w * 0.16f, expression, outline)
}

private fun DrawScope.drawKiwi(
    w: Float,
    h: Float,
    outline: Color,
    strokeW: Float,
    expression: FruitExpression
) {
    val center = Offset(w * 0.50f, h * 0.54f)
    val outerR = w * 0.35f
    // Fuzzy brown skin rim
    drawCircle(Color(0xFFA67B5B), radius = outerR, center = center)
    drawCircle(outline, radius = outerR, center = center, style = Stroke(width = strokeW))

    // Vibrant green flesh
    val fleshR = outerR * 0.85f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFC5F78B), Color(0xFF7ED957), Color(0xFF54B435)),
            center = center,
            radius = fleshR
        ),
        radius = fleshR,
        center = center
    )

    // Pale core
    drawCircle(Color(0xFFECFFD1), radius = outerR * 0.35f, center = center)

    // Tiny black seed ring
    for (i in 0 until 10) {
        val angle = (i * 36.0) * PI / 180.0
        val sx = center.x + (outerR * 0.52f * cos(angle)).toFloat()
        val sy = center.y + (outerR * 0.52f * sin(angle)).toFloat()
        drawCircle(Color(0xFF3E2723), radius = w * 0.017f, center = Offset(sx, sy))
    }

    drawKawaiiFace(center.copy(y = center.y + h * 0.02f), w * 0.24f, expression, outline)
}

private fun DrawScope.drawKawaiiFace(
    center: Offset,
    faceWidth: Float,
    expression: FruitExpression,
    outline: Color
) {
    val eyeOffset = faceWidth * 0.36f
    val eyeY = center.y - faceWidth * 0.08f
    val eyeR = (faceWidth * 0.13f).coerceAtLeast(2.2f)

    // Rosy blush cheeks
    val blushColor = Color(0x88FF7597)
    drawOval(
        color = blushColor,
        topLeft = Offset(center.x - eyeOffset - eyeR * 1.4f, eyeY + eyeR * 0.9f),
        size = Size(eyeR * 2.2f, eyeR * 1.3f)
    )
    drawOval(
        color = blushColor,
        topLeft = Offset(center.x + eyeOffset - eyeR * 0.8f, eyeY + eyeR * 0.9f),
        size = Size(eyeR * 2.2f, eyeR * 1.3f)
    )

    when (expression) {
        FruitExpression.HAPPY -> {
            // Sparkling eyes
            listOf(-eyeOffset, eyeOffset).forEach { dx ->
                val ex = center.x + dx
                drawCircle(outline, radius = eyeR, center = Offset(ex, eyeY))
                drawCircle(Color.White, radius = eyeR * 0.42f, center = Offset(ex - eyeR * 0.28f, eyeY - eyeR * 0.28f))
            }
            // Cute smile arc
            val smile = Path().apply {
                moveTo(center.x - faceWidth * 0.18f, center.y + faceWidth * 0.10f)
                quadraticTo(center.x, center.y + faceWidth * 0.28f, center.x + faceWidth * 0.18f, center.y + faceWidth * 0.10f)
            }
            drawPath(smile, outline, style = Stroke(width = (faceWidth * 0.09f).coerceAtLeast(1.8f), cap = StrokeCap.Round))
        }
        FruitExpression.EXCITED -> {
            // Happy ^ ^ eyes
            listOf(-eyeOffset, eyeOffset).forEach { dx ->
                val ex = center.x + dx
                val arc = Path().apply {
                    moveTo(ex - eyeR * 1.1f, eyeY + eyeR * 0.3f)
                    quadraticTo(ex, eyeY - eyeR * 1.1f, ex + eyeR * 1.1f, eyeY + eyeR * 0.3f)
                }
                drawPath(arc, outline, style = Stroke(width = (faceWidth * 0.09f).coerceAtLeast(1.8f), cap = StrokeCap.Round))
            }
            // Big open joyful mouth
            drawArc(
                color = outline,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - faceWidth * 0.20f, center.y + faceWidth * 0.04f),
                size = Size(faceWidth * 0.40f, faceWidth * 0.26f)
            )
            drawArc(
                color = Color(0xFFFF6B8B),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = true,
                topLeft = Offset(center.x - faceWidth * 0.14f, center.y + faceWidth * 0.11f),
                size = Size(faceWidth * 0.28f, faceWidth * 0.16f)
            )
        }
        FruitExpression.BLINK -> {
            listOf(-eyeOffset, eyeOffset).forEach { dx ->
                val ex = center.x + dx
                drawLine(
                    color = outline,
                    start = Offset(ex - eyeR, eyeY),
                    end = Offset(ex + eyeR, eyeY),
                    strokeWidth = (faceWidth * 0.09f).coerceAtLeast(1.8f),
                    cap = StrokeCap.Round
                )
            }
            val smile = Path().apply {
                moveTo(center.x - faceWidth * 0.15f, center.y + faceWidth * 0.10f)
                quadraticTo(center.x, center.y + faceWidth * 0.24f, center.x + faceWidth * 0.15f, center.y + faceWidth * 0.10f)
            }
            drawPath(smile, outline, style = Stroke(width = (faceWidth * 0.08f).coerceAtLeast(1.6f), cap = StrokeCap.Round))
        }
        FruitExpression.DIZZY -> {
            // Cute > < eyes + wavy mouth
            listOf(-eyeOffset, eyeOffset).forEach { dx ->
                val ex = center.x + dx
                drawLine(outline, Offset(ex - eyeR, eyeY - eyeR * 0.7f), Offset(ex + eyeR, eyeY + eyeR * 0.7f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawLine(outline, Offset(ex + eyeR, eyeY - eyeR * 0.7f), Offset(ex - eyeR, eyeY + eyeR * 0.7f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            }
            drawCircle(outline, radius = eyeR * 0.85f, center = Offset(center.x, center.y + faceWidth * 0.18f))
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawMiniCrown(cx: Float, topY: Float, width: Float, outline: Color) {
    val h = width * 0.62f
    val left = cx - width / 2f
    val path = Path().apply {
        moveTo(left, topY + h)
        lineTo(left - width * 0.06f, topY + h * 0.15f)
        lineTo(left + width * 0.28f, topY + h * 0.55f)
        lineTo(cx, topY)
        lineTo(left + width * 0.72f, topY + h * 0.55f)
        lineTo(left + width * 1.06f, topY + h * 0.15f)
        lineTo(left + width, topY + h)
        close()
    }
    drawPath(path, Color(0xFFFFD54F))
    drawPath(path, outline, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
    drawCircle(Color(0xFFFF4D8D), radius = width * 0.09f, center = Offset(cx, topY + h * 0.65f))
}
