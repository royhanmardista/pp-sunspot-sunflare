/*
 * Global.java
 *
 * Created on March 27, 2008, 11:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


package org.sunspotworld.demo;
import java.util.Vector;
import java.util.concurrent.locks.*;
/**
 *
 * @author Winnie
 */
public class Global {
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
    
    public static double currentTime = 0;
    public static ReentrantReadWriteLock currentTimeLock  = new ReentrantReadWriteLock();
    public static Condition currentTimeCondition = currentTimeLock.writeLock().newCondition();
}
