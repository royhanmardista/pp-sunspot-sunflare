package sunflare.plugin;



public class PluginFactory {
    
    /**
     * Retreives plugins for the specified callback
     * @param callBack name of the callback
     * @return the plugin associated with the callback
     */
    public Plugin getPluginFromFactory(String callBack){
        
        Class pluginClass = null;
        Plugin pluginInstance = null;
        
        try {
            pluginClass = Class.forName(callBack);
            pluginInstance = (Plugin) pluginClass.newInstance();
            
        } catch (ClassNotFoundException e) {
            
            e.printStackTrace();
        } catch (InstantiationException e) {
            
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            
            e.printStackTrace();
        }
        
        return pluginInstance;
    }
}
