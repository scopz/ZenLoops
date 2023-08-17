package org.oar.zenloops.elements

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import org.oar.zenloops.R
import org.oar.zenloops.models.LinkType

object LinkGraphics {

    const val WIDTH = 200
    const val HEIGHT = 200

    const val MARGIN_X = 156
    const val MARGIN_Y_STEP = 90
    const val MARGIN_Y = MARGIN_Y_STEP * 3

    //private Paint paint;

    private var vectors = mutableMapOf<LinkType, Drawable>()
    private var vectorsLinked = mutableMapOf<LinkType, Drawable>()

    fun loadGraphics(ctx: Context) {
        vectors[LinkType.SINGLE] = getDrawable(ctx, R.drawable.i_simple_dark)
        vectors[LinkType.DOUBLE] = getDrawable(ctx, R.drawable.i_link_dark)
        vectors[LinkType.TRIPLE] = getDrawable(ctx, R.drawable.i_double_dark)
        vectors[LinkType.TRIPLE_B] = getDrawable(ctx, R.drawable.i_triple_dark)

        vectorsLinked[LinkType.SINGLE] = getDrawable(ctx, R.drawable.i_simple)
        vectorsLinked[LinkType.DOUBLE] = getDrawable(ctx, R.drawable.i_link)
        vectorsLinked[LinkType.TRIPLE] = getDrawable(ctx, R.drawable.i_double)
        vectorsLinked[LinkType.TRIPLE_B] = getDrawable(ctx, R.drawable.i_triple)
    }

    fun getDrawable(type: LinkType): Drawable {
        return vectors[type]!!
    }

    fun getLinkedDrawable(type: LinkType): Drawable {
        return vectorsLinked[type]!!
    }

    private fun getDrawable(ctx: Context, drawableRes: Int): Drawable {
        return AppCompatResources.getDrawable(ctx, drawableRes)
            ?: throw Exception("Drawable $drawableRes couldn't be found")
    }
}