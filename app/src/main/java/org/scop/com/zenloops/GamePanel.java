package org.scop.com.zenloops;

import android.content.Context;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.scop.com.zenloops.elements.Grid;
import org.scop.com.zenloops.elements.Link;
import org.scop.com.zenloops.elements.LinkGraphics;

/**
 * Created by Oscar on 18/09/2016.
 */
public class GamePanel extends View {
    private Grid grid;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    public GamePanel(Context context) {
        super(context);
        LinkGraphics.getInstance().loadGraphics(this.getContext());
        grid = new Grid(this,2,2);
        grid.randomFill();
        this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.scaleDetector.setQuickScaleEnabled(false);
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        grid.draw(canvas,x,y,scale,wScaled,hScaled);
    }

    // CONTROLS:
    private boolean isResizing = false;
    private boolean isMoving = false;
    private float w,h,x,y,wScaled=0,hScaled=0,scale=1;
    private float minScale = 0.7f;
    private float maxScale = 5f;
    private float dragXpos,dragYpos;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        this.scale=1;
        this.hScaled = h;
        this.wScaled = w;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public boolean onTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        scaleDetector.onTouchEvent(e);

        if (!scaleDetector.isInProgress()) {
            int action = e.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    dragXpos = e.getX();
                    dragYpos = e.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (this.isResizing) return true;

                    float X = e.getX();
                    float Y = e.getY();

                    int dx = Math.round((X - dragXpos) / scale);
                    int dy = Math.round((Y - dragYpos) / scale);

                    if (isMoving || Math.abs(dx)+Math.abs(dy)>35){
                        dragXpos = X;
                        dragYpos = Y;
                        x+=dx;
                        y+=dy;
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
        public boolean onSingleTapUp(MotionEvent e) {
            GamePanel p = GamePanel.this;
            float x = e.getX()/p.scale-p.x;
            float y = e.getY()/p.scale-p.y;
            Link l = grid.getLink(x,y);
            if (l!=null){
                l.rotate();
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
            if (e.getAction()!=MotionEvent.ACTION_UP) return false;
            onSingleTapUp(e);
            return true;
        }
    }
}
