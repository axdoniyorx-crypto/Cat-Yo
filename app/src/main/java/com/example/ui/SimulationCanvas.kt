package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.simulation.model.Agent
import com.example.simulation.model.AgentState
import com.example.simulation.model.POIType
import com.example.simulation.model.PointOfInterest
import com.example.simulation.model.SimulationWorld
import com.example.simulation.model.WeatherType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SimulationCanvas(
    world: SimulationWorld,
    agents: List<Agent>,
    selectedAgentId: Int?,
    onSelectAgent: (Int?) -> Unit,
    onSelectPoi: (PointOfInterest?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for selected agent halo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Android native text paint objects for crisp overhead text rendering
    val namePaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(3f, 0f, 1f, android.graphics.Color.BLACK)
        }
    }

    val emotePaint = remember {
        Paint().apply {
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val poiTitlePaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
        }
    }

    val poiDescPaint = remember {
        Paint().apply {
            color = 0xFFCCCCCC.toInt()
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Responsive Scaling to fit portrait device screen
        val scaleX = screenWidth / world.width
        val scaleY = screenHeight / world.height
        val scale = minOf(scaleX, scaleY)
        val offsetX = (screenWidth - world.width * scale) / 2f
        val offsetY = (screenHeight - world.height * scale) / 2f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("simulation_canvas")
                .pointerInput(agents, world, scale, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        val worldX = (tapOffset.x - offsetX) / scale
                        val worldY = (tapOffset.y - offsetY) / scale
                        val tapWorld = Offset(worldX, worldY)

                        // 1. Check if user tapped an agent (generous touch radius)
                        val touchRadius = 38f
                        val tappedAgent = agents.find { agent ->
                            val dx = agent.position.x - tapWorld.x
                            val dy = agent.position.y - tapWorld.y
                            sqrt(dx * dx + dy * dy) <= touchRadius
                        }

                        if (tappedAgent != null) {
                            onSelectAgent(tappedAgent.id)
                            return@detectTapGestures
                        }

                        // 2. Check if user tapped a POI
                        val tappedPoi = world.pois.find { it.contains(tapWorld) }
                        if (tappedPoi != null) {
                            onSelectPoi(tappedPoi)
                            return@detectTapGestures
                        }

                        // 3. Tapped empty space
                        onSelectAgent(null)
                    }
                }
        ) {
            // Background base
            drawRect(color = Color(0xFF0D111A))

            // Apply world coordinate transformation
            // Everything inside this block is drawn in World Units (1000 x 1350)
            val matrix = androidx.compose.ui.graphics.Matrix()
            matrix.translate(offsetX, offsetY)
            matrix.scale(scale, scale)

            // Draw Background Grid Lines
            val gridColor = Color(0xFF1B2333)
            var gx = 0f
            while (gx <= world.width) {
                val p1 = Offset(offsetX + gx * scale, offsetY)
                val p2 = Offset(offsetX + gx * scale, offsetY + world.height * scale)
                drawLine(gridColor, p1, p2, strokeWidth = 1f)
                gx += 60f
            }
            var gy = 0f
            while (gy <= world.height) {
                val p1 = Offset(offsetX, offsetY + gy * scale)
                val p2 = Offset(offsetX + world.width * scale, offsetY + gy * scale)
                drawLine(gridColor, p1, p2, strokeWidth = 1f)
                gy += 60f
            }

            // Draw City Boundary Wall
            drawRoundRect(
                color = Color(0xFF243048),
                topLeft = Offset(offsetX + 15f * scale, offsetY + 35f * scale),
                size = Size((world.width - 30f) * scale, (world.height - 55f) * scale),
                cornerRadius = CornerRadius(24f * scale, 24f * scale),
                style = Stroke(width = 3f * scale)
            )

            // Draw Pathways & Roads connecting POIs
            drawPathways(scale, offsetX, offsetY, world)

            // Draw Points of Interest
            for (poi in world.pois) {
                drawPoi(poi, scale, offsetX, offsetY, poiTitlePaint, poiDescPaint)
            }

            // Draw Agents (20 autonomous citizens)
            for (agent in agents) {
                val isSelected = agent.id == selectedAgentId
                drawAgent(
                    agent = agent,
                    isSelected = isSelected,
                    pulse = if (isSelected) pulseScale else 1.0f,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    namePaint = namePaint,
                    emotePaint = emotePaint
                )
            }

            // Draw Environmental Lighting Overlay (Day/Night cycle)
            drawEnvironmentalLighting(world, scale, offsetX, offsetY)

            // Draw Dynamic Weather Atmospheric Effects (Rain, Thunderstorm, Heatwave, Snow)
            drawWeatherAtmosphere(world, scale, offsetX, offsetY)
        }
    }
}

/**
 * Draws roads and stone walkways between zones.
 */
private fun DrawScope.drawPathways(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    world: SimulationWorld
) {
    val roadColor = Color(0xFF1E2838)
    val roadBorder = Color(0xFF28364C)

    // Center North-South Main Avenue
    drawRect(
        color = roadColor,
        topLeft = Offset(offsetX + 465f * scale, offsetY + 110f * scale),
        size = Size(70f * scale, (world.height - 230f) * scale)
    )
    drawRect(
        color = roadBorder,
        topLeft = Offset(offsetX + 465f * scale, offsetY + 110f * scale),
        size = Size(70f * scale, (world.height - 230f) * scale),
        style = Stroke(width = 1.5f * scale)
    )

    // Center East-West Main Avenue
    drawRect(
        color = roadColor,
        topLeft = Offset(offsetX + 40f * scale, offsetY + 610f * scale),
        size = Size((world.width - 80f) * scale, 70f * scale)
    )
    drawRect(
        color = roadBorder,
        topLeft = Offset(offsetX + 40f * scale, offsetY + 610f * scale),
        size = Size((world.width - 80f) * scale, 70f * scale),
        style = Stroke(width = 1.5f * scale)
    )

    // Walking cross-paths
    val stoneColor = Color(0xFF2B3A50)
    for (i in 0 until 18) {
        val y = 140f + i * 55f
        drawLine(
            color = stoneColor,
            start = Offset(offsetX + 495f * scale, offsetY + y * scale),
            end = Offset(offsetX + 505f * scale, offsetY + y * scale),
            strokeWidth = 3f * scale
        )
    }
}

/**
 * Renders an enclosed Point of Interest with styled geometric architecture.
 */
private fun DrawScope.drawPoi(
    poi: PointOfInterest,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    titlePaint: Paint,
    descPaint: Paint
) {
    val left = offsetX + poi.bounds.left * scale
    val top = offsetY + poi.bounds.top * scale
    val width = poi.bounds.width * scale
    val height = poi.bounds.height * scale

    // Zone background card
    drawRoundRect(
        color = poi.color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(18f * scale, 18f * scale)
    )

    // Glowing border outline
    drawRoundRect(
        color = poi.accentColor.copy(alpha = 0.65f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(18f * scale, 18f * scale),
        style = Stroke(width = 2.5f * scale)
    )

    // Distinct interior decorations based on POI type
    when (poi.type) {
        POIType.FOOD_SOURCE -> {
            // Draw orchard trees & crop rows
            val foliageGreen = Color(0xFF27AE60)
            val trunkBrown = Color(0xFF795548)
            val berryRed = Color(0xFFE74C3C)

            for (row in 0..2) {
                for (col in 0..3) {
                    val tx = left + 45f * scale + col * 85f * scale
                    val ty = top + 130f * scale + row * 70f * scale

                    // Tree trunk
                    drawRect(trunkBrown, Offset(tx - 3f * scale, ty), Size(6f * scale, 16f * scale))
                    // Foliage circle
                    drawCircle(foliageGreen, radius = 20f * scale, center = Offset(tx, ty - 6f * scale))
                    // Red fruit dots
                    drawCircle(berryRed, radius = 3.5f * scale, center = Offset(tx - 7f * scale, ty - 10f * scale))
                    drawCircle(berryRed, radius = 3.5f * scale, center = Offset(tx + 8f * scale, ty - 8f * scale))
                    drawCircle(berryRed, radius = 3.5f * scale, center = Offset(tx, ty - 16f * scale))
                }
            }
        }

        POIType.REST_AREA -> {
            // Draw cozy dormitory cabins
            val roofColor = Color(0xFF2C3E50)
            val wallColor = Color(0xFF34495E)
            val windowGlow = Color(0xFFF1C40F)

            for (col in 0..1) {
                for (row in 0..1) {
                    val cx = left + 35f * scale + col * 180f * scale
                    val cy = top + 110f * scale + row * 125f * scale
                    val cw = 150f * scale
                    val ch = 95f * scale

                    // Cabin wall
                    drawRoundRect(
                        wallColor,
                        topLeft = Offset(cx, cy),
                        size = Size(cw, ch),
                        cornerRadius = CornerRadius(8f * scale, 8f * scale)
                    )
                    // Cabin roof
                    drawRoundRect(
                        roofColor,
                        topLeft = Offset(cx - 5f * scale, cy - 8f * scale),
                        size = Size(cw + 10f * scale, 28f * scale),
                        cornerRadius = CornerRadius(6f * scale, 6f * scale)
                    )
                    // Glowing windows
                    drawRect(windowGlow, Offset(cx + 25f * scale, cy + 40f * scale), Size(24f * scale, 24f * scale))
                    drawRect(windowGlow, Offset(cx + 95f * scale, cy + 40f * scale), Size(24f * scale, 24f * scale))
                }
            }
        }

        POIType.TOWN_PLAZA -> {
            // Central Plaza & Grand Fountain
            val plazaCenter = Offset(left + width / 2f, top + height / 2f)

            // Concentric cobblestone rings
            drawCircle(Color(0xFF251C37), radius = 110f * scale, center = plazaCenter)
            drawCircle(Color(0xFF4A3B69), radius = 110f * scale, center = plazaCenter, style = Stroke(width = 2f * scale))
            drawCircle(Color(0xFF3B2F55), radius = 75f * scale, center = plazaCenter)

            // Fountain basin
            val fountainWater = Color(0xFF3498DB)
            drawCircle(Color(0xFF1ABC9C), radius = 45f * scale, center = plazaCenter)
            drawCircle(fountainWater, radius = 38f * scale, center = plazaCenter)
            drawCircle(Color(0xFFE0F7FA), radius = 16f * scale, center = plazaCenter)
            // Fountain spray ripples
            drawCircle(Color(0xFFB2EBF2), radius = 26f * scale, center = plazaCenter, style = Stroke(width = 1.5f * scale))
        }

        POIType.WORK_AREA -> {
            // Lumberyard timber stacks and quarry rocks
            val timberColor = Color(0xFFD35400)
            val stoneColor = Color(0xFF7F8C8D)

            // Stacked logs
            for (i in 0..2) {
                val lx = left + 40f * scale + i * 115f * scale
                val ly = top + 130f * scale
                drawRoundRect(timberColor, Offset(lx, ly), Size(90f * scale, 35f * scale), CornerRadius(8f * scale, 8f * scale))
                drawCircle(Color(0xFFE67E22), 12f * scale, Offset(lx + 80f * scale, ly + 17f * scale))
            }

            // Quarry boulder deposits
            for (i in 0..3) {
                val sx = left + 50f * scale + i * 85f * scale
                val sy = top + 215f * scale
                drawCircle(stoneColor, 22f * scale, Offset(sx, sy))
                drawCircle(Color(0xFFBDC3C7), 10f * scale, Offset(sx - 4f * scale, sy - 6f * scale))
            }
        }

        POIType.RECREATION_PARK -> {
            // Zen Garden Pond and Cherry Blossom Trees
            val lakeCenter = Offset(left + width * 0.45f, top + height * 0.55f)
            drawCircle(Color(0xFF16A085), radius = 68f * scale, center = lakeCenter)
            drawCircle(Color(0xFF1ABC9C), radius = 58f * scale, center = lakeCenter)

            // Water lily pads
            drawCircle(Color(0xFF27AE60), radius = 8f * scale, center = Offset(lakeCenter.x - 22f * scale, lakeCenter.y + 12f * scale))
            drawCircle(Color(0xFF27AE60), radius = 10f * scale, center = Offset(lakeCenter.x + 18f * scale, lakeCenter.y - 15f * scale))

            // Cherry blossom trees
            val blossomPink = Color(0xFFFF85A1)
            drawCircle(blossomPink, radius = 26f * scale, center = Offset(left + 50f * scale, top + 125f * scale))
            drawCircle(blossomPink, radius = 24f * scale, center = Offset(left + width - 60f * scale, top + 135f * scale))
            drawCircle(blossomPink, radius = 22f * scale, center = Offset(left + width - 55f * scale, top + height - 55f * scale))
        }
    }

    // POI Header Title & Emoji
    drawIntoCanvas { canvas ->
        val titleText = "${poi.emoji} ${poi.name}"
        canvas.nativeCanvas.drawText(
            titleText,
            left + 16f * scale,
            top + 34f * scale,
            titlePaint.apply { textSize = 21f * scale }
        )
        canvas.nativeCanvas.drawText(
            poi.description,
            left + 16f * scale,
            top + 58f * scale,
            descPaint.apply { textSize = 14f * scale }
        )
    }
}

/**
 * Renders an autonomous AI agent with body, direction indicator, selection halo,
 * overhead Name & State tag ("Reno: Sleeping"), and emote bubble.
 */
private fun DrawScope.drawAgent(
    agent: Agent,
    isSelected: Boolean,
    pulse: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    namePaint: Paint,
    emotePaint: Paint
) {
    val screenX = offsetX + agent.position.x * scale
    val screenY = offsetY + agent.position.y * scale
    val baseRadius = 15f * scale

    // 1. Draw Selection Pulse Ring if selected
    if (isSelected) {
        val haloRadius = (baseRadius + 14f * scale) * pulse
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.75f),
            radius = haloRadius,
            center = Offset(screenX, screenY),
            style = Stroke(width = 3.5f * scale)
        )
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
            radius = haloRadius,
            center = Offset(screenX, screenY)
        )
    }

    // 2. Drop Shadow
    drawCircle(
        color = Color(0x77000000),
        radius = baseRadius + 2f * scale,
        center = Offset(screenX, screenY + 3.5f * scale)
    )

    // 3. Agent Body Outer Ring (Theme Color)
    drawCircle(
        color = agent.color,
        radius = baseRadius,
        center = Offset(screenX, screenY)
    )

    // 4. Inner core
    val coreColor = when (agent.activeState) {
        AgentState.SLEEPING -> Color(0xFF2C3E50)
        AgentState.EATING -> Color(0xFF27AE60)
        AgentState.GATHERING -> Color(0xFFE67E22)
        AgentState.SOCIALIZING -> Color(0xFFE84393)
        else -> Color.White.copy(alpha = 0.9f)
    }
    drawCircle(
        color = coreColor,
        radius = baseRadius * 0.55f,
        center = Offset(screenX, screenY)
    )

    // 5. Direction indicator (Facing pointer)
    val noseLength = baseRadius * 1.35f
    val noseX = screenX + cos(agent.facingAngle) * noseLength
    val noseY = screenY + sin(agent.facingAngle) * noseLength
    drawLine(
        color = Color.White,
        start = Offset(screenX, screenY),
        end = Offset(noseX, noseY),
        strokeWidth = 2.5f * scale
    )

    // 6. Overhead Name & Current State Tag (MANDATORY REQUIREMENT)
    // Format: "Reno: Sleeping", "Ather: Gathering", "Liora: Socializing"
    val infoTag = "${agent.name}: ${agent.activeState.displayName}"

    drawIntoCanvas { canvas ->
        val textScaledPaint = namePaint.apply {
            textSize = 15f * scale
            color = if (isSelected) android.graphics.Color.CYAN else android.graphics.Color.WHITE
        }

        val textWidth = textScaledPaint.measureText(infoTag)
        val pillPadH = 7f * scale
        val pillPadV = 4f * scale
        val textY = screenY - baseRadius - 12f * scale
        val pillLeft = screenX - (textWidth / 2f) - pillPadH
        val pillRight = screenX + (textWidth / 2f) + pillPadH
        val pillTop = textY - (15f * scale) - pillPadV
        val pillBottom = textY + pillPadV

        // Background pill behind text for sharp readability against any zone
        val pillPaint = Paint().apply {
            color = 0xDD111622.toInt()
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawRoundRect(
            pillLeft,
            pillTop,
            pillRight,
            pillBottom,
            6f * scale,
            6f * scale,
            pillPaint
        )

        // Overhead text
        canvas.nativeCanvas.drawText(
            infoTag,
            screenX,
            textY - 2f * scale,
            textScaledPaint
        )

        // 7. Overhead Emote Bubble (if active)
        val emote = agent.activeEmote ?: when (agent.activeState) {
            AgentState.SLEEPING -> "💤"
            AgentState.EATING -> "🍎"
            AgentState.GATHERING -> "⛏️"
            AgentState.SOCIALIZING -> "💬"
            AgentState.ENTERTAINING -> "🌸"
            else -> null
        }

        if (emote != null) {
            val bubbleY = pillTop - 12f * scale
            // Emote bubble circle
            val bubbleBg = Paint().apply {
                color = 0xEE1F2839.toInt()
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawCircle(screenX, bubbleY, 13f * scale, bubbleBg)
            val bubbleBorder = Paint().apply {
                color = agent.color.toArgb()
                style = Paint.Style.STROKE
                strokeWidth = 1.5f * scale
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawCircle(screenX, bubbleY, 13f * scale, bubbleBorder)

            // Draw Emoji
            val emoteScaled = emotePaint.apply { textSize = 16f * scale }
            canvas.nativeCanvas.drawText(
                emote,
                screenX,
                bubbleY + 5.5f * scale,
                emoteScaled
            )
        }
    }
}

/**
 * Renders ambient Day / Dusk / Night sky lighting overlay.
 */
private fun DrawScope.drawEnvironmentalLighting(
    world: SimulationWorld,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val h = world.timeOfDayHours

    val tintColor: Color? = when {
        // Night (21:00 - 05:00)
        h >= 21f || h < 5f -> Color(0xFF070B19).copy(alpha = 0.38f)
        // Dawn (05:00 - 07:00)
        h in 5f..7f -> Color(0xFFE67E22).copy(alpha = 0.12f)
        // Day (07:00 - 18:00) -> clear
        h in 7f..18f -> null
        // Dusk (18:00 - 21:00)
        h in 18f..21f -> Color(0xFF9B59B6).copy(alpha = 0.18f)
        else -> null
    }

    if (tintColor != null) {
        drawRect(
            color = tintColor,
            topLeft = Offset(offsetX, offsetY),
            size = Size(world.width * scale, world.height * scale)
        )
    }
}

/**
 * Renders atmospheric weather particles and ambient visual cues
 * (e.g. rain streaks, lightning flashes, heatwave shimmer, drifting snow).
 */
private fun DrawScope.drawWeatherAtmosphere(
    world: SimulationWorld,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val weather = world.weather.current
    val w = world.width * scale
    val h = world.height * scale
    val time = System.currentTimeMillis()

    when (weather) {
        WeatherType.CLEAR -> {
            if (!world.isNight) {
                drawRect(
                    color = Color(0xFFFDE047).copy(alpha = 0.03f),
                    topLeft = Offset(offsetX, offsetY),
                    size = Size(w, h)
                )
            }
        }
        WeatherType.RAIN, WeatherType.THUNDERSTORM -> {
            val isStorm = weather == WeatherType.THUNDERSTORM
            val rainColor = Color(0xFF60A5FA).copy(alpha = if (isStorm) 0.55f else 0.38f)
            val dropCount = if (isStorm) 70 else 45

            for (i in 0 until dropCount) {
                val seed = (i * 197 + (time / 14)).toFloat()
                val rx = (seed * 37f) % world.width
                val ry = (seed * 53f) % world.height
                val startX = offsetX + rx * scale
                val startY = offsetY + ry * scale
                drawLine(
                    color = rainColor,
                    start = Offset(startX, startY),
                    end = Offset(startX - 6f * scale, startY + 18f * scale),
                    strokeWidth = (if (isStorm) 2f else 1.5f) * scale
                )
            }

            // Thunderstorm lightning flash
            if (isStorm) {
                val cycle = time % 4500
                if (cycle in 100..240) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.28f),
                        topLeft = Offset(offsetX, offsetY),
                        size = Size(w, h)
                    )
                }
            }
        }
        WeatherType.HEATWAVE -> {
            drawRect(
                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                topLeft = Offset(offsetX, offsetY),
                size = Size(w, h)
            )
        }
        WeatherType.SNOW -> {
            val snowColor = Color.White.copy(alpha = 0.8f)
            for (i in 0 until 50) {
                val seed = (i * 131 + (time / 32)).toFloat()
                val rx = (seed * 41f) % world.width
                val ry = (seed * 29f) % world.height
                val sx = offsetX + rx * scale
                val sy = offsetY + ry * scale
                drawCircle(
                    color = snowColor,
                    radius = (2.2f + (i % 3)) * scale,
                    center = Offset(sx, sy)
                )
            }
        }
    }
}
