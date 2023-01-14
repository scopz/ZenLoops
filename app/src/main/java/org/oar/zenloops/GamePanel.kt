package org.oar.zenloops

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.core.content.ContextCompat
import org.oar.zenloops.core.Animate
import org.oar.zenloops.core.ViewAnimator
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.elements.LinkGraphics
import java.io.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class GamePanel(
    context: Context,
    load: Boolean
) : ViewAnimator(context), Animate {

    private var grid: Grid
    private val localFileSaveState: String

    private val gestureDetector = GestureDetector(context, GestureListener())
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
        .apply { isQuickScaleEnabled = false }

    // BACKGROUND & ANIMATION
    private val bgColor = ContextCompat.getColor(context, R.color.backgroundColor)
    private val endBgColor = ContextCompat.getColor(context, R.color.endBackgroundColor)
    private var currentBgColor = bgColor
    private val argbEvaluator = ArgbEvaluator()
    private var animationStep = 0f
    private var finished = false

    // CONTROLS:
    private var isResizing = false
    private var isMoving = false
    private var width = 0f
    private var height = 0f
    private var xPos = 0f
    private var yPos = 0f
    private var wScaled = 0f
    private var hScaled = 0f
    private var zoom = 0f
    private var minZoom = 0.3f
    private var maxZoom = 2f

    private var initialTouchedPositionX = 0f
    private var initialTouchedPositionY = 0f

    init {
        localFileSaveState = ContextWrapper(context).filesDir.path + "/state.save"
        LinkGraphics.loadGraphics(context)

        val loadedGrid = if (load) loadState() else null
        grid = loadedGrid ?: Grid(this, 16, 35, 0.7f).apply { randomFill() }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (grid.isFinished != finished) {
            finished = !finished
            addAnimation(this)
        }

        canvas.drawColor(currentBgColor)
        grid.draw(canvas, xPos, yPos, zoom, wScaled, hScaled)
    }

    override fun updateAnimation(): Boolean {
        animationStep = min(animationStep + 0.15f, 1f)

        currentBgColor = if (grid.isFinished)
            argbEvaluator.evaluate(animationStep, bgColor, endBgColor) as Int
        else
            argbEvaluator.evaluate(animationStep, endBgColor, bgColor) as Int

        if (animationStep >= 1) {
            animationStep = 0f
            return false
        }
        return true
    }

    fun saveState() {
        try {
            File(localFileSaveState.substring(0, localFileSaveState.lastIndexOf("/"))).mkdirs()
            val f = File(localFileSaveState)
            if (f.exists()) f.delete()
            val fos = FileOutputStream(f)
            val dos = DataOutputStream(fos)
            var str = grid.toPack()
            str += ":" + -xPos.toInt() + ":" + -yPos.toInt() + ":" + (zoom * 100).toInt() + "|"
            val map = str.toCharArray()
            for (c in map) dos.writeChar(c.code)
            dos.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadState(): Grid? {
        try {
            val f = File(localFileSaveState)
            val fis = FileInputStream(f)
            val dis = DataInputStream(fis)
            var str = ""
            while (true) {
                val c = dis.readChar()
                if (c == '|') break
                str += c
            }
            dis.close()
            fis.close()
            val read = str.split(":").toTypedArray()
            val grid = Grid(
                this,
                read[0].toInt(),
                read[1].toInt(),
                read[2].toInt() / 10000f,
                read[3].toInt() / 10000f
            )
            grid.fromPack(read[4])
            xPos = -read[5].toInt().toFloat()
            yPos = -read[6].toInt().toFloat()
            zoom = read[7].toInt() / 100f
            wScaled = width / zoom
            hScaled = height / zoom
            postInvalidate()
            return grid
        } catch (e: EOFException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.width = w.toFloat()
        this.height = h.toFloat()
        val viewPort = grid.viewport

        minZoom = min(
            this.width / min(viewPort.width, 3164),
            this.height / min(viewPort.height, 5780)) * 0.64f

        maxZoom = min(
            this.width / LinkGraphics.WIDTH / 3,
            this.height / LinkGraphics.HEIGHT / 3)

        if (zoom == 0f) { // defaultZoom
            zoom = (minZoom * 3 + maxZoom) / 4
        }
        hScaled = h / zoom
        wScaled = w / zoom
        super.onSizeChanged(w, h, oldw, oldh)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        if (!scaleDetector.isInProgress) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchedPositionX = event.x
                    initialTouchedPositionY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isResizing) return true

                    val dx = ((event.x - initialTouchedPositionX) / zoom).roundToInt()
                    val dy = ((event.y - initialTouchedPositionY) / zoom).roundToInt()

                    if (isMoving || abs(dx) + abs(dy) > 35) {
                        initialTouchedPositionX = event.x
                        initialTouchedPositionY = event.y
                        xPos += dx.toFloat()
                        yPos += dy.toFloat()
                        adjustXY()
                        isMoving = true
                        postInvalidate()
                    }
                }
                MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_UP -> {
                    isMoving = false
                    isResizing = false
                }
                MotionEvent.ACTION_CANCEL -> {}
            }
        }
        return true //super.onTouchEvent(event);
    }

    private fun adjustXY() {
        val viewPort = grid.viewport
        val halfWScaled = wScaled / 2
        val halfHScaled = hScaled / 2
        viewPort.width += halfWScaled.toInt()
        viewPort.height += halfHScaled.toInt()

        //halfWScaled = Math.max(halfWScaled,2000);
        if (xPos > halfWScaled) xPos = halfWScaled
        if (yPos > halfHScaled) yPos = halfHScaled

        val iw = wScaled - xPos
        val ih = hScaled - yPos

        if (iw > viewPort.width)  xPos = wScaled - viewPort.width
        if (ih > viewPort.height) yPos = hScaled - viewPort.height
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            isResizing = true
            val ratio = detector.scaleFactor
            val iScale = zoom

            zoom *= ratio
            zoom = max(minZoom, min(zoom, maxZoom))

            wScaled = width / zoom
            hScaled = height / zoom

            val dd = 1 / zoom - 1 / iScale
            xPos += dd * width / 2
            yPos += dd * height / 2

            postInvalidate()
            return true
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onLongPress(event: MotionEvent) {
            //if (grid.isFinished()){
            if (!isResizing && !isMoving) {
                grid.restartNew()
                postInvalidate()
            }
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            val x = event.x / zoom - xPos
            val y = event.y / zoom - yPos

            val success = grid.rotate(x, y)
            if (success) {
                grid.validate()
                postInvalidate()
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (e.action != MotionEvent.ACTION_DOWN) isMoving = true
            if (e.action != MotionEvent.ACTION_UP) return false
            onSingleTapUp(e)
            return true
        }
    }
}