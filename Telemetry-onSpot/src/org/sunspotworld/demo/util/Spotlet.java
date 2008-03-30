/*
 * Copyright (c) 2007 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.sunspotworld.demo.util;

import com.sun.spot.util.IBootloaderListenerCallback;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.peripheral.ILed;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.SpotFatalException;
import com.sun.spot.peripheral.ota.*;
import com.sun.spot.util.BootloaderListener;

/**
 * Start of a framework for robust SPOT applications. Provides simple lifecycle
 * management of application code by handling any fatal errors the application
 * causes. The application main class should extend Spotlet and implement the
 * following methods: (only <b>run</b> is necessary)
 *
 * <ul>
 * <li> <b>initialize()</b> - do any initialization needed by the Spot application.
 * <li> <b>run()</b> - the Spot application code to run.
 * <li> <b>reinitialize()</b> - reinitialize the world before re-running the Spot application.
 *  Called after an error has terminated the applications run method.
 * <li> <b>quit()</b> - if the Spot application is running have it stop.
 * <li> <b>cleanup()</b> - cleanup any resources used by the Spot application.
 * </ul>
 * <p> Other methods specify how many times to retry the application after errors, 
 * whether to run a OTACommandServer for over-the-air deployment of new code, etc.
 *
 * @author Ron Goldman<br>
 * Date: April 18, 2006 
 */
abstract public class Spotlet extends MIDlet implements IOTACommandServerListener, IBootloaderListenerCallback {
    
    private BootloaderListener listener = null;
    private boolean runApp = true;

    private int numRuns = 0;
    private int numRetries = -1;
    private long timeBetweenRetries = 0;
    private Throwable lastError = null;
    private boolean blinkLEDs = true;
    private boolean demoboardPresent = false;
    private boolean listenForBootloader = true;
    private long totalRunTime = 0;
    private long lastRunTime = 0;
    private long runTimeStart = 0;

    
    /**
     * Creates a new instance of Spotlet
     */
    public Spotlet() {
        try {
            EDemoBoard.getInstance();       // check if a sensor board is present
            demoboardPresent = true;
        } catch (SpotFatalException e) {
            demoboardPresent = false;
        }
    }

    /**
     * Do any initialization needed by the Spot application.
     * Can be defined by a subclass of Spotlet.
     */
    public void initialize () { 
    }
    
    /**
     * The Spot application code to run as defined by a subclass of Spotlet.
     */
    abstract public void run ();
    
    /**
     * Reinitialize the world before re-running the Spot application.
     * Called after an error has terminated the applications run method.
     * Can be defined by a subclass of Spotlet.
     */
    public void reinitialize () { 
    }

    /**
     * If the Spot application is running have it stop.
     * Can be defined by a subclass of Spotlet.
     */
    public void quit () { 
    }

    /**
     * Cleanup any resources used by the Spot application.
     * Can be defined by a subclass of Spotlet.
     */
    public void cleanup () { 
    }
    
    
    /**
     * Return the number of times the application has been run.
     *
     * @return the number of times the application's run method has been called
     *
     */
    public int getNumberRuns () { 
        return numRuns; 
    }
    
    /**
     * Indicate if this is the first time the application has been run.
     *
     * @return true if this is the first time that the application's run method has been called
     */
    public boolean firstRun () { 
        return numRuns == 1; 
    }

    /**
     * Return the last Error or Exception thrown by the application.
     *
     * @return the last Error or Exception thrown by the application, null if none thrown.
     */
    public Throwable getLastError() { 
        return lastError; 
    }

    /**
     * Return the number of times to retry the application after an error. 
     * Default value is -1 which means retry forever.
     *
     * @return the number of times to retry the application, -1 = retry forever
     */
    public int getNumberRetries () { 
        return numRetries; 
    }
    
    /**
     * Set the number of times to retry the application after an error.
     *
     * @param num the number of times to retry the application, -1 = retry forever
     */
    public void setNumberRetries (int num) { 
        numRetries = num; 
    }

    /**
     * Return the time in milliseconds to wait before retrying the application after an error. 
     * Default value is 0.
     *
     * @return the time to wait before retrying the application
     */
    public long getTimeBetweenRetries () { 
        return timeBetweenRetries; 
    }
    
    /**
     * Set the time in milliseconds to wait before retrying the application after an error. 
     *
     * @param time the time in milliseconds to wait before retrying the application
     */
    public void setTimeBetweenRetries (long time) { 
        if (time < 0) { 
            time = 0;       // negative values are not allowed
        }
        timeBetweenRetries = time; 
    }

    /**
     * Set whether the LEDs should be blinked between retries of the application and when done.
     * They will blink: 
     * <UL>
     *   <li>green before each call to initialize() or reinitialize()
     *   <li>red after any errors are thrown by initialize(), reinitialize(), run() or cleanup()
     *   <li>blue if quitting because of an OTA download or bootloader command
     * </UL>
     * Default action is to blink the LEDs.
     *
     * @param blink if true blink the LEDs
     */
    public void setBlinkLEDs (boolean blink) { 
        blinkLEDs = blink; 
    }
    
    /**
     * Set whether or not to monitor for OTA download requests or bootloader commands over 
     * the USB connection. Default action is to monitor requests.
     *
     * @param monitor if true spawn threads to listen for download/bootloader requests
     */
    public void setListenForBootloader (boolean monitor) {
        listenForBootloader = monitor; 
    }
    
    /**
     * Return the elapsed time for the current call to the application's run method.
     * If the run method is not running, return the time it took the last time it was called.
     *
     * @return the time in milliseconds since the run method was called
     */
    public long getCurrentRunTime () { 
        return (runTimeStart == 0) ? lastRunTime : (System.currentTimeMillis() - runTimeStart); 
    }

    /**
     * Return the elapsed time for the previous call to the application's run method.
     * If the run method is not running, return the time it took the last time it was called.
     *
     * @return the time in milliseconds since the run method was called
     */
    public long getLastRunTime () {
        return lastRunTime; 
    }
    
    /**
     * Return the total time that this application has been running.
     *
     * @return the time in milliseconds since the Spot started running this application.
     */
    public long getTotalRunTime () { 
        return totalRunTime; 
    }

    /**
     * Pause for a specified time.
     *
     * @param time the number of milliseconds to pause
     */
    public static void pause (long time) {
        try {
            Thread.currentThread().sleep(time);
        } catch (InterruptedException ex) { /* ignore */ }
    }
    
    /**
     * Run the actual application
     */
    private void runApp () {
        runApp = true;
        numRuns = 0;
        totalRunTime = 0;
        lastRunTime = 0;
        runTimeStart = 0;
        blinkLEDs(LEDColor.GREEN);
        try {
            initialize();
        } catch (Throwable th) {
            lastError = th;
            blinkLEDs(LEDColor.RED);
            System.out.println("Error initializing application: " + th.toString());
            th.printStackTrace();
            runApp = false;
        }

        if (runApp && listenForBootloader) {
            listener = new BootloaderListener(this);
            listener.start();
            OTACommandServer spotMon;
            try {     // ok to start OTA command server up if it is not already running
                spotMon = Spot.getInstance().getOTACommandServer();
                spotMon.addListener(this);
            } catch (IOException ex) {
                // ignore
            }
        }

        while (runApp) {
            try {
                runTimeStart = System.currentTimeMillis();
                numRuns++;
                run();
            } catch (Throwable th) {
                lastError = th;
                blinkLEDs(LEDColor.RED);
                System.out.println("Error while running application: " + th.toString());
                th.printStackTrace();
            } finally {
                lastRunTime = System.currentTimeMillis() - runTimeStart;
                totalRunTime += lastRunTime;
                runTimeStart = 0;
            }
            
            if ((numRetries >= 0) && (numRuns > numRetries)) {
                break;
            }
            
            pause(timeBetweenRetries);      // wait specified time before retrying
            
            try {
                blinkLEDs(LEDColor.GREEN);
                reinitialize();
            } catch (Throwable th) {
                lastError = th;
                blinkLEDs(LEDColor.RED);
                System.out.println("Error reinitializing application: " + th.toString());
                runApp = false;
            }
        }
        
        listener.cancel();
        try {
            cleanup();
        } catch (Throwable th) {
            blinkLEDs(LEDColor.RED);
            System.out.println("Error cleaning up after application: " + th.toString());
        }
        notifyDestroyed();   // exit from MIDlet
    }

    /**
     * Auxiliary routine to quit application and cleanup before invoking bootloader
     */
    private void cleanupBeforeNewApp () {
        try {
            if (listener != null) {
                listener.cancel();
            }
            blinkLEDs(LEDColor.BLUE);
            quit();
            cleanup();
        } catch (Throwable th) {
            System.out.println("Exception quitting application and cleaning up: " + th.toString());
        }
    }
  
    /**
     * Callback from USB serial listener indicating that application should quit and 
     * control be passed to the system bootloader.
     */
    public void prepareToExit() {
        cleanupBeforeNewApp();
        // return control to bootloader
    }
    
    /**
     * Called by OTACommandServer before flashing a new application.
     */
    public void preFlash() {
        cleanupBeforeNewApp();
    }
    
    /**
     * Called by OTACommandServer after flashing a new application.
     */
    public void postFlash() {
    }
    
    /**
     * Blink all of the LEDs then leave them off
     *
     * @param color the color that they should blink
     */
    private void blinkLEDs(LEDColor color) {
        if (blinkLEDs) {
            if (demoboardPresent) {
                ITriColorLED leds[] = EDemoBoard.getInstance().getLEDs();
                for (int i = 0; i < leds.length; i++) {
                    leds[i].setOff();
                    leds[i].setColor(color);
                    leds[i].setOn();
                    leds[i].setOff();
                }
            }
            try {
                ILed ledGreen = Spot.getInstance().getGreenLed();
                ILed ledRed = Spot.getInstance().getRedLed();
                if (color == LEDColor.GREEN) {
                    ledGreen.setOn();
                    ledGreen.setOff();
                } else if (color == LEDColor.RED) {
                    ledRed.setOn();
                    ledRed.setOff();
                } else {
                    ledGreen.setOn();
                    ledRed.setOn();
                    ledGreen.setOff();
                    ledRed.setOff();
                }
            } catch (SpotFatalException ex) {
                // ignore if we are running in a child Isolate without access to processor board LEDs
            }
        }
    }
    
    /**
     * MIDlet call to start our application.
     */
    protected void startApp() throws MIDletStateChangeException {
        runApp();
    }

    /**
     * This will never be called by the Squawk VM.
     */
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     * 
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }

}
