package org.scop.com.zenloops.core;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oscar on 28/09/2016.
 */
public class Animator {
    private static Animator instance;
    private static View view;
    public static Animator getInstance(){
        if (instance==null){
            instance = new Animator();
        }
        return instance;
    }
    public static Animator getInstance(View view){
        Animator i = getInstance();
        Animator.view = view;
        return i;
    }

    private List<Animate> animations;
    private boolean framerActive = false;
    private Framer framer;

    private Animator(){
        animations = new ArrayList();
    };

    public void addAnimation(Animate a){
        if (!animations.contains(a)) {
            animations.add(a);
        }
        if (animations.size() > 0 && !getFramerActive()) {
            setFramerActive(true);
            framer = new Framer();
            framer.start();
        }
    }

    protected void update(){
        for (int i = 0; i < animations.size(); i++) {
            boolean stillGoing = animations.get(i).updateAnimation();
            if (!stillGoing){
                animations.remove(i);
                i--;
            }
        }
    }

    public synchronized void setFramerActive(boolean value){
        framerActive = value;
    }

    public synchronized boolean getFramerActive(){
        return framerActive;
    }

    public class Framer extends Thread{
        public Framer(){
        }

        @Override
        public void run() {
            super.run();
            long a,b;
            framerActive = true;

            float time = 1000/60;
            int sleepTime;

            while (animations.size()>0){
                try {
                    a = System.nanoTime();

                    update();
                    view.postInvalidate();

                    b = System.nanoTime();
                    sleepTime = Math.round(time - ((b - a) / 1000000));

                    if (sleepTime>0){
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setFramerActive(false);
        }
    }
}
