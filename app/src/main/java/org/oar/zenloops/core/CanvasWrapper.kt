package org.oar.zenloops.core

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import org.oar.zenloops.grid.GridPosition
import java.io.Closeable

class CanvasWrapper(
    val canvas: Canvas,
    gridPosition: GridPosition,
    view: View
): Closeable {

    val viewport: Rect

    init {
        val posX = gridPosition.posX
        val posY = gridPosition.posY
        val scale = gridPosition.scale

        canvas.save()
        canvas.translate(posX, posY)
        canvas.scale(scale, scale)

        viewport = Rect().apply {
            left = (-posX / scale).toInt()
            right = ((view.width - posX) / scale).toInt()
            top = (-posY / scale).toInt()
            bottom = ((view.height - posY) / scale).toInt()
        }
    }

    override fun close() {
        canvas.restore()
    }
}