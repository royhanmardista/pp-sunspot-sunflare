/*
 * Controller.java
 *
 * Created on April 24, 2008, 12:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;
import java.util.*;
/**
 * This controller serves as the middle layer between the GUI and the backend
 * The GUI can call the methods in this class to alter system states
 * The controller thread determines the operations that need to be done according to the system state
 * @author Winnie
 */
public class Controller extends Thread{
    private boolean running = false;
    private Vector recordedBasicGestures = new Vector();
    private int numPerformedBasicGesture = 0;
    private boolean confirmed;
    private static boolean debug = true;
    private int numGesture = 0;
    private Gesture recordedGesture = new Gesture();
    /** Creates a new instance of Controller */
    public Controller() {
    }
    public int min(int a, int b){
        if(a<b)
            return a;
        return b;
    }
    public void debug(String s){
        if(debug)
            System.out.println("Controller: "+s);
    }
    public void doIt(){
        boolean stopRecording = false;
        Global.systemStateLock.writeLock().lock();
      
        Gesture gesturePerformed;
        PluginRef p;
        try{
            if(Global.systemState == Global.SYS_STOP_RECORDING){
                //capture the basic gestures that the user has done, this happens when the stop recording button is clicked
                Global.classifiedBasicGesturesLock.writeLock().lock();
                try{
                    //always take the first MAX_NUM_BASIC_GESTURE basic gestures
                    for(int i=0; i<min(Global.MAX_NUM_BASIC_GESTURE,Global.classifiedBasicGestures.size()); i++){
                        recordedBasicGestures.addElement(Global.classifiedBasicGestures.elementAt(i));
                        
                    }
                    Global.classifiedBasicGestures.removeAllElements();   //clear the vector after copying to the local recordedBasicGestures vector
                    debug("recordedBasicGesturess has size " + recordedBasicGestures.size());
                    debug("classifedBasicGestures vector is cleared");
                    Global.systemState = Global.SYS_IDLE;
                    debug("System state has been changed to SYS_IDLE");
                    p = new PluginRef("TestApp","blah");
                    recordedGesture = new Gesture(new Vector(recordedBasicGestures), p);
                    
                    if(Global.gestureDB.gestureExists(recordedGesture)){
                        debug("The same gesture already exists.  Please record a different gesture.");
                        recordedBasicGestures.removeAllElements();
                        recordedGesture = null;
                    }
           
                } finally{
                    Global.classifiedBasicGesturesLock.writeLock().unlock();
                }
                
            }else if(Global.systemState == Global.SYS_TEST_GESTURE){
                //we know our freshly recorded basic gestures, now compare it to what the user is doing
                int matched = 0;
                Global.classifiedBasicGesturesLock.writeLock().lock();
                try{
                    //first check if the user has performed the correct number of gestures
                    if(Global.classifiedBasicGestures.size()==recordedBasicGestures.size() && recordedBasicGestures.size()!=0){
                        p = new PluginRef("TestApp","blah");
                        gesturePerformed = new Gesture(new Vector(Global.classifiedBasicGestures),p);

                        if(gesturePerformed.equals(recordedGesture)){
                            //confirmed!
                            //store it
                            Vector v = new Vector(recordedGesture.getBasicGestures());
                            
                            Gesture g = new Gesture(v, p);
                            debug("Gesture confirmed!");
                            Global.gestureDBLock.writeLock().lock();
                            try{
                                if(Global.gestureDB.addGesture(g))
                                    debug("Gesture added to the database");
                                else
                                    debug("The same gesture is found in the database, cannot add duplicate gesture");
                            }finally{
                                Global.gestureDBLock.writeLock().unlock();
                            }
                            
                            
                            Global.systemState = Global.SYS_IDLE;//set system to idle so that it won't stay in confirm mode
                            debug("System state has been changed to SYS_IDLE");
                            recordedBasicGestures.removeAllElements();//empty out the vector
                            //notify GUI
                            
                            //run gesture acceptance algorithm to validate the newly recorded gesture
                        } else{
                            //gesture not confirmed
                            debug("Gesture not confirmed, try again!");
                            Global.classifiedBasicGestures.removeAllElements();//start over
                            //notify GUI
                            
                        }
                        Global.endTestGesture();
                    }
                    
                    //nothing has been recorded, cannot test
                    else if(recordedBasicGestures.size() == 0 ){
                        Global.systemState = Global.SYS_IDLE;
                        debug("You did not record any gestures, click on 'Record Gesture' to start over");
                        debug("System state has been changed to SYS_IDLE");
                        Global.endTestGesture();
                        Global.classifiedBasicGestures.removeAllElements();
                        recordedBasicGestures.removeAllElements(); //just to be safe
                    } else{
                        //waiting
                        
                    }
                    //else do nothing, wait for the next turn
                    
                } finally{
                    Global.classifiedBasicGesturesLock.writeLock().unlock();
                }
            }
            
            else if(Global.systemState == Global.SYS_IDLE){
                //do nothing? clear some global variables and vectors?
            } else if(Global.systemState == Global.SYS_RECOGNITION_MODE){
                //look at Global.gesture and find it in database
                //if not found, do nothing?
                //if found, fire action
            }
            
        } finally{
            Global.systemStateLock.writeLock().unlock();
        }
        
    }
    /*
    public void doIt2(){
        boolean stopRecording = false;
        Global.systemStateLock.writeLock().lock();
      
        Gesture gesturePerformed;
        PluginRef p;
        try{
            if(Global.systemState == Global.SYS_STOP_RECORDING){
                //capture the basic gestures that the user has done, this happens when the stop recording button is clicked
                Global.classifiedBasicGesturesLock.writeLock().lock();
                try{
                    //always take the first MAX_NUM_BASIC_GESTURE basic gestures
                    for(int i=0; i<min(Global.MAX_NUM_BASIC_GESTURE,Global.classifiedBasicGestures.size()); i++){
                        recordedBasicGestures.addElement(Global.classifiedBasicGestures.elementAt(i));
                        
                    }
                    Global.classifiedBasicGestures.removeAllElements();   //clear the vector after copying to the local recordedBasicGestures vector
                    debug("recordedBasicGesturess has size " + recordedBasicGestures.size());
                    debug("classifedBasicGestures vector is cleared");
                    Global.systemState = Global.SYS_IDLE;
                    debug("System state has been changed to SYS_IDLE");
                    p = new PluginRef("TestApp","blah");
                    recordedGesture = new Gesture(new Vector(recordedBasicGestures), p);
                    
                    if(Global.gestureDB.gestureExists(recordedGesture)){
                        debug("The same gesture already exists.  Please record a different gesture.");
                        recordedBasicGestures.removeAllElements();
                        recordedGesture = null;
                    }
           
                } finally{
                    Global.classifiedBasicGesturesLock.writeLock().unlock();
                }
                
            }else if(Global.systemState == Global.SYS_TEST_GESTURE){
                //we know our freshly recorded basic gestures, now compare it to what the user is doing
                int matched = 0;
                Global.classifiedBasicGesturesLock.writeLock().lock();
                try{
                    //first check if the user has performed the correct number of gestures
                    if(Global.classifiedBasicGestures.size()==recordedBasicGestures.size() && recordedBasicGestures.size()!=0){
                        p = new PluginRef("TestApp","blah");
                        gesturePerformed = new Gesture(new Vector(Global.classifiedBasicGestures),p);

                        if(gesturePerformed.equals(recordedGesture)){
                            //confirmed!
                            //store it
                            Vector v = new Vector(recordedGesture.getBasicGestures());
                            
                            Gesture g = new Gesture(v, p);
                            debug("Gesture confirmed!");
                            Global.gestureDBLock.writeLock().lock();
                            try{
                                if(Global.gestureDB.addGesture(g))
                                    debug("Gesture added to the database");
                                else
                                    debug("The same gesture is found in the database, cannot add duplicate gesture");
                            }finally{
                                Global.gestureDBLock.writeLock().unlock();
                            }
                            
                            
                            Global.systemState = Global.SYS_IDLE;//set system to idle so that it won't stay in confirm mode
                            debug("System state has been changed to SYS_IDLE");
                            recordedBasicGestures.removeAllElements();//empty out the vector
                            //notify GUI
                            
                            //run gesture acceptance algorithm to validate the newly recorded gesture
                        } else{
                            //gesture not confirmed
                            debug("Gesture not confirmed, try again!");
                            Global.classifiedBasicGestures.removeAllElements();//start over
                            //notify GUI
                            
                        }
                        Global.endTestGesture();
                    }
                    
                    //nothing has been recorded, cannot test
                    else if(recordedBasicGestures.size() == 0 ){
                        Global.systemState = Global.SYS_IDLE;
                        debug("You did not record any gestures, click on 'Record Gesture' to start over");
                        debug("System state has been changed to SYS_IDLE");
                        Global.endTestGesture();
                        Global.classifiedBasicGestures.removeAllElements();
                        recordedBasicGestures.removeAllElements(); //just to be safe
                    } else{
                        //waiting
                        
                    }
                    //else do nothing, wait for the next turn
                    
                } finally{
                    Global.classifiedBasicGesturesLock.writeLock().unlock();
                }
            }
            
            else if(Global.systemState == Global.SYS_IDLE){
                //do nothing? clear some global variables and vectors?
            } else if(Global.systemState == Global.SYS_RECOGNITION_MODE){
                //look at Global.gesture and find it in database
                //if not found, do nothing?
                //if found, fire action
            }
            
        } finally{
            Global.systemStateLock.writeLock().unlock();
        }
        
    }*/
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void clear() {
        // clear private data
    }
    public void run() {
        System.out.println("Controller Thread started ...");
        hostLoop();
    }
//main loop
    public void hostLoop(){
        running = true;
        while(running)
            try{
                doIt();
            } catch(Exception e){
                System.out.println(e);
            }
    }
    public void doQuit(){
        running = false;
    }
    
    public void testGesture(){
        Global.classifiedBasicGesturesLock.writeLock().lock();
        try{
            Global.classifiedBasicGestures.removeAllElements();
            debug("classifiedBasicGestures vector cleared");
        } finally{
            Global.classifiedBasicGesturesLock.writeLock().unlock();
        }
        Global.systemStateLock.writeLock().lock();
        try{
            Global.systemState = Global.SYS_TEST_GESTURE;
            debug("System state has been changed to Test Gesture");
        } finally{
            Global.systemStateLock.writeLock().unlock();
        }
    }
    
    public void recordGesture(){
        
        Global.systemStateLock.writeLock().lock();
        try{
            Global.systemState = Global.SYS_RECORDING_MODE;
            debug("System state has been changed to SYS_RECORDING_MODE");
        } finally{
            Global.systemStateLock.writeLock().unlock();
        }
        
        recordedBasicGestures.removeAllElements();
        debug("recordedBasicGestures vector has been cleared");
    }
    public void stopRecording(){
        Global.systemStateLock.writeLock().lock();
        try{
            Global.systemState = Global.SYS_STOP_RECORDING;
            debug("System state has been changed to SYS_STOP_RECORDING");
        } finally{
            Global.systemStateLock.writeLock().unlock();
        }
    }
}
