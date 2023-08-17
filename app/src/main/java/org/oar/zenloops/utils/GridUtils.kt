package org.oar.zenloops.utils

import org.oar.zenloops.core.Matrix
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.elements.Link
import org.oar.zenloops.elements.LinkGraphics
import org.oar.zenloops.elements.LinkPosition
import org.oar.zenloops.grid.GridPosition
import org.oar.zenloops.models.LinkType
import org.oar.zenloops.models.NeighborLink
import org.oar.zenloops.models.Point
import org.oar.zenloops.models.Position
import kotlin.math.roundToInt

object GridUtils {
    fun Grid.getLinkByScreenCoords(screenPoint: Point, gridPosition: GridPosition): Link? {
        val scale = gridPosition.scale
        val relativeX = (screenPoint.x - gridPosition.posX) / scale
        val relativeY = (screenPoint.y - gridPosition.posY) / scale

        if (relativeX < 0 || relativeY < 0 || relativeX >= pxWidth || relativeY >= pxHeight)
            return null

        return getLinkByCoords(
            Position(relativeX.roundToInt(), relativeY.roundToInt())
        )
    }

    fun Grid.getLinkByCoords(pixelPos: Position): Link? {
        val gridCoord = Position(
            pixelPos.x / LinkGraphics.MARGIN_X,
            pixelPos.y / LinkGraphics.MARGIN_Y
        )

        var currentDist = Float.MAX_VALUE
        var currentLink: Link? = null

        getSurroundingLinks(gridCoord)
            .forEach {
                val dist = pixelPos.distance(it.rect.exactCenterX(), it.rect.exactCenterY())
                if (dist < currentDist) {
                    currentDist = dist
                    currentLink = it
                }
            }

        return if (currentDist > LinkGraphics.WIDTH) null else currentLink
    }



    fun Grid.getPossibleLinks(pos: Position): List<NeighborLink> {
        val list = mutableListOf<NeighborLink>()

        val x = pos.x
        val y = pos.y

        val even = x % 2 == 0
        val isT = y % 2 == 0

        if (even == isT) {
            if (y > 0) list.add(NeighborLink(LinkPosition.TOP, Position(x, y - 1)))
            if (x > 0) list.add(NeighborLink(LinkPosition.BOT_LEFT, Position(x - 1, y)))
            if (x < width - 1) list.add(NeighborLink(LinkPosition.BOT_RIGHT, Position(x + 1, y)))
        } else {
            if (x > 0) list.add(NeighborLink(LinkPosition.TOP_LEFT, Position(x - 1, y)))
            if (x < width - 1) list.add(NeighborLink(LinkPosition.TOP_RIGHT, Position(x + 1, y)))
            if (y < height - 1) list.add(NeighborLink(LinkPosition.BOT, Position(x, y + 1)))
        }
        return list
    }

    private fun Grid.getSurroundingLinks(pos: Position): List<Link> {
        return mutableListOf(
            links[pos],
            links[pos.x-1, pos.y],
            links[pos.x-1, pos.y-1],
            links[pos.x-1, pos.y+1],
            links[pos.x, pos.y-1],
            links[pos.x, pos.y+1],
            links[pos.x+1, pos.y],
            links[pos.x+1, pos.y-1],
            links[pos.x+1, pos.y+1],
        ).filterNotNull()
    }


    fun Grid.toPack(): String {
        val sb = StringBuilder()
            .append(width).append(":")
            .append(height).append(":")
            .append((percent * 10000).toInt()).append(":")
            .append((tolerance * 10000).toInt()).append(":")

        links.forEachWithPositionComplete { _, l ->
            val char = if (l == null) {
                '_'
            } else {
                val rot = l.rotation
                when (l.type) {
                    LinkType.SINGLE ->   if (rot < 70) 'q' else if (rot < 190) 't' else 'p'
                    LinkType.DOUBLE ->   if (rot < 70) 'e' else if (rot < 190) 'i' else 's'
                    LinkType.TRIPLE ->   if (rot < 70) 'w' else if (rot < 190) 'u' else 'a'
                    LinkType.TRIPLE_B -> if (rot < 70) 'r' else if (rot < 190) 'y' else 'o'
                }
            }
            sb.append(char)
        }

        return sb.toString()
    }

    fun Grid.linksFromPack(str: String): Matrix<Link> {
        val links = Matrix<Link>(width, height)

        var k = 0
        links.forEachWithPositionComplete { position, _ ->
            val character = str[k++]
            when (character) {
                'q' -> Link(LinkType.SINGLE, position)
                't' -> Link(LinkType.SINGLE, position).apply { rotate() }
                'p' -> Link(LinkType.SINGLE, position).apply { rotate(); rotate() }
                'e' -> Link(LinkType.DOUBLE, position)
                'i' -> Link(LinkType.DOUBLE, position).apply { rotate() }
                's' -> Link(LinkType.DOUBLE, position).apply { rotate(); rotate() }
                'w' -> Link(LinkType.TRIPLE, position)
                'u' -> Link(LinkType.TRIPLE, position).apply { rotate() }
                'a' -> Link(LinkType.TRIPLE, position).apply { rotate(); rotate() }
                'r' -> Link(LinkType.TRIPLE_B, position)
                'y' -> Link(LinkType.TRIPLE_B, position).apply { rotate() }
                'o' -> Link(LinkType.TRIPLE_B, position).apply { rotate(); rotate() }
                else -> null
            }?.also { links[position] = it }
        }

        return links
    }
}