/*
 * Global.java
 *
 * Created on March 27, 2008, 11:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


package org.sunspotworld.demo;
import org.sunspotworld.demo.Gesture;
import java.util.Vector;
import java.util.HashMap;
import java.util.concurrent.locks.*;
/**
 *
 * @author Winnie
 */
public class Global {
    public static TelemetryFrame mainWindow;
    public static ReentrantReadWriteLock rwData  = new ReentrantReadWriteLock();
    public static Condition rwDataCondition = rwData.writeLock().newCondition();
    
    public static ReentrantReadWriteLock gestureSegmentsLock  = new ReentrantReadWriteLock();
    public static Condition gestureSegmentsCondition = gestureSegmentsLock.writeLock().newCondition();
    
    public static Vector gestureSegments = new Vector();
    public final static double GESTURE_START = -100;
    public final static double GESTURE_END = -999;
    
    public static int numGesturesDetected = 0;
    
    public static Vector basicGestures = new Vector();
    public static ReentrantReadWriteLock basicGesturesLock  = new ReentrantReadWriteLock();
    public static Condition basicGesturesCondition = basicGesturesLock.writeLock().newCondition();

    public static Gesture gesture = new Gesture();
    public static ReentrantReadWriteLock gestureLock  = new ReentrantReadWriteLock();
    public static Condition gestureCondition = gestureLock.writeLock().newCondition();    
    
    public static double currentTime = 0;
    public static ReentrantReadWriteLock currentTimeLock  = new ReentrantReadWriteLock();
    public static Condition currentTimeCondition = currentTimeLock.writeLock().newCondition();
    
    public static final int NUMBER_OF_MOVEMENTS_PER_GESTURE = 2;
    public static final int RIGHT       = 1;
    public static final int LEFT        = 2;
    public static final int UP          = 3;
    public static final int DOWN        = 4;
    public static final int FORWARD     = 5;
    public static final int BACKWARD    = 6;
    public static final int SHAKE       = 7;
    
    public static HashMap definedGestures = new HashMap();
   
    public Global(){
        // Predefined gestures
        // RIGHT-LEFT 12
        definedGestures.put(12,"RL");
        // Left- Right 21
        definedGestures.put(21,"LR");
        // Up-down 34
        definedGestures.put(34,"UD");
        // Down-Up 43
        definedGestures.put(43,"DU");
        // Down-left 42
        definedGestures.put(42,"DL");
        // Left-Down 24
        definedGestures.put(24,"LD");
        // Down-Right 41
        definedGestures.put(41,"DL");
        // Right-Down 14
        definedGestures.put(14,"LD");
        // Up-Up
        definedGestures.put(33,"UU");
        // Up-Right
        definedGestures.put(31,"UR");
        // Up-Left
        definedGestures.put(32,"UL");
        // Shake
        definedGestures.put(77,"SHAKE");
    }
    
    
    
}
