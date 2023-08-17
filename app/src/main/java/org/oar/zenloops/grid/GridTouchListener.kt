package org.oar.zenloops.grid

interface GridTouchListener {
    fun pressed(x: Float, y: Float)
    fun longPressed(x: Float, y: Float)
    fun move(dx: Float, dy: Float)
    fun scale(ratio: Float)
}