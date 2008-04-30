package sunflare.plugin;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sunflare.server.Gesture;
import sunflare.utils.XMLStructFactory;
import sunflare.utils.XMLUtils;


public class PluginLayer {
    
    private PluginFactory _PF;
    private Vector<PluginRef> pluginRefs;
    private HashMap<String,Plugin> myPlugins;
    private GestureListener gestureListener;
    
    private static FileFilter pluginXmlFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.toString().endsWith("plugin.xml");
        }
    };
    
    public PluginLayer(){
        
        _PF = new PluginFactory();
        pluginRefs = new Vector<PluginRef>();
        myPlugins = new HashMap<String,Plugin>();
        gestureListener = null;
        
        //parse out plugin config file dirs
                /*String pluginDirUris = System
                .getProperty("sunflare.plugin.dirs");
                 
                List pluginDirList = null;
                if (pluginDirUris != null) {
                        String[] dirUris = pluginDirUris.split(",");
                        pluginDirList = Arrays.asList(dirUris);
                }*/
        
        //load plugin refs from the xml files
        //loadPluginInfo(pluginDirList);
        
        loadPluginInfo();
        //load the plugins via the factory
        for(int i=0;i<pluginRefs.size();i++){
            Plugin plugin = _PF.getPluginFromFactory(pluginRefs.get(i).getClassPath());
            myPlugins.put(pluginRefs.get(i).getName(), plugin);
        }
    }
    
    public PluginLayer(GestureListener gl){
        
        _PF = new PluginFactory();
        pluginRefs = new Vector<PluginRef>();
        myPlugins = new HashMap<String,Plugin>();
        gestureListener = gl;
        
        //parse out plugin config file dirs
                /*String pluginDirUris = System
                .getProperty("sunflare.plugin.dirs");
                 
                List pluginDirList = null;
                if (pluginDirUris != null) {
                        String[] dirUris = pluginDirUris.split(",");
                        pluginDirList = Arrays.asList(dirUris);
                }*/
        
        //load plugin refs from the xml files
        //loadPluginInfo(pluginDirList);
        
        loadPluginInfo();
        //load the plugins via the factory
        for(int i=0;i<pluginRefs.size();i++){
            Plugin plugin = _PF.getPluginFromFactory(pluginRefs.get(i).getClassPath());
            myPlugins.put(pluginRefs.get(i).getName(), plugin);
        }
    }
    
    public Vector getAllPluginRefs(){
        return pluginRefs;
    }
    
    public int getNumPluginRefs(){
        return pluginRefs.size();
    }
    
    public void executePlugin(String pluginName, Gesture gesture){
        if (gestureListener == null) {
            Plugin p = myPlugins.get(pluginName);
            p.fireCallBack(gesture);
        }
        else {
            System.out.println("action performed in executePlugin()");
            GestureEvent ge = new GestureEvent();
            ge.setGesture(gesture);
            gestureListener.actionPerformed(ge);
        }
    }
    
    
    //private void loadPluginInfo(List dirUris) {
    private void loadPluginInfo() {
        
           /* if (dirUris != null && dirUris.size() > 0) {
              for (Iterator i = dirUris.iterator(); i.hasNext();) {
                String dirUri = (String) i.next();
            
                try {
                  File pluginDir = new File(new URI(dirUri));
                  if (pluginDir.isDirectory()) {
            
                    String pluginDirStr = pluginDir.getAbsolutePath();
            
                    if (!pluginDirStr.endsWith("/")) {
                      pluginDirStr += "/";
                    }
            
                    // get all the plugin xml files
                    File[] pluginFiles = pluginDir.listFiles(pluginXmlFilter);*/
        
        // for (int j = 0; j < pluginFiles.length; j++) {
        
        //String pluginXmlFile = pluginFiles[j].getAbsolutePath();
        String pluginXmlFile = "plugin.xml";
        Document pluginRoot = XMLUtils.getDocumentRoot(pluginXmlFile);
        NodeList nodeList = pluginRoot.getElementsByTagName("plugin");
        if(nodeList == null){
            System.out.println();
            System.out.println("Node List was null");
            System.out.println();
        }
        
        
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int k = 0; k < nodeList.getLength(); k++) {
                PluginRef plugin = XMLStructFactory.getPluginRefs((Element) nodeList.item(k));
                pluginRefs.add(plugin);
            }
        }
        //}
    }
    //} catch (URISyntaxException e) {
    //  e.printStackTrace();
    //}
    // }
    //}
    //s}
    
    
}
