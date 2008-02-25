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
 * AccelerometerListener.java
 *
 * Routines to locate and connect to a data gathering program running on a remote SPOT.
 * This code sends a variety of requests that the SPOT needs to handle and reply to.
 *
 * author: Ron Goldman  
 * date: May 2, 2006 
 */

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.IProprietaryRadio;
import com.sun.spot.peripheral.radio.IRadioPolicyManager;
import com.sun.spot.util.IEEEAddress;

import java.io.*;
import javax.microedition.io.*;

import javax.swing.*;

/**
 * Framework class to locate a remote service (on a SPOT), to connect to it
 * and send it a variety of commands. In this case to set or calibrate the SPOT's
 * accelerometer and to return a stream of accelerometer telemetry information. 
 */
public class AccelerometerListener extends Thread {
    private static final int CHANNEL_NUMBER = IProprietaryRadio.DEFAULT_CHANNEL;
    private static final short PAN_ID       = IRadioPolicyManager.DEFAULT_PAN_ID;
    private static final String BROADCAST_PORT = "42";
    private static final String CONNECTED_PORT = "43";
    private static final long SERVER_CHECK_INTERVAL = 10000;      // = 10 seconds

    // Command & reply codes for data packets
    
    /** Client command to locate a display server. */
    private static final byte LOCATE_DISPLAY_SERVER_REQ  = 1;    // sent to display host
    /** Host command to indicate it is restarting. */
    private static final byte DISPLAY_SERVER_RESTART     = 2;
    /** Host command to indicate it is quiting. */
    private static final byte DISPLAY_SERVER_QUITTING    = 3;
    /** Host command to request the current accelerometer scale being used. */
    private static final byte GET_ACCEL_SCALE_REQ        = 4;
    /** Host command to specify the accelerometer scale to be used. */
    private static final byte SET_ACCEL_SCALE_REQ        = 5;
    /** Host command to request the accelerometer be calibrated. */
    private static final byte CALIBRATE_ACCEL_REQ        = 6;
    /** Host command to request accelerometer data be sent. */
    private static final byte SEND_ACCEL_DATA_REQ        = 7;
    /** Host command to request accelerometer data stop being sent. */
    private static final byte STOP_ACCEL_DATA_REQ        = 8;
    /** Host command to ping the remote SPOT and get the radio signal strength. */
    private static final byte PING_REQ                   = 9;

    /** Host reply to indicate it is available. */
    private static final byte DISPLAY_SERVER_AVAIL_REPLY = 11;
    /** Client reply to indicate the current accelerometer scale being used. */
    private static final byte GET_ACCEL_SCALE_REPLY      = 14;
    /** Client reply to indicate the current accelerometer scale being used. */
    private static final byte SET_ACCEL_SCALE_REPLY      = 15;
    /** Client reply to indicate the current accelerometer zero offsets. */
    private static final byte CALIBRATE_ACCEL_REPLY      = 16;
    /** Client reply with current accelerometer readings taken using the 2G scale. */
    private static final byte ACCEL_2G_DATA_REPLY        = 27;
    /** Client reply with current accelerometer readings taken using the 6G scale. */
    private static final byte ACCEL_6G_DATA_REPLY        = 67;
    /** Client reply to a ping includes the radio signal strength & battery level. */
    private static final byte PING_REPLY                 = 19;
    /** Client reply with any error message for the host to display. */
    private static final byte MESSAGE_REPLY              = 29;
    
    // Non Linearity as specified by data sheet when running at 6G.
    // 200mv/G
    private static final double SENSITIVITY_2G = 182; // was 195;
    private static final double SENSITIVITY_6G = 62;
    
    // Vdd/2 offset (500/2=250)
    private static int zeroGoffset[][] = { { 450, 450, 450 } , { 450, 450, 450 } };
    
    private boolean baseStationPresent = false;
    private RadiogramConnection conn = null;
    private Radiogram xdg = null;
    private boolean running = true;
    private boolean looking = true;
    private boolean connected = false;
    private long spotAddress = 0;
    private long timeStampOffset = -1;
    private int index = 0;
    private int scaleInUse = 2;
    
    private GraphView graphView = null;
    private TelemetryFrame guiFrame = null;

    /**
     * Create a new AccelerometerListener to connect to the remote SPOT over the radio.
     */
    public AccelerometerListener () {
        init();
    }
    
    /** 
     * Convenience function to sleep for specified time.
     *
     * @param time number of milliseconds to sleep
     */
    private void pause (long time) {
        try {
            Thread.currentThread().sleep(time);
        } catch (InterruptedException ex) { /* ignore */ }
    }

    /**
     * Connect to base station & other initialization.
     */
    private void init () {
        try {
            // currently just use default channel + pan ID
            // Spot.getInstance().getRadioPolicyManager().setChannelNumber(CHANNEL_NUMBER);
            // Spot.getInstance().getRadioPolicyManager().setPanId(PAN_ID);
            baseStationPresent = true;
        } catch (Exception ex) {
            baseStationPresent = false;
            System.out.println("Problem connecting to base station: " + ex);
        }
    }
    
    /**
     * Report which scale is the accelerometer is using.
     *
     * @return true if using 2G scale, false for 6G scale
     */
    public boolean is2GScale () {
        return scaleInUse == 2;
    }

    /**
     * Specify the GUI window that shows whether connected to a remote SPOT.
     *
     * @param fr the TelemetryFrame GUI that will be used to display the connection status to the remote SPOT
     */
    public void setGUI (TelemetryFrame fr) {
        guiFrame = fr;
        updateConnectionStatus(connected);
    }

    /**
     * Update the GUI with the current connection status.
     */
    private void updateConnectionStatus (boolean isConnected) {
        if (guiFrame != null) {
            final String status;
            final boolean connected = isConnected;
            final TelemetryFrame fr = guiFrame;
            if (!baseStationPresent) {      // not running in separate thread, so safe to call directly
                guiFrame.setConnectionStatus(false, "No Base Station");
            } else {
                if (isConnected) {
                    IEEEAddress ieeeAddr = new IEEEAddress(spotAddress);
                    status = "Connected to " + ieeeAddr.asDottedHex();
                } else {
                    status = "Not Connected";
                }
                SwingUtilities.invokeLater( new Runnable() {
                                                public void run() {
                                                    fr.setConnectionStatus(connected, status);
                                                } } );
            }
        }
    }

    /**
     * Send a request to the remote SPOT to report on which accelerometer scale it is using.
     */
    public void doGetScale () {
        sendCmd(GET_ACCEL_SCALE_REQ);
    }

    
    /**
     * Send a request to the remote SPOT to set which accelerometer scale it will use.
     *
     * @param val the scale to use: 2 or 6
     */
    public void doSetScale (int val) {
        if (conn != null) {
            try {
                xdg.reset();
                xdg.writeByte(SET_ACCEL_SCALE_REQ);
                xdg.writeByte(val);
                conn.send(xdg);
            } catch (NoAckException nex) {
                connected = false;
                updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        }
    }                                               

    /**
     * Send a request to the remote SPOT to calibrate the accelerometer.
     */
    public void doCalibrate () {
        doGetScale();
        sendCmd(CALIBRATE_ACCEL_REQ);
    }

    /**
     * Send a request to the remote SPOT to start or stop sending accelerometer readings.
     *
     * @param sendIt true to start sending, false to stop
     * @param gView the GraphView display to pass the data to
     */
    public void doSendData (boolean sendIt, GraphView gView) {
        graphView = gView;
        sendCmd(sendIt ? SEND_ACCEL_DATA_REQ : STOP_ACCEL_DATA_REQ);
        graphView.liveData(sendIt);
    }

    /**
     * Send a request to the remote SPOT to report on radio signal strength.
     */
    public void doPing() {
        sendCmd(PING_REQ);
    }

    /**
     * Stop running. Also notify the remote SPOT that we are no longer listening to it.
     */
    public void doQuit () {
        sendCmd(DISPLAY_SERVER_QUITTING);
        running = false;
    }

    /**
     * Send a request to the remote SPOT to report on radio signal strength.
     */
    public void reconnect() {
        connected = false;
        updateConnectionStatus(connected);
        announceStarting();
    }

    /**
     * Send a simple command request to the remote SPOT.
     *
     * @param cmd the command requested
     */
    private void sendCmd (byte cmd) {
        if (conn != null) {
            try {
                xdg.reset();
                xdg.writeByte(cmd);
                conn.send(xdg);
            } catch (NoAckException nex) {
                connected = false;
                updateConnectionStatus(connected);
            } catch (IOException ex) {
                // ignore any other problems
            }
        }
    }

    /**
     * Routine to reset after old data has been cleared from the GUI display.
     */
    public void clear () {
        index = 0;
        timeStampOffset = -1;
    }

    /**
     * Main runtime loop to connect to a remote SPOT.
     * Do not call directly. Call start() instead.
     */
    public void run () {
        if (baseStationPresent) {
            System.out.println("Accelerometer Reader Thread Started ...");
            hostLoop();
        }
    }

    /**
     * Process telemetry data sent by remote SPOT. 
     * Pass the data gathered to the GraphView to be displayed.
     *
     * @param dg the packet containing the accelerometer data
     * @param twoG the scale that was used to collect the data (true = 2G, false = 6G)
     */
    private void receive (Datagram dg, boolean twoG) {
        boolean skipZeros = (index == 0);
        skipZeros = false;
        int scale = twoG ? 0 : 1;
        double gValue = twoG ? SENSITIVITY_2G : SENSITIVITY_6G;
        try {
            String address = dg.getAddress();
            long timeStamp = dg.readLong() - timeStampOffset;
            if (timeStampOffset <= 0) {
                timeStampOffset += timeStamp;
                timeStamp = 0;
            }
            int sampleSize = dg.readByte();         // Number of SensorData contained in the datagram
            for (int i = 0; i < sampleSize; i++) {
                long sampleTime = timeStamp + (long)dg.readByte();
                int xValue = dg.readShort() - zeroGoffset[scale][0];
                int yValue = dg.readShort() - zeroGoffset[scale][1];
                int zValue = dg.readShort() - zeroGoffset[scale][2];

/*                if (skipZeros &&                    // Ignore leading values until they become non-zero
                    ((Math.abs(xValue) > 20) || 
                     (Math.abs(yValue) > 20) || 
                     (Math.abs(zValue) > 20))) {
                    skipZeros = false;
                    timeStampOffset += sampleTime;
                    timeStamp -= sampleTime;
                    sampleTime = 0;
                }
*/
		double x  = xValue / gValue;        // Convert to G's
		double y  = yValue / gValue;
		double z  = zValue / gValue;

                double g = Math.sqrt(x*x + y*y + z*z);        // Square vector of the total Gs

                if (!skipZeros) {
                    graphView.takeData(address, sampleTime, index, x, -y, z, g, twoG);
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast that the host display server is (re)starting.
     */
    private void announceStarting () {
        DatagramConnection txConn = null;
        try {
            txConn = (DatagramConnection)Connector.open("radiogram://broadcast:" + CONNECTED_PORT);
            Datagram dg = txConn.newDatagram(txConn.getMaximumLength());
            dg.writeByte(DISPLAY_SERVER_RESTART);        // packet type
            txConn.send(dg);                             // broadcast it
        } catch (Exception ex) {
            System.out.println("Error sending display server startup message: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (txConn != null) { 
                    txConn.close();
                }
            } catch (IOException ex) { /* ignore */ }
        }
    }
    
    /**
     * Wait for a remote SPOT to request a connection.
     */
    private void waitForSpot () {
        RadiogramConnection rcvConn = null;
        DatagramConnection txConn = null;
        spotAddress = 0;
        try {
            rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + BROADCAST_PORT);
            rcvConn.setTimeout(10000);             // timeout in 10 seconds
            Datagram dg = rcvConn.newDatagram(rcvConn.getMaximumLength());            
            while (true) {
                try {
                    dg.reset();
                    rcvConn.receive(dg);            // wait until we receive a request
                    if (dg.readByte() == LOCATE_DISPLAY_SERVER_REQ) {       // type of packet
                        String addr = dg.getAddress();
                        IEEEAddress ieeeAddr = new IEEEAddress(addr);
                        long macAddress = ieeeAddr.asLong();
                        System.out.println("Received request from: " + ieeeAddr.asDottedHex());
                        // txConn = (DatagramConnection)Connector.open("radiogram://" + addr + ":" + BROADCAST_PORT);
                        // Datagram xdg = txConn.newDatagram(txConn.getMaximumLength());
                        Datagram xdg = rcvConn.newDatagram(rcvConn.getMaximumLength());                                dg.reset();
                        xdg.reset();
                        xdg.setAddress(dg);
                        xdg.writeByte(DISPLAY_SERVER_AVAIL_REPLY);        // packet type
                        xdg.writeLong(macAddress);                        // requestor's ID
                        rcvConn.send(xdg);                                // broadcast it
                        spotAddress = macAddress;
                        break;
                    }
                } catch (TimeoutException ex) {
                    announceStarting();
                }
            }
        } catch (Exception ex) {
            System.out.println("Error waiting for remote Spot: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (rcvConn != null) { 
                    rcvConn.close();
                }
                if (txConn != null) { 
                    txConn.close();
                }
            } catch (IOException ex) { /* ignore */ }
        }
    }

    /**
     * Main receive loop. Receive a packet sent by remote SPOT and handle it.
     */
    private void hostLoop() {
        running = true;
        announceStarting();  // announce we are starting up - in case a Spot thinks it's connected to us
        while (running) {
            waitForSpot();   // connect to a Spot with accelerometer telemetry to display
            if (spotAddress != 0) {
                try {
                    conn = (RadiogramConnection)Connector.open("radiogram://" + spotAddress + ":" + CONNECTED_PORT);
                    conn.setTimeout(1000);             // timeout every second
                    Radiogram rdg = (Radiogram)conn.newDatagram(conn.getMaximumLength());
                    xdg = (Radiogram)conn.newDatagram(conn.getMaximumLength());
                    connected = true;
                    updateConnectionStatus(connected);
                    index = 0;
                    timeStampOffset = -1;
                    while (connected) {
                        try {
                            rdg.reset();
                            conn.receive(rdg);            // wait until we receive a reply
                        } catch (TimeoutException ex) {
                            continue;
                        }
                        byte packetType = rdg.readByte();
                        switch (packetType) {
                            case GET_ACCEL_SCALE_REPLY:
                                scaleInUse = rdg.readByte();
                                System.out.println("Accelerometer scale is set to " + scaleInUse + "G");
                                updateConnectionStatus(connected);
                                break;
                            case SET_ACCEL_SCALE_REPLY:
                                int newScale = rdg.readByte();
                                if (newScale > 0) {
                                    scaleInUse = newScale;
                                    System.out.println("Accelerometer scale now set to " + newScale + "G");
                                } else {
                                    System.out.println("Invalid Accelerometer scale requested!");
                                }
                                break;
                            case CALIBRATE_ACCEL_REPLY:
                                System.out.println("Accelerometer calibrated:");
                                for (int i = 0; i < 2; i++) {
                                    System.out.print((i == 0 ? "  2G: " : "  6G: "));
                                    for (int j = 0; j < 3; j++) {
                                        zeroGoffset[i][j] = rdg.readShort();
                                        System.out.print(zeroGoffset[i][j] + (j < 2 ? ", " : ""));
                                    }
                                    System.out.println();
                                }
                                break;
                            case ACCEL_2G_DATA_REPLY:
                            case ACCEL_6G_DATA_REPLY:
                                receive(rdg, packetType == ACCEL_2G_DATA_REPLY);
                                break;
                            case PING_REPLY:
                                System.out.println("Ping reply:  (linkQuality : corr : rssi)");
                                System.out.println("   host->spot: " + rdg.readInt() + " : " + rdg.readInt() + " : " + rdg.readInt());
                                System.out.println("   spot->host: " + rdg.getLinkQuality() + " : " + rdg.getCorr() + " : " + rdg.getRssi());
                                System.out.println("   spot battery voltage: " + rdg.readInt() + " mv");
                                break;
                            case MESSAGE_REPLY:
                                String str = rdg.readUTF();
                                System.out.println("Message from sensor: " + str);
                                break;
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Error communicating with remote Spot: " + ex.toString());
                } finally {
                    try {
                        connected = false;
                        updateConnectionStatus(connected);
                        if (conn != null) { 
                            xdg.reset();
                            xdg.writeByte(DISPLAY_SERVER_QUITTING);        // packet type
                            conn.send(xdg);                                // broadcast it
                            conn.close();
                            conn = null;
                        }
                    } catch (IOException ex) { /* ignore */ }
                }
            }
        }
    }

}
