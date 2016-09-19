package org.scop.com.zenloops.elements;

import android.graphics.Canvas;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
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
    public List<int[]> getNeighborLinks(int x, int y){
        List<int[]> l = new ArrayList<>();
        boolean even = x%2==0;
        boolean isT = y%2==0;
        if (!even) isT = !isT;

        if (isT){
            if (y>0) l.add(new int[]{x, y-1});
            if (x>0) l.add(new int[]{x-1, y});
            if (x<this.mixedRows-1) l.add(new int[]{x+1, y});
        } else {
            if (x>0) l.add(new int[]{x-1, y});
            if (x<this.mixedRows-1) l.add(new int[]{x+1, y});
            if (y<this.height-1) l.add(new int[]{x, y+1});
        }
        return l;
    }

    public void randomFill(){
        Node[][] schema = new Node[mixedRows][height];
        Node n,n2;

        for (int i=0; i<mixedRows; i++){
            for (int j=0; j<height; j++){
                n = getNode(schema,i,j);
                if (n.value==3) continue;

                List<int[]> cps = getNeighborLinks(i,j);
                Collections.shuffle(cps);

                for (int[] cp : cps){
                    n2 = getNode(schema,cp[0],cp[1]);
                    if (n2.value==3) continue;

                    if (!n.hasLink(cp[0],cp[1])){
                        n.addNode(cp[0],cp[1]);
                        n2.addNode(i,j);
                        break;
                    }
                }
            }
        }
        convert(schema);
    }

    public Node getNode(Node[][] s, int x, int y){
        if (s[x][y]==null){
            Node n = new Node();
            s[x][y] = n;
            return n;
        }
        return s[x][y];
    }

    private void convert(Node[][] a){
        Random r = new Random();
        Node n;
        Link l;
        int rotations;
        int type;
        pieces = new Link[mixedRows][height];
        for (int i=0; i<a.length; i++){
            for (int j=0; j<a[0].length; j++){
                n = a[i][j];
                if (n != null && n.value > 0){
                    type = n.value>2? (r.nextBoolean()? Link.TYPE3a:(r.nextBoolean()? Link.TYPE3a:Link.TYPE3b)) : n.value;
                    l = new Link(type,i,j);
                    pieces[i][j] = l;
                    rotations = r.nextInt(3);
                    for (int rot=0;rot < rotations; rot++){
                        l.rotate();
                    }
                }
            }
        }
    }

    class Node{
        public int value;
        public int[][] nodeLinks;
        public int num = 0;

        public Node() {
            this.value = 0;
            this.nodeLinks = new int[3][2];
            this.num = 0;
        }
        public boolean addNode(int x, int y){
            if (num<3){
                nodeLinks[num][0] = x;
                nodeLinks[num][1] = y;
                num++;
                value++;
                return true;
            }
            return false;
        }
        public boolean hasLink(int x,int y){
            for (int i=0; i<num;i++){
                if (nodeLinks[i][0]==x && nodeLinks[i][1]==y)
                    return true;
            }
            return false;
        }
    }
}
