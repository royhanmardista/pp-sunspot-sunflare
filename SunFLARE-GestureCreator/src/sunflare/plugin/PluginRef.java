/*
 * PluginRef.java
 *
 * Created on April 24, 2008, 11:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.plugin;

/**
 * PluginRef object is used by the Gesture object
 * @author Winnie
 */
public class PluginRef {
    String name;
    String actionDescription;
    String classpath;
    String application;
    String description;

    
    /** Creates a new instance of PluginRef */
    public PluginRef() {

    }

    public PluginRef(String name, String application, String description, String classpath){
        this.name = name;
        this.application = application;
        this.description = description;
        this.classpath = classpath;
    }

   public String getApplication() {
	   return application;
   }
   
   public String getClassPath(){
	   return classpath;
   }
   
   public void setApplication(String application) {
	   this.application = application;
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
    
   public void setClassPath(String classpath){
	   this.classpath = classpath;
   }

   public String toString(){
	  String rep = this.application+": "+this.description;
	  return rep;
   }    

}
