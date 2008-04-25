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
 * @author Praveen
 */

import java.util.Vector;

public class Gesture {
    private Vector basicGestures;
    /** Creates a new instance of Gesture */
    public Gesture() {
        basicGestures = new Vector();
    }

    public void addToVector(int a){
        basicGestures.addElement(a);
    }
    
    public int getVectorSize(){
        return basicGestures.size();
    }
    
    public Vector getVector(){
        return basicGestures;
    }
    
    public void clearVector(){
        basicGestures.removeAllElements();
    }
    public boolean validateGesture(){
        return false;
    }
}
