package org.scop.com.zenloops;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.scop.com.zenloops.core.Animate;
import org.scop.com.zenloops.core.Animator;
import org.scop.com.zenloops.elements.Grid;
import org.scop.com.zenloops.elements.Link;
import org.scop.com.zenloops.elements.LinkGraphics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Oscar on 18/09/2016.
 */
public class GamePanel extends View implements Animate {
    private Grid grid;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private String save_state_path;
    public Animator animator;

    public GamePanel(Context context, boolean load) {
        super(context);

        this.save_state_path = new ContextWrapper(context).getFilesDir().getPath()+"/state.save";

        LinkGraphics.getInstance().loadGraphics(this.getContext());
        if (load){
            load = loadState();
        }
        if (!load){
            grid = new Grid(this,10*7/3,21*7/3,0.76f,0.105f);
            grid.randomFill();
        }
        this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.scaleDetector.setQuickScaleEnabled(false);
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.animator = Animator.getInstance(this);
    }

    private int backgroundColor = 0xFF000000;
    private boolean finishedNotified = false;
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (grid.isFinished() != finishedNotified){
            finishedNotified = !finishedNotified;
            animator.addAnimation(this);
        }
        canvas.drawColor(backgroundColor);
        grid.draw(canvas, x, y, scale, wScaled, hScaled);
    }

    @Override
    public boolean updateAnimation(){
        if (grid.isFinished()){
            backgroundColor+= 0x000F0F0F;
            if (backgroundColor > 0xFFFFFFFF){
                backgroundColor = 0xFFFFFFFF;
                return false;
            }
        } else {
            backgroundColor-= 0x00161616;
            if (backgroundColor < 0xFF000000){
                backgroundColor = 0xFF000000;
                return false;
            }
        }
        return true;
    }

    public void saveState(){
        try {
            new File(save_state_path.substring(0,save_state_path.lastIndexOf("/"))).mkdirs();
            File f = new File(save_state_path);
            if (f.exists()) f.delete();
            FileOutputStream fos = new FileOutputStream (f);
            DataOutputStream dos = new DataOutputStream(fos);

            String str = grid.toPack();
            str+=":"+(int)-x+":"+(int)-y+":"+(int)(scale*100)+"|";

            char[] map = str.toCharArray();
            for (char c : map)
                dos.writeChar(c);
            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadState(){
        try {
            File f = new File(save_state_path);
            FileInputStream fis = new FileInputStream (f);
            DataInputStream dis = new DataInputStream(fis);

            String str = "";
            while (true) {
                char c = dis.readChar();
                if (c=='|') break;
                str += c;
            }

            dis.close();
            fis.close();

            String[] read = str.split(":");

            grid = new Grid(this,Integer.parseInt(read[0]),Integer.parseInt(read[1]),Integer.parseInt(read[2])/10000f,Integer.parseInt(read[3])/10000f);
            grid.fromPack(read[4]);

            x = -Integer.parseInt(read[5]);
            y = -Integer.parseInt(read[6]);
            scale = Integer.parseInt(read[7])/100f;
            wScaled = w/scale;
            hScaled = h/scale;
            postInvalidate();
            return true;
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // CONTROLS:
    private boolean isResizing = false;
    private boolean isMoving = false;
    private float w,h,x,y,wScaled,hScaled,scale=0;
    private float minScale = 0.3f;
    private float maxScale = 2f;
    private float posX0,posY0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        int[] vp = grid.getViewport();

        this.minScale = Math.min( (float)w/Math.min(vp[0],3164), (float)h/Math.min(vp[1],5780))*0.94f;
        this.maxScale = Math.min( (float)w/LinkGraphics.WIDTH/3, (float)h/LinkGraphics.HEIGHT/3);
        if (this.scale==0) this.scale = (this.minScale*3+this.maxScale)/4;

        this.hScaled = h/scale;
        this.wScaled = w/scale;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public boolean onTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        scaleDetector.onTouchEvent(e);

        if (!scaleDetector.isInProgress()) {
            int action = e.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    posX0 = e.getX();
                    posY0 = e.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (this.isResizing) return true;

                    float X = e.getX();
                    float Y = e.getY();

                    int dx = Math.round((X - posX0) / scale);
                    int dy = Math.round((Y - posY0) / scale);

                    if (isMoving || Math.abs(dx)+Math.abs(dy)>35){
                        posX0 = X;
                        posY0 = Y;
                        x+=dx;
                        y+=dy;
                        adjustXY();
                        this.isMoving = true;
                        postInvalidate();
                    }
                    break;

                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP:
                    this.isMoving = false;
                    this.isResizing = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
        }
        return true;//super.onTouchEvent(event);
    }

    private void adjustXY(){
        int[] vp = grid.getViewport();

        float halfWScaled = wScaled/2,
                halfHScaled = hScaled/2;
        vp[0] += halfWScaled;
        vp[1] += halfHScaled;

        //halfWScaled = Math.max(halfWScaled,2000);
        if (x>halfWScaled) x=halfWScaled;
        if (y>halfHScaled) y=halfHScaled;

        float iw = x-wScaled;
        float ih = y-hScaled;

        if (-iw>vp[0]) x = wScaled-vp[0];
        if (-ih>vp[1]) y = hScaled-vp[1];
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isResizing = true;

            float ratio = detector.getScaleFactor();

            float iScale = scale;
            scale *= ratio;
            scale = Math.max(minScale, Math.min(scale, maxScale));
            wScaled = w/scale;
            hScaled = h/scale;

            float dd = (1/scale - 1/iScale);
            x += dd*w/2;
            y += dd*h/2;

            postInvalidate();
            return true;
        }
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            //if (grid.isFinished()){
            if (!isResizing && !isMoving){
                grid.restartNew();
                postInvalidate();
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            GamePanel p = GamePanel.this;
            float x = e.getX()/p.scale-p.x;
            float y = e.getY()/p.scale-p.y;
            boolean success = grid.rotate(x,y);
            if (success){
                grid.validate();
                postInvalidate();
            }
            return true;
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e){
            return true;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getAction()!=MotionEvent.ACTION_DOWN) isMoving = true;
            if (e.getAction()!=MotionEvent.ACTION_UP) return false;
            onSingleTapUp(e);
            return true;
        }
    }
}
