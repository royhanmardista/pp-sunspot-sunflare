/*
 * SlopeDuration.java
 *
 * Created on March 30, 2008, 4:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

/**
 *
 * @author Winnie
 */
public class SlopeWeight{
    public boolean slope;
    public int weight;
    /** Creates a new instance of SlopeDuration */
    public SlopeWeight() {
        slope = false;
        weight = 0;
    }
    public SlopeWeight(boolean s, int count){
        slope = s;
        weight = count;
    }
    
}
