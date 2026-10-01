package com.example.simulation.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

/**
 * Dynamic weather conditions influencing agents' speed, stamina drain, and municipal conditions.
 */
enum class WeatherType(
    val displayName: String,
    val emoji: String,
    val speedMultiplier: Float,
    val energyDrainMultiplier: Float,
    val description: String
) {
    CLEAR("Clear & Sunny", "☀️", 1.0f, 1.0f, "Optimal weather. Normal travel and stamina."),
    RAIN("Rainfall", "🌧️", 0.80f, 1.25f, "Slick streets reduce travel speed; chilly rain drains extra stamina."),
    THUNDERSTORM("Thunderstorm", "⛈️", 0.65f, 1.50f, "Heavy wind and thunder heavily slow movement and exhaust citizens."),
    HEATWAVE("Heatwave", "☀️🔥", 0.85f, 1.40f, "Blistering heat causes citizens to fatigue rapidly."),
    SNOW("Gentle Snow", "❄️", 0.75f, 1.30f, "Icy flurries cool the city, slowing foot traffic.")
}

/**
 * Active weather state container with remaining in-game hours duration.
 */
data class WeatherState(
    val current: WeatherType = WeatherType.CLEAR,
    val durationHoursRemaining: Float = 3.5f
)

/**
 * City stockpile and global municipal statistics.
 */
data class CityStockpile(
    val foodStock: Int = 120,
    val timberStock: Int = 45,
    val mineralStock: Int = 30,
    val totalWorkDone: Int = 0,
    val socialInteractionsCount: Int = 0
)

/**
 * World container managing the boundaries, POIs, day/night cycle, weather system, and city stockpile.
 */
data class SimulationWorld(
    val width: Float = 1000f,
    val height: Float = 1350f,
    val pois: List<PointOfInterest> = createDefaultPois(),
    val stockpile: CityStockpile = CityStockpile(),
    val timeOfDayHours: Float = 8.0f, // 08:00 AM start
    val simulationDay: Int = 1,
    val weather: WeatherState = WeatherState(),
    val recentNews: List<String> = listOf("MicroLife simulation initialized with 20 autonomous citizens.")
) {
    /**
     * Normalized day phase (0.0 at midnight, 0.5 at noon).
     */
    val isNight: Boolean
        get() = timeOfDayHours < 6.0f || timeOfDayHours > 20.0f

    /**
     * Formatted digital clock: "08:45 AM (Day 1)"
     */
    val formattedClock: String
        get() {
            val totalMinutes = (timeOfDayHours * 60).toInt() % (24 * 60)
            val h24 = totalMinutes / 60
            val m = totalMinutes % 60
            val amPm = if (h24 < 12) "AM" else "PM"
            val h12 = if (h24 == 0) 12 else if (h24 > 12) h24 - 12 else h24
            return "%02d:%02d %s (Day %d)".format(h12, m, amPm, simulationDay)
        }

    fun findPoiById(id: String): PointOfInterest? = pois.find { it.id == id }

    fun findPoiByType(type: POIType): PointOfInterest? = pois.find { it.type == type }

    companion object {
        fun createDefaultPois(): List<PointOfInterest> = listOf(
            PointOfInterest(
                id = "poi_food",
                name = "Berry Orchard & Farm",
                type = POIType.FOOD_SOURCE,
                bounds = Rect(40f, 110f, 440f, 400f),
                color = Color(0xFF1E3A2B),
                accentColor = Color(0xFF2ECC71),
                emoji = "🍏",
                description = "Nutritious orchard and hydroponic berry garden. Satisfies hunger.",
                resourceType = "Berries"
            ),
            PointOfInterest(
                id = "poi_rest",
                name = "Cozy Cabins & Dorms",
                type = POIType.REST_AREA,
                bounds = Rect(560f, 110f, 960f, 400f),
                color = Color(0xFF1A2744),
                accentColor = Color(0xFF4A90E2),
                emoji = "🛌",
                description = "Warm sleeping pods and plush beds. Restores stamina and energy."
            ),
            PointOfInterest(
                id = "poi_plaza",
                name = "Central Plaza & Fountain",
                type = POIType.TOWN_PLAZA,
                bounds = Rect(310f, 500f, 690f, 790f),
                color = Color(0xFF32274A),
                accentColor = Color(0xFF9B59B6),
                emoji = "⛲",
                description = "Vibrant gathering plaza with marble fountain. Social hub of the city."
            ),
            PointOfInterest(
                id = "poi_work",
                name = "Workshop & Quarry",
                type = POIType.WORK_AREA,
                bounds = Rect(40f, 890f, 440f, 1180f),
                color = Color(0xFF3D271D),
                accentColor = Color(0xFFE67E22),
                emoji = "⚒️",
                description = "Timber yard and artisan crafting workshop. Trains gathering skills and builds stockpile.",
                resourceType = "Timber & Ore"
            ),
            PointOfInterest(
                id = "poi_park",
                name = "Zen Garden & Lake",
                type = POIType.RECREATION_PARK,
                bounds = Rect(560f, 890f, 960f, 1180f),
                color = Color(0xFF19323A),
                accentColor = Color(0xFF1ABC9C),
                emoji = "🌸",
                description = "Peaceful walking paths, cherry blossom trees, and koi lake. Alleviates boredom."
            )
        )
    }
}
