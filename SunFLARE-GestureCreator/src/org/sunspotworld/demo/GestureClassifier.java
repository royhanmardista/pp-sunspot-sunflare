/*
 * GestureClassifier.java
 *
 * Created on April 1, 2008, 9:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

import java.util.Vector;
/**
 *
 * @author Praveen
 */
public class GestureClassifier extends Thread{
    private boolean running = false;
    
    private boolean debug;
    /** Creates a new instance of GestureClassifier */
    public GestureClassifier() {
    }
    public void classifyGesture(Gesture gesture){
        /*
        Vector basicGestures = gesture.getBasicGestures();
        int gestureValue = 0;
        int tens = 1;
        for(int i = (basicGestures.size()-1) ; i >= 0 ; i--){
            gestureValue += ((Integer)basicGestures.elementAt(i)).intValue()*tens;
            tens*=10;
        }
         
        System.err.println("Gesture value  == "+gestureValue);
        // clear the vestor so that it starts finding new gestures
        gesture.removeAllBasicGestures();
        // Find this gesture value in the map and return the string corresponding to it
        if(Global.definedGestures.get(gestureValue) != null){
            String g = Global.definedGestures.get(gestureValue).toString();
            // update the gesture window
            Global.mainWindow.setGesture((gestureValue/10),gestureValue%10);
            Global.mainWindow.repaint();
         
            System.err.println("Gesture Found :" + Global.definedGestures.get(gestureValue));
        }else{
            System.err.println("Gesture Not Found ");
        }
         
         */
        PluginRef p;
        Global.gestureDBLock.writeLock().lock();
        try{
            p = Global.gestureDB.search(gesture);
        } finally{
            Global.gestureDBLock.writeLock().unlock();
        }
        if(p!=null){
            debug("Gesture found: " + p.getName()+ " " + p.getActionDescription());
        } else{
            debug("Gesture not found");
        }
        
        
        // since the classified is always the first one in the gestures vector, it is safe to remove it from the vector
        Global.gesturesLock.writeLock().lock();
        try{
            Global.gestures.removeElement(gesture);
            //Global.gestures.removeElementAt(0);
        }finally{
            Global.gesturesLock.writeLock().unlock();
        }
        
        
        
        
        
        
    }
    public void classifier(){
        /*Global.gestureLock.writeLock().lock();
        try{
            if(Global.gesture.getNumBasicGestures() >= Global.NUMBER_OF_MOVEMENTS_PER_GESTURE ){
                classifyGesture(Global.gesture);
            }// end of if(Global.gesture.getVectorSize() >= Global.NUMBER_OF_MOVEMENTS_PER_GESTURE )
        }finally{
            Global.gestureLock.writeLock().unlock();
        }
         */
        
        //get current time
        double currentTime;
        Global.currentTimeLock.writeLock().lock();
        try{
            currentTime = Global.currentTime;
        } finally{
            Global.currentTimeLock.writeLock().unlock();
        }
        
        Global.gesturesLock.writeLock().lock();
        try{
            if(Global.gestures.size()>=2){
                classifyGesture((Gesture)Global.gestures.firstElement());
            } else if(Global.gestures.size() == 1 && currentTime + Global.IDLE_TIME_BTWN_GESTURES < ((Gesture)Global.gestures.elementAt(0)).getEndTimestamp()){
                classifyGesture((Gesture)Global.gestures.firstElement());
            } else{
                //the gesture is not completed by the user yet or the gestures vector is empty
            }
            
        }finally{
            Global.gesturesLock.writeLock().unlock();
        }
        
    }
    public void clear() {
        // clear private data
    }
    public void run() {
        System.out.println("GestureClassifier Thread started ...");
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
    public void debug(String s){
        if(debug)
            System.out.println("GestureClassifier: "+s);
    }
    
}
