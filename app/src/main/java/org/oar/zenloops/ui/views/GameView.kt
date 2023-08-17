package org.oar.zenloops.ui.views

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import org.oar.zenloops.R
import org.oar.zenloops.core.Animate
import org.oar.zenloops.core.CanvasWrapper
import org.oar.zenloops.core.ViewAnimator
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.elements.LinkGraphics
import org.oar.zenloops.grid.GridPosition
import org.oar.zenloops.grid.GridTouchControl
import org.oar.zenloops.grid.GridTouchListener
import org.oar.zenloops.models.Point
import org.oar.zenloops.utils.GridUtils.linksFromPack
import org.oar.zenloops.utils.GridUtils.toPack
import org.oar.zenloops.utils.SaveStateUtils.loadState
import org.oar.zenloops.utils.SaveStateUtils.saveState
import java.io.*
import kotlin.math.min

class GameView(
    context: Context,
    attrs: AttributeSet,
) : ViewAnimator(context, attrs), Animate, GridTouchListener {

    companion object {
        private val gridPosition = GridPosition()
    }

    private lateinit var grid: Grid

    // BACKGROUND & ANIMATION
    private val bgColor = ContextCompat.getColor(context, R.color.backgroundColor)
    private val endBgColor = ContextCompat.getColor(context, R.color.endBackgroundColor)
    private var currentBgColor = bgColor
    private val argbEvaluator = ArgbEvaluator()
    private var animationStep = 0f
    private var finished = false

    private val gridTouchControl = GridTouchControl(context, this)

    fun setGrid(grid: Grid) {
        this.grid = grid

        gridPosition.setContentDimensions(
            grid.pxWidth.toFloat(),
            grid.pxHeight.toFloat(),
            this@GameView)

        grid.initializeLinks()
    }

    override fun onTouchEvent(event: MotionEvent) = gridTouchControl.onTouchEvent(event)

    override fun pressed(x: Float, y: Float) {
        val success = grid.rotate(Point(x, y), gridPosition)
        if (success) {
            grid.checkWin()
            postInvalidate()
        }
    }

    override fun longPressed(x: Float, y: Float) {
        grid.restartNew()
        postInvalidate()
    }
    override fun move(dx: Float, dy: Float) {
        gridPosition.translate(dx, dy, this)
        postInvalidate()
    }

    override fun scale(ratio: Float) {
        gridPosition.zoom(ratio, this)
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (grid.isFinished != finished) {
            finished = !finished
            addAnimation(this)
        }

        canvas.drawColor(currentBgColor)

        CanvasWrapper(canvas, gridPosition, this).use { canvasW ->
            grid.draw(canvasW)
        }

        gridTouchControl.nextScrollAnimation()
    }

    override fun updateAnimation(): Boolean {
        animationStep = min(animationStep + 0.02f, 1f)

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
        var str = grid.toPack()
        str += ":" + -gridPosition.posX.toInt() + ":" + -gridPosition.posY.toInt() + ":" + (gridPosition.scale * 100).toInt()
        context.saveState(str)
    }

    fun loadState(): Grid? {
        try {
            val str = context.loadState()
                ?: return null

            val read = str.split(":").toTypedArray()

            val grid = Grid(
                this,
                width = read[0].toInt(),
                height = read[1].toInt(),
                percent = read[2].toInt() / 10000f,
                tolerance = read[3].toInt() / 10000f
            )

            val loadedLinks = grid.linksFromPack(read[4])
            grid.links.clear()
            grid.links.putAll(loadedLinks)

            gridPosition.setValues(
                -read[5].toInt().toFloat(),
                -read[6].toInt().toFloat(),
                read[7].toInt() / 100f
            )

            return grid
        } catch (e: EOFException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}