package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Immutable
data class ParticleBurstRequest(
    val id: Long,
    val origin: Offset,
    val color: Color,
    val isCelebration: Boolean = false
)

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Color,
    var life: Float,
    val maxLife: Float,
    var rotation: Float,
    val rotSpeed: Float,
    val isStar: Boolean
)

@Composable
fun ParticleOverlay(
    burstRequest: ParticleBurstRequest?,
    modifier: Modifier = Modifier
) {
    // Plain ArrayList + single pre-allocated Path eliminates per-frame GC allocations!
    val particles = remember { ArrayList<Particle>(64) }
    val reusableStarPath = remember { Path() }
    var frameTick by remember { mutableLongStateOf(0L) }
    var activeRunToken by remember { mutableLongStateOf(0L) }

    LaunchedEffect(burstRequest?.id) {
        if (burstRequest != null) {
            val count = if (burstRequest.isCelebration) 36 else 14
            val palette = arrayOf(
                burstRequest.color,
                Color(0xFF2ED19A),
                Color(0xFFFFC72C),
                Color(0xFFFF4D8D),
                Color(0xFF7B4DFF),
                Color(0xFF4BB8FF)
            )
            // Keep max active particles bounded for guaranteed 60 FPS on low-end devices
            if (particles.size > 72) {
                particles.subList(0, particles.size - 48).clear()
            }
            for (i in 0 until count) {
                val angle = (Random.nextFloat() * 2f * PI).toFloat()
                val speed = if (burstRequest.isCelebration) {
                    Random.nextFloat() * 520f + 160f
                } else {
                    Random.nextFloat() * 340f + 100f
                }
                particles.add(
                    Particle(
                        x = burstRequest.origin.x,
                        y = burstRequest.origin.y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed - if (burstRequest.isCelebration) 220f else 120f,
                        size = Random.nextFloat() * 10f + 7f,
                        color = palette[i % palette.size],
                        life = 1f,
                        maxLife = if (burstRequest.isCelebration) 1.2f else 0.70f,
                        rotation = Random.nextFloat() * 360f,
                        rotSpeed = Random.nextFloat() * 360f - 180f,
                        isStar = i % 2 == 0
                    )
                )
            }
            activeRunToken = burstRequest.id
        }
    }

    LaunchedEffect(activeRunToken) {
        if (particles.isEmpty()) return@LaunchedEffect
        var lastNanos = 0L
        while (particles.isNotEmpty()) {
            withFrameNanos { now ->
                if (lastNanos == 0L) lastNanos = now
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = now
                var i = particles.size - 1
                while (i >= 0) {
                    val p = particles[i]
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.vy += 580f * dt
                    p.rotation += p.rotSpeed * dt
                    p.life -= dt / p.maxLife
                    if (p.life <= 0f) {
                        particles.removeAt(i)
                    }
                    i--
                }
                frameTick = now
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val unused = frameTick
        for (i in 0 until particles.size) {
            val p = particles[i]
            val alpha = p.life.coerceIn(0f, 1f)
            val drawColor = p.color.copy(alpha = alpha)
            if (p.isStar) {
                rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                    drawBurstStar(
                        reusableStarPath,
                        Offset(p.x, p.y),
                        p.size * (0.5f + 0.5f * alpha),
                        drawColor
                    )
                }
            } else {
                drawCircle(
                    color = drawColor,
                    radius = p.size * 0.55f * (0.4f + 0.6f * alpha),
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}

private fun DrawScope.drawBurstStar(
    path: Path,
    center: Offset,
    radius: Float,
    color: Color
) {
    path.reset()
    val innerRadius = radius * 0.45f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * 36.0 - 90.0) * PI / 180.0
        val px = center.x + (r * cos(angle)).toFloat()
        val py = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color)
}
