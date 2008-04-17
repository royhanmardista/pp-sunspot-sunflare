/*
 * DataStruct.java
 *
 * Created on March 30, 2008, 12:08 AM, Modified on April 16, 2008, 18:08 by Nikhilesh
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

//package org.sunspotworld.demo;
package gestures;

/**
 *
 * @author Winnie
 */
public class DataStruct {
    private Long id;	// primary key 
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

	public Long getId()	// returns the id
	{
		return id;
	}

	private void setId( Long id )	// hibernate sets the id
	{
		this.id = id;
	}
    
    public double getX(){
        return x;
    }

	public void setX( double x )	// set x
	{
		this.x = x;
	}

    public double getY()
	{
        return y;
    }

	public void setY( double y )	// set y
	{
		this.y = y;
	}

    public double getZ(){
        return z;
    }

	public void setZ( double z )	// set z
	{
		this.z = z;
	}

    public double getDx(){
        return dx;
    }

	public void setDx( double dx )	// set dx
	{
		this.dx = dx;
	}

    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("x = " + x + " dx = " + dx);
        return result.toString();
    }
    
	public double getDy(){
        return dy;
    }

	public void setDy( double dy )	// set dy
	{
		this.dy = dy;
	}

    public double getDz(){
        return dz;
    }

	public void setDz( double dz )	// set dz
	{
		this.dz = dz;
	}

    public double getTotalG(){
        return totalG;
    }

	public void setTotalG( double totalG )	// set totalG
	{
		this.totalG = totalG;
	}

    public double getTimeStamp(){
        return timeStamp;
    }

	public void setTimeStamp( double timeStamp )	// set timeStamp
	{
		this.timeStamp = timeStamp;
	}

    public void setInactiveAxis(String axis){
        inactiveAxis = new String(axis);
    }

	public String getInactiveAxis()	// get inactiveAxis
	{
		return inactiveAxis;
	}
}
