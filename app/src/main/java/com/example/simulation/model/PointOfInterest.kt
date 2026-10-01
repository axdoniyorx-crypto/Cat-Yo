package com.example.simulation.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

/**
 * Types of static Points of Interest (POIs) in the micro-city.
 */
enum class POIType {
    FOOD_SOURCE,
    REST_AREA,
    WORK_AREA,
    TOWN_PLAZA,
    RECREATION_PARK
}

/**
 * Represents an enclosed interactive location in the micro-city where agents satisfy
 * their needs, perform tasks, or interact with one another.
 */
data class PointOfInterest(
    val id: String,
    val name: String,
    val type: POIType,
    val bounds: Rect,
    val color: Color,
    val accentColor: Color,
    val emoji: String,
    val description: String,
    val resourceType: String = ""
) {
    val center: Offset get() = Offset(
        bounds.left + bounds.width / 2f,
        bounds.top + bounds.height / 2f
    )

    /**
     * Checks if a point is within this POI's physical zone.
     */
    fun contains(point: Offset): Boolean {
        return bounds.contains(point)
    }

    /**
     * Returns an interior target point with random jitter so multiple agents don't stack on one pixel.
     */
    fun getInteriorTarget(seed: Int): Offset {
        val margin = 24f
        val w = (bounds.width - margin * 2f).coerceAtLeast(10f)
        val h = (bounds.height - margin * 2f).coerceAtLeast(10f)
        val rx = ((seed * 37 + 13) % 100) / 100f
        val ry = ((seed * 59 + 29) % 100) / 100f
        return Offset(
            bounds.left + margin + rx * w,
            bounds.top + margin + ry * h
        )
    }
}
