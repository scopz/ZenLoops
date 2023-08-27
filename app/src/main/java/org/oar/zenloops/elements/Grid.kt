package org.oar.zenloops.elements

import org.oar.zenloops.core.CanvasWrapper
import org.oar.zenloops.core.Matrix
import org.oar.zenloops.core.ViewAnimator
import org.oar.zenloops.elements.LinkGraphics.HEIGHT
import org.oar.zenloops.elements.LinkGraphics.MARGIN_X
import org.oar.zenloops.elements.LinkGraphics.MARGIN_Y
import org.oar.zenloops.elements.LinkGraphics.MARGIN_Y_STEP
import org.oar.zenloops.elements.LinkGraphics.WIDTH
import org.oar.zenloops.generator.RandomGenerator
import org.oar.zenloops.grid.GridPosition
import org.oar.zenloops.models.Point
import org.oar.zenloops.utils.GridUtils.getLinkByScreenCoords
import org.oar.zenloops.utils.GridUtils.getPossibleLinks

class Grid(
    private val view: ViewAnimator,
    val width: Int,
    val height: Int,
) {
    val links = Matrix<Link>(width, height)

    val pxWidth = width * MARGIN_X + (WIDTH - MARGIN_X)
    val pxHeight = height * MARGIN_Y + (HEIGHT - MARGIN_Y_STEP)

    var isFinished = false
        private set

    fun restartNew() {
        randomFill()
        initializeLinks()
    }
    fun randomFill() {
        RandomGenerator().generateNewGrid(this)
    }

    fun initializeLinks() {
        links.values.forEach { it.view = view }

        addNeighborsConnections()
        updateLinkedStateFromGrid()
        checkWin()
    }

    fun rotate(point: Point, gridPosition: GridPosition): Boolean {
        val link = getLinkByScreenCoords(point, gridPosition)
            ?: return false

        getConnectedPiece(link)?.forEach { it.setLinkedState(false) }
        link.rotate(true)
        getConnectedPiece(link)?.forEach { it.setLinkedState(true) }
        return true
    }

    // VALIDATOR:
    fun checkWin(): Boolean {
        isFinished = links.values.none { !it.isConnected() }
        return isFinished
    }

    private fun addNeighborsConnections() {
        links.forEachWithPosition { pos, link ->
            getPossibleLinks(pos).forEach {
                val neighbor = links[it.pos]
                if (neighbor != null) link[it.direction] = neighbor
            }
        }
    }
    private fun updateLinkedStateFromGrid() {
        val analyzed = mutableListOf<Link>()
        links.values.forEach { link ->
            if (!analyzed.contains(link)) {
                link.setLinkedState(false)

                getConnectedPiece(link)
                    ?.onEach { it.setLinkedState(true) }
                    ?.also { analyzed.addAll(it) }
                    ?: run { analyzed.add(link) }
            }
        }
    }

    private fun getConnectedPiece(
        link: Link,
        pieceAccumulator: MutableList<Link> = mutableListOf()
    ): List<Link>? {
        pieceAccumulator.add(link)

        if (!link.isConnected()) {
            return null
        }

        val isConnected = link.getConnectedNeighbour()
            .filter { !pieceAccumulator.contains(it) }
            .none { getConnectedPiece(it, pieceAccumulator) == null }

        return if (isConnected) pieceAccumulator else null
    }

    fun draw(canvasW: CanvasWrapper) {
        links.values.forEach {
            it.draw(canvasW)
        }
    }
}
