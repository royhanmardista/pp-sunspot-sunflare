/*
 * PluginRef.java
 *
 * Created on April 24, 2008, 11:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

/**
 * PluginRef object is used by the Gesture object
 * @author Winnie
 */
public class PluginRef {
    String name;
    String actionDescription;
    
    /** Creates a new instance of PluginRef */
    public PluginRef() {

    }

    public String getActionDescription() {
        return actionDescription;
    }
    public PluginRef(String pluginName, String action){
        name = pluginName;
        actionDescription = action;
    }

    public String getName() {
        return name;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public void setName(String name) {
        this.name = name;
    }

}
