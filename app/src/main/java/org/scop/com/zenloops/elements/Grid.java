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
    private int mixedRows,height;
    private Link[][] pieces;
    private boolean finished = false;

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

    public boolean isFinished() {
        return finished;
    }

    public void restartNew(){
        randomFill();
    }

    public void draw(Canvas c, float x, float y, float scale, float limitW, float limitH){
        for (int i = 0; i<pieces.length; i++){
            for (int j = 0; j<pieces[0].length; j++){
                Link l = pieces[i][j];
                if (l!=null) l.draw(c, x, y, scale, limitW, limitH);
            }
        }
    }

    // VALIDATOR:
    public boolean validate(){
        for (int i=0; i<pieces.length; i++){
            for (int j=0; j<pieces[0].length; j++){
                Link l = pieces[i][j];
                if (l != null && !l.isConnected()){
                    finished = false;
                    return false;
                }
            }
        }
        finished = true;
        return true;
    }

    // RANDOM FILLER:
    public List<NeighborLink> getNeighborLinks(int x, int y){
        List<NeighborLink> l = new ArrayList<>();
        boolean even = x%2==0;
        boolean isT = y%2==0;
        if (!even) isT = !isT;

        if (isT){
            if (y>0) l.add(new NeighborLink(Link.TOP,x,y-1));
            if (x>0) l.add(new NeighborLink(Link.BOTL,x-1,y));
            if (x<this.mixedRows-1) l.add(new NeighborLink(Link.BOTR,x+1,y));
        } else {
            if (x>0) l.add(new NeighborLink(Link.TOPL,x-1,y));
            if (x<this.mixedRows-1) l.add(new NeighborLink(Link.TOPR,x+1,y));
            if (y<this.height-1) l.add(new NeighborLink(Link.BOT,x,y+1));
        }
        return l;
    }

    public void randomFill(){
        Random r = new Random();
        Node[][] schema = new Node[mixedRows][height];

        boolean balanced = false;
        if (balanced) {
            for (int i=0; i<mixedRows; i++){
                for (int j=0; j<height; j++){
                    tryAddPulse(schema,i,j);
                }
            }
        } else {
            boolean res;
            int x,y,pulses = Math.round(mixedRows*height*0.76f);
            for (int i=0; i<pulses; i++){
                x = r.nextInt(mixedRows);
                y = r.nextInt(height);
                res = tryAddPulse(schema,x,y);
                if (!res) i--;
            }
        }
        convert(r,schema);
        validate();
    }

    public boolean tryAddPulse(Node[][] s, int x, int y){
        Node n2, n = getNode(s,x,y);
        if (n.value==3) return false;

        List<NeighborLink> nbl = getNeighborLinks(x,y);
        Collections.shuffle(nbl);

        for (NeighborLink nl : nbl){
            n2 = getNode(s,nl.x,nl.y);
            if (n2.value==3) continue;

            if (!n.hasLink(nl.x,nl.y)){
                n.addNode(nl.x,nl.y);
                n2.addNode(x,y);
                return true;
            }
        }
        return false;
    }

    public Node getNode(Node[][] s, int x, int y){
        if (s[x][y]==null){
            Node n = new Node();
            s[x][y] = n;
            return n;
        }
        return s[x][y];
    }

    private void convert(Random r, Node[][] a){
        pieces = new Link[mixedRows][height];
        for (int i=0; i<a.length; i++){
            for (int j=0; j<a[0].length; j++){
                Node n = a[i][j];
                if (n != null && n.value > 0){
                    int type = n.value>2? (r.nextInt(4)>0? Link.TYPE3a:Link.TYPE3b) : n.value;
                    Link l = new Link(type,i,j);
                    pieces[i][j] = l;
                    int rotations = r.nextInt(3);
                    for (int rot=0;rot < rotations; rot++){
                        l.rotate();
                    }
                }
            }
        }
        for (int i=0; i<pieces.length; i++){
            for (int j=0; j<pieces[0].length; j++){
                Link l = pieces[i][j];
                if (l != null && l.getFreeLinks() > 0){
                    List<NeighborLink> nbl = getNeighborLinks(i,j);
                    for (NeighborLink nl : nbl){
                        Link l2 = pieces[nl.x][nl.y];
                        if (l2!=null) l.setNeighboor(l2,nl.direction);
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

    class NeighborLink{
        public int direction,x,y;
        public NeighborLink(int direction, int x, int y) {
            this.direction = direction;
            this.x = x;
            this.y = y;
        }
    }
}
