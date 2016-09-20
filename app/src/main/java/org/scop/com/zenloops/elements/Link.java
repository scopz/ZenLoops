package org.scop.com.zenloops.elements;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

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

    public static final int MARGS_X = 158;
    public static final int MARGS_Y = 91;

    private Link[] neighboors;
    private boolean[] active;
    private int type;
    private boolean isTopRow;
    private int rotation,posX,posY;
    private Drawable vector;

    public Link(int type, int x, int y) {
        this.isTopRow = (x+y)%2==0;//isTopRow;
        this.type = type;
        this.posX = x;
        this.posY = y;
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

        vector = LinkGraphics.getInstance().getDrawable(type);
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
        rotation %= 360;
        boolean[] newActive = new boolean[6];
        for (int i = 0; i < 6; i++) {
            newActive[(i+2)%6] = active[i];
        }
        active = newActive;
    }

    public int getConnections() {
        switch(type){
            case TYPE1: return 1;
            case TYPE2: return 2;
            default: return 3;
        }
    }

    public int getNeighboorCount() {
        int count = 0;
        for (Link l : neighboors){
            if (l!=null) count++;
        }
        return count;
    }

    public int getFreeLinks(){
        return getConnections()-getNeighboorCount();
    }

    public boolean isConnected(){
        for (int i = 0; i < 6; i++) {
            if (active[i]){
                if (neighboors[i]==null || !neighboors[i].isConnected(this,(i+3)%6))
                    return false;
            }
        }
        return true;
    }

    public boolean isConnected(Link me, int position){
        return active[position] && neighboors[position] == me;
    }

    public boolean itsMe(float x, float y){
        float x0 = posX*MARGS_X;
        float y0 = posY*MARGS_Y*3;

        if (!isTopRow){
            y0+=MARGS_Y;
        }

        float xf = x0 + LinkGraphics.WIDTH;
        float yf = y0 + LinkGraphics.HEIGHT;

        if (x0 <= x && x <= xf && y0 <= y && y <= yf){
            return true;
        }
        return false;
    }

    public void draw(Canvas canvas, float parentX, float parentY, float scale, float wCut, float hCut){
        float x = posX*MARGS_X+parentX;
        float y = posY*MARGS_Y*3+parentY;

        if (!isTopRow){
            y+=MARGS_Y;
        }

        if (x>wCut || y>hCut){
            return;
        }

        float xwScaled = (x + LinkGraphics.WIDTH) * scale;
        float yhScaled = (y + LinkGraphics.HEIGHT) * scale;

        if (xwScaled<0 || yhScaled<0){
            return;
        }

        vector.setBounds((int) (x * scale), (int) (y * scale), (int) (xwScaled), (int) (yhScaled));

        int save_status =  canvas.save();
        canvas.rotate(rotation, (x+LinkGraphics.WIDTH/2)*scale, (y+LinkGraphics.HEIGHT/2)*scale);
        vector.draw(canvas);
        canvas.restoreToCount(save_status);
    }

    public int getType() {
        return type;
    }

    public int getRotation() {
        return rotation;
    }
}
