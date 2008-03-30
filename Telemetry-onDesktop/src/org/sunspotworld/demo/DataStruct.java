/*
 * DataStruct.java
 *
 * Created on March 30, 2008, 12:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

/**
 *
 * @author Winnie
 */
public class DataStruct {
    private double x;
    private double y;
    private double z;
    private double totalG;
    private double timeStamp;
    private String inactiveAxis;
    private double dx;
    private double dy;
    private double dz;
    /** Creates a new instance of DataStruct */
    public DataStruct() {
        x = 0;
        y = 0;
        z = 0;
        totalG = 0;
        timeStamp = 0;
        dx = 0;
        dy = 0;
        dz = 0;
    }
    
    public DataStruct(double xg, double yg, double zg, double tg, double time, double d_x, double d_y, double d_z){
        x = xg;
        y = yg;
        z = zg;
        totalG = tg;
        timeStamp = time;
        dx = d_x;
        dy = d_y;
        dz = d_z;
    }
    
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getZ(){
        return z;
    }
    public double getDx(){
        return dx;
    }
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("x = " + x + " dx = " + dx);
        return result.toString();
    }
    public double getDy(){
        return dy;
    }
    public double getDz(){
        return dz;
    }
    public double getTotalG(){
        return totalG;
    }
    public double getTimeStamp(){
        return timeStamp;
    }
    
    public void setInactiveAxis(String axis){
        inactiveAxis = new String(axis);
    }
}
