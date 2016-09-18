package org.scop.com.zenloops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.view.View;

import org.scop.com.zenloops.elements.Link;
import org.scop.com.zenloops.elements.LinkVector;

/**
 * Created by Oscar on 18/09/2016.
 */
public class GamePanel extends View {
    public GamePanel(Context context) {
        super(context);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        LinkVector.getInstance().loadGraphics(this.getContext());

        VectorDrawable vector = (VectorDrawable) this.getResources().getDrawable(R.drawable.f_link);

        Link l1 = new Link(Link.TYPE1,true);
        Link l2 = new Link(Link.TYPE2,false);

        l1.draw(canvas,0,0);
        l2.draw(canvas,0,0);
    }

    public void vectorDraw(VectorDrawable v,Canvas canvas,int x, int y, float scale, int rotation){
        v.setBounds((int) (x * scale), (int) (y * scale), (int) ((x + 200) * scale), (int) ((y + 200) * scale));

        int save_status =  canvas.save();
        canvas.rotate(rotation, (x+100)*scale, (y+100)*scale);
        v.draw(canvas);
        canvas.restoreToCount(save_status);
    }
}
