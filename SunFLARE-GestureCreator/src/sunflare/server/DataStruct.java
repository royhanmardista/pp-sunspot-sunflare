/*
 * DataStruct.java
 *
 * Created on March 30, 2008, 12:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.server;

/**
 * Holds data received from the SunSPOT
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
    /**
     * Creates new DataStruct with specified data
     * @param xg acceleration along the x-axis
     * @param yg acceleration along the y-axis
     * @param zg acceleration along the z-axis
     * @param tg total gravity acting on the sunspot
     * @param time timestamp of the data
     * @param d_x no longer used
     * @param d_y no longer used
     * @param d_z no longer used 
     */
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
    
    /**
     * Returns acceleration along x-axis
     * @return acceleration along x-axis
     */
    public double getX(){
        return x;
    }
    /**
     * Returns acceleration along y-axis
     * @return acceleration along y-axis
     */
    public double getY(){
        return y;
    }
    /**
     * Returns acceleration along z-axis
     * @return acceleration along z-axis
     */
    public double getZ(){
        return z;
    }
    public double getDx(){
        return dx;
    }
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("z/" + z + " ts/" + timeStamp);
        return result.toString();
    }
    public double getDy(){
        return dy;
    }
    public double getDz(){
        return dz;
    }
    
    /**
     * Returns total gravity acting on sunspot
     * @return total gravity
     */
    public double getTotalG(){
        return totalG;
    }
    /**
     * Returns the timestamp of this data
     * @return timestamp
     */
    public double getTimeStamp(){
        return timeStamp;
    }
    
    /**
     * Sets the axis that is inactive
     * @param axis name of the axis
     */
    public void setInactiveAxis(String axis){
        inactiveAxis = new String(axis);
    }
}
