package org.scop.com.zenloops.elements;

import android.graphics.Canvas;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Oscar on 18/09/2016.
 */
public class Grid {
    public int mixedRows,height;
    public Link[][] pieces;

    private View view;

    public Grid(View view, int width, int height) {
        this.mixedRows = width*2;
        this.height = height;
        this.view = view;

        pieces = new Link[mixedRows][height];

        for (int i = 0; i<pieces.length; i++){
            for (int j = 0; j<pieces[0].length; j++){
                pieces[i][j] = new Link(Link.TYPE3a,i,j);
            }
        }
    }

    public Link getLink(float x, float y){
        for (Link[] r : pieces){
            for (Link l : r){
                if (l!=null && l.itsMe(x,y)){
                    return l;
                }
            }
        }
        return null;
    }

    public void draw(Canvas c, float x, float y, float scale, float limitW, float limitH){
        for (int i = 0; i<pieces.length; i++){
            for (int j = 0; j<pieces[0].length; j++){
                Link l = pieces[i][j];
                if (l!=null) l.draw(c, x, y, scale, limitW, limitH);
            }
        }
    }

    // RANDOM FILLER:
    public List<CandidatePack> getIdxLinks(int x, int y){
        List<CandidatePack> l = new ArrayList<>();
        boolean even = x%2==0;
        boolean isT = y%2==0;
        if (!even) isT = !isT;

        if (isT){
            if (y>0) l.add(new CandidatePack(Link.TOP,x,y-1));
            if (x>0) l.add(new CandidatePack(Link.BOTL,x-1,y));
            if (x<this.mixedRows-1) l.add(new CandidatePack(Link.BOTR,x+1,y));
        } else {
            if (x>0) l.add(new CandidatePack(Link.TOPL,x-1,y));
            if (x<this.mixedRows-1) l.add(new CandidatePack(Link.TOPR,x+1,y));
            if (y<this.height-1) l.add(new CandidatePack(Link.BOT,x,y+1));
        }
        return l;
    }

    public boolean isAvailable(int x,int y){
        if (0 <= x && x < mixedRows && 0 <= y && y < height){
            Link l = pieces[x][y];
            if (l==null){
                return true;
            } else {
                return l.getFreeLinks()>0;
            }
        } else {
            return false;
        }
    }

    public int nearAvailable(int x, int y){
        int c = 0;
        boolean isT = (x+y)%2==0;
        if (isAvailable(x-1,y)) c++;
        if (isAvailable(x+1,y)) c++;
        if (isAvailable(x,isT? (y-1):(y+1))) c++;
        return c;
    }
    public List<Integer> typesAvailable(int x, int y) {
        boolean isT = (x+y)%2==0;
        List<Integer> l = new ArrayList<>();

        int c = nearAvailable(x,y);

        if (c>0) l.add(Link.TYPE1);

        if (x==0 && (y==0 || y==height-1) || y==0 && x==mixedRows-1){
            return l;
        }
        if (c>1){
            l.add(Link.TYPE2);
        }
        if (x==0 || y==0 || y==height-1 || x==mixedRows-1){
            return l;
        }
        if (c>2) {
            l.add(Link.TYPE3a);
            l.add(Link.TYPE3b);
        }
        return l;
    }


    public void recursiveFill(Random r, int x, int y, Link l){
        int possible = l.getConnections();
        List<CandidatePack> cons = getIdxLinks(x, y);
        int act = 0;
        CandidatePack cp0 = null;
        for (CandidatePack cp : cons){
            if (pieces[cp.x][cp.y] != null) continue;
            cp0 = cp;
            List<Integer> ta = typesAvailable(cp.x, cp.y);
            if (ta.size()==0) continue;

            int link = ta.get(r.nextInt(ta.size()));
            Link l2 = new Link(link,cp.x,cp.y);

            l.setNeighboor(l2, cp.direction);
            pieces[cp.x][cp.y] = l2;

            recursiveFill(r, cp.x, cp.y, l2);
            if ((++act)==possible) break;
        }
    }
    public void randomFill(){
        pieces = new Link[mixedRows][height];
        Random rnd = new Random();

        //int x0=rnd.nextInt(mixedRows);
        //int y0=rnd.nextInt(height);

        //List<Integer> ta = typesAvailable(x0, y0);
        //int idx = rnd.nextInt(ta.size());

        //Link l = new Link(idx,x0,y0);
        //pieces[x0][y0] = l;

        //recursiveFill(rnd,x0,y0,l);

        Link l = new Link(Link.TYPE3a,1,0);
        pieces[1][0] = l;

        Link l2 = new Link(Link.TYPE3b,1,1);
        l.setNeighboor(l2,Link.BOT);
        pieces[1][1] = l2;

        recursiveFill(rnd,1,0,l);
        recursiveFill(rnd,1,1,l2);
    }

    class CandidatePack{
        public int direction,x,y;
        public CandidatePack(int direction, int x, int y) {
            this.direction = direction;
            this.x = x;
            this.y = y;
        }
    }
}
