/*
 * PersistentJava.java
 *
 * Created on April 27, 2008, 2:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.persistence;

/**
 *
 * @author Praveen
 */
public class PersistentGesture {
 
    private int movment1, moment2, moment3;
    private String pluginRef, action;
    private long id;
    
    /** Creates a new instance of PersistentGesture */
    
    public PersistentGesture(){}
    
	public int getMovment1() {
		return movment1;
	}
	public void setMovment1(int movment1) {
		this.movment1 = movment1;
	}
	public int getMoment2() {
		return moment2;
	}
	public void setMoment2(int moment2) {
		this.moment2 = moment2;
	}
	public int getMoment3() {
		return moment3;
	}
	public void setMoment3(int moment3) {
		this.moment3 = moment3;
	}
	public String getPluginRef() {
		return pluginRef;
	}
	public void setPluginRef(String pluginRef) {
		this.pluginRef = pluginRef;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public long getId() {
		return id;
	}
	private void setId(long id) {
		this.id = id;
	}
    
}
