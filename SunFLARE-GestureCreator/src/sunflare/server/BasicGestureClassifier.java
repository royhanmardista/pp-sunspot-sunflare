/*
 * BasicGestureClassifier.java
 *
 * Created on April 1, 2008, 2:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.server;

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
    private boolean debug = true;
    private static double latestBasicGestureTimestamp;
    private boolean serviceMode;
    /** Creates a new instance of BasicGestureClassifier */
    public BasicGestureClassifier() {
        currentTime = 0;
        basicGesturesIndex = 0;
        latestBasicGestureTimestamp = 0;
        serviceMode = false;
    }
    public BasicGestureClassifier(boolean serviceMode) {
        currentTime = 0;
        basicGesturesIndex = 0;
        latestBasicGestureTimestamp = 0;
        this.serviceMode = serviceMode;
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
                    System.out.println("Some movement performed");
                    //Global.mainWindow.setGesture(basicGesturesIndex-1,g.getID());
                }
            }
        } finally{
            Global.basicGesturesLock.writeLock().unlock();
        }
        
    }
    
    public int leftOrRight(BasicGesture g){
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
        if(minIndex > maxIndex){
            debug("Left");
            return Global.LEFT;
        }else{
            debug("Right");
            return Global.RIGHT;
        }
        
        
    }
    public int forwardOrBackward(BasicGesture g){
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
        if(minIndex > maxIndex){
            debug("Backward");
            return Global.BACKWARD;
            
        }else{
            debug("Forward");
            return Global.FORWARD;
            
        }
        
        
    }
    public int upOrDown(BasicGesture g){
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
        if(minIndex > maxIndex){
            debug("Up");
            return Global.UP;
            
        }else{
            debug("Down");
            return Global.DOWN;
            
        }
        
        //debug("minT/" + ((DataStruct)dataset.elementAt(minIndex)).getTimeStamp() + " maxT/" + ((DataStruct)dataset.elementAt(maxIndex)).getTimeStamp());
    }
    public int shake(){
        debug("Shake");
        return Global.SHAKE;
    }
    public void classifyBasicGesture(BasicGesture bg){
        int thisBasicGesture = 0;
        if(bg.getActiveAxis().equals("x"))
            thisBasicGesture = leftOrRight(bg);
        else if(bg.getActiveAxis().equals("y"))
            thisBasicGesture = forwardOrBackward(bg);
        else if(bg.getActiveAxis().equals("z"))
            thisBasicGesture = upOrDown(bg);
        else if(bg.getActiveAxis().equals("s")){
            thisBasicGesture = shake();
        }
        bg.setID(thisBasicGesture);
        /*******************old code needs to be deleted*************************/
//        Global.gestureLock.writeLock().lock();
//        try{
//            Global.gesture.addBasicGesture(thisBasicGesture);
//        } finally{
//            Global.gestureLock.writeLock().unlock();
//        }
        /**************************end old code***********************************/
        
        Global.gesturesLock.writeLock().lock();
        try{
            //if gestures vector is empty or this current gesture is performed after the time window or the previous gesture has size == GLOBAl.NUMBER_OF_MOVEMENTS_PER_GESTURE
            //create a new gesture
            if(Global.gestures.isEmpty()
            || (!Global.gestures.isEmpty() && latestBasicGestureTimestamp + Global.IDLE_TIME_BTWN_GESTURES < bg.getEndTimeStamp())
            || (!Global.gestures.isEmpty() && ((Gesture)(Global.gestures.lastElement())).getNumBasicGestures()>=Global.NUMBER_OF_MOVEMENTS_PER_GESTURE)){
                Gesture newGesture = new Gesture(bg);
                Global.gestures.addElement(newGesture);
                //if(!serviceMode)
                    //Global.mainWindow.clearGestureBoxes();
            }
            //add it to the latest gesture
            else{
                ((Gesture)Global.gestures.lastElement()).addBasicGesture(bg);
            }
            //for(int k=0; k<Global.gestures.size();k++)
            //    debug(Global.gestures.elementAt(k).toString());
            //update the timestamp
            latestBasicGestureTimestamp = bg.getEndTimeStamp();
            if(!serviceMode)
                Global.mainWindow.setGesture(((Gesture)Global.gestures.lastElement()).getNumBasicGestures()-1,bg.getID());
        } finally{
            Global.gesturesLock.writeLock().unlock();
        }
        
        //classifiedBasicGestures is for the Controller thread
        Global.classifiedBasicGesturesLock.writeLock().lock();
        try{
            Global.classifiedBasicGestures.addElement(bg);
        } finally{
            Global.classifiedBasicGesturesLock.writeLock().unlock();
        }
        
        
        
        //update GUI's movement box
        
        
        
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
        basicGesturesIndex = 0;
        latestBasicGestureTimestamp = 0;
    }
    public void run() {
        debug("BasicGestureClassifier Thread started ...");
        hostLoop();
    }
    
    //main loop
    public void hostLoop(){
        running = true;
        while(running)
            try{
                classifier();
            } catch(Exception e){
                debug(e.toString());
            }
    }
    public void doQuit(){
        running = false;
    }
    public void debug(String s){
        if(debug)
            System.out.println("BasicGestureClassifier: "+s);
    }
    
    
}
