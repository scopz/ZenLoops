package org.oar.zenloops.models

import java.util.*
import kotlin.math.abs

data class Position (
    val x: Int,
    val y: Int,
) {
    fun distance(position: Position) = distance(position.x, position.y)
    fun distance(x: Int, y: Int) = distance(x.toFloat(), y.toFloat())
    fun distance(x: Float, y: Float): Float {
        return abs(this.x - x) + abs(this.y - y)
    }

    fun toPoint() = Point(x.toFloat(), y.toFloat())

    override fun toString(): String {
        return "Position($x, $y)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as Position
        return x == other.x && y == other.y
    }
    override fun hashCode() = Objects.hash(x, y)
}