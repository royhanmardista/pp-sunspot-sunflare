/*
 * GestureListener.java
 *
 * Created on April 29, 2008, 10:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.plugin;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import sunflare.server.Gesture;

/**
 * The interface for all listeners
 */
public interface GestureListener extends ActionListener {
    
    /** Creates a new instance of GestureListener */
    public void actionPerformed(ActionEvent e);
    
}
