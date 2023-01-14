package org.oar.zenloops.elements

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import org.oar.zenloops.R
import org.oar.zenloops.elements.LinkGraphics

object LinkGraphics {

    const val WIDTH = 200
    const val HEIGHT = 200
    //private Paint paint;

    private var vectors = mutableMapOf<LinkType, Drawable>()
    private var vectorsLinked = mutableMapOf<LinkType, Drawable>()

    fun loadGraphics(ctx: Context) {
        vectors[LinkType.SINGLE] = getDrawable(ctx, R.drawable.i_simple)
        vectors[LinkType.DOUBLE] = getDrawable(ctx, R.drawable.i_link)
        vectors[LinkType.TRIPLE] = getDrawable(ctx, R.drawable.i_double)
        vectors[LinkType.TRIPLE_B] = getDrawable(ctx, R.drawable.i_triple)

        vectorsLinked[LinkType.SINGLE] = getDrawable(ctx, R.drawable.i_simple_l)
        vectorsLinked[LinkType.DOUBLE] = getDrawable(ctx, R.drawable.i_link_l)
        vectorsLinked[LinkType.TRIPLE] = getDrawable(ctx, R.drawable.i_double_l)
        vectorsLinked[LinkType.TRIPLE_B] = getDrawable(ctx, R.drawable.i_triple_l)
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