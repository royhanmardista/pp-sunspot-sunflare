/*
 * MouseListener.java
 *
 * Created on April 28, 2008, 9:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.examples.plugins.mouseListener;

import sunflare.server.Gesture;
import sunflare.plugin.Plugin;

import java.awt.Robot;

/**
 *
 * @author Praveen
 */
public class UpMove implements Plugin{
    
    /** Creates a new instance of MouseListener */
    public UpMove() {
    
    }
    
    public void fireCallBack(Gesture g) {
            System.out.println("Plugin 1 fired callback");
//            try{
//            
//            }catch{
//            
//            }
//            Robot robot = new Robot();
//            robot.mouseMove(24, 34);
    }    
    
}
