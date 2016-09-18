package org.scop.com.zenloops.elements;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.VectorDrawable;

import org.scop.com.zenloops.R;

/**
 * Created by Oscar on 18/09/2016.
 */
public class LinkVector {
    private static LinkVector factory;
    //private Paint paint;
    private VectorDrawable[] vectors;

    public static LinkVector getInstance(){
        if (factory==null){
            factory = new LinkVector();
        }
        return factory;
    }

    private LinkVector(){
        //paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        //paint.setAntiAlias(true);
        //paint.setFilterBitmap(true);
        //paint.setDither(true);
    }

    public void loadGraphics(Context ctx){
        vectors = new VectorDrawable[4];
        vectors[0] = (VectorDrawable) ctx.getResources().getDrawable(R.drawable.f_simple);
        vectors[1] = (VectorDrawable) ctx.getResources().getDrawable(R.drawable.f_link);
        vectors[2] = (VectorDrawable) ctx.getResources().getDrawable(R.drawable.f_double);
        vectors[3] = (VectorDrawable) ctx.getResources().getDrawable(R.drawable.f_triple);
    }

    public VectorDrawable getVectorDrawable(int type){
        return vectors[type-1];
    }

}
