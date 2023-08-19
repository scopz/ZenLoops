package org.oar.zenloops.elements

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import org.oar.zenloops.R
import org.oar.zenloops.models.LinkType

object LinkGraphics {

    const val WIDTH = 200
    const val HEIGHT = 200

    const val MARGIN_X = 156
    const val MARGIN_Y_STEP = 90
    const val MARGIN_Y = MARGIN_Y_STEP * 3

    const val MIN_SCALE = 0.15f
    const val MAX_SCALE = 2f

    //private Paint paint;

    private var vectors = mutableMapOf<LinkType, Bitmap>()
    private var vectorsLinked = mutableMapOf<LinkType, Bitmap>()

    fun loadGraphics(ctx: Context) {
        vectors[LinkType.SINGLE] = getBitmap(ctx, R.drawable.f_simple_dark)
        vectors[LinkType.DOUBLE] = getBitmap(ctx, R.drawable.f_link_dark)
        vectors[LinkType.TRIPLE] = getBitmap(ctx, R.drawable.f_double_dark)
        vectors[LinkType.TRIPLE_B] = getBitmap(ctx, R.drawable.f_triple_dark)

        vectorsLinked[LinkType.SINGLE] = getBitmap(ctx, R.drawable.f_simple)
        vectorsLinked[LinkType.DOUBLE] = getBitmap(ctx, R.drawable.f_link)
        vectorsLinked[LinkType.TRIPLE] = getBitmap(ctx, R.drawable.f_double)
        vectorsLinked[LinkType.TRIPLE_B] = getBitmap(ctx, R.drawable.f_triple)
    }

    fun getBitmap(type: LinkType): Bitmap {
        return vectors[type]
            ?: throw RuntimeException("Vector not found for type \"${type.name}\"")
    }

    fun getLinkedBitmap(type: LinkType): Bitmap {
        return vectorsLinked[type]
            ?: throw RuntimeException("VectorLinked not found for type \"${type.name}\"")
    }

    private fun getBitmap(ctx: Context, @DrawableRes drawableRes: Int): Bitmap {
        val maxW = (WIDTH * MAX_SCALE).toInt()
        val maxH = (HEIGHT * MAX_SCALE).toInt()
        return AppCompatResources.getDrawable(ctx, drawableRes)?.toBitmap(maxW, maxH)
            ?: throw Exception("Drawable $drawableRes couldn't be found")
    }
}