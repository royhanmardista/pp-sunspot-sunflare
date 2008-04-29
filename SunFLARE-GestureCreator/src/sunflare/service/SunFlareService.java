package sunflare.service;

import java.util.Vector;

import sunflare.gui.GestureCreatorGUI;
import sunflare.plugin.PluginLayer;
import sunflare.server.Gesture;
import sunflare.plugin.PluginRef;

public class SunFlareService {
    
    private PluginLayer pLayer;
    
    public static void main(String[] args){
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SunFLARE Gesture Creator");
        GestureCreatorGUI gui = new GestureCreatorGUI();
    }
    
    
    public SunFlareService(){
        pLayer = new PluginLayer();        
    }
    //gui.show();
    
    
    //Vector<PluginRef> refs =  pLayer.getAllPluginRefs();
    
                /*System.out.println("Got the following Plugin References:");
                 
                for(int i=0;i<refs.size();i++){
                        System.out.println(i+". Name: "+refs.get(i).getName());
                        System.out.println("   App: "+refs.get(i).getApplication());
                        System.out.println("   Class: "+refs.get(i).getClassPath());
                        System.out.println("   Desc: "+refs.get(i).getActionDescription());
                        System.out.println();
                }
                 
                Gesture g = new Gesture ();
                for(int i=0;i<refs.size();i++){
                        pLayer.executePlugin(refs.get(i).getName(), g);
                }*/
    
    
}
