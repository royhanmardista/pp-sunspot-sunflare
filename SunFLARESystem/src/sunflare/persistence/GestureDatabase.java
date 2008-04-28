/*
 * GestureDatabase.java
 *
 * Created on April 25, 2008, 11:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.persistence;
import java.util.*;
import sunflare.plugin.PluginRef;
import sunflare.server.*;

import java.util.List;
import java.util.Iterator;

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
    
    public PluginRef search(Gesture g){
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
    
    public boolean addGesture(Gesture g){
       if(!gestureExists(g)){
           definedGestures.addElement(g);
          }
       return false;
    }

    public boolean gestureExists(Gesture g){
//        List gestures = listGestures();
//        Vector basicGestures = g.getBasicGestures();
//        for(int index = 0 ; index < basicGestures.size() ; index++ ){
//            ((BasicGesture)basicGestures.elementAt(index)).getID();
//            for ( Iterator i = gestures.iterator(); i.hasNext(); ) 
//            {
//                PersistentGesture gesture = ( PersistentGesture ) i.next();
//               // gesture.
//            }
//        }
        
        for(int i = 0; i<definedGestures.size();i++){
            Gesture k = (Gesture)(definedGestures.elementAt(i));
            if(g.equals(k))
                return true;
        }
        return false;
    }
    
    private boolean equal(Vector v, Vector k){
        for(int i = 0; i<v.size(); i++){
            if(((BasicGesture)v.elementAt(i)).getID() != ((BasicGesture)k.elementAt(i)).getID())
                return false;
        }
        return true;
    }
    public Vector getAllDefinedGestures(){
        return definedGestures;
    }
    
    public Vector getDefinedGesturesForPlugin(PluginRef p){
        String pluginName = p.getName();
        Vector returnV = new Vector();
        for(int i=0; i < definedGestures.size(); i++){
            if(pluginName.equals(((Gesture)definedGestures.elementAt(i)).getPlugin().getName()))
                returnV.addElement(definedGestures.elementAt(i));
        }
        return returnV;
    }
    
//    private List listGestures() 
//    {
//	Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//	session.beginTransaction();
//	List result = session.createQuery( "from Gestures" ).list();
//	session.getTransaction().commit();
//	return result;
//    }
    
    private void createAndStoreGesture( double x, double y, double z, double totalG, double time, double dx, double dy, double dz, String ia )
    {
//	Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//	session.beginTransaction();
//        session.getTransaction().commit();
    }
}
