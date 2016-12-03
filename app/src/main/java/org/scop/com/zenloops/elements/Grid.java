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
    private int vpWidth,vpHeight;
    private float percent, tolerance;
    private View view;

    public Grid(View view, int width, int height) {
        this(view,width,height,0.755f,0.105f);
    }

    public Grid(View view, int width, int height, float percent, float tolerance) {
        this.mixedRows = width*2;
        this.height = height;
        this.view = view;
        this.percent = percent;
        this.tolerance = tolerance;

        this.vpWidth = mixedRows*Link.MARGS_X+LinkGraphics.WIDTH-Link.MARGS_X;
        this.vpHeight = height*Link.MARGS_Y*3+LinkGraphics.HEIGHT-Link.MARGS_Y;
    }

    private final int wb_v = (LinkGraphics.WIDTH-Link.MARGS_X)/2; //22
    //private final int hb_v = (LinkGraphics.HEIGHT-Link.MARGS_Y)/2; //55
    private final int mr_g = (LinkGraphics.HEIGHT-Link.MARGS_Y*2)/2; //10
    private final int tramW = Link.MARGS_X*2;
    private final int tramH = Link.MARGS_Y*3;

    private Link getLink(float x, float y){
        if (x<0 || y<0 || x >= this.vpWidth || y >= this.vpHeight){
            return null;
        }
        x-=wb_v; y-=mr_g;

        int xTram = (int) x/tramW;
        int yTram = (int) y/tramH;

        int rx = (x%tramW > Link.MARGS_X)? 1:0;

        int resX = xTram*2+rx;
        int resY = yTram;

        if (resX >= this.mixedRows){
            resX = this.mixedRows-1;
        }
        if (resY >= this.height){
            resY = this.height-1;
        }

        return pieces[resX][resY];
    }

    public boolean rotate(float x, float y){
        Link l = this.getLink(x,y);
        if (l!=null){

            List<Link> visited = new ArrayList();
            boolean connected = seePieceLinked(l,visited);
            if (connected) {
                for (Link link : visited) {
                    link.setLinked(false);
                }
            }

            l.rotate(true);

            visited = new ArrayList();
            connected = seePieceLinked(l, visited);
            if (connected) {
                for (Link link : visited) {
                    link.setLinked(true);
                }
            }

            return true;
        }
        return false;
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

    private boolean seePieceLinked(Link l,List<Link> visited){
        visited.add(l);
        if (l.isConnected()){
            List<Link> nbs = l.getConnectedNeighboor();
            for (Link n : nbs){
                if (n==null || visited.contains(n)) continue;
                if (!seePieceLinked(n, visited)){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
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

        boolean res;
        int x,y,pulses = Math.round(mixedRows*height* (r.nextFloat()*tolerance*2 - tolerance + percent));
        for (int i=0; i<pulses; i++){
            x = r.nextInt(mixedRows);
            y = r.nextInt(height);
            res = tryAddPulse(schema,x,y);
            if (!res) i--;
        }
        convert(r,schema);
        examineNeighbor();
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
    }

    private void examineNeighbor(){
        for (int i=0; i<pieces.length; i++){
            for (int j=0; j<pieces[0].length; j++){
                Link l = pieces[i][j];

                if (l != null){// && l.getFreeLinks() > 0){
                    List<NeighborLink> nbl = getNeighborLinks(i,j);

                    for (NeighborLink nl : nbl){
                        Link l2 = pieces[nl.x][nl.y];
                        if (l2!=null) l.setNeighboor(l2,nl.direction,false);
                        //if (l2!=null) l.setNeighboor(l2,nl.direction);
                    }
                }
            }
        }

        // MARK LINKED:
        List<Link> visited,analyzed = new ArrayList();
        boolean connected;

        for (int i=0; i<pieces.length; i++){
            for (int j=0; j<pieces[0].length; j++){
                Link l = pieces[i][j];
                if (l == null || analyzed.contains(l)) continue;

                visited = new ArrayList();
                connected = seePieceLinked(l, visited);
                analyzed.addAll(visited);

                for (Link link : visited){
                    link.setLinked(connected);
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

    public String toPack(){
        String str = (mixedRows/2)+":"+height+":"+((int)(percent*10000))+":"+(int)(tolerance*10000)+":";
        for (int i=0; i<pieces.length; i++){
            for (int j=0; j<pieces[0].length; j++){
                Link l = pieces[i][j];
                if (l==null){
                    str+="_";
                    continue;
                }
                int rot = l.getRotation();
                switch(l.getType()){
                    case Link.TYPE1:  str+= rot<70? "q" : (rot<190? "t": "p"); break;
                    case Link.TYPE2:  str+= rot<70? "e" : (rot<190? "i": "s"); break;
                    case Link.TYPE3a: str+= rot<70? "w" : (rot<190? "u": "a"); break;
                    case Link.TYPE3b: str+= rot<70? "r" : (rot<190? "y": "o"); break;
                    default: return null;
                }
            }
        }
        return str;
    }

    public void fromPack(String str){
        pieces = new Link[mixedRows][height];
        int k = 0;
        for (int i=0; i<mixedRows; i++){
            for (int j=0; j<height; j++){
                Link l = null;

                switch(str.charAt(k)){
                    case 'q': l = new Link(Link.TYPE1,i,j); break;
                    case 't': l = new Link(Link.TYPE1,i,j); l.rotate(); break;
                    case 'p': l = new Link(Link.TYPE1,i,j); l.rotate(); l.rotate(); break;

                    case 'e': l = new Link(Link.TYPE2,i,j); break;
                    case 'i': l = new Link(Link.TYPE2,i,j); l.rotate(); break;
                    case 's': l = new Link(Link.TYPE2,i,j); l.rotate(); l.rotate(); break;

                    case 'w': l = new Link(Link.TYPE3a,i,j); break;
                    case 'u': l = new Link(Link.TYPE3a,i,j); l.rotate(); break;
                    case 'a': l = new Link(Link.TYPE3a,i,j); l.rotate(); l.rotate(); break;

                    case 'r': l = new Link(Link.TYPE3b,i,j); break;
                    case 'y': l = new Link(Link.TYPE3b,i,j); l.rotate(); break;
                    case 'o': l = new Link(Link.TYPE3b,i,j); l.rotate(); l.rotate(); break;
                    default: break;
                }
                pieces[i][j] = l;
                k++;
            }
        }
        examineNeighbor();
        validate();
    }

    public int[] getViewport(){
        return new int[] {vpWidth,vpHeight};
    }
}
