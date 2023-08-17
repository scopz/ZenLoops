package org.oar.zenloops.grid

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import org.oar.zenloops.models.Point
import org.oar.zenloops.utils.ScreenProperties
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow

class GridTouchControl(
    context: Context,
    val callback: GridTouchListener
) {
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    private var dragPos = Point(0f, 0f)
    private var isResizing = false
    private var isMoving = false

    private var animation: Runnable? = null
    private var speedHistory = mutableListOf<Point>()
    private var lastTime = 0L

    init {
        scaleDetector.isQuickScaleEnabled = false
        gestureDetector.setIsLongpressEnabled(true)
    }

    fun nextScrollAnimation() {
        animation?.run()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        if (!scaleDetector.isInProgress) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    animation = null
                    speedHistory.clear()

                    dragPos = Point(event.x, event.y)
                    lastTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isResizing)
                        return true

                    val dx = event.x - dragPos.x
                    val dy = event.y - dragPos.y

                    if (isMoving || abs(dx) + abs(dy) > 35) {
                        dragPos = Point(event.x, event.y)

                        System.currentTimeMillis()
                            .also {
                                speedHistory.add(Point(dx, dy))
                                if (speedHistory.size > 5) speedHistory.removeAt(0)
                                lastTime = it
                            }

                        isMoving = true

                        callback.move(dx, dy);
                    }
                }
                MotionEvent.ACTION_OUTSIDE,
                MotionEvent.ACTION_UP -> {
                    if ((System.currentTimeMillis() - lastTime) < 25) {
                        continueScrollingAnimation()
                    }
                    isMoving = false
                    isResizing = false
                }
                else -> {}
            }
        }
        return true
    }


    private fun continueScrollingAnimation() {
        val speed = if (speedHistory.isEmpty()) Point(0f, 0f)
            else Point(
                    speedHistory.map { it.x }.average().toFloat(),
                    speedHistory.map { it.y }.average().toFloat(),
                )

        if (speed.zero()) {
            animation = null
            return
        }

        val totalFrames = ScreenProperties.FRAME_RATE
        var frame = 0

        val delta = (totalFrames * (totalFrames + 1) / 2) / (totalFrames + 1)

        var relativeEnd = Point(speed.x * delta, speed.y * delta)
        var relativePos = Point(0f, 0f)

        animation = Runnable {
            val x = (frame / totalFrames)
                .let { x -> cos((x.pow(0.5f) - 1) * Math.PI / 2).toFloat() }

            var dx = relativeEnd.x * x - relativePos.x
            var dy = relativeEnd.y * x - relativePos.y

            if (abs(dx) > abs(speed.x)) {
                relativeEnd = relativeEnd.add(speed.x - dx, 0f)
                dx = speed.x
            }

            if (abs(dy) > abs(speed.y)) {
                relativeEnd = relativeEnd.add(0f, speed.y - dy)
                dy = speed.y
            }

            relativePos = relativePos.add(dx, dy)
            callback.move(dx, dy)

            if (frame > totalFrames) {
                animation = null
            } else {
                frame++
            }
        }.apply { run() }
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            isResizing = true

            val ratio = detector.scaleFactor

            callback.scale(ratio)
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            if (isMoving) return
            callback.longPressed(e.x, e.y)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            callback.pressed(e.x, e.y)
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (e.action != MotionEvent.ACTION_UP) return false
            return if (isMoving) false else onSingleTapUp(e)
        }
    }
}
