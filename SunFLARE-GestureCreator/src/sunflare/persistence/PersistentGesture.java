/*
 * PersistentJava.java
 *
 * Created on April 27, 2008, 2:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.persistence;

import sunflare.server.BasicGesture;
import sunflare.server.Gesture;
import java.util.Vector;

/**
 *
 * @author Praveen
 */
public class PersistentGesture {
 
    private int movment1,movment2,movment3;
    private String pluginRef, action;
    private long id;
    
    /** Creates a new instance of PersistentGesture */
    
    public PersistentGesture(){}

   public PersistentGesture(Gesture g){
       Vector basicGestures = g.getBasicGestures();
       int count = g.getNumBasicGestures();
       if(count == 0){
            movment1 = movment2 = movment3 = -1;
       }else if(count == 1){
            movment1 = ((BasicGesture)basicGestures.elementAt(0)).getID();
            movment2 = movment3 = -1;
       }else if(count == 2){
            movment1 = ((BasicGesture)basicGestures.elementAt(0)).getID();
            movment2 = ((BasicGesture)basicGestures.elementAt(1)).getID();
            movment3 = -1;       
       }else if(count == 3){
            movment1 = ((BasicGesture)basicGestures.elementAt(0)).getID();
            movment2 = ((BasicGesture)basicGestures.elementAt(1)).getID();
            movment3 = ((BasicGesture)basicGestures.elementAt(2)).getID();              
       }
       pluginRef = g.getPlugin().getName();
       action = g.getPlugin().getDescription();
       
   }
       
    public int getMovment1() {
        return movment1;
    }
    public void setMovment1(int movment1) {
            this.movment1 = movment1;
    }
    public int getMovment2() {
            return movment2;
    }
    public void setMovment2(int movment2) {
            this.movment2 = movment2;
    }
    public int getMovment3() {
            return movment3;
    }
    public void setMovment3(int movment3) {
            this.movment3 = movment3;
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
