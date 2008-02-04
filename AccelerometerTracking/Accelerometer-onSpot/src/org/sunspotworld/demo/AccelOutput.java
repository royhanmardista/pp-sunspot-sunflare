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
 * AccelOutput.java
 *
 * Routines to control and read data from the SPOT's accelerometer and send them
 * via Radiogram packets to a display program running on a host computer.
 *
 * author: Ron Goldman  
 * date: May 8, 2006 
 */

import com.sun.spot.peripheral.ChannelBusyException;
import com.sun.spot.peripheral.NoAckException;
import java.io.IOException;
import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.io.IScalarInput;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;

/**
 * Routines to control and read data from the SPOT's accelerometer and send them
 * via Radiogram packets to a display program running on a host computer.
 * 
 * @author Ron Goldman
 */
public class AccelOutput implements Runnable {
    private IAccelerometer3D acc;
    private IScalarInput x, y, z;
    private int index = 0;
    private int[] xOffset, yOffset, zOffset;
    private byte[] packetHdr = { AccelMain.ACCEL_2G_DATA_REPLY, AccelMain.ACCEL_6G_DATA_REPLY };

    private AccelMain main;
    private int sampleSize = 7;
    private int sampleInterval = 5;     // in milliseconds
    private boolean running;
    
    private RadiogramConnection conn;
    private Datagram dg;

    /**
     * Create a new accelerometer controller.
     *
     * @param m reference to the main program getting commands from the host display
     */
    public AccelOutput(AccelMain m) {
        main = m;
        acc = EDemoBoard.getInstance().getAccelerometer();
        acc.setRange(0);        // start using 2G scale
        index = 0;
        x = acc.getXAxis();
        y = acc.getYAxis();
        z = acc.getZAxis();
        xOffset = new int [2];
        yOffset = new int [2];
        zOffset = new int [2];
        xOffset[0] = xOffset[1] = yOffset[0] = yOffset[1] = zOffset[0] = zOffset[1] = 455;
    }
    
    /**
     * A convenience function to sleep a bit.
     *
     * @param time the number of milliseconds to sleep
     */
    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }
    
    /**
     * Set the RadiogramConnection to use to talk to host.
     *
     * @param conn the RadiogramConnection to send packets
     *
     */
    public void setRadiogramConnection (RadiogramConnection conn) {
        this.conn = conn;
        try {
            sampleSize = (conn.getMaximumLength() - 1 - 8 - 1) / 7;
        } catch (IOException ex) { 
            sampleSize = 5; 
        }

        try {
            dg = conn.newDatagram(conn.getMaximumLength());
        } catch (IOException e) {
        }
    }


    /**
     * Is the accelerometer using the 2G scale?
     *
     * @return true if the accelerometer using the 2G scale
     */
    public boolean is2GScale () {
        return (index == 0);
    }

    /**
     * Send a packet to inform host of current accelerometer scale: 2G or 6G
     */
    public void getScale() {
        try {
            dg.reset();
            dg.writeByte(AccelMain.GET_ACCEL_SCALE_REPLY);      // packet type
            dg.writeByte((is2GScale() ? 2 : 6));
            conn.send(dg);
        } catch (IOException ex) {
            // ignore errors - display server can repeat request if need be
        }
    }
    
    /**
     * Set the accelerometer to use either the 2G or 6G scale.
     * Will send a packet to acknowledge the request. 
     * The reply will include the current scale (2 or 6) or
     * be 0 if an invalid scale was requested.
     *
     * @param b the scale to use = 2 or 6
     */
    public void setScale(byte b) {
        try {
            dg.reset();
            dg.writeByte(AccelMain.SET_ACCEL_SCALE_REPLY);      // packet type
            if (b == 2) {
                index = 0;
                acc.setRange(0);
                dg.writeByte(2);
            } else if (b == 6) {
                index = 1;
                acc.setRange(1);
                dg.writeByte(6);
            } else {
                dg.writeByte(0);
            }
            conn.send(dg);
        } catch (IOException ex) {
            // ignore errors - display server can repeat request if need be
        }
    }
    
    /**
     * Calibrate the accelerometer. For each scale read 50 (x, y, z) values and 
     * take the average value to be the zero point. Send a packet back to the host
     * with the 6 offset values.
     */
    public void calibrate () {
        int curScale = acc.getCurrentRange();
        try {
            dg.reset();
            dg.writeByte(AccelMain.CALIBRATE_ACCEL_REPLY);      // packet type
            for (int sc = 0; sc < 2; sc++) {
                acc.setRange(sc);
                pause(100);         // give it time to settle
                long aveX = 0;
                long aveY = 0;
                long aveZ = 0;
                for (int i = 0; i < 50; i++) {
                    try {
                        aveX += x.getValue();
                        aveY += y.getValue();
                        aveZ += z.getValue();
                    } catch (Exception e) { }
                    pause(20);
                }
                xOffset[sc] = (int) (aveX / 50);
                yOffset[sc] = (int) (aveY / 50);
                zOffset[sc] = (int) (aveZ / 50);
                dg.writeShort((short)xOffset[sc]);
                dg.writeShort((short)yOffset[sc]);
                dg.writeShort((short)zOffset[sc]);
                System.out.println((sc == 0 ? "2" : "6") + "G Scale: " + 
                                   xOffset[sc] + ", " + yOffset[sc] + ", " + zOffset[sc]);
            }
            conn.send(dg);
        } catch (IOException ex) {
            // ignore errors - display server can repeat request if need be
        } finally {
            acc.setRange(curScale);
        }

    }

    /**
     * Stop sending accelerometer data to host.
     */
    public void stop () {
        running = false;
    }
    
    /**
     * Return if currently sending accelerometer data to host.
     *
     * @return true if currently sending accelerometer data to host.
     */
    public boolean isRunning () {
        return running;
    }

    /**
     * Start sending accelerometer data to host.
     */
    public void run() {
        running = true;
        while (running) {
            sendSensorData();
        }
    }

    /**
     * Send one packet of accelerometer readings back to host.
     * Each packet contains several (x, y, z) readings.
     */
    private void sendSensorData() {
        boolean readyToSend = false;
        try {
            long startTime = System.currentTimeMillis();
            dg.reset();
            dg.writeByte(packetHdr[index]);
            dg.writeLong(startTime);
            dg.writeByte(sampleSize);
            for (int i = 0; i < sampleSize; i++) {
                dg.writeByte((int) (System.currentTimeMillis() - startTime));
                dg.writeShort(x.getValue());
                dg.writeShort(y.getValue());
                dg.writeShort(z.getValue());

                if (i < (sampleSize - 1)) {     // Don't pause after writing last set of samples.
                    pause(sampleInterval);      // Sending the packet will take some time.
                }
            }
            readyToSend = true;
            conn.send(dg);                      // normally takes 9-12 msec, but can take 70+ msec
        } catch (NoAckException ne) {
            main.queueMessage("No Ack: " + ne.toString());
            if (readyToSend) {
                try {
                    conn.send(dg);              // retry sending packet
                } catch (Exception e) {
                }
            }
        } catch (ChannelBusyException be) {
            main.queueMessage("Busy Channel: " + be.toString());
            if (readyToSend) {
                try {
                    conn.send(dg);              // retry sending packet
                } catch (Exception e) {
                }
            }
        } catch (IOException ie) {
            main.queueMessage("IO exception: " + ie.toString());
        } catch (Exception e) {
            main.queueMessage("Other exception: " + e.toString());
            running = false;
            System.err.println("SENDER problem " + e);
            System.err.println("We should write to flash now!");
        }
    }
}
