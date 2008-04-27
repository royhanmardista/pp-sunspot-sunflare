/*
 * PluginDatabase.java
 *
 * Created on April 26, 2008, 6:20 PM
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


public class PluginDatabase {
    private Vector pluginRefs;
    /** Creates a new instance of PluginDatabase */
    public PluginDatabase() {
        pluginRefs = new Vector();
        PluginRef pr = new PluginRef("App1", "Action 1");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App1", "Action 2");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App1", "Action 3");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App1", "Action 4");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App1", "Action 5");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App1", "Action 6");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App2", "Action 1");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App2", "Action 2");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App2", "Action 3");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App2", "Action 4");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App2", "Action 5");
        pluginRefs.addElement(pr);
        pr = new PluginRef("App2", "Action 6");
        pluginRefs.addElement(pr);
    }
    
    public Vector getAllPluginRefs(){
        return pluginRefs;
    }
    public int getNumPluginRefs(){
        return pluginRefs.size();
    }
    
    
}
