package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simulation.model.Agent
import com.example.simulation.model.PointOfInterest
import com.example.simulation.model.SimulationWorld

@Composable
fun SimulationHUD(
    world: SimulationWorld,
    agents: List<Agent>,
    selectedAgentId: Int?,
    speedMultiplier: Float,
    isPaused: Boolean,
    onSelectAgent: (Int) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onTogglePause: () -> Unit,
    onOpenStats: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Top HUD Bar: Clock, Stockpile, Speed, Stats
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xEB131926),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334A)),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // City Title & Clock
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (world.isNight) "🌙" else "☀️",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = world.formattedClock,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Population: ${agents.size} AI Citizens",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Dynamic Weather Badge
                val weather = world.weather.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2838))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(text = weather.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = weather.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val spdDiff = ((weather.speedMultiplier - 1.0f) * 100).toInt()
                        val spdText = if (spdDiff == 0) "100% Spd" else "$spdDiff% Spd"
                        Text(
                            text = spdText,
                            fontSize = 9.sp,
                            color = if (spdDiff < 0) Color(0xFFF87171) else Color(0xFF4ADE80),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // City Stockpile Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StockpileBadge(emoji = "🍏", count = world.stockpile.foodStock)
                    StockpileBadge(emoji = "🪵", count = world.stockpile.timberStock)
                    StockpileBadge(emoji = "💎", count = world.stockpile.mineralStock)
                }

                // Simulation Speed Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pause / Play
                    IconButton(
                        onClick = onTogglePause,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) Color(0xFFFACC15) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Speed toggles: 1x, 2x, 4x
                    SpeedPill(label = "1x", isSelected = !isPaused && speedMultiplier == 1.0f) {
                        onSetSpeed(1.0f)
                    }
                    SpeedPill(label = "2x", isSelected = !isPaused && speedMultiplier == 2.0f) {
                        onSetSpeed(2.0f)
                    }
                    SpeedPill(label = "4x", isSelected = !isPaused && speedMultiplier == 4.0f) {
                        onSetSpeed(4.0f)
                    }

                    // Stats Button
                    IconButton(
                        onClick = onOpenStats,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("city_stats_button")
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "City Statistics",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Quick-Access Citizen Carousel
        // Highlights Reno, Ather, and Liora as founders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            agents.forEach { agent ->
                val isSelected = agent.id == selectedAgentId
                val isFounder = agent.id < 3 // Reno (0), Ather (1), Liora (2)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF1E293B) else Color(0xCC0F172A))
                        .border(
                            width = if (isSelected) 2.dp else if (isFounder) 1.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFF38BDF8) else if (isFounder) Color(0xFFF59E0B) else Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectAgent(agent.id) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("agent_pill_${agent.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Colored dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(agent.color)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = agent.name,
                            fontSize = 12.sp,
                            fontWeight = if (isFounder || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = agent.activeState.iconEmoji,
                            fontSize = 11.sp
                        )

                        if (agent.skillLevel > 1) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "v${agent.skillLevel}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockpileBadge(emoji: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A2234))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = emoji, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SpeedPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun PoiInspectorCard(
    poi: PointOfInterest?,
    agents: List<Agent>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (poi == null) return

    val occupants = agents.filter { poi.contains(it.position) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("poi_inspector_card"),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xF0131926),
        border = androidx.compose.foundation.BorderStroke(1.dp, poi.accentColor.copy(alpha = 0.8f)),
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = poi.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = poi.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = poi.description,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Present Citizens (${occupants.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8)
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (occupants.isEmpty()) {
                    Text(text = "None currently inside", fontSize = 11.sp, color = Color(0xFF64748B))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        occupants.forEach { citizen ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(citizen.color.copy(alpha = 0.2f))
                                    .border(1.dp, citizen.color, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = citizen.name,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
