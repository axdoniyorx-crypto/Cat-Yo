package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simulation.model.Agent
import com.example.simulation.model.WeatherType

@Composable
fun AgentInspectorSheet(
    agent: Agent?,
    weather: WeatherType = WeatherType.CLEAR,
    onDismiss: () -> Unit,
    onFeed: () -> Unit,
    onRest: () -> Unit,
    onInspire: () -> Unit,
    onSelectNext: () -> Unit,
    onSelectPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = agent != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (agent == null) return@AnimatedVisibility

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("agent_inspector_card"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF141923),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Drag handle & top bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B4861))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Header Row: Avatar, Name, Personality, Cycle Buttons, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Agent Avatar Icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(agent.color)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = agent.activeEmote ?: agent.name.take(1),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = agent.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.testTag("inspector_agent_name")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // State Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1F2B3E))
                                    .border(1.dp, agent.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${agent.activeState.iconEmoji} ${agent.activeState.displayName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                        Text(
                            text = "${agent.personality.label} • Speed: ${agent.efficiencyMultiplier}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Navigation buttons to flip between citizens
                    IconButton(
                        onClick = onSelectPrev,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("prev_agent_button")
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Agent",
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = onSelectNext,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("next_agent_button")
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Agent",
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_inspector_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Inspector",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Weather Impact
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF161F2E))
                        .border(1.dp, Color(0xFF2B3A50), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = weather.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = weather.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    val speedMod = ((weather.speedMultiplier - 1f) * 100).toInt()
                    val drainMod = ((weather.energyDrainMultiplier - 1f) * 100).toInt()
                    Text(
                        text = "Spd: ${if (speedMod >= 0) "+$speedMod%" else "$speedMod%"} • Drain: ${if (drainMod >= 0) "+$drainMod%" else "$drainMod%"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Core Dynamic Stats (0-100)
                Text(
                    text = "VITAL NEEDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatRow(
                    label = "Hunger / Fullness",
                    value = agent.hunger,
                    displayLabel = "${agent.hunger.toInt()}%",
                    barColor = if (agent.hunger < 25f) Color(0xFFEF4444) else Color(0xFF22C55E),
                    icon = "🍎"
                )

                Spacer(modifier = Modifier.height(6.dp))

                StatRow(
                    label = "Energy / Stamina",
                    value = agent.energy,
                    displayLabel = "${agent.energy.toInt()}%",
                    barColor = if (agent.energy < 20f) Color(0xFFEF4444) else Color(0xFF3B82F6),
                    icon = "⚡"
                )

                Spacer(modifier = Modifier.height(6.dp))

                StatRow(
                    label = "Social Satisfaction",
                    value = agent.social,
                    displayLabel = "${agent.social.toInt()}%",
                    barColor = Color(0xFFEC4899),
                    icon = "💬"
                )

                Spacer(modifier = Modifier.height(6.dp))

                StatRow(
                    label = "Mental Stimulation",
                    value = agent.stimulationPercent,
                    displayLabel = "${agent.stimulationPercent.toInt()}% (Boredom: ${agent.boredom.toInt()}%)",
                    barColor = Color(0xFFA855F7),
                    icon = "🌸"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Progression & Memory Learning
                Text(
                    text = "SKILL & LEARNING PROGRESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B2332))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gathering Skill Level ${agent.skillLevel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+${((agent.efficiencyMultiplier - 1.0f) * 100).toInt()}% Yield",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // XP Bar
                        val xpProgress = (agent.skillXp / agent.xpForNextLevel).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier
                                .width(190.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFF59E0B),
                            trackColor = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${agent.skillXp.toInt()} / ${agent.xpForNextLevel.toInt()} XP to Lv. ${agent.skillLevel + 1}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${agent.tasksCompleted} Tasks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "${agent.resourcesGathered} Harvested",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Recent Activity Log / Memories
                Text(
                    text = "RECENT MEMORY & THOUGHTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F141E))
                        .padding(10.dp)
                ) {
                    agent.recentThoughts.take(3).forEach { thought ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = "• ", color = Color(0xFF38BDF8), fontSize = 12.sp)
                            Text(text = thought, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Overseer Touch Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onFeed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("feed_agent_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A2B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Feed", modifier = Modifier.size(16.dp), tint = Color(0xFF4ADE80))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Feed", fontSize = 12.sp, color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRest,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rest_agent_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = "Rest", modifier = Modifier.size(16.dp), tint = Color(0xFF60A5FA))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Energize", fontSize = 12.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onInspire,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inspire_agent_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37243E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Inspire", modifier = Modifier.size(16.dp), tint = Color(0xFFF472B6))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Inspire", fontSize = 12.sp, color = Color(0xFFF472B6), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: Float,
    displayLabel: String,
    barColor: Color,
    icon: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, fontSize = 12.sp, color = Color(0xFFCBD5E1))
            }
            Text(text = displayLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFF1E293B)
        )
    }
}
