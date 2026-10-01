package com.example.simulation.engine

import androidx.compose.ui.geometry.Offset
import com.example.simulation.ai.UtilityBrain
import com.example.simulation.model.Agent
import com.example.simulation.model.AgentState
import com.example.simulation.model.CityStockpile
import com.example.simulation.model.Personality
import com.example.simulation.model.SimulationWorld
import com.example.simulation.model.WeatherState
import com.example.simulation.model.WeatherType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance, offline 2D simulation loop engine for the micro-city.
 * Updates agent physics, AI state transitions, need depletion, skill progression,
 * and day/night environmental cycles.
 */
class SimulationEngine {

    /**
     * Initializes exactly 20 autonomous agents.
     * The first three agents are guaranteed to be named "Reno", "Ather", and "Liora".
     */
    fun createInitialAgents(world: SimulationWorld): List<Agent> {
        val personalities = Personality.values()
        return (0 until 20).map { id ->
            val name = if (id < Agent.PRESET_NAMES.size) Agent.PRESET_NAMES[id] else "Citizen-$id"
            val color = Agent.AGENT_PALETTES[id % Agent.AGENT_PALETTES.size]
            val personality = personalities[id % personalities.size]

            // Spawn clustered around town plaza with initial spread
            val angle = (id / 20f) * 2f * Math.PI.toFloat()
            val radius = 60f + (id % 5) * 25f
            val spawnX = (world.width / 2f) + cos(angle) * radius
            val spawnY = (world.height / 2f) + sin(angle) * radius

            Agent(
                id = id,
                name = name,
                color = color,
                personality = personality,
                position = Offset(spawnX, spawnY),
                hunger = 60f + (id * 9 % 35),
                energy = 70f + (id * 11 % 25),
                social = 50f + (id * 13 % 45),
                boredom = 20f + (id * 7 % 30),
                skillLevel = if (id == 0) 2 else 1, // Reno starts with minor experience
                recentThoughts = listOf("Excited to settle into the micro-city!")
            )
        }
    }

    /**
     * Advances the entire micro-city simulation by delta-time seconds (scaled by speed multiplier).
     */
    fun tick(
        world: SimulationWorld,
        agents: List<Agent>,
        dt: Float
    ): Pair<SimulationWorld, List<Agent>> {
        val clampedDt = dt.coerceIn(0.001f, 0.25f)

        // 1. Advance World Clock (e.g. 24 real seconds = 1 in-game hour at 1x)
        val hoursPerSecond = 1.0f / 18.0f // 1 game day lasts ~7.2 minutes at 1x
        val elapsedHours = clampedDt * hoursPerSecond
        var newHours = world.timeOfDayHours + elapsedHours
        var newDay = world.simulationDay
        if (newHours >= 24f) {
            newHours -= 24f
            newDay += 1
        }

        // Dynamic Weather Update: Periodically transitions world weather conditions
        var currentWeather = world.weather.current
        var remainingWeatherHours = world.weather.durationHoursRemaining - elapsedHours
        val updatedNews = world.recentNews.toMutableList()

        if (remainingWeatherHours <= 0f) {
            // Probabilistic weather shift
            val roll = (0..100).random()
            val nextWeather = when {
                roll < 35 -> WeatherType.CLEAR
                roll < 58 -> WeatherType.RAIN
                roll < 74 -> WeatherType.HEATWAVE
                roll < 88 -> WeatherType.THUNDERSTORM
                else -> WeatherType.SNOW
            }
            currentWeather = nextWeather
            remainingWeatherHours = 2.5f + (roll % 25) / 10f // 2.5 to 5.0 in-game hours

            val speedPct = (currentWeather.speedMultiplier * 100).toInt()
            val staminaPct = (currentWeather.energyDrainMultiplier * 100).toInt()
            val alert = "Weather Shift: ${currentWeather.displayName} ${currentWeather.emoji} has set in! (Speed: ${speedPct}%, Stamina Drain: ${staminaPct}%)"
            updatedNews.add(0, alert)
            if (updatedNews.size > 8) updatedNews.removeLast()
        }

        val updatedWeather = WeatherState(
            current = currentWeather,
            durationHoursRemaining = remainingWeatherHours
        )

        var updatedStockpile = world.stockpile

        // 2. Update each agent's physics, needs, and AI state machine
        val updatedAgents = agents.mapIndexed { index, currentAgent ->
            var agent = currentAgent

            // A. Deplete or adjust dynamic needs (energy drain is influenced by current weather)
            val hungerDrain = 1.2f * clampedDt
            val baseEnergyDrain = if (agent.activeState == AgentState.GATHERING) 3.5f * clampedDt else 1.0f * clampedDt
            val energyDrain = baseEnergyDrain * currentWeather.energyDrainMultiplier
            val socialDrain = 1.1f * clampedDt
            val boredomGain = if (agent.activeState == AgentState.GATHERING) 1.8f * clampedDt else 1.2f * clampedDt

            var newHunger = (agent.hunger - hungerDrain).coerceIn(0f, 100f)
            var newEnergy = (agent.energy - energyDrain).coerceIn(0f, 100f)
            var newSocial = (agent.social - socialDrain).coerceIn(0f, 100f)
            var newBoredom = (agent.boredom + boredomGain).coerceIn(0f, 100f)

            var newSkillLevel = agent.skillLevel
            var newSkillXp = agent.skillXp
            var newTasksCompleted = agent.tasksCompleted
            var newResourcesGathered = agent.resourcesGathered
            val newThoughts = agent.recentThoughts.toMutableList()
            var newEmote = agent.activeEmote
            var newEmoteDuration = (agent.emoteDuration - clampedDt).coerceAtLeast(0f)
            if (newEmoteDuration <= 0f) {
                newEmote = null
            }

            var nextState = agent.activeState
            var stateDuration = agent.stateDuration + clampedDt
            var targetPos = agent.targetPosition

            // B. State Execution & Logic
            when (agent.activeState) {
                AgentState.WANDERING -> {
                    // Check if needs demand immediate attention
                    if (newHunger < 30f || newEnergy < 25f || newBoredom > 70f || stateDuration > 5.0f) {
                        agent = UtilityBrain.decideNextState(
                            agent.copy(hunger = newHunger, energy = newEnergy, social = newSocial, boredom = newBoredom),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }

                AgentState.SEEKING_FOOD -> {
                    val foodPoi = world.findPoiById(agent.targetPoiId ?: "poi_food")
                    if (targetPos == null && foodPoi != null) {
                        targetPos = foodPoi.getInteriorTarget(agent.id)
                    }
                    if (targetPos != null && distance(agent.position, targetPos) < 40f) {
                        nextState = AgentState.EATING
                        stateDuration = 0f
                        newEmote = "🍎"
                        newEmoteDuration = 4.0f
                    }
                }

                AgentState.EATING -> {
                    // Replenish hunger and slight energy
                    newHunger = (newHunger + 25f * clampedDt).coerceIn(0f, 100f)
                    newEnergy = (newEnergy + 5f * clampedDt).coerceIn(0f, 100f)
                    newEmote = "🍎"

                    if (stateDuration >= 3.5f || newHunger >= 98f) {
                        newThoughts.add(0, "Enjoyed delicious fresh orchard berries.")
                        if (newThoughts.size > 8) newThoughts.removeLast()
                        agent = UtilityBrain.decideNextState(
                            agent.copy(hunger = newHunger, energy = newEnergy, recentThoughts = newThoughts),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }

                AgentState.SEEKING_REST -> {
                    val restPoi = world.findPoiById(agent.targetPoiId ?: "poi_rest")
                    if (targetPos == null && restPoi != null) {
                        targetPos = restPoi.getInteriorTarget(agent.id)
                    }
                    if (targetPos != null && distance(agent.position, targetPos) < 40f) {
                        nextState = AgentState.SLEEPING
                        stateDuration = 0f
                        newEmote = "💤"
                        newEmoteDuration = 6.0f
                    }
                }

                AgentState.SLEEPING -> {
                    // Restore energy and reduce boredom
                    newEnergy = (newEnergy + 20f * clampedDt).coerceIn(0f, 100f)
                    newBoredom = (newBoredom - 10f * clampedDt).coerceIn(0f, 100f)
                    newEmote = "💤"

                    if (stateDuration >= 5.0f || newEnergy >= 98f) {
                        newThoughts.add(0, "Woke up feeling deeply rested and energized.")
                        if (newThoughts.size > 8) newThoughts.removeLast()
                        agent = UtilityBrain.decideNextState(
                            agent.copy(energy = newEnergy, boredom = newBoredom, recentThoughts = newThoughts),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }

                AgentState.SEEKING_WORK -> {
                    val workPoi = world.findPoiById(agent.targetPoiId ?: "poi_work")
                    if (targetPos == null && workPoi != null) {
                        targetPos = workPoi.getInteriorTarget(agent.id)
                    }
                    if (targetPos != null && distance(agent.position, targetPos) < 40f) {
                        nextState = AgentState.GATHERING
                        stateDuration = 0f
                        newEmote = "⛏️"
                        newEmoteDuration = 5.0f
                    }
                }

                AgentState.GATHERING -> {
                    // Gathering task execution: efficiency multiplier speeds up completion!
                    val effectiveWorkSpeed = agent.efficiencyMultiplier
                    newEmote = "⛏️"

                    // If critical hunger or exhaustion strikes, abandon work
                    if (newHunger < 20f || newEnergy < 15f) {
                        agent = UtilityBrain.decideNextState(
                            agent.copy(hunger = newHunger, energy = newEnergy),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    } else if (stateDuration >= (4.0f / effectiveWorkSpeed)) {
                        // Task Completed!
                        newTasksCompleted += 1
                        val yield = (1 + (agent.skillLevel * 0.75f)).toInt()
                        newResourcesGathered += yield

                        // Update city stockpile
                        updatedStockpile = updatedStockpile.copy(
                            timberStock = updatedStockpile.timberStock + yield,
                            mineralStock = updatedStockpile.mineralStock + (yield / 2).coerceAtLeast(1),
                            totalWorkDone = updatedStockpile.totalWorkDone + 1
                        )

                        // Gain Skill XP (The more they work, the more skilled they become)
                        val xpGain = 25f * effectiveWorkSpeed
                        newSkillXp += xpGain

                        // Check Level Up!
                        if (newSkillXp >= agent.xpForNextLevel) {
                            newSkillXp -= agent.xpForNextLevel
                            newSkillLevel += 1
                            newThoughts.add(0, "⭐ Promoted! Advanced to Gathering Skill Lv. $newSkillLevel!")
                            newEmote = "⭐"
                            newEmoteDuration = 3.5f

                            if (updatedNews.size > 6) updatedNews.removeLast()
                            updatedNews.add(0, "${agent.name} mastered Gathering Skill Level $newSkillLevel!")
                        } else {
                            newThoughts.add(0, "Harvested $yield resources at the workshop (Lv. $newSkillLevel).")
                        }
                        if (newThoughts.size > 8) newThoughts.removeLast()

                        agent = UtilityBrain.decideNextState(
                            agent.copy(
                                skillLevel = newSkillLevel,
                                skillXp = newSkillXp,
                                tasksCompleted = newTasksCompleted,
                                resourcesGathered = newResourcesGathered,
                                recentThoughts = newThoughts
                            ),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }

                AgentState.SEEKING_SOCIAL -> {
                    // Track moving partner if present
                    val partnerId = agent.targetAgentId
                    val partner = if (partnerId != null) agents.find { it.id == partnerId } else null
                    val destination = partner?.position ?: targetPos

                    if (destination != null && distance(agent.position, destination) < 55f) {
                        nextState = AgentState.SOCIALIZING
                        stateDuration = 0f
                        newEmote = "💬"
                        newEmoteDuration = 3.5f
                        updatedStockpile = updatedStockpile.copy(
                            socialInteractionsCount = updatedStockpile.socialInteractionsCount + 1
                        )
                    } else if (stateDuration > 8.0f) {
                        // Timed out seeking, pick a new goal
                        agent = UtilityBrain.decideNextState(agent, world, agents)
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }

                AgentState.SOCIALIZING -> {
                    newSocial = (newSocial + 30f * clampedDt).coerceIn(0f, 100f)
                    newBoredom = (newBoredom - 25f * clampedDt).coerceIn(0f, 100f)
                    newEmote = "💬"

                    if (stateDuration >= 3.0f || newSocial >= 95f) {
                        val partnerName = agent.targetAgentId?.let { pid -> agents.find { it.id == pid }?.name } ?: "citizens"
                        newThoughts.add(0, "Had an inspiring conversation with $partnerName.")
                        if (newThoughts.size > 8) newThoughts.removeLast()

                        agent = UtilityBrain.decideNextState(
                            agent.copy(social = newSocial, boredom = newBoredom, recentThoughts = newThoughts),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }

                AgentState.ENTERTAINING -> {
                    newBoredom = (newBoredom - 30f * clampedDt).coerceIn(0f, 100f)
                    newEmote = "🌸"

                    if (stateDuration >= 3.5f || newBoredom <= 10f) {
                        newThoughts.add(0, "Relaxed by the peaceful lake and cherry blossoms.")
                        if (newThoughts.size > 8) newThoughts.removeLast()

                        agent = UtilityBrain.decideNextState(
                            agent.copy(boredom = newBoredom, recentThoughts = newThoughts),
                            world,
                            agents
                        )
                        nextState = agent.activeState
                        targetPos = agent.targetPosition
                        stateDuration = 0f
                    }
                }
            }

            // C. Smooth Movement & Pathfinding towards targetPosition
            var currentPos = agent.position
            var currentFacing = agent.facingAngle
            var currentVel = Offset.Zero

            val isStationaryState = nextState == AgentState.SLEEPING ||
                    nextState == AgentState.EATING ||
                    nextState == AgentState.GATHERING ||
                    nextState == AgentState.SOCIALIZING

            if (!isStationaryState && targetPos != null) {
                val dx = targetPos.x - currentPos.x
                val dy = targetPos.y - currentPos.y
                val dist = sqrt(dx * dx + dy * dy)

                if (dist > 6f) {
                    val nx = dx / dist
                    val ny = dy / dist
                    val speed = agent.moveSpeed * currentWeather.speedMultiplier * (if (nextState == AgentState.SEEKING_FOOD && newHunger < 20f) 1.25f else 1.0f)
                    currentVel = Offset(nx * speed, ny * speed)
                    currentPos = Offset(currentPos.x + currentVel.x * clampedDt, currentPos.y + currentVel.y * clampedDt)
                    currentFacing = atan2(ny, nx)
                }
            }

            // Weather reaction thought (occasional ambient awareness)
            if (currentWeather != WeatherType.CLEAR && (agent.id + (newHours * 10).toInt()) % 17 == 0 && stateDuration < clampedDt * 2) {
                val weatherThought = when (currentWeather) {
                    WeatherType.RAIN -> "The rain makes the cobblestones slick."
                    WeatherType.THUNDERSTORM -> "The thunderstorm is fierce! Hard to move."
                    WeatherType.HEATWAVE -> "The heatwave is scorching and exhausting."
                    WeatherType.SNOW -> "Snow flurries are blanketing the avenues."
                    WeatherType.CLEAR -> null
                }
                if (weatherThought != null && (newThoughts.isEmpty() || newThoughts.first() != weatherThought)) {
                    newThoughts.add(0, weatherThought)
                    if (newThoughts.size > 8) newThoughts.removeLast()
                }
            }

            // D. Soft Separation Force: Agents repel each other gently when overlapping
            var separationX = 0f
            var separationY = 0f
            for (j in agents.indices) {
                if (j != index) {
                    val other = agents[j]
                    val odx = currentPos.x - other.position.x
                    val ody = currentPos.y - other.position.y
                    val odist = sqrt(odx * odx + ody * ody)
                    if (odist in 0.1f..32f) {
                        val force = (32f - odist) / 32f
                        separationX += (odx / odist) * force * 35f * clampedDt
                        separationY += (ody / odist) * force * 35f * clampedDt
                    }
                }
            }
            currentPos = Offset(currentPos.x + separationX, currentPos.y + separationY)

            // E. Strict Screen Boundaries Clamping (Enclosed micro-city)
            val margin = 26f
            val clampedX = currentPos.x.coerceIn(margin, world.width - margin)
            val clampedY = currentPos.y.coerceIn(margin + 50f, world.height - margin - 30f)
            currentPos = Offset(clampedX, clampedY)

            agent.copy(
                position = currentPos,
                velocity = currentVel,
                facingAngle = currentFacing,
                hunger = newHunger,
                energy = newEnergy,
                social = newSocial,
                boredom = newBoredom,
                skillLevel = newSkillLevel,
                skillXp = newSkillXp,
                tasksCompleted = newTasksCompleted,
                resourcesGathered = newResourcesGathered,
                recentThoughts = newThoughts,
                activeState = nextState,
                stateDuration = stateDuration,
                targetPosition = targetPos,
                activeEmote = newEmote,
                emoteDuration = newEmoteDuration
            )
        }

        val updatedWorld = world.copy(
            timeOfDayHours = newHours,
            simulationDay = newDay,
            weather = updatedWeather,
            stockpile = updatedStockpile,
            recentNews = updatedNews
        )

        return Pair(updatedWorld, updatedAgents)
    }

    private fun distance(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}
