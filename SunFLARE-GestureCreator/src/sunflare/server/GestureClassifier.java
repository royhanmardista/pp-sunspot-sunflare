/*
 * GestureClassifier.java
 *
 * Created on April 1, 2008, 9:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.server;

import java.util.Vector;
import sunflare.plugin.PluginRef;
import sunflare.plugin.PluginLayer;
/**
 *
 * @author Praveen
 */
public class GestureClassifier extends Thread{
    private boolean running = false;
    private Gesture recordedGesture = new Gesture();
    private boolean debug=true;
    private boolean serviceMode;
    private PluginLayer pLayer;
    /** Creates a new instance of GestureClassifier */
    public GestureClassifier() {
        //by default it is the GestureCreator mode
        serviceMode = false;
        this.pLayer = null;
    }
    /** Creates a new instance of GestureClassifier */
    public GestureClassifier(PluginLayer pLayer){
        this.pLayer = pLayer;
        this.serviceMode = true;
    }
    /** Looks for the Gesture in the database.  If it is in service mode, it also executes the plugin
     */
    public void classifyGesture(Gesture gesture){
        PluginRef p;
        Global.gestureDBLock.writeLock().lock();
        try{
            gesture.scan(); //mark this gesture as scanned
        } finally{
            Global.gestureDBLock.writeLock().unlock();
        }
        
        if(serviceMode){
            p = Global.gestureDB.search(gesture);
            if(p!=null){
                pLayer.executePlugin(p.getName(),gesture);
                debug("Gesture found: " + p.getApplication()+ " " + p.getName());
            } else{
                debug("Gesture not found");
            }
        }

        // remove it from the vector
        if(serviceMode){
            Global.gesturesLock.writeLock().lock();
            try{
                Global.gestures.removeElement(gesture);
            }finally{
                Global.gesturesLock.writeLock().unlock();
            }
        }
    }
    
    /** Classifies a not-yet-been classified Gesture if it is completed
     */
    public void classifier(){
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
            } else if(Global.gestures.size() == 1 && ((Gesture)Global.gestures.firstElement()).getEndTimestamp()+ Global.IDLE_TIME_BTWN_GESTURES < currentTime){
                classifyGesture((Gesture)Global.gestures.firstElement());
            } else{
                //the gesture is not completed by the user yet or the gestures vector is empty
            }
            
        }finally{
            Global.gesturesLock.writeLock().unlock();
        }
        
    }
    /** Clears private data*/
    public void clear() {
        // clear private data
    }
    /** Runs the thread*/
    public void run() {
        System.out.println("GestureClassifier Thread started ...");
        hostLoop();
    }
    
    /** Main loop*/
    public void hostLoop(){
        running = true;
        while(running)
            try{
                classifier();
            } catch(Exception e){
                System.out.println(e);
            }
    }
    
    /** Kills thread*/
    public void doQuit(){
        running = false;
    }
    /** Prints debugging statements
     */
    public void debug(String s){
        if(debug)
            System.out.println("GestureClassifier: "+s);
    }
    
}
