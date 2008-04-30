package sunflare.examples.OrbitGame.src.orbit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import sunflare.plugin.GestureListener;
import sunflare.server.Gesture;

public class SunflareListener implements GestureListener {
	BinaryInput binaryInput;
    public void setBinaryInput(BinaryInput binaryInput) {
    	this.binaryInput = binaryInput;
    }
      
    public void actionPerformed(ActionEvent e) {
       if (binaryInput!=null){
    	   if (binaryInput.getButtonState()==1){//button was on
    		   binaryInput.buttonChanged(false);
    	   }
    	   else if (binaryInput.getButtonState()==0){//button was off
    		   binaryInput.buttonChanged(true);
    		   
    	   }
       }
    }
}
