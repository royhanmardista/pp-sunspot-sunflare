/*
 * GestureDatabase.java
 *
 * Created on April 25, 2008, 11:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.persistence;
import com.sun.squawk.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import sunflare.plugin.PluginRef;
import sunflare.server.*;
import java.util.Iterator;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import java.io.FileWriter;



/**
 *
 * @author Winnie
 */
public class GestureDatabase {
    Vector definedGestures = new Vector();
    BufferedWriter out;
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
//        // Winnie's Part
//        Vector key = g.getBasicGestures();       
//        for(int i=0; i<definedGestures.size(); i++){
//            Gesture definedGesture = (Gesture)definedGestures.elementAt(i);
//            Vector v = definedGesture.getBasicGestures();
//            if(key.size() == v.size()){
//                if(equal(v,key))
//                    return definedGesture.getPlugin();
//            }
//        }
//        return null;
//        // Winnie's Part
        
        Vector key = g.getBasicGestures();
        getAllDefinedGestures();
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
//       // Winnie's Part
//       if(!gestureExists(g)){
//           definedGestures.addElement(g);
//          }
//       return false;
//       // Winnie's Part

       if(!gestureExists(g)){
           createAndStoreGesture(g);
           return true;
       }
       return false;
    }

    public boolean gestureExists(Gesture g){
//        // Winnie's Part
//        for(int i = 0; i<definedGestures.size();i++){
//            Gesture k = (Gesture)(definedGestures.elementAt(i));
//            if(g.equals(k))
//                return true;
//        }
//        return false;        
//        // Winnie's Part
        
        List gestures = listGestures();
        if(gestures == null){
            System.out.println("GestureDataBase : Gesture List is Empty");
        }
        for ( Iterator i = gestures.iterator(); i.hasNext(); ) {
            PersistentGesture p = ( PersistentGesture ) i.next();
            if(equals(g,p)){
                return true;
            }
        }
        return false;
    }
    
    private boolean equals(Gesture g, PersistentGesture p){
        if(g == null){
            System.out.println("GestureDataBase : Gesture is null");
        }
        Vector basicGestures = g.getBasicGestures();
        int count = basicGestures.size();
        if(count == 0){
            if( /*g.getPlugin().getName().equalsIgnoreCase(p.getPluginRef()) && 
                g.getPlugin().getActionDescription().equalsIgnoreCase(p.getAction())*/ true){
                return true;
            }
        }else if(count == 1){
            if( /*g.getPlugin().getName().equalsIgnoreCase(p.getPluginRef()) && 
                g.getPlugin().getActionDescription().equalsIgnoreCase(p.getAction()) && */
                (((BasicGesture)basicGestures.elementAt(0)).getID() == p.getMovment1() )   ){
                return true;
            }        
        }else if(count == 2){
            if( /*g.getPlugin().getName().equalsIgnoreCase(p.getPluginRef()) && 
                g.getPlugin().getActionDescription().equalsIgnoreCase(p.getAction()) && */
                (((BasicGesture)basicGestures.elementAt(0)).getID() == p.getMovment1()) &&
                (((BasicGesture)basicGestures.elementAt(1)).getID() == p.getMovment2()) ){
                return true;
            }        
        }else if(count == 3){
            if( /*g.getPlugin().getName().equalsIgnoreCase(p.getPluginRef()) && 
                g.getPlugin().getActionDescription().equalsIgnoreCase(p.getAction()) && */
                (((BasicGesture)basicGestures.elementAt(0)).getID() == p.getMovment1()) &&
                (((BasicGesture)basicGestures.elementAt(1)).getID() == p.getMovment2()) && 
                (((BasicGesture)basicGestures.elementAt(2)).getID() == p.getMovment3()) ){
                return true;
            }        
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
//        //Winnie's Part
//        return definedGestures;        
//        //Winnie's Part
        
        List gestures = listGestures();
        // definedGestures.removeAllElements();
        // Vector returnV = new Vector();
        definedGestures.clear();
        Gesture gesture = null;
        for ( Iterator i = gestures.iterator(); i.hasNext(); ) {
            gesture = new Gesture((PersistentGesture)i.next());
            definedGestures.addElement(gesture);
        }        
        return definedGestures;
        //return returnV;
    }
    
    public Vector getDefinedGesturesForPlugin(PluginRef p){
//        //Winnie's Part
//        String pluginName = p.getName();
//        Vector returnV = new Vector();
//        for(int i=0; i < definedGestures.size(); i++){
//            if(pluginName.equals(((Gesture)definedGestures.elementAt(i)).getPlugin().getName()))
//                returnV.addElement(definedGestures.elementAt(i));
//        }
//        return returnV;        
//        //Winnie's Part
        
        
        String pluginName = p.getName();
        Vector returnV = new Vector();
        List gestures = listGestures();
        Gesture gesture = null;
        for ( Iterator i = gestures.iterator(); i.hasNext(); ) {
            if(pluginName.equals( ( (PersistentGesture)i.next() ).getPluginRef() )){
                gesture = new Gesture((PersistentGesture)i.next());
                returnV.addElement(gesture);
            }            
        }                
//        for(int i=0; i < definedGestures.size(); i++){
//            if(pluginName.equals(((Gesture)definedGestures.elementAt(i)).getPlugin().getName()))
//                returnV.addElement(definedGestures.elementAt(i));
//        }
        return returnV;
    }
    
    private List listGestures() 
    {
	Session session = PersistentGestureUtil.getSessionFactory().getCurrentSession();
	session.beginTransaction();
	List result = session.createQuery( "from PersistentGesture" ).list();
	session.getTransaction().commit();
	return result;
    }
    
    private void createAndStoreGesture(Gesture g )
    {
	Session session = PersistentGestureUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        PersistentGesture persistentGesture = new PersistentGesture(g);
        // Write to file 
       try {
            System.out.println("GestureDatabase : Writing to file");
            out = new BufferedWriter(new FileWriter("database.txt",true));
//            out.write("My here");
//            out.write("My here");
//            System.out.println(gesture.getAction());
//            System.out.println(gesture.getPluginRef());
//            System.out.println(gesture.getMovment1());
//            System.out.println(gesture.getMovment2());
//            System.out.println(gesture.getMovment3());
//            out.write(gesture.getAction());
            out.write(persistentGesture.getAction());
            out.write(" ");
            out.write(persistentGesture.getPluginRef());
            out.write(" ");
            out.write('0' + persistentGesture.getMovment1());
            out.write(" ");
            out.write('0' + persistentGesture.getMovment2());
            out.write(" ");
            out.write('0' + persistentGesture.getMovment3());
            out.write("\n");
            out.close();
        } catch (IOException e) {
            System.out.println("GestureDatabase : File IO Exception");
        }
        session.save( persistentGesture );        
        session.getTransaction().commit();
    }
}
