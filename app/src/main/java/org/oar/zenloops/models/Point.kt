package org.oar.zenloops.models

import java.util.Objects
import kotlin.math.abs

data class Point (
    val x: Float,
    val y: Float,
) {
    fun zero() = x == 0f && y == 0f

    fun add(point: Point): Point {
        return add(point.x, point.y)
    }

    fun add(x: Float, y: Float): Point {
        return Point(
            this.x + x,
            this.y + y,
        )
    }

    fun distance(point: Point): Float {
        return abs(x - point.x) + abs(y - point.y)
    }

    fun toPosition() = Position(x.toInt(), y.toInt())
    override fun toString(): String {
        return "Point($x, $y)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as Point
        return x == other.x && y == other.y
    }
    override fun hashCode() = Objects.hash(x, y)
}