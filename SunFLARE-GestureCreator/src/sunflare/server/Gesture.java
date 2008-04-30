package sunflare.server;
/*
 * Gesture.java
 *
 * Created on April 1, 2008, 8:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * Gesture object consists of a vector of BasicGesture objects and a PluginRef object
 *
 * @author Praveen
 */

import java.util.Vector;
import sunflare.plugin.PluginRef;
import sunflare.persistence.PersistentGesture;

public class Gesture {
    private Vector basicGestures;
    private PluginRef plugin;
    private boolean scanned = false;
    /** Creates a new instance of Gesture */
    public Gesture() {
        basicGestures = new Vector();
        plugin = new PluginRef();
    }
    
    /**
     * Creates a new gesture from a gesture in the database
     * @param p The gesture from the database
     */
   public Gesture(PersistentGesture p) {
        plugin = new PluginRef(p.getPluginRef(),p.getAction());
        basicGestures = new Vector();
        if(p.getMovment1() != -1){
            basicGestures.addElement(new BasicGesture(p.getMovment1()));        
        }
        if(p.getMovment2() != -1){
            basicGestures.addElement(new BasicGesture(p.getMovment2()));                
        }
        if(p.getMovment3() != -1){
            basicGestures.addElement(new BasicGesture(p.getMovment3()));                
        }
   }
   
    //this method should be removed in the future
   /**
    * Not in use
    */
    public Gesture(int basicGestureID){
        basicGestures = new Vector();
        basicGestures.addElement(basicGestureID);
    }
    
    /**
     * Creates a new gesture from a single movement
     * @param basicGesture the movement made
     */
    public Gesture(BasicGesture basicGesture){
        basicGestures = new Vector();
        basicGestures.addElement(basicGesture);
    }
    /**
     * Creates a new gesture from a vector of movements and a PluginRef
     * @param basicGestures vector of movements
     * @Param plugin pluging associated with gesture
     */
    public Gesture(Vector basicGestures, PluginRef plugin){
        if(basicGestures.size() <= Global.MAX_NUM_BASIC_GESTURE){
            this.basicGestures = new Vector(basicGestures);
            this.plugin = plugin;
        }
    }
    //this method should be removed in the future
    /**
     * Not in use
     */
    public boolean addBasicGesture(int basicGestureID){
        if(basicGestures.size()<Global.NUMBER_OF_MOVEMENTS_PER_GESTURE){
            basicGestures.addElement(basicGestureID);
            return true;
        } else
            return false;
        
    }
    /**
     * Returns the last timestamp
     * @return the timestamp
     */
    public double getEndTimestamp(){
        return ((BasicGesture)basicGestures.lastElement()).getEndTimeStamp();
    }
    /**
     * Adds a single movement into the gesture
     * @param basicGesture the movement to be added
     * @return true if movement is successfully added, false otherwise
     */
    public boolean addBasicGesture(BasicGesture basicGesture){
        if(basicGestures.size()<Global.NUMBER_OF_MOVEMENTS_PER_GESTURE){
            basicGestures.addElement(basicGesture);
            return true;
        } else
            return false;
    }
    
    /**
     * Returns current number of movements in the gesture
     * @return number of movements in the gesture
     */
    public int getNumBasicGestures(){
        return basicGestures.size();
    }
    /**
     * Returns vector of movements in the gesture
     * @return vector of movements
     */
    public Vector getBasicGestures(){
        return basicGestures;
    }
    /**
     * Clears the gesture of all movements
     */
    public void removeAllBasicGestures(){
        basicGestures.removeAllElements();
    }
    /**
     * Returns the plugin assigned to the gesture
     * @return assigned plugin
     */
    public PluginRef getPlugin() {
        return plugin;
    }
    /**
     * Sets the plugin for the gesture
     * @param plugin plugin to be used
     */
    public void setPlugin(PluginRef plugin) {
        this.plugin = plugin;
    }
    //pluginref and basic movements are all the same
    /**
     * Checks to see if the gesture passed in matches this gesture
     * @param h the gesture passed in
     * @return true if the two gestures are the same
     */
    boolean equals(Gesture h){
        Vector v = this.getBasicGestures();
        Vector k = h.getBasicGestures();
        if(v.size() != k.size())
            return false;
        for(int i = 0; i<v.size(); i++){
            if(!this.getPlugin().getName().equals(h.getPlugin().getName()) || ((BasicGesture)v.elementAt(i)).getID() != ((BasicGesture)k.elementAt(i)).getID())
                return false;
        }
        return true;
    }
    //returns true if the basic gestures are the same, regardless of the pluginref
    /**
     * Checks to see if the movements are the same, ignores PluginRef
     * @param h the gesture to be checked
     * @return true if the movements are the same (regardless of the assigned plugin)
     */
    boolean sameMovements(Gesture h){
        Vector v = this.getBasicGestures();
        Vector k = h.getBasicGestures();
        if(v.size() != k.size())
            return false;
        for(int i = 0; i<v.size(); i++){
            if(((BasicGesture)v.elementAt(i)).getID() != ((BasicGesture)k.elementAt(i)).getID())
                return false;
        }
        return true;
    }
    
    public String toString(){
        StringBuilder result = new StringBuilder();
        for(int i=0; i<basicGestures.size();i++){
            result.append(((BasicGesture)basicGestures.elementAt(i)).toString()+" ");
        }
        return result.toString();
    }
    /**
     * Sets scanned to be true
     */
    public void scan(){
        scanned = true;
    }
    /**
     * Returns if the gesture has been scanned or not
     * @return true if the gesture has been scanned
     */
    public boolean isScanned(){
        return scanned;
    }
}
