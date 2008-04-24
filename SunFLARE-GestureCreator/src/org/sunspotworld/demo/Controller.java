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
 *
 * @author Winnie
 */
public class Controller extends Thread{
    private boolean running = false;
    private Vector recordedBasicGestures = new Vector();
    /** Creates a new instance of Controller */
    public Controller() {
    }
    public void doIt(){
        Global.systemStateLock.writeLock().lock();
        try{
            if(Global.systemState == Global.SYS_RECORDING_MODE){
                //checks Global.basicGestures vector
                //saves it to recordedBasicGestures
            } else if(Global.systemState == Global.SYS_CONFIRM_MODE){
                //checks Global.basicGestures vector
                //compare it to recordedBasicGestures
                //if done right, call a GUI method and store the gesture
                //if done wrong, call a GUI method and clear basicGestures vector 
            } else if(Global.systemState == Global.SYS_IDLE){
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
    
}
