package org.oar.zenloops.core

class Matrix<T>(
    val maxW: Int,
    val maxH: Int,
) {
    private val maxIndex = maxW * maxH
    val map = mutableMapOf<Int, T>()

    inline fun forEach(action: (T) -> Unit) {
        map.values.forEach { action(it) }
    }

    inline fun forEachIndexed(action: (Int, Int, T) -> Unit) {
        map.forEach { (index, value) ->
            val xy = getXY(index)
            action(xy[0], xy[1], value)
        }
    }

    inline fun forEachIndexedComplete(action: (Int, Int, T?) -> Unit) {
        for (i in 0 until maxW) {
            for (j in 0 until maxH) {
                val index = getIndex(i, j)
                action(i, j, map[index])
            }
        }
    }

    operator fun get(x: Int, y: Int): T? {
        validate(x, y)
        val index = getIndex(x, y)
        return map[index]
    }

    operator fun set(x: Int, y: Int, value: T) {
        validate(x, y)
        val index = getIndex(x, y)
        map[index] = value
    }

    fun getIndex(x: Int, y: Int) = y * maxW + x
    fun getXY(index: Int) = arrayOf(index % maxW, index / maxW)

    private fun validate(x: Int, y: Int) {
        if (x >= maxW)
            throw ArrayIndexOutOfBoundsException(x)

        if (y >= maxH)
            throw ArrayIndexOutOfBoundsException(y)

        val index = getIndex(x, y)
        if (index >= maxIndex)
            throw ArrayIndexOutOfBoundsException(index)
    }
}