package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.simulation.ai.UtilityBrain
import com.example.simulation.engine.SimulationEngine
import com.example.simulation.model.Agent
import com.example.simulation.model.AgentState
import com.example.simulation.model.SimulationWorld
import com.example.simulation.model.WeatherState
import com.example.simulation.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MicroLife: 2D AI Sim", appName)
    }

    @Test
    fun `test exactly 20 agents initialized with required names`() {
        val engine = SimulationEngine()
        val world = SimulationWorld()
        val agents = engine.createInitialAgents(world)

        assertEquals(20, agents.size)
        // First three names must be explicitly Reno, Ather, and Liora
        assertEquals("Reno", agents[0].name)
        assertEquals("Ather", agents[1].name)
        assertEquals("Liora", agents[2].name)
    }

    @Test
    fun `test utility brain prioritizes food when starving`() {
        val world = SimulationWorld()
        val engine = SimulationEngine()
        val agents = engine.createInitialAgents(world)

        // Starving agent
        val starvingAgent = agents[0].copy(
            hunger = 5f,
            energy = 80f,
            social = 80f,
            boredom = 10f
        )

        val nextAgent = UtilityBrain.decideNextState(starvingAgent, world, agents)
        assertEquals(AgentState.SEEKING_FOOD, nextAgent.activeState)
        assertNotNull(nextAgent.targetPosition)
    }

    @Test
    fun `test utility brain prioritizes sleep when exhausted`() {
        val world = SimulationWorld()
        val engine = SimulationEngine()
        val agents = engine.createInitialAgents(world)

        // Exhausted agent
        val exhaustedAgent = agents[0].copy(
            hunger = 90f,
            energy = 5f,
            social = 80f,
            boredom = 10f
        )

        val nextAgent = UtilityBrain.decideNextState(exhaustedAgent, world, agents)
        assertEquals(AgentState.SEEKING_REST, nextAgent.activeState)
        assertNotNull(nextAgent.targetPosition)
    }

    @Test
    fun `test simulation tick advances physics and bounds agents`() {
        val engine = SimulationEngine()
        val world = SimulationWorld()
        val initialAgents = engine.createInitialAgents(world)

        val (nextWorld, updatedAgents) = engine.tick(world, initialAgents, 0.1f)

        assertEquals(20, updatedAgents.size)
        assertTrue(nextWorld.timeOfDayHours >= world.timeOfDayHours)

        // Verify agents remain bounded inside micro-city
        for (agent in updatedAgents) {
            assertTrue("Agent ${agent.name} x is out of bounds", agent.position.x in 20f..world.width)
            assertTrue("Agent ${agent.name} y is out of bounds", agent.position.y in 20f..world.height)
        }
    }

    @Test
    fun `test dynamic weather system influences energy consumption and movement`() {
        val engine = SimulationEngine()
        val initialAgents = engine.createInitialAgents(SimulationWorld())

        // 1. Tick under Clear weather
        val clearWorld = SimulationWorld(weather = WeatherState(current = WeatherType.CLEAR, durationHoursRemaining = 3f))
        val (_, agentsAfterClear) = engine.tick(clearWorld, initialAgents, 0.1f)

        // 2. Tick under Thunderstorm (1.5x stamina drain, 0.65x speed)
        val stormWorld = SimulationWorld(weather = WeatherState(current = WeatherType.THUNDERSTORM, durationHoursRemaining = 3f))
        val (_, agentsAfterStorm) = engine.tick(stormWorld, initialAgents, 0.1f)

        // Compare energy depletion of first agent: Storm must cause higher energy drain
        val clearEnergyDrain = initialAgents[0].energy - agentsAfterClear[0].energy
        val stormEnergyDrain = initialAgents[0].energy - agentsAfterStorm[0].energy

        assertTrue("Storm energy drain ($stormEnergyDrain) must be greater than clear drain ($clearEnergyDrain)",
            stormEnergyDrain > clearEnergyDrain)
    }

    @Test
    fun `test periodic weather transition when duration expires`() {
        val engine = SimulationEngine()
        val initialAgents = engine.createInitialAgents(SimulationWorld())

        // Weather state with duration 0 (should immediately transition)
        val expiringWorld = SimulationWorld(
            weather = WeatherState(current = WeatherType.RAIN, durationHoursRemaining = 0.0001f)
        )

        val (updatedWorld, _) = engine.tick(expiringWorld, initialAgents, 0.1f)

        // New duration must be reset
        assertTrue("New weather duration must be greater than 1 hour", updatedWorld.weather.durationHoursRemaining > 1f)
        // Recent news should announce the weather change
        assertTrue("News should contain weather alert", updatedWorld.recentNews.any { it.contains("Weather") })
    }
}
