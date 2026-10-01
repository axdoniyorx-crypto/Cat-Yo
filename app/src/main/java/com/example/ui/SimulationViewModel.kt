package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simulation.engine.SimulationEngine
import com.example.simulation.model.Agent
import com.example.simulation.model.PointOfInterest
import com.example.simulation.model.SimulationWorld
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State holding the entire simulation presentation layer.
 */
data class SimulationUiState(
    val world: SimulationWorld = SimulationWorld(),
    val agents: List<Agent> = emptyList(),
    val selectedAgentId: Int? = null,
    val selectedPoi: PointOfInterest? = null,
    val speedMultiplier: Float = 1.0f,
    val isPaused: Boolean = false,
    val showStatsDialog: Boolean = false,
    val followSelectedAgent: Boolean = false
) {
    val selectedAgent: Agent?
        get() = agents.find { it.id == selectedAgentId }
}

class SimulationViewModel : ViewModel() {

    private val engine = SimulationEngine()

    private val _uiState = MutableStateFlow(
        SimulationUiState(
            world = SimulationWorld(),
            agents = engine.createInitialAgents(SimulationWorld()),
            selectedAgentId = 0 // Default to Reno
        )
    )
    val uiState: StateFlow<SimulationUiState> = _uiState.asStateFlow()

    init {
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                val nowNanos = System.nanoTime()
                val elapsedSeconds = ((nowNanos - lastTimeNanos) / 1_000_000_000.0f).coerceIn(0.005f, 0.1f)
                lastTimeNanos = nowNanos

                val current = _uiState.value
                if (!current.isPaused && current.speedMultiplier > 0f) {
                    val effectiveDt = elapsedSeconds * current.speedMultiplier
                    val (updatedWorld, updatedAgents) = engine.tick(
                        current.world,
                        current.agents,
                        effectiveDt
                    )

                    _uiState.value = current.copy(
                        world = updatedWorld,
                        agents = updatedAgents
                    )
                }

                // Maintain ~45-50 ticks per second for silky smooth continuous movement
                delay(22L)
            }
        }
    }

    fun selectAgent(agentId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedAgentId = agentId,
            selectedPoi = null
        )
    }

    fun selectPoi(poi: PointOfInterest?) {
        _uiState.value = _uiState.value.copy(
            selectedPoi = poi
        )
    }

    fun dismissInspector() {
        _uiState.value = _uiState.value.copy(
            selectedAgentId = null,
            selectedPoi = null
        )
    }

    fun setSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(
            speedMultiplier = speed,
            isPaused = speed == 0f
        )
    }

    fun togglePause() {
        val current = _uiState.value
        _uiState.value = current.copy(
            isPaused = !current.isPaused
        )
    }

    fun toggleStatsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStatsDialog = show)
    }

    // --- Interactive God-Mode Powers ---

    fun feedSelectedAgent() {
        val id = _uiState.value.selectedAgentId ?: return
        val currentAgents = _uiState.value.agents.map { agent ->
            if (agent.id == id) {
                val thoughts = agent.recentThoughts.toMutableList()
                thoughts.add(0, "A heavenly snack appeared! (+35 Hunger)")
                if (thoughts.size > 8) thoughts.removeLast()
                agent.copy(
                    hunger = (agent.hunger + 35f).coerceAtMost(100f),
                    recentThoughts = thoughts,
                    activeEmote = "🍏",
                    emoteDuration = 2.5f
                )
            } else agent
        }
        _uiState.value = _uiState.value.copy(agents = currentAgents)
    }

    fun restSelectedAgent() {
        val id = _uiState.value.selectedAgentId ?: return
        val currentAgents = _uiState.value.agents.map { agent ->
            if (agent.id == id) {
                val thoughts = agent.recentThoughts.toMutableList()
                thoughts.add(0, "Blessed with rejuvenating energy (+30 Energy)")
                if (thoughts.size > 8) thoughts.removeLast()
                agent.copy(
                    energy = (agent.energy + 30f).coerceAtMost(100f),
                    recentThoughts = thoughts,
                    activeEmote = "⚡",
                    emoteDuration = 2.5f
                )
            } else agent
        }
        _uiState.value = _uiState.value.copy(agents = currentAgents)
    }

    fun inspireSelectedAgent() {
        val id = _uiState.value.selectedAgentId ?: return
        val currentAgents = _uiState.value.agents.map { agent ->
            if (agent.id == id) {
                var xp = agent.skillXp + 45f
                var level = agent.skillLevel
                val thoughts = agent.recentThoughts.toMutableList()
                if (xp >= agent.xpForNextLevel) {
                    xp -= agent.xpForNextLevel
                    level += 1
                    thoughts.add(0, "Divine inspiration prompted a Skill Level Up! (Lv. $level)")
                } else {
                    thoughts.add(0, "Felt deeply inspired to excel in craftsmanship! (+45 XP)")
                }
                if (thoughts.size > 8) thoughts.removeLast()
                agent.copy(
                    skillLevel = level,
                    skillXp = xp,
                    boredom = (agent.boredom - 30f).coerceAtLeast(0f),
                    recentThoughts = thoughts,
                    activeEmote = "✨",
                    emoteDuration = 3.0f
                )
            } else agent
        }
        _uiState.value = _uiState.value.copy(agents = currentAgents)
    }

    fun resetSimulation() {
        val defaultWorld = SimulationWorld()
        _uiState.value = SimulationUiState(
            world = defaultWorld,
            agents = engine.createInitialAgents(defaultWorld),
            selectedAgentId = 0,
            speedMultiplier = 1.0f,
            isPaused = false
        )
    }
}
