package org.oar.zenloops.generator

import org.oar.zenloops.core.Matrix
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.elements.LinkPosition
import org.oar.zenloops.models.Position
import org.oar.zenloops.utils.GridUtils.getPossibleLinks
import java.util.*
import kotlin.math.roundToInt

class RootRandomGenerator(
) : RandomGenerator() {

    companion object {
        private val BOTTOM_LINKS = listOf(LinkPosition.BOT_LEFT, LinkPosition.BOT_RIGHT, LinkPosition.BOT)
    }

    override fun generateNewGrid(grid: Grid) {
        val w = grid.width
        val h = grid.height

        val random = Random()
        val schema = Matrix<NodePositions>(w, h)

        val totalLinks = (w * h * (random.nextFloat() * tolerance * 2-tolerance+percent))
            .roundToInt() - addVerticalRoot(grid, schema)

        addRandomLinks(grid, schema, totalLinks)

        grid.links.apply {
            clear()
            putAll(schema.toLinkMatrix())
        }
    }

    private fun addVerticalRoot(grid: Grid, schema: Matrix<NodePositions>): Int {
        val random = Random()

        val w = schema.maxW
        val h = schema.maxH

        var position = Position(random.nextInt(w), 0)
        var node = getOrCreateNode(schema, position)

        while (position.y + 1 < h) {
            val newPos = grid.getPossibleLinks(position)
                .filter { BOTTOM_LINKS.contains(it.direction) }
                .shuffled()
                .first()
                .pos

            node.add(newPos)
            node = getOrCreateNode(schema, newPos)
                .apply { add(position) }

            position = newPos
        }

        return h
    }
}