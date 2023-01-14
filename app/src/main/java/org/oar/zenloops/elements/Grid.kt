package org.oar.zenloops.elements

import android.graphics.Canvas
import org.oar.zenloops.core.Matrix
import org.oar.zenloops.core.ViewAnimator
import java.util.*
import kotlin.math.roundToInt

class Grid(
    private val view: ViewAnimator,
    width: Int,
    private val height: Int,
    private val percent: Float = 0.755f,
    private val tolerance: Float = 0.105f
) {
    private val mixedRows = width * 2

    private var pieces: Matrix<Link>? = null

    var isFinished = false
        private set

    private val vpWidth = mixedRows * Link.MARGS_X + LinkGraphics.WIDTH - Link.MARGS_X
    private val vpHeight = height * Link.MARGS_Y * 3 + LinkGraphics.HEIGHT - Link.MARGS_Y

    private val wb_v = (LinkGraphics.WIDTH - Link.MARGS_X) / 2 //22

    //private final int hb_v = (LinkGraphics.HEIGHT-Link.MARGS_Y)/2; //55
    private val mr_g = (LinkGraphics.HEIGHT - Link.MARGS_Y * 2) / 2 //10

    private val tramW = Link.MARGS_X * 2
    private val tramH = Link.MARGS_Y * 3

    val viewport: Viewport
        get() = Viewport(vpWidth, vpHeight)

    private fun getLink(x: Float, y: Float): Link? {
        if (x < 0 || y < 0 || x >= vpWidth || y >= vpHeight) {
            return null
        }

        val xx = x - wb_v.toFloat()
        val yy = y - mr_g.toFloat()

        val xTram = xx.toInt() / tramW
        val yTram = yy.toInt() / tramH
        val rx = if (xx % tramW > Link.MARGS_X) 1 else 0

        var resX = xTram * 2 + rx
        var resY = yTram

        if (resX >= mixedRows) {
            resX = mixedRows - 1
        }
        if (resY >= height) {
            resY = height - 1
        }
        return pieces?.let { it[resX, resY] }
    }

    fun rotate(x: Float, y: Float): Boolean {
        val link = getLink(x, y)
        if (link != null) {
            var visited = mutableListOf<Link>()
            var connected = seePieceLinked(link, visited)
            if (connected) {
                visited.forEach { it.setLinked(false) }
            }

            link.rotate(true)

            visited = mutableListOf()
            connected = seePieceLinked(link, visited)
            if (connected) {
                visited.forEach { it.setLinked(true) }
            }
            return true
        }
        return false
    }

    fun restartNew() {
        randomFill()
    }

    fun draw(canvas: Canvas, x: Float, y: Float, scale: Float, limitW: Float, limitH: Float) {
        pieces?.forEach {
            it.draw(canvas, x, y, scale, limitW, limitH)
        }
    }

    // VALIDATOR:
    fun validate(): Boolean {
        mutableListOf<Int>().forEach {
            return false
        }

        pieces?.forEach {
            if (!it.isConnected()) {
                isFinished = false
                return false
            }
        }
        isFinished = true
        return true
    }

    private fun seePieceLinked(link: Link, visited: MutableList<Link>): Boolean {
        visited.add(link)

        if (link.isConnected()) {
            link.getConnectedNeighbour().forEach {
                if (!visited.contains(it) && !seePieceLinked(it, visited)) {
                    return false
                }
            }
            return true
        }
        return false
    }

    // RANDOM FILLER:
    private fun getNeighborLinks(x: Int, y: Int): List<NeighbourLink> {
        val list = mutableListOf<NeighbourLink>()

        val even = x % 2 == 0
        val isT = y % 2 == 0

        //if (!even) isT = !isT
        //if (isT) {
        if (even == isT) {
            if (y > 0) list.add(NeighbourLink(LinkPosition.TOP, x, y - 1))
            if (x > 0) list.add(NeighbourLink(LinkPosition.BOT_LEFT, x - 1, y))
            if (x < mixedRows - 1) list.add(NeighbourLink(LinkPosition.BOT_RIGHT, x + 1, y))
        } else {
            if (x > 0) list.add(NeighbourLink(LinkPosition.TOP_LEFT, x - 1, y))
            if (x < mixedRows - 1) list.add(NeighbourLink(LinkPosition.TOP_RIGHT, x + 1, y))
            if (y < height - 1) list.add(NeighbourLink(LinkPosition.BOT, x, y + 1))
        }
        return list
    }

    fun randomFill() {
        val random = Random()
        val schema = Matrix<Node>(mixedRows, height)

        val pulses = (mixedRows * height * (random.nextFloat() * tolerance * 2-tolerance+percent))
            .roundToInt()

        var i = 0
        while (i < pulses) {
            val x = random.nextInt(mixedRows)
            val y = random.nextInt(height)
            val res = tryAddPulse(schema, x, y)
            if (res) i++
        }

        convert(random, schema)
        examineNeighbours()
        validate()
    }

    private fun tryAddPulse(schema: Matrix<Node>, x: Int, y: Int): Boolean {
        val node = getNode(schema, x, y)
        if (node.value >= 3) return false

        val neighbors = getNeighborLinks(x, y).toMutableList()
        neighbors.shuffle()

        neighbors.forEach {
            val neighbor = getNode(schema, it.x, it.y)
            if (node.value < 3) {
                if (!node.hasLink(it.x, it.y)) {
                    node.addNode(it.x, it.y)
                    neighbor.addNode(x, y)
                    return true
                }
            }
        }
        return false
    }

    private fun getNode(schema: Matrix<Node>, x: Int, y: Int): Node {
        return schema[x, y] ?: Node().also {
            schema[x, y] = it
        }
    }

    private fun convert(random: Random, nodes: Matrix<Node>) {
        pieces = Matrix(mixedRows, height)

        nodes.forEachIndexed { i, j, node ->
            if (node.value > 0) {
                val type = when(node.value) {
                    1 -> LinkType.SINGLE
                    2 -> LinkType.DOUBLE
                    else -> if (random.nextInt(4) > 0) LinkType.TRIPLE else LinkType.TRIPLE_B
                }

                val link = Link(view, type, i, j)
                pieces!![i, j] = link
                val rotations = random.nextInt(3)
                for (rot in 0 until rotations) {
                    link.rotate()
                }
            }
        }
    }

    private fun examineNeighbours() {
        pieces?.forEachIndexed { i, j, link ->
            //if (l.getFreeLinks() > 0) {
            getNeighborLinks(i, j).forEach {
                val nLink = pieces!![it.x, it.y]
                if (nLink != null) link.setNeighbour(nLink, it.direction)
            }
            //}
        }

        // MARK LINKED:
        val analyzed = mutableListOf<Link>()
        pieces?.forEach { link ->
            if (!analyzed.contains(link)) {
                val visited = mutableListOf<Link>()
                val connected = seePieceLinked(link, visited)

                analyzed.addAll(visited)
                visited.forEach { it.setLinked(connected) }
            }
        }
    }

    inner class Node {
        private val nodeLinks = mutableListOf<IntArray>()
        val value
            get() = nodeLinks.size

        fun addNode(x: Int, y: Int): Boolean {
            if (nodeLinks.size < 3) {
                nodeLinks.add(intArrayOf(x, y))
                return true
            }
            return false
        }

        fun hasLink(x: Int, y: Int): Boolean {
            return nodeLinks.any { it[0] == x && it[1] == y }
        }
    }

    inner class NeighbourLink(var direction: LinkPosition, var x: Int, var y: Int)

    fun toPack(): String {
        val sb = StringBuilder()
            .append(mixedRows / 2).append(":")
            .append(height).append(":")
            .append((percent * 10000).toInt()).append(":")
            .append((tolerance * 10000).toInt()).append(":")

        pieces?.forEachIndexedComplete { _, _, l ->
            val char = if (l == null) {
                '_'
            } else {
                val rot = l.rotation
                when (l.type) {
                    LinkType.SINGLE -> if (rot < 70) 'q' else if (rot < 190) 't' else 'p'
                    LinkType.DOUBLE -> if (rot < 70) 'e' else if (rot < 190) 'i' else 's'
                    LinkType.TRIPLE -> if (rot < 70) 'w' else if (rot < 190) 'u' else 'a'
                    LinkType.TRIPLE_B -> if (rot < 70) 'r' else if (rot < 190) 'y' else 'o'
                }
            }
            sb.append(char)
        }

        return sb.toString()
    }

    fun fromPack(str: String) {
        pieces = Matrix(mixedRows, height)

        var k = 0
        pieces!!.forEachIndexedComplete { i, j, _ ->
            val link = when (str[k]) {
                'q' -> Link(view, LinkType.SINGLE, i, j)
                't' -> Link(view, LinkType.SINGLE, i, j).apply { rotate() }
                'p' -> Link(view, LinkType.SINGLE, i, j).apply { rotate(); rotate() }
                'e' -> Link(view, LinkType.DOUBLE, i, j)
                'i' -> Link(view, LinkType.DOUBLE, i, j).apply { rotate() }
                's' -> Link(view, LinkType.DOUBLE, i, j).apply { rotate(); rotate() }
                'w' -> Link(view, LinkType.TRIPLE, i, j)
                'u' -> Link(view, LinkType.TRIPLE, i, j).apply { rotate() }
                'a' -> Link(view, LinkType.TRIPLE, i, j).apply { rotate(); rotate() }
                'r' -> Link(view, LinkType.TRIPLE_B, i, j)
                'y' -> Link(view, LinkType.TRIPLE_B, i, j).apply { rotate() }
                'o' -> Link(view, LinkType.TRIPLE_B, i, j).apply { rotate(); rotate() }
                else -> null
            }
            if (link != null) pieces!![i, j] = link
            k++
        }

        examineNeighbours()
        validate()
    }
}
