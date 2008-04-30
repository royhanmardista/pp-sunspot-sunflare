package sunflare.examples.OrbitGame.src.orbit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

class GestureAction extends AbstractAction {
	BinaryInput binaryInput;
    public GestureAction(BinaryInput binaryInput) {
        super();
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

