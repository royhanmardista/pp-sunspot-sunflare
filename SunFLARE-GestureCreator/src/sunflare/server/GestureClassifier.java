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
/**
 *
 * @author Praveen
 */
public class GestureClassifier extends Thread{
    private boolean running = false;
    private Gesture recordedGesture = new Gesture();
    private boolean debug=true;
    /** Creates a new instance of GestureClassifier */
    public GestureClassifier() {
        
    }
    public void classifyGesture(Gesture gesture){
        PluginRef p;
        Global.gestureDBLock.writeLock().lock();
        try{
           // p = Global.gestureDB.search(gesture);
            gesture.scan();
        } finally{
            Global.gestureDBLock.writeLock().unlock();
        }
        /*
        if(p!=null){
            debug("Gesture found: " + p.getName()+ " " + p.getActionDescription());
        } else{
            debug("Gesture not found");
        }
        */
        
        // since the classified is always the first one in the gestures vector, it is safe to remove it from the vector
        /*
        Global.gesturesLock.writeLock().lock();
        try{
            Global.gestures.removeElement(gesture);
            //Global.gestures.removeElementAt(0);
        }finally{
            Global.gesturesLock.writeLock().unlock();
        }
     */   
    }
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
