package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.simulation.model.Agent
import com.example.simulation.model.SimulationWorld

@Composable
fun CityStatsDialog(
    world: SimulationWorld,
    agents: List<Agent>,
    onDismiss: () -> Unit,
    onSelectAgent: (Int) -> Unit,
    onResetSimulation: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("city_stats_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF111723),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3A52))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MicroLife Municipal Registry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Municipal Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2435))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "COMMUNITY SUMMARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Population", "${agents.size} AI NPCs")
                            StatItem("Total Tasks", "${world.stockpile.totalWorkDone}")
                            StatItem("Social Bonds", "${world.stockpile.socialInteractionsCount}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Citizen Roster & Skill Rankings
                Text(
                    text = "CITIZEN ROSTER & SKILL RANKINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val sortedAgents = agents.sortedWith(
                    compareByDescending<Agent> { it.skillLevel }
                        .thenByDescending { it.skillXp }
                        .thenByDescending { it.resourcesGathered }
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sortedAgents) { agent ->
                        CitizenRosterItem(
                            agent = agent,
                            rank = sortedAgents.indexOf(agent) + 1,
                            onClick = {
                                onSelectAgent(agent.id)
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reset button
                Button(
                    onClick = {
                        onResetSimulation()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_simulation_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Simulation",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Re-seed Simulation World",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun CitizenRosterItem(
    agent: Agent,
    rank: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161E2E))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Text(
            text = "#$rank",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) Color(0xFFF59E0B) else Color(0xFF64748B),
            modifier = Modifier.width(24.dp)
        )

        // Agent color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(agent.color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = agent.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (agent.id < 3) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Founding Member",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }
            Text(
                text = "${agent.activeState.iconEmoji} ${agent.activeState.displayName} • ${agent.personality.label}",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Skill Lv. ${agent.skillLevel}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Text(
                text = "${agent.resourcesGathered} Harvested",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
