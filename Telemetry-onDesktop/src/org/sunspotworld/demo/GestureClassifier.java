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
    /** Creates a new instance of GestureClassifier */
    public GestureClassifier() {
    }
    public void classifyGesture(Gesture gesture){
        Vector basicGestures = gesture.getVector();
        int gestureValue = 0;
        int tens = 1;
        for(int i = (basicGestures.size()-1) ; i >= 0 ; i--){
            gestureValue += ((Integer)basicGestures.elementAt(i)).intValue()*tens;
            tens*=10;
        }
        
        System.err.println("Gesture value  == "+gestureValue);
        // clear the vestor so that it starts finding new gestures
        gesture.clearVector();          
        Global.mainWindow.setGesture((gestureValue/10),gestureValue%10);
        Global.mainWindow.repaint();
        // Find this gesture value in the map and return the string corresponding to it
        if(Global.definedGestures.get(gestureValue) != null){
            String g = Global.definedGestures.get(gestureValue).toString();
            //int first = Integer.parseInt(g.substring(0,0));
            //int second = Integer.parseInt(g.substring(1,1));
            //Global.mainWindow.setGesture(first,second);
            System.err.println("Gesture Found :" + Global.definedGestures.get(gestureValue));
        }else{
            System.err.println("Gesture Not Found ");
        }
        
        
    }
    public void classifier(){
        Global.gestureLock.writeLock().lock();
        try{
            if(Global.gesture.getVectorSize() >= Global.NUMBER_OF_MOVEMENTS_PER_GESTURE ){
                classifyGesture(Global.gesture);             
            }// end of if(Global.gesture.getVectorSize() >= Global.NUMBER_OF_MOVEMENTS_PER_GESTURE )
        }finally{
            Global.gestureLock.writeLock().unlock();
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
        
 }
