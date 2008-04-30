/*
 * MouseListener.java
 *
 * Created on April 28, 2008, 9:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.examples.plugins;

import sunflare.server.Gesture;
import sunflare.plugin.Plugin;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

/**
 *
 * @author Praveen
 */
public class LeftMove implements Plugin{
    
    /** Creates a new instance of MouseListener */
    public LeftMove() {
    
    }
    
    public void fireCallBack(Gesture g) {
            System.out.println("Left Move fired callback");
       try {
            Robot robot = new Robot();
            // Creates the delay of 5 sec so that you can open notepad before
            // Robot start writting
            // robot.delay(5000);
            
            for(int x = 500; x >= 200; x--){
                robot.delay(20);
                robot.mouseMove(x, 500);
            }     
            robot.keyPress(KeyEvent.VK_CONTROL);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }    
    
}
