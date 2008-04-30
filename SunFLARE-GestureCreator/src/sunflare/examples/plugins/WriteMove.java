package sunflare.examples.plugins;

import sunflare.server.Gesture;
import sunflare.plugin.Plugin;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;


public class WriteMove implements Plugin{

	public void fireCallBack(Gesture g) {
		System.out.println("Write Move fired callback");
           try {
                Robot robot = new Robot();
                // Creates the delay of 5 sec so that you can open notepad before
                // Robot start writting
                robot.delay(5000);
                robot.keyPress(KeyEvent.VK_H);
                robot.keyPress(KeyEvent.VK_E);
                robot.keyPress(KeyEvent.VK_L);
                robot.keyPress(KeyEvent.VK_L);
                robot.keyPress(KeyEvent.VK_O);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_T);
                robot.keyPress(KeyEvent.VK_H);
                robot.keyPress(KeyEvent.VK_I);
                robot.keyPress(KeyEvent.VK_S);                
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_I);
                robot.keyPress(KeyEvent.VK_S);                                
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_T);
                robot.keyPress(KeyEvent.VK_H);
                robot.keyPress(KeyEvent.VK_E);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_S);
                robot.keyPress(KeyEvent.VK_U);
                robot.keyPress(KeyEvent.VK_N);
                robot.keyPress(KeyEvent.VK_S);
                robot.keyPress(KeyEvent.VK_P);
                robot.keyPress(KeyEvent.VK_O);
                robot.keyPress(KeyEvent.VK_T);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_D);
                robot.keyPress(KeyEvent.VK_E);
                robot.keyPress(KeyEvent.VK_M);
                robot.keyPress(KeyEvent.VK_O);                
            } catch (AWTException e) {
                e.printStackTrace();
            }                
	}
}
