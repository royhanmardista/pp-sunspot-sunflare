/*
 * GestureDatabase.java
 *
 * Created on April 25, 2008, 11:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;
import java.util.*;
/**
 *
 * @author Winnie
 */
public class GestureDatabase {
    Vector definedGestures = new Vector();
    /** Creates a new instance of GestureDatabase */
    public GestureDatabase() {
        PluginRef plugin;
        plugin = new PluginRef("TestApp", "Gesture 1");
        Gesture g = new Gesture();
        g.setPlugin(plugin);
        g.addBasicGesture(new BasicGesture(Global.LEFT));
        g.addBasicGesture(new BasicGesture(Global.RIGHT));
        g.addBasicGesture(new BasicGesture(Global.LEFT));
        definedGestures.addElement(g);
        g = new Gesture();
        plugin = new PluginRef("TestApp", "Gesture 2");
        g.setPlugin(plugin);
        g.addBasicGesture(new BasicGesture(Global.UP));
        g.addBasicGesture(new BasicGesture(Global.RIGHT));
        definedGestures.addElement(g);
        g = new Gesture();
        plugin = new PluginRef("TestApp", "Gesture 3");
        g.setPlugin(plugin);
        g.addBasicGesture(new BasicGesture(Global.FORWARD));
        g.addBasicGesture(new BasicGesture(Global.LEFT));
        definedGestures.addElement(g);
        g = new Gesture();
        plugin = new PluginRef("TestApp", "Gesture 4");
        g.setPlugin(plugin);
        g.addBasicGesture(new BasicGesture(Global.FORWARD));
        g.addBasicGesture(new BasicGesture(Global.LEFT));
        definedGestures.addElement(g);
        g = new Gesture();
        plugin = new PluginRef("TestApp", "Gesture 5");
        g.setPlugin(plugin);
        g.addBasicGesture(new BasicGesture(Global.DOWN));
        g.addBasicGesture(new BasicGesture(Global.BACKWARD));
        definedGestures.addElement(g);
        g = new Gesture();
        plugin = new PluginRef("TestApp", "Gesture 6");
        g.setPlugin(plugin);
        g.addBasicGesture(new BasicGesture(Global.SHAKE));
        definedGestures.addElement(g);
        
        
    }
    
    PluginRef search(Gesture g){
        Vector key = g.getBasicGestures();
        
        for(int i=0; i<definedGestures.size(); i++){
            Gesture definedGesture = (Gesture)definedGestures.elementAt(i);
            Vector v = definedGesture.getBasicGestures();
            if(key.size() == v.size()){
                if(equal(v,key))
                    return definedGesture.getPlugin();
            }
        }
        return null;
    }
    
    boolean addGesture(Gesture g){
       if(!gestureExists(g)){
           definedGestures.addElement(g);
           return true;
       }
       return false;
    }

    boolean gestureExists(Gesture g){
        for(int i = 0; i<definedGestures.size();i++){
            Gesture k = (Gesture)(definedGestures.elementAt(i));
            if(g.equals(k))
                return true;
        }
        return false;
    }
    
    
    boolean equal(Vector v, Vector k){
        for(int i = 0; i<v.size(); i++){
            if(((BasicGesture)v.elementAt(i)).getID() != ((BasicGesture)k.elementAt(i)).getID())
                return false;
        }
        return true;
    }
    
}
