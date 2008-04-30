/*
 * BasicGesture.java
 *
 * Created on March 30, 2008, 2:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.server;

import java.util.Vector;
/**
 * Holds a single movement for a gesture
 * @author Winnie
 */
public class BasicGesture {
    private Vector dataset;
    private Vector nodes;
    private Vector pattern;
    private String inactiveAxis;
    private String activeAxis;
    private int ID;
    private double endTimeStamp;
    
    /** Creates a new instance of BasicGesture */
    
    public BasicGesture() {
        dataset = new Vector();
        nodes = new Vector();
        pattern = new Vector();
        inactiveAxis = "";
        endTimeStamp = 0;
        ID = Global.UNDEFINED;
    }
    
    public BasicGesture(int id){
        ID = id;
    }
    /**
     * Create a new instance of BasicGesture
     *
     * @param d the vector of dataset
     */
    public BasicGesture(Vector d){
        dataset = d;
    }
    /**
     * Create a new instance of BasicGesture
     *
     * @param d dataset vector
     * @param ts timestmap of the last element in d
     */
    public BasicGesture(Vector d, double ts){
        dataset = d;
        endTimeStamp = ts;
    }
    /**
     * Set timestamp
     *
     * @param ts timestamp
     */
    public void setEndTimeStamp(double ts){
        endTimeStamp = ts;
    }
    /**
     * Set pattern vector
     *
     * @param p pattern vector
     */
    public void setPattern(Vector p){
        pattern = new Vector(p);
    }
    /**
     * Set active axis
     *
     * @param s lower case of the axis; i.e. x,y,z,s (s is shaking)
     */
    public void setActiveAxis(String s){
        activeAxis = new String(s);
    }
    
    /**
     * Set data
     *
     * @param d dataset vector
     */
    public void setData(Vector d){
        dataset = new Vector(d);
    }
    /**
     * Append dataset to this.dataset
     *
     * @param d dataset to be appended
     */
    public void appendData(Vector d){
        dataset.addAll(d);
    }
    
    /**
     * Merge two gestures together by appending the passed-in gesture's dataset to this.dataset.
     * It also does an intelligent merge of patterns.
     *
     * @param g the gesture that you want to merge
     */
    public void combine(BasicGesture g){
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
    /**
     * Get timestamp
     * @return timestamp
     */
    public double getEndTimeStamp(){
        return endTimeStamp;
    }
    
    /**
     * Get active axis
     * @return active axis
     */
    public String getActiveAxis(){
        return activeAxis;
    }
    /**
     * Get pattern vector
     * @return pattern vector
     */
    public Vector getPattern(){
        return pattern;
    }
    /**
     * Get dataset vector
     * @return dataset vector
     */
    public Vector getDataset(){
        return dataset;
    }
    /**
     * Set inactve axis
     * @param s lower case letter of the inactive axis; i.e. x,y,z
     */
    public void setInactiveAxis(String s){
        inactiveAxis = new String(s);
    }
    /**
     * Get inactive axis
     * @return inactive axis
     */
    public String getInactiveAxis(){
        return inactiveAxis;
    }
    /**
     * Get active axis
     * @return String representation of the BasicGesture
     */
    public String toString(){/*
        StringBuilder result = new StringBuilder();
        
        result.append("active axis/" + activeAxis + " pattern size = "+pattern.size()+ " ");
        for(int i=0; i<pattern.size(); i++){
            result.append(pattern.elementAt(i) + " ");
        }
        return result.toString();
        */
        StringBuilder result = new StringBuilder();
        result.append("id/" + ID);
        return result.toString();
    }
    
    /**
     * Sets the id of the basic gesture
     * @param ID id the basic gesture is to be set to
     */
    public void setID(int ID) {
        this.ID = ID;
    }
    /**
     * Returns the id of the basic gesture
     * @return the id of the basic gesture
     */
    public int getID() {
        return ID;
    }
}
