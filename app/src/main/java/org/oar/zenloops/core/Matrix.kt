package org.oar.zenloops.core

import org.oar.zenloops.models.Position


class Matrix<T>(
    val maxW: Int,
    val maxH: Int,
): LinkedHashMap<Int, T>() {

    private val maxIndex = maxW * maxH

    inline fun forEachValueIndexed(action: (Int, Int, T) -> Unit) {
        forEach { (index, value) ->
            val xy = getXY(index)
            action(xy.x, xy.y, value)
        }
    }

    inline fun forEachWithPosition(action: (Position, T) -> Unit) {
        forEach { (index, value) ->
            val position = getXY(index)
            action(position, value)
        }
    }

    inline fun forEachWithPositionComplete(action: (Position, T?) -> Unit) {
        for (i in 0 until maxW) {
            for (j in 0 until maxH) {
                val index = getIndex(i, j)
                action(Position(i, j), this[index])
            }
        }
    }

    fun contains(x: Int, y: Int): Boolean {
        if (!validate(x, y)) {
            return false
        }
        val index = getIndex(x, y)
        return this.contains(index)
    }

    operator fun get(x: Int, y: Int): T? {
        if (!validate(x, y)) {
            return null
        }
        val index = getIndex(x, y)
        return this[index]
    }
    operator fun get(pos: Position) = this[pos.x, pos.y]

    operator fun set(x: Int, y: Int, value: T) {
        if (validate(x, y)) {
            val index = getIndex(x, y)
            this[index] = value
        }
    }
    operator fun set(pos: Position, value: T) {
        if (validate(pos.x, pos.y)) {
            val index = getIndex(pos.x, pos.y)
            this[index] = value
        }
    }

    fun getIndex(x: Int, y: Int) = y * maxW + x
    fun getXY(index: Int) = Position(index % maxW, index / maxW)

    private fun validate(x: Int, y: Int): Boolean {
        if (x >= maxW) return false
        if (y >= maxH) return false
        if (getIndex(x, y) >= maxIndex) return false
        return true
    }
}