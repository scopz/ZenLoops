package org.oar.zenloops.generator

import org.oar.zenloops.core.Matrix
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.elements.Link
import org.oar.zenloops.models.LinkType
import org.oar.zenloops.models.Position
import org.oar.zenloops.utils.GridUtils.getPossibleLinks
import java.util.*
import kotlin.math.roundToInt

open class RandomGenerator(
    val percent: Float = 0.76f,
    val tolerance: Float = 0.105f
) : GridGenerator {

    inner class NodePositions: ArrayList<Position>()

    override fun generateNewGrid(grid: Grid) {
        val w = grid.width
        val h = grid.height

        val random = Random()
        val schema = Matrix<NodePositions>(w, h)

        val totalLinks = (w * h * (random.nextFloat() * tolerance * 2-tolerance+percent))
            .roundToInt()

        var i = 0
        while (i < totalLinks) {
            val x = random.nextInt(w)
            val y = random.nextInt(h)
            if (tryAddLink(grid, schema, Position(x, y))) i++
        }

        grid.links.apply {
            clear()
            putAll(schema.toLinkMatrix())
        }
    }

    private fun tryAddLink(grid: Grid, schema: Matrix<NodePositions>, position: Position): Boolean {

        val node = getOrCreateNode(schema, position)
        if (node.size >= 3) return false

        return grid.getPossibleLinks(position)
            .toMutableList()
            .apply { shuffle() }
            .firstOrNull { !node.contains(it.pos) }
            ?.also {
                getOrCreateNode(schema, it.pos).add(position)
                node.add(it.pos)
            } != null
    }



    private fun getOrCreateNode(schema: Matrix<NodePositions>, pos: Position): NodePositions {
        return schema[pos] ?: NodePositions().apply { schema[pos] = this }
    }

    private fun Matrix<NodePositions>.toLinkMatrix(): Matrix<Link> {
        val random = Random()
        val linksMatrix = Matrix<Link>(maxW, maxH)

        this.forEachWithPosition { pos, node ->
            if (node.size > 0) {
                val linkType = LinkType.findByConnections(node.size)

                linksMatrix[pos] = Link(linkType, pos)
                    .apply {
                        repeat(random.nextInt(3)) { rotate() }
                    }
            }
        }

        return linksMatrix
    }
}