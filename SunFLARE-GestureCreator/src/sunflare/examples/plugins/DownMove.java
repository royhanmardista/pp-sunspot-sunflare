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
public class DownMove implements Plugin{
    
    /** Creates a new instance of MouseListener */
    public DownMove() {
    
    }
    
    public void fireCallBack(Gesture g) {
            System.out.println("Down Move fired callback");
       try {
            Robot robot = new Robot();
            // Creates the delay of 5 sec so that you can open notepad before
            // Robot start writting
            // robot.delay(5000);
            for(int y = 500; y <= 800; y++ ){
                robot.delay(20);
                robot.mouseMove(500, y);
            }
            robot.keyPress(KeyEvent.VK_CONTROL);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }    
    
}
