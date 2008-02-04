/*
* Copyright (c) 2006 Sun Microsystems, Inc.
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
 **/       
package org.sunspotworld.demo;

/*
 * BootLoaderListener.java
 *
 * Simple class to listen to the serial input over the USB connection and
 * pass control to the bootloader.
 *
 * author: Ron Goldman  
 * date: April 18, 2006 
 */


import java.io.*;
import javax.microedition.io.*;

import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.IUSBPowerDaemon;

/**
 * Simple class to listen to the serial input over the USB connection and
 * pass control to the bootloader. Means you do not have to push the reset
 * button on the SPOT when downloading new code.
 *
 * @author Ron Goldman
 */
 public class BootLoaderListener extends Thread {   // Used to monitor the USB serial line
        
    private Spotlet parent;
    private InputStream in;
    private boolean runBootLoaderListener = true;

    /**
     * Check if the SPOT is currently connected over USB.
     *
     * @return true if connected, false if not
     */
    public static boolean checkUSB () {
        return (Spot.getInstance().getUsbPowerDaemon().getCurrentState() == IUSBPowerDaemon.STATE_ENUMERATED);
    }

    /**
     * Creates a new instance of BootLoaderListener.
     *
     * @param st reference to our parent = main application
     */
    public BootLoaderListener (Spotlet st) {
        parent = st;
    }

    /**
     * Cleanup after ourself and stop running.
     */
    public void cleanup() {
        try {
            in.close();
        } catch (IOException ex) {
            // ignore any exceptions
        }
        runBootLoaderListener = false;
    }

    /**
     * Loop reading characters sent over USB connection and dispatch to bootloader when requested.
     */
    public void run () {
        try {
            in = Connector.openInputStream("serial://");

            while (runBootLoaderListener) {
                char c = (char)in.read();
                if ('A' <= c && c <= 'P') {
                    parent.invokeBootLoader();
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception while listening to serial line: " + ex);
        }
    }
}        
