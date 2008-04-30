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

    /** Creates a PluginRef with specified parameters
     * @param name name of the plugin
     * @param application name of the application
     * @param description description of the plugin
     * @param classpath location of the class
     */
    public PluginRef(String name, String application, String description, String classpath){
        this.name = name;
        this.application = application;
        this.description = description;
        this.classpath = classpath;
    }

    /**
     * Gets name of the application
     * @return name of the application
     */
   public String getApplication() {
	   return application;
   }
   /**
    * Gets the class path
    * @return class path of the plugin
    */
   public String getClassPath(){
	   return classpath;
   }
   /**
    * Sets the name of the application
    * @param application name of the application
    */
   public void setApplication(String application) {
	   this.application = application;
   }

    public String getActionDescription() {
        return actionDescription;
    }
    public String getDescription() {
        return description;
    }
    
    /*
    public PluginRef(String pluginName, String action){
        name = pluginName;
        actionDescription = action;
    }*/
    /**
     * Creates a new plugin with the specified parameters
     * @param application name of the application
     * @param description of the plugin
     */
     public PluginRef(String application, String description){
        this.application = application;
        this.description = description;
        this.name = application;
        this.actionDescription = description;
    }

     /**
      * Gets the name of the plugin
      * @return name of the plugin
      */
    public String getName() {
        return name;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }
    /**
     * Sets the name of the plugin
     * @param name name of the plugin
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Sets the class path of the plugin
     * @param classpath class path of the plugin
     */
   public void setClassPath(String classpath){
	   this.classpath = classpath;
   }

   public String toString(){
	  String rep = this.application+": "+this.description;
	  return rep;
   }    

}
