package org.scop.com.zenloops.elements;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.scop.com.zenloops.R;

/**
 * Created by Oscar on 18/09/2016.
 */
public class LinkGraphics {
    public static final int WIDTH = 200;
    public static final int HEIGHT = 200;

    private static LinkGraphics factory;
    //private Paint paint;
    private Drawable[] vectors;

    public static LinkGraphics getInstance(){
        if (factory==null){
            factory = new LinkGraphics();
        }
        return factory;
    }

    private LinkGraphics(){
        //paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        //paint.setAntiAlias(true);
        //paint.setFilterBitmap(true);
        //paint.setDither(true);
    }

    public void loadGraphics(Context ctx){
        vectors = new Drawable[4];
        vectors[0] = ctx.getDrawable(R.drawable.i_simple);
        vectors[1] = ctx.getDrawable(R.drawable.i_link);
        vectors[2] = ctx.getDrawable(R.drawable.i_double);
        vectors[3] = ctx.getDrawable(R.drawable.i_triple);
    }

    public Drawable getDrawable(int type){
        return vectors[type-1];
    }

}
