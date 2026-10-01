package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AgentInspectorSheet
import com.example.ui.CityStatsDialog
import com.example.ui.PoiInspectorCard
import com.example.ui.SimulationCanvas
import com.example.ui.SimulationHUD
import com.example.ui.SimulationViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MicroLifeScreen()
            }
        }
    }
}

@Composable
fun MicroLifeScreen(
    viewModel: SimulationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button to dismiss inspector sheet if open
    BackHandler(enabled = uiState.selectedAgentId != null || uiState.selectedPoi != null) {
        viewModel.dismissInspector()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0E17)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Core 2D World Simulation Canvas (Responsive Scaling)
            SimulationCanvas(
                world = uiState.world,
                agents = uiState.agents,
                selectedAgentId = uiState.selectedAgentId,
                onSelectAgent = { agentId -> viewModel.selectAgent(agentId) },
                onSelectPoi = { poi -> viewModel.selectPoi(poi) },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Top Municipal HUD & Quick Citizen Carousel
            SimulationHUD(
                world = uiState.world,
                agents = uiState.agents,
                selectedAgentId = uiState.selectedAgentId,
                speedMultiplier = uiState.speedMultiplier,
                isPaused = uiState.isPaused,
                onSelectAgent = { agentId -> viewModel.selectAgent(agentId) },
                onSetSpeed = { speed -> viewModel.setSpeed(speed) },
                onTogglePause = { viewModel.togglePause() },
                onOpenStats = { viewModel.toggleStatsDialog(true) },
                onReset = { viewModel.resetSimulation() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 3. POI Inspector Card (when a POI is clicked)
            if (uiState.selectedPoi != null && uiState.selectedAgentId == null) {
                PoiInspectorCard(
                    poi = uiState.selectedPoi,
                    agents = uiState.agents,
                    onDismiss = { viewModel.dismissInspector() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 4. Agent Inspector Modal Sheet (when an agent is clicked)
            // Displays exact live dynamic stats: Hunger, Energy, Social, Boredom, and Skill Progression
            val selectedAgent = uiState.selectedAgent
            if (selectedAgent != null) {
                AgentInspectorSheet(
                    agent = selectedAgent,
                    weather = uiState.world.weather.current,
                    onDismiss = { viewModel.dismissInspector() },
                    onFeed = { viewModel.feedSelectedAgent() },
                    onRest = { viewModel.restSelectedAgent() },
                    onInspire = { viewModel.inspireSelectedAgent() },
                    onSelectNext = {
                        val nextId = (selectedAgent.id + 1) % uiState.agents.size
                        viewModel.selectAgent(nextId)
                    },
                    onSelectPrev = {
                        val prevId = (selectedAgent.id - 1 + uiState.agents.size) % uiState.agents.size
                        viewModel.selectAgent(prevId)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 5. Municipal Statistics & Leaderboard Dialog
            if (uiState.showStatsDialog) {
                CityStatsDialog(
                    world = uiState.world,
                    agents = uiState.agents,
                    onDismiss = { viewModel.toggleStatsDialog(false) },
                    onSelectAgent = { agentId -> viewModel.selectAgent(agentId) },
                    onResetSimulation = { viewModel.resetSimulation() }
                )
            }
        }
    }
}
