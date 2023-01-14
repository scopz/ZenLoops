package org.oar.zenloops.elements

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import org.oar.zenloops.core.Animate
import org.oar.zenloops.core.ViewAnimator

class Link(
    private val view: ViewAnimator,
    val type: LinkType,
    private val posX: Int,
    private val posY: Int,
) : Animate {

    companion object {
        const val MARGS_X = 156
        const val MARGS_Y = 90
    }

    private val isTopRow: Boolean = (posX + posY) % 2 == 0

    private val neighbours = mutableMapOf<LinkPosition, Link>()
    private var active = listOf<LinkPosition>()

    private var visibleRotation = 0
    var rotation = 0
        private set

    private var vector: Drawable
    private var linked = false

    init {
        val mutableActive = mutableListOf<LinkPosition>()

        if (isTopRow) {
            mutableActive.add(LinkPosition.BOT_RIGHT)
            if (type >= LinkType.DOUBLE) {
                mutableActive.add(LinkPosition.BOT_LEFT)
                if (type >= LinkType.TRIPLE)
                    mutableActive.add(LinkPosition.TOP)
            }

            active = mutableActive
            rotation = 0
            visibleRotation = 0
        } else {
            mutableActive.add(LinkPosition.BOT)
            if (type >= LinkType.DOUBLE) {
                mutableActive.add(LinkPosition.TOP_LEFT)
                if (type >= LinkType.TRIPLE)
                    mutableActive.add(LinkPosition.TOP_RIGHT)
            }

            active = mutableActive
            rotation = 60
            visibleRotation = 60
        }
        vector = LinkGraphics.getDrawable(type)
    }


    fun setNeighbour(link: Link, position: LinkPosition) {
        neighbours[position] = link
    }

    fun getConnectedNeighbour() = active.mapNotNull { neighbours[it] }

    fun setLinked(linked: Boolean) {
        this.linked = linked

        vector = if (linked) {
            LinkGraphics.getLinkedDrawable(type)
        } else {
            LinkGraphics.getDrawable(type)
        }
    }

    fun rotate(animate: Boolean = false) {
        rotation += 120
        rotation %= 360

        active = active.map { it.next }

        if (animate) {
            view.addAnimation(this)
        } else {
            visibleRotation = rotation
        }
    }

    override fun updateAnimation(): Boolean {
        visibleRotation += 15
        visibleRotation %= 360
        return visibleRotation != rotation
    }


    fun isConnected(): Boolean {
        return !active.any {
            val neighbor = neighbours[it]
            neighbor == null || !neighbor.isConnected(this, it.oposite)
        }
    }

    private fun isConnected(other: Link, position: LinkPosition): Boolean {
        return active.contains(position) && neighbours[position] === other
    }

    fun itsMe(x: Float, y: Float): Boolean {
        val x0 = (posX * MARGS_X).toFloat()
        var y0 = (posY * MARGS_Y * 3).toFloat()
        if (!isTopRow) {
            y0 += MARGS_Y.toFloat()
        }

        val xf = x0 + LinkGraphics.WIDTH
        val yf = y0 + LinkGraphics.HEIGHT

        return x in x0..xf && y in y0..yf
    }

    fun draw(
        canvas: Canvas,
        parentX: Float,
        parentY: Float,
        scale: Float,
        wCut: Float,
        hCut: Float
    ) {
        val x = posX * MARGS_X + parentX
        var y = posY * MARGS_Y * 3 + parentY
        if (!isTopRow) {
            y += MARGS_Y.toFloat()
        }

        if (x > wCut || y > hCut) {
            return
        }

        val xScaled = (x * scale).toInt()
        val yScaled = (y * scale).toInt()

        val xwScaled = xScaled + LinkGraphics.WIDTH * scale
        val yhScaled = yScaled + LinkGraphics.HEIGHT * scale

        if (xwScaled < 0 || yhScaled < 0) {
            return
        }

        vector.setBounds(xScaled, yScaled, xwScaled.toInt(), yhScaled.toInt())

        if (visibleRotation == 0) {
            vector.draw(canvas)

        } else {
            val canvasSaved = canvas.save()
            canvas.rotate(
                visibleRotation.toFloat(),
                xScaled + LinkGraphics.WIDTH / 2 * scale,
                yScaled + LinkGraphics.HEIGHT / 2 * scale
            )
            vector.draw(canvas)
            canvas.restoreToCount(canvasSaved)
        }
    }
}