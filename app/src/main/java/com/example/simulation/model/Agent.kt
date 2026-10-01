package com.example.simulation.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Finite State Machine active states for autonomous AI agents.
 */
enum class AgentState(val displayName: String, val iconEmoji: String) {
    WANDERING("Wandering", "🚶"),
    SEEKING_FOOD("Seeking Food", "🍎"),
    EATING("Eating", "🍴"),
    SEEKING_REST("Seeking Rest", "😴"),
    SLEEPING("Sleeping", "💤"),
    SEEKING_WORK("Seeking Work", "⚒️"),
    GATHERING("Gathering", "⛏️"),
    SEEKING_SOCIAL("Seeking Social", "💬"),
    SOCIALIZING("Socializing", "🗣️"),
    ENTERTAINING("Entertaining", "🎉")
}

/**
 * Distinct personality archetype modifying utility decision weights.
 */
enum class Personality(val label: String) {
    BALANCED("Balanced Citizen"),
    HARDWORKER("Industrious Builder"),
    SOCIALITE("Outgoing Conversationalist"),
    BON_VIVANT("Epicurean Explorer"),
    SCHOLAR("Curious Thinker")
}

/**
 * Represents one of the 20 autonomous AI citizens living in the micro-city.
 */
data class Agent(
    val id: Int,
    val name: String,
    val color: Color,
    val personality: Personality,
    // Spatial & Physics properties
    val position: Offset,
    val velocity: Offset = Offset.Zero,
    val targetPosition: Offset? = null,
    val facingAngle: Float = 0f,
    val moveSpeed: Float = 75f,

    // Dynamic depleting needs (0.0 to 100.0)
    // 100 = fully satisfied, 0 = critically depleted
    val hunger: Float = 70f + (id * 7 % 30),
    val energy: Float = 80f - (id * 5 % 30),
    val social: Float = 60f + (id * 9 % 35),
    val boredom: Float = 25f + (id * 11 % 40), // 0 = stimulated/happy, 100 = critically bored

    // Progression, Learning & Memory
    val skillLevel: Int = 1,
    val skillXp: Float = 0f,
    val tasksCompleted: Int = 0,
    val resourcesGathered: Int = 0,
    val recentThoughts: List<String> = listOf("Arrived at the micro-city"),

    // State Machine & AI Execution
    val activeState: AgentState = AgentState.WANDERING,
    val stateDuration: Float = 0f,
    val targetPoiId: String? = null,
    val targetAgentId: Int? = null,
    val activeEmote: String? = null,
    val emoteDuration: Float = 0f
) {
    /**
     * Skill efficiency multiplier derived from learning and experience.
     * High skill allows agents to gather resources faster and complete tasks quicker.
     */
    val efficiencyMultiplier: Float
        get() = 1.0f + (skillLevel - 1) * 0.25f

    /**
     * Fullness percentage (100% = satisfied, 0% = starving)
     */
    val fullnessPercent: Float get() = hunger.coerceIn(0f, 100f)

    /**
     * Energy percentage (100% = energetic, 0% = exhausted)
     */
    val energyPercent: Float get() = energy.coerceIn(0f, 100f)

    /**
     * Social satisfaction percentage (100% = bonded, 0% = lonely)
     */
    val socialPercent: Float get() = social.coerceIn(0f, 100f)

    /**
     * Stimulation percentage (100% = very excited, 0% = bored out of mind)
     */
    val stimulationPercent: Float get() = (100f - boredom).coerceIn(0f, 100f)

    /**
     * Total Experience required to advance to the next skill level.
     */
    val xpForNextLevel: Float get() = skillLevel * 100f

    companion object {
        val PRESET_NAMES = listOf(
            "Reno",     // Required Agent 1
            "Ather",    // Required Agent 2
            "Liora",    // Required Agent 3
            "Kael",
            "Vesper",
            "Dax",
            "Maya",
            "Zephyr",
            "Lyra",
            "Orion",
            "Silas",
            "Nova",
            "Finn",
            "Cassian",
            "Rowan",
            "Tessa",
            "Bram",
            "Mira",
            "Jace",
            "Elora"
        )

        val AGENT_PALETTES = listOf(
            Color(0xFF38EF7D), // Emerald neon
            Color(0xFF00C9FF), // Cyan electric
            Color(0xFFFF416C), // Vibrant rose
            Color(0xFFFFA07A), // Light salmon
            Color(0xFF9D50BB), // Royal purple
            Color(0xFFFFD200), // Sunflower yellow
            Color(0xFF4FACFE), // Azure breeze
            Color(0xFFFF6A00), // Tangerine
            Color(0xFF20BF6B), // Jade
            Color(0xFFEB3B5A), // Crimson
            Color(0xFF0FB9B1), // Turquoise
            Color(0xFF45AAF2), // Dodger blue
            Color(0xFFFA8231), // Orange
            Color(0xFFA55EEA), // Lavender purple
            Color(0xFF26DE81), // Mint
            Color(0xFFFC5C65), // Coral red
            Color(0xFF2bcbba), // Teal
            Color(0xFFfd9644), // Amber
            Color(0xFF8854d0), // Deep violet
            Color(0xFF3867d6)  // Cobalt
        )
    }
}
