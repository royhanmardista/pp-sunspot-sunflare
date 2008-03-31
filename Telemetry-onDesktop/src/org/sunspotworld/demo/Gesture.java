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
    private Vector pattern;
    private String inactiveAxis;
    private String activeAxis;
    private double endTimeStamp;
    
    /** Creates a new instance of Gesture */
    public Gesture() {
        dataset = new Vector();
        nodes = new Vector();
        pattern = new Vector();
        inactiveAxis = "";
        endTimeStamp = 0;
    }
    public Gesture(Vector d){
        dataset = d;
    }
    public Gesture(Vector d, double ts){
        dataset = d;
        endTimeStamp = ts;
    }
    public void setEndTimeStamp(double ts){
        endTimeStamp = ts;
    }
    public void setPattern(Vector p){
        pattern = new Vector(p);
    }
    public void setActiveAxis(String s){
        activeAxis = new String(s);
    }
    public void setData(Vector d){
        dataset = new Vector(d);
    }
    public void appendData(Vector d){
        dataset.addAll(d);
    }
    public void combine(Gesture g){
         dataset.addAll(g.getDataset());
         int originalPatternSize = pattern.size();
         //first, combine the patterns
         pattern.addAll(g.getPattern());
         //check to see if the slope of the last element of the original pattern is the same as the slope of the first element of the newly added pattern
         if(((SlopeWeight)pattern.elementAt(originalPatternSize-1)).slope == ((SlopeWeight)g.getPattern().firstElement()).slope){
             int oldWeight = ((SlopeWeight)pattern.elementAt(originalPatternSize-1)).weight;
             pattern.removeElementAt(originalPatternSize-1);
             ((SlopeWeight)pattern.elementAt(originalPatternSize-1)).weight += oldWeight;
         }
         endTimeStamp = g.getEndTimeStamp();
    }
    public double getEndTimeStamp(){
        return endTimeStamp;
    }
    
    public String getActiveAxis(){
        return activeAxis;
    }
    public Vector getPattern(){
        return pattern;
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
    public String toString(){
        StringBuilder result = new StringBuilder();
        
        result.append("active axis/" + activeAxis + " pattern size = "+pattern.size()+ " ");
        for(int i=0; i<pattern.size(); i++){
            result.append(pattern.elementAt(i) + " ");
        }
        return result.toString();

    }
}
