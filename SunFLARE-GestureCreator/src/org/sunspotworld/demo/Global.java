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
    //public static TelemetryFrame mainWindow;
    public static GestureCreatorGUI mainWindow;
    public static ReentrantReadWriteLock rwData  = new ReentrantReadWriteLock();
    public static Condition rwDataCondition = rwData.writeLock().newCondition();
    
    public static ReentrantReadWriteLock gestureSegmentsLock  = new ReentrantReadWriteLock();
    public static Condition gestureSegmentsCondition = gestureSegmentsLock.writeLock().newCondition();
    
    public static Vector gestureSegments = new Vector();
    
    //
    public static int numBasicGesturesDetected = 0;
    
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
        definedGestures.put(11,"RR");
        definedGestures.put(12,"RL");
        definedGestures.put(13,"RU");
        definedGestures.put(14,"RD");
        definedGestures.put(15,"RF");
        definedGestures.put(16,"RB");
        definedGestures.put(21,"LR");
        definedGestures.put(22,"LL");
        definedGestures.put(23,"LU");
        definedGestures.put(24,"LD");
        definedGestures.put(25,"LF");
        definedGestures.put(26,"LB");
        definedGestures.put(31,"UR");
        definedGestures.put(32,"UL");
        definedGestures.put(33,"UU");
        definedGestures.put(34,"UD");
        definedGestures.put(35,"UF");
        definedGestures.put(36,"UB");
        definedGestures.put(41,"DR");
        definedGestures.put(42,"DL");
        definedGestures.put(43,"DU");
        definedGestures.put(44,"DD");
        definedGestures.put(45,"DF");
        definedGestures.put(46,"DB");
        definedGestures.put(51,"FR");
        definedGestures.put(52,"FL");
        definedGestures.put(53,"FU");
        definedGestures.put(54,"FD");
        definedGestures.put(55,"FF");
        definedGestures.put(56,"FB");
        definedGestures.put(61,"BR");
        definedGestures.put(62,"BL");
        definedGestures.put(63,"BU");
        definedGestures.put(64,"BD");
        definedGestures.put(65,"BF");
        definedGestures.put(66,"BB");
        
        // Shake
        definedGestures.put(77,"SHAKE");
        
        definedGestures.put(71,"SR");
        definedGestures.put(72,"SL");
        definedGestures.put(73,"SU");
        definedGestures.put(74,"SD");
        definedGestures.put(75,"SF");
        definedGestures.put(76,"SB");

        definedGestures.put(17,"RS");
        definedGestures.put(27,"LS");
        definedGestures.put(37,"US");
        definedGestures.put(47,"DS");
        definedGestures.put(57,"FS");
        definedGestures.put(67,"BS");        
        
        
    }
    
    
    
}
