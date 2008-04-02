/*
 * BasicGestureClassifier.java
 *
 * Created on April 1, 2008, 2:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

import java.util.Vector;
import java.util.concurrent.locks.*;
/**
 *
 * @author Winnie
 */
public class BasicGestureClassifier extends Thread{
    private boolean running = false;
    private static double currentTime;
    private int basicGesturesIndex;
    private final static double IDLE_TIME = 500; 
    /** Creates a new instance of BasicGestureClassifier */
    public BasicGestureClassifier() {
        currentTime = 0;
        basicGesturesIndex = 0;
    }
    public void classifier(){
        Global.basicGesturesLock.writeLock().lock();
        try{
            BasicGesture g = new BasicGesture();
            Vector dataset = new Vector();
            if(Global.basicGestures.size()>0 && basicGesturesIndex < Global.basicGestures.size()){
                g = (BasicGesture)Global.basicGestures.elementAt(basicGesturesIndex);
                dataset = g.getDataset();
                updateCurrentTimeStamp();
                
                if(Global.basicGestures.size() > (basicGesturesIndex+1) ||
                        currentTime > (((DataStruct)dataset.lastElement()).getTimeStamp() + IDLE_TIME)) {
                    classifyBasicGesture(g);
                    basicGesturesIndex++;
                }
                }
            } finally{
                Global.basicGesturesLock.writeLock().unlock();
            }
            
        }
        
        public void leftOrRight(BasicGesture g){
            Vector dataset = g.getDataset();
            double max = -1000, min = 1000;
            int maxIndex = -1, minIndex = -1;
            for(int i=0; i<dataset.size(); i++){
                double currX = ((DataStruct)dataset.elementAt(i)).getX();
                if(currX > max){
                    max = currX;
                    maxIndex = i;
                }
                if(currX < min){
                    min = currX;
                    minIndex = i;
                }
            }
            if(minIndex > maxIndex)
                System.out.println("Left");
            else
                System.out.println("Right");
            
        }
        public void forwardOrBackward(BasicGesture g){
            Vector dataset = g.getDataset();
            double max = -1000, min = 1000;
            int maxIndex = -1, minIndex = -1;
            for(int i=0; i<dataset.size(); i++){
                double currY = ((DataStruct)dataset.elementAt(i)).getY();
                if(currY > max){
                    max = currY;
                    maxIndex = i;
                }
                if(currY < min){
                    min = currY;
                    minIndex = i;
                }
            }
            if(minIndex > maxIndex)
                System.out.println("Backward");
            else
                System.out.println("Forward");
            
        }
        public void upOrDown(BasicGesture g){
            Vector dataset = g.getDataset();
            double max = -1000, min = 1000;
            int maxIndex = -1, minIndex = -1;
            for(int i=0; i<dataset.size(); i++){
                double currZ = ((DataStruct)dataset.elementAt(i)).getZ();
                if(currZ > max){
                    max = currZ;
                    maxIndex = i;
                }
                if(currZ < min){
                    min = currZ;
                    minIndex = i;
                }
            }
            if(minIndex > maxIndex)
                System.out.println("Up");
            else
                System.out.println("Down");
            System.out.println("minT/" + ((DataStruct)dataset.elementAt(minIndex)).getTimeStamp() + " maxT/" + ((DataStruct)dataset.elementAt(maxIndex)).getTimeStamp());
        }
        public void classifyBasicGesture(BasicGesture g){
            if(g.getActiveAxis().equals("x"))
                leftOrRight(g);
            else if(g.getActiveAxis().equals("y"))
                forwardOrBackward(g);
            else if(g.getActiveAxis().equals("z"))
                upOrDown(g);
            else if(g.getActiveAxis().equals("s"))
                System.out.println("Shake");
        }
        public void updateCurrentTimeStamp(){
            Global.currentTimeLock.writeLock().lock();
            try{
                currentTime = Global.currentTime;
            } finally{
                Global.currentTimeLock.writeLock().unlock();
            }
        }
        public void clear() {
            // clear private data
        }
        public void run() {
            System.out.println("BasicGestureClassifier Thread started ...");
            hostLoop();
        }
        
        //main loop
        public void hostLoop(){
            running = true;
            while(running)
                try{
                    classifier();
                } catch(Exception e){
                    System.out.println(e);
                }
        }
        public void doQuit(){
            running = false;
        }
        
    }
