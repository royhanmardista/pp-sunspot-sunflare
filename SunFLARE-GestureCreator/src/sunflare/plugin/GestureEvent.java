/*
 * GestureEvent.java
 *
 * Created on April 30, 2008, 12:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.plugin;

import java.awt.event.ActionEvent;
import sunflare.server.Gesture;

/**
 * Event that is sent to listeners when a gesture is performed
 */
public class GestureEvent extends ActionEvent {
    private Gesture gesture;
    
    /** 
     * Creates a new GestureEvent
     */
    public GestureEvent() {
        super(new Object(),0,"Gesture");
    }
    
    /**
     * Sets the gesture
     * @param g gesture to be set
     */
    public void setGesture(Gesture g) {
        gesture = g;
    }
    
    /**
     * Returns the gesture
     * @return the gesture associated with the event
     */
    public Gesture getGesture() {
        return gesture;
    }
}
