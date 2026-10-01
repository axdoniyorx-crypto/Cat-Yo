package com.example.simulation.ai

import androidx.compose.ui.geometry.Offset
import com.example.simulation.model.Agent
import com.example.simulation.model.AgentState
import com.example.simulation.model.POIType
import com.example.simulation.model.Personality
import com.example.simulation.model.SimulationWorld
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Utility evaluation scores computed by an agent's Small Brain.
 */
data class UtilityScores(
    val eat: Float,
    val rest: Float,
    val work: Float,
    val social: Float,
    val entertain: Float,
    val wander: Float
) {
    val highestCategory: String get() {
        val map = mapOf(
            "Food" to eat,
            "Rest" to rest,
            "Work" to work,
            "Social" to social,
            "Entertain" to entertain,
            "Wander" to wander
        )
        return map.maxByOrNull { it.value }?.key ?: "Idle"
    }
}

/**
 * Utility AI and Finite State Machine decision engine.
 * Each NPC independently processes its internal drives, surroundings,
 * and skill progressions to make rational, lifelike decisions.
 */
object UtilityBrain {

    /**
     * Evaluates utility curves for all potential actions based on the agent's
     * live dynamic needs (Hunger, Energy, Social, Boredom), time of day, and personality.
     */
    fun evaluateUtility(
        agent: Agent,
        world: SimulationWorld,
        allAgents: List<Agent>
    ): UtilityScores {
        // 1. Food Utility: Inverse curve of hunger (0 = starving, 100 = full)
        // High urgency when hunger < 30, critical emergency when < 15
        val hungerDeficit = (100f - agent.hunger).coerceIn(0f, 100f) / 100f
        var eatScore = hungerDeficit.pow(2.2f) * 1.5f
        if (agent.hunger < 25f) {
            eatScore += 0.8f // Critical hunger override
        }
        if (agent.personality == Personality.BON_VIVANT) {
            eatScore *= 1.25f
        }

        // 2. Rest Utility: Inverse curve of energy (0 = exhausted, 100 = fully rested)
        val energyDeficit = (100f - agent.energy).coerceIn(0f, 100f) / 100f
        var restScore = energyDeficit.pow(2.0f) * 1.4f
        if (agent.energy < 20f) {
            restScore += 0.9f // Critical exhaustion override
        }
        // Increased sleep drive at night
        if (world.isNight) {
            restScore += 0.45f
        }

        // 3. Social Utility: Inverse curve of social satisfaction
        val socialDeficit = (100f - agent.social).coerceIn(0f, 100f) / 100f
        var socialScore = socialDeficit.pow(1.6f) * 1.1f
        if (agent.personality == Personality.SOCIALITE) {
            socialScore *= 1.4f
        }
        // Boost if other agents are congregating in the Plaza
        val agentsInPlaza = allAgents.count { it.activeState == AgentState.SOCIALIZING || it.activeState == AgentState.SEEKING_SOCIAL }
        if (agentsInPlaza > 0) {
            socialScore += (agentsInPlaza * 0.05f).coerceAtMost(0.3f)
        }

        // 4. Work & Gathering Utility: High when basic physiological needs are fulfilled
        val physiologicalReadiness = (agent.energy / 100f) * (agent.hunger / 100f)
        var workScore = 0.55f * physiologicalReadiness.coerceIn(0f, 1f)
        if (agent.personality == Personality.HARDWORKER) {
            workScore *= 1.4f
        }
        // High skill citizens feel confident working
        workScore += (agent.skillLevel - 1) * 0.05f
        // Don't seek work during deep night or if exhausted
        if (world.isNight || agent.energy < 30f || agent.hunger < 30f) {
            workScore *= 0.2f
        }

        // 5. Entertainment Utility: Increases with boredom
        val boredomScore = (agent.boredom / 100f).pow(1.8f) * 1.1f
        var entertainScore = boredomScore
        if (agent.personality == Personality.SCHOLAR) {
            entertainScore *= 1.2f
        }

        // 6. Base wandering baseline
        val wanderScore = 0.15f

        return UtilityScores(
            eat = eatScore,
            rest = restScore,
            work = workScore,
            social = socialScore,
            entertain = entertainScore,
            wander = wanderScore
        )
    }

    /**
     * Executes the FSM decision update when an agent needs a new goal.
     */
    fun decideNextState(
        agent: Agent,
        world: SimulationWorld,
        allAgents: List<Agent>
    ): Agent {
        val utility = evaluateUtility(agent, world, allAgents)

        // Find highest utility intention
        val highest = maxOf(
            utility.eat,
            utility.rest,
            utility.work,
            utility.social,
            utility.entertain,
            utility.wander
        )

        return when (highest) {
            utility.eat -> {
                val foodPoi = world.findPoiByType(POIType.FOOD_SOURCE)
                val target = foodPoi?.getInteriorTarget(agent.id) ?: Offset(200f, 250f)
                agent.copy(
                    activeState = AgentState.SEEKING_FOOD,
                    targetPosition = target,
                    targetPoiId = foodPoi?.id,
                    targetAgentId = null,
                    stateDuration = 0f,
                    activeEmote = "🍎",
                    emoteDuration = 2.0f
                )
            }
            utility.rest -> {
                val restPoi = world.findPoiByType(POIType.REST_AREA)
                val target = restPoi?.getInteriorTarget(agent.id) ?: Offset(750f, 250f)
                agent.copy(
                    activeState = AgentState.SEEKING_REST,
                    targetPosition = target,
                    targetPoiId = restPoi?.id,
                    targetAgentId = null,
                    stateDuration = 0f,
                    activeEmote = "😴",
                    emoteDuration = 2.0f
                )
            }
            utility.work -> {
                val workPoi = world.findPoiByType(POIType.WORK_AREA)
                val target = workPoi?.getInteriorTarget(agent.id) ?: Offset(200f, 1000f)
                agent.copy(
                    activeState = AgentState.SEEKING_WORK,
                    targetPosition = target,
                    targetPoiId = workPoi?.id,
                    targetAgentId = null,
                    stateDuration = 0f,
                    activeEmote = "⚒️",
                    emoteDuration = 2.0f
                )
            }
            utility.social -> {
                // Find a nearby peer who is also awake and not currently sleeping/eating
                val availablePartner = allAgents
                    .filter { it.id != agent.id && it.activeState != AgentState.SLEEPING && it.activeState != AgentState.EATING }
                    .minByOrNull { distanceBetween(agent.position, it.position) }

                val plazaPoi = world.findPoiByType(POIType.TOWN_PLAZA)
                val target = if (availablePartner != null && distanceBetween(agent.position, availablePartner.position) < 250f) {
                    availablePartner.position
                } else {
                    plazaPoi?.getInteriorTarget(agent.id) ?: Offset(500f, 650f)
                }

                agent.copy(
                    activeState = AgentState.SEEKING_SOCIAL,
                    targetPosition = target,
                    targetPoiId = plazaPoi?.id,
                    targetAgentId = availablePartner?.id,
                    stateDuration = 0f,
                    activeEmote = "💬",
                    emoteDuration = 2.0f
                )
            }
            utility.entertain -> {
                val parkPoi = world.findPoiByType(POIType.RECREATION_PARK)
                val target = parkPoi?.getInteriorTarget(agent.id) ?: Offset(750f, 1000f)
                agent.copy(
                    activeState = AgentState.ENTERTAINING,
                    targetPosition = target,
                    targetPoiId = parkPoi?.id,
                    targetAgentId = null,
                    stateDuration = 0f,
                    activeEmote = "🌸",
                    emoteDuration = 2.0f
                )
            }
            else -> {
                // Idle Wander to random point within city bounds
                val rx = 100f + ((agent.id * 89 + (agent.position.x).toInt()) % 800).toFloat()
                val ry = 150f + ((agent.id * 103 + (agent.position.y).toInt()) % 1050).toFloat()
                agent.copy(
                    activeState = AgentState.WANDERING,
                    targetPosition = Offset(rx.coerceIn(80f, 920f), ry.coerceIn(120f, 1230f)),
                    targetPoiId = null,
                    targetAgentId = null,
                    stateDuration = 0f,
                    activeEmote = null
                )
            }
        }
    }

    private fun distanceBetween(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}
