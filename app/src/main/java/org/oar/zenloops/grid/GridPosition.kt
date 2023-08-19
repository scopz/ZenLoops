package org.oar.zenloops.grid

import android.view.View
import org.oar.zenloops.elements.LinkGraphics.MAX_SCALE
import org.oar.zenloops.elements.LinkGraphics.MIN_SCALE
import org.oar.zenloops.utils.ScreenProperties.toDpi

class GridPosition(
    posX: Float = 0f,
    posY: Float = 0f,
    scale: Float = 0.2491268f.toDpi(),
) {
    var posX = posX
        private set
    var posY = posY
        private set
    var scale = scale
        private set

    private var contentWidth = 0f
    private var contentHeight = 0f

    fun setContentDimensions(w: Float, h: Float, view: View) {
        contentWidth = w
        contentHeight = h
        translate(0f, 0f, view)
    }

    fun setValues(posX: Float, posY: Float, scale: Float) {
        this.posX = posX
        this.posY = posY
        this.scale = scale
    }

    fun translate(x: Float, y: Float, view: View) {
        posX += x
        posY += y

        // Check limits:
        val widthHalf = view.width / 2f
        if (posX > widthHalf)
            posX = widthHalf
        else if (posX < widthHalf - contentWidth * scale)
            posX = widthHalf - contentWidth * scale

        val heightHalf = view.height / 2f
        if (posY > heightHalf)
            posY = heightHalf
        else if (posY < heightHalf - contentHeight * scale)
            posY = heightHalf - contentHeight * scale
    }

    fun zoom(ratio: Float, view: View) {
        val newScale = (scale * ratio).coerceIn(MIN_SCALE, MAX_SCALE)
        val ratioApplied = newScale / scale
        scale = newScale

        // Use center screen as anchor:
        val widthHalf = view.width / 2f
        posX = widthHalf - (widthHalf - posX) * ratioApplied

        val heightHalf = view.height / 2f
        posY = heightHalf - (heightHalf - posY) * ratioApplied
    }
}