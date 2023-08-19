package org.oar.zenloops.elements

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import org.oar.zenloops.core.Animate
import org.oar.zenloops.core.CanvasWrapper
import org.oar.zenloops.core.ViewAnimator
import org.oar.zenloops.elements.LinkGraphics.HEIGHT
import org.oar.zenloops.elements.LinkGraphics.MARGIN_X
import org.oar.zenloops.elements.LinkGraphics.MARGIN_Y
import org.oar.zenloops.elements.LinkGraphics.MARGIN_Y_STEP
import org.oar.zenloops.elements.LinkGraphics.WIDTH
import org.oar.zenloops.models.LinkType
import org.oar.zenloops.models.Position

class Link(
    val type: LinkType,
    val pos: Position,
    var view: ViewAnimator? = null,
) : Animate {
    private val isTopRow: Boolean = (pos.x + pos.y) % 2 == 0

    private val neighbors = mutableMapOf<LinkPosition, Link>()
    private var tentacles = listOf<LinkPosition>()

    private var visibleRotation = 0
    var rotation = 0
        private set

    private var bitmap: Bitmap
    private var linked = false

    val rect = Rect().apply {
        left = pos.x * MARGIN_X
        right = left + WIDTH
        top = pos.y * MARGIN_Y
        if (!isTopRow) {
            top += MARGIN_Y_STEP
        }
        bottom = top + HEIGHT
    }

    init {
        val mutableTentacles = mutableListOf<LinkPosition>()

        if (isTopRow) {
            mutableTentacles.add(LinkPosition.BOT_RIGHT)
            if (type >= LinkType.DOUBLE) {
                mutableTentacles.add(LinkPosition.BOT_LEFT)
                if (type >= LinkType.TRIPLE)
                    mutableTentacles.add(LinkPosition.TOP)
            }

            rotation = 0
            visibleRotation = 0
        } else {
            mutableTentacles.add(LinkPosition.BOT)
            if (type >= LinkType.DOUBLE) {
                mutableTentacles.add(LinkPosition.TOP_LEFT)
                if (type >= LinkType.TRIPLE)
                    mutableTentacles.add(LinkPosition.TOP_RIGHT)
            }

            rotation = 60
            visibleRotation = 60
        }

        tentacles = mutableTentacles
        bitmap = LinkGraphics.getBitmap(type)
    }

    operator fun set(position: LinkPosition, link: Link) {
        neighbors[position] = link
    }

    operator fun get(position: LinkPosition): Link? {
        return neighbors[position]
    }

    fun getConnectedNeighbour() = tentacles.mapNotNull { this[it] }

    fun setLinkedState(linked: Boolean) {
        this.linked = linked

        bitmap = if (linked) {
            LinkGraphics.getLinkedBitmap(type)
        } else {
            LinkGraphics.getBitmap(type)
        }
    }

    fun rotate(animate: Boolean = false) {
        rotation += 120
        rotation %= 360

        tentacles = tentacles.map { it.next }

        if (animate) {
            view?.addAnimation(this)
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
        return tentacles.none {
            val neighbor = neighbors[it]
            neighbor == null || !neighbor.isConnected(this, it.oposite)
        }
    }

    private fun isConnected(other: Link, position: LinkPosition): Boolean {
        return tentacles.contains(position) && neighbors[position] === other
    }

    fun draw(canvasW: CanvasWrapper) {
        if (Rect.intersects(canvasW.viewport, rect)) {
            val canvas = canvasW.canvas

            if (visibleRotation == 0) {
                canvas.drawBitmap(bitmap, null, rect, Paint())

            } else {
                val canvasSaved = canvas.save()
                canvas.rotate(
                    visibleRotation.toFloat(),
                    rect.exactCenterX(),
                    rect.exactCenterY()
                )
                canvas.drawBitmap(bitmap, null, rect, Paint())
                canvas.restoreToCount(canvasSaved)
            }
        }
    }
}