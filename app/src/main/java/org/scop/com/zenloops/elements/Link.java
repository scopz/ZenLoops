package org.scop.com.zenloops.elements;

import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oscar on 18/09/2016.
 */
public class Link {
    public static final int TYPE1 = 1;
    public static final int TYPE2 = 2;
    public static final int TYPE3a = 3;
    public static final int TYPE3b = 4;

    public static final int TOP = 0;
    public static final int TOPR = 1;
    public static final int BOTR = 2;
    public static final int BOT = 3;
    public static final int BOTL = 4;
    public static final int TOPL = 5;

    private Link[] neighboors;
    private boolean[] active;
    private int type;
    private boolean isTopRow;
    private int rotation,posX,posY;
    private VectorDrawable vector;

    public Link(int type, boolean isTopRow) {
        this.isTopRow = isTopRow;
        this.type = type;
        this.active = new boolean[6];
        this.neighboors = new Link[6];

        if (isTopRow){
            active[TOP]  = type >= TYPE3a;
            active[TOPR] = false;
            active[BOTR] = true;
            active[BOT]  = false;
            active[BOTL] = type >= TYPE2;
            active[TOPL] = false;
            rotation=0;
        } else {
            active[TOP]  = false;
            active[TOPR] = type >= TYPE3a;
            active[BOTR] = false;
            active[BOT]  = true;
            active[BOTL] = false;
            active[TOPL] = type >= TYPE2;
            rotation=60;
        }

        vector = LinkVector.getInstance().getVectorDrawable(type);
    }

    public void setNeighboor(Link l, int position, boolean bidirectional){
        this.neighboors[position] = l;
        if (bidirectional) {
            l.setNeighboor(this, (position + 3) % 6, false);
        }
    }
    public void setNeighboor(Link l, int position){
        this.setNeighboor(l, position, true);
    }

    public Link[] getLinks() {
        return new Link[0];
    }

    public void rotate() {
        rotation += 120;
    }

    public int getType() {
        return type;
    }

    public void draw(Canvas canvas, float parentX, float parentY){
        float scale = 1;
        int x = posX;
        int y = posY;

        if (!isTopRow){
            x+=158;
            y+=91;
        }

        vector.setBounds((int) (x * scale), (int) (y * scale), (int) ((x + 200) * scale), (int) ((y + 200) * scale));

        int save_status =  canvas.save();
        canvas.rotate(rotation, (x+100)*scale, (y+100)*scale);
        vector.draw(canvas);
        canvas.restoreToCount(save_status);
    }


}
