/*
 * Gesture.java
 *
 * Created on March 30, 2008, 2:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

import java.util.Vector;
/**
 *
 * @author Winnie
 */
public class Gesture {
    private Vector dataset;
    private Vector nodes;
    private String inactiveAxis;
    /** Creates a new instance of Gesture */
    public Gesture() {
    }
    public Gesture(Vector d){
        dataset = d;
    }
    public void setData(Vector d){
        dataset = new Vector(d);
    }
    public Vector getDataset(){
        return dataset;
    }
    public void setInactiveAxis(String s){
        inactiveAxis = new String(s);
    }
    public String getInactiveAxis(){
        return inactiveAxis;
    }
}
