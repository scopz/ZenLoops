package org.oar.zenloops.core

import android.content.Context
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToLong

open class ViewAnimator(
    context: Context,
    attrs: AttributeSet,
): View(context, attrs) {

    private val animations = mutableSetOf<Animate>()

    private var framer: Framer? = null

    fun addAnimation(animate: Animate) {
        animations.add(animate)
        framer = framer ?: Framer().apply { start() }
    }

    private fun update() {
        animations.removeIf { !it.updateAnimation() }
    }

    inner class Framer : Thread() {
        override fun run() {
            super.run()

            val time = (1000 / 60).toFloat()

            while (animations.size > 0) {
                try {
                    val initTime = System.nanoTime()

                    update()
                    this@ViewAnimator.postInvalidate()

                    val endTime = System.nanoTime()

                    val sleepTime = (time - (endTime - initTime) / 1000000).roundToLong()
                    if (sleepTime > 0) {
                        sleep(sleepTime)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            framer = null
        }
    }
}