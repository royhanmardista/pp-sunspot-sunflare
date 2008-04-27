package org.sunspotworld.demo;
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

public class Gesture {
    private Vector basicGestures;
    private PluginRef plugin;
    
    /** Creates a new instance of Gesture */
    public Gesture() {
        basicGestures = new Vector();
        plugin = new PluginRef();
    }
    //this method should be removed in the future
    public Gesture(int basicGestureID){
        basicGestures = new Vector();
        basicGestures.addElement(basicGestureID);
    }
    
    public Gesture(BasicGesture basicGesture){
        basicGestures = new Vector();
        basicGestures.addElement(basicGesture);
    }
    public Gesture(Vector basicGestures, PluginRef plugin){
        if(basicGestures.size() <= Global.MAX_NUM_BASIC_GESTURE){
            this.basicGestures = new Vector(basicGestures);
            this.plugin = plugin;
        }
    }
    //this method should be removed in the future
    public boolean addBasicGesture(int basicGestureID){
        if(basicGestures.size()<Global.NUMBER_OF_MOVEMENTS_PER_GESTURE){
            basicGestures.addElement(basicGestureID);
            return true;
        } else
            return false;
        
    }
    
    public double getEndTimestamp(){
        return ((BasicGesture)basicGestures.lastElement()).getEndTimeStamp();
    }
    public boolean addBasicGesture(BasicGesture basicGesture){
        if(basicGestures.size()<Global.NUMBER_OF_MOVEMENTS_PER_GESTURE){
            basicGestures.addElement(basicGesture);
            return true;
        } else
            return false;
    }
    
    public int getNumBasicGestures(){
        return basicGestures.size();
    }
    public Vector getBasicGestures(){
        return basicGestures;
    }
    public void removeAllBasicGestures(){
        basicGestures.removeAllElements();
    }
    public PluginRef getPlugin() {
        return plugin;
    }
    public void setPlugin(PluginRef plugin) {
        this.plugin = plugin;
    }
    //pluginref and basic movements are all the same
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
}
