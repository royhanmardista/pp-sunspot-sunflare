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

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.LIS3L02AQAccelerometer;
import com.sun.spot.peripheral.ChannelBusyException;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.util.Utils;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.radio.IRadiogramProtocolManager;
import com.sun.spot.peripheral.radio.RadioPacket;

import java.io.IOException;
import javax.microedition.io.Datagram;

/**
 * Routines to control and read data from the SPOT's accelerometer and send them
 * via Radiogram packets to a display program running on a host computer.
 * 
 * @author Ron Goldman
 */
public class AccelOutput implements Runnable {
    
    private static final int PAYLOAD_SIZE = RadioPacket.MIN_PAYLOAD_LENGTH - IRadiogramProtocolManager.DATA_OFFSET;
    private static final int HEADER_SIZE = 1 + 8 + 1;        // packet type + timestamp + #samples
    private static final int DATA_RECORD_SIZE = PAYLOAD_SIZE - HEADER_SIZE;
    private static final int SAMPLE_SIZE = 1 + 3 * 2;   // 1 = delta t + 3 * 2 = xyz accels
    private static final int MAX_SAMPLES = DATA_RECORD_SIZE / SAMPLE_SIZE;

    
    private LIS3L02AQAccelerometer acc;
    private int index = 0;
    private int[][] offsets = new int[2][3];
    private byte[] packetHdr = { AccelMain.ACCEL_2G_DATA_REPLY, AccelMain.ACCEL_6G_DATA_REPLY };

    private AccelMain main;
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
        acc = (LIS3L02AQAccelerometer)EDemoBoard.getInstance().getAccelerometer();
        acc.setScale(LIS3L02AQAccelerometer.SCALE_2G);        // start using 2G scale
        index = 0;
        copyRestOffsets();
    }

    private void copyRestOffsets() {
        double offs[][] = acc.getRestOffsets();
        for (int sc = 0; sc < 2; sc++) {
            for (int i = 0; i < 3; i++) {
                offsets[sc][i] = (int)offs[sc][i];
            }
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
            dg = conn.newDatagram(PAYLOAD_SIZE);
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
                acc.setScale(LIS3L02AQAccelerometer.SCALE_2G);
                dg.writeByte(2);
            } else if (b == 6) {
                index = 1;
                acc.setScale(LIS3L02AQAccelerometer.SCALE_6G);
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
        try {
            acc.setRestOffsets();
            copyRestOffsets();
            dg.reset();
            dg.writeByte(AccelMain.CALIBRATE_ACCEL_REPLY);      // packet type
            for (int sc = 0; sc < 2; sc++) {
                dg.writeShort((short)offsets[sc][0]);
                dg.writeShort((short)offsets[sc][1]);
                dg.writeShort((short)offsets[sc][2]);
                System.out.println((sc == 0 ? "2" : "6") + "G Scale: " + 
                                   offsets[sc][0] + ", " + offsets[sc][1] + ", " + offsets[sc][2]);
            }
            conn.send(dg);
        } catch (IOException ex) {
            // ignore errors - display server can repeat request if need be
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
            dg.writeByte(MAX_SAMPLES);
            for (int i = 0; i < MAX_SAMPLES; i++) {
                dg.writeByte((int) (System.currentTimeMillis() - startTime));
                dg.writeShort(acc.getRawX());
                dg.writeShort(acc.getRawY());
                dg.writeShort(acc.getRawZ());

                if (i < (MAX_SAMPLES - 1)) {        // Don't sleep after writing last set of samples.
                    Utils.sleep(sampleInterval);    // Sending the packet will take some time.
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
