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

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.radio.*;
import com.sun.spot.peripheral.ChannelBusyException;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.SpotFatalException;
import com.sun.spot.util.IEEEAddress;

import java.io.*;
import javax.microedition.io.*;

import javax.swing.*;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple example class to locate a remote service (on a SPOT), to connect to it
 * and send it a variety of commands. In this case to set or calibrate the SPOT's
 * accelerometer and to return a stream of accelerometer telemetry information. 
 *
 * @author Ron Goldman<br>
 * Date: May 2, 2006 
 */
public class AccelerometerListener extends Thread implements PacketTypes {
    
    // Non Linearity as specified by data sheet when running at 6G.
    // 200mv/G
    private static final double SENSITIVITY_2G = 182; // was 195;
    private static final double SENSITIVITY_6G = 62;
    
    // Vdd/2 offset (500/2=250)
    private double zeroOffsets[][] = { { 465, 465, 465 }, { 465, 465, 465 } };
    private double gains[][]       = { { 186.2, 186.2, 186.2 }, { 62.07, 62.07, 62.07 } };
    private double restOffsets[][] = { { 465, 465, 465 + 186 }, { 465, 465, 465 + 62 } };  // w/SPOT sitting flat Z up
    
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

    private double absoluteSums [] = {0,0,0};
    private double prevX = 0,prevY = 0,prevZ= 0;
    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Vector dataset = new Vector();
    private boolean recordData = false;
    private static final int MSEC_OF_DATA = 10 * 60 * 1000;
    private static final int MSEC_PER_SAMPLE = 10;
    private double yDataX_KF [] = new double[MSEC_OF_DATA/MSEC_PER_SAMPLE+20];
    private double yDataX_KF_Cov [] = new double[MSEC_OF_DATA/MSEC_PER_SAMPLE+20];
    private double yDataY_KF [] = new double[MSEC_OF_DATA/MSEC_PER_SAMPLE+20];
    private double yDataY_KF_Cov [] = new double[MSEC_OF_DATA/MSEC_PER_SAMPLE+20];
    private double yDataZ_KF [] = new double[MSEC_OF_DATA/MSEC_PER_SAMPLE+20];
    private double yDataZ_KF_Cov [] = new double[MSEC_OF_DATA/MSEC_PER_SAMPLE+20];

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
        RadiogramConnection rcvConn = null;
        try {
            rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + BROADCAST_PORT);
            baseStationPresent = true;
        } catch (Exception ex) {
            baseStationPresent = false;
            System.out.println("Problem connecting to base station: " + ex);
        } finally {
            try {
                if (rcvConn != null) {
                    rcvConn.close();
                }
            } catch (IOException ex) { /* ignore */ }
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
        sendCmd(GET_ACCEL_INFO_REQ);
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
        setGOffsets();
        sendCmd(sendIt ? SEND_ACCEL_DATA_REQ : STOP_ACCEL_DATA_REQ);
    }

    /**
     * Send a request to the remote SPOT to report on radio signal strength.
     */
    public void doPing() {
        sendCmd(PING_REQ);
    }

    /**
     * Send a request to the remote SPOT to blink its LEDs.
     */
    public void doBlink() {
        sendCmd(BLINK_LEDS_REQ);
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
        Global.numGesturesDetected = 0;
        absoluteSums[0]=0;
        absoluteSums[1]=0;
        absoluteSums[2]=0;
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

    private void recognize(double g[]) {
      
        double totalGThreshold = 1.3;
        double x = g[0];
        double y = g[1];
        double z = g[2];
        double totalG = g[3];
        double timeStamp = g[4];
        double dx = g[5];
        double dy = g[6];
        double dz = g[7];
//                        DataStruct data = new DataStruct(x,y,z,totalG,timeStamp,dx,dy,dz);
//                dataset.addElement(data);
//                System.out.println("adding");
        if(totalG>totalGThreshold){
           //mark the start of a gesture
           if(absoluteSums[0]==0 && absoluteSums[1]==0 && absoluteSums[2]==0){ //going from inactivity to activity, start of a gesture
               System.out.println("GESTURE START");
               dx = x;
               recordData = true;      
           }
           absoluteSums[0] += Math.abs(x);
           absoluteSums[1] += Math.abs(y);
           absoluteSums[2] += Math.abs(z);


        } else{
            if(absoluteSums[0]!=0 && absoluteSums[1]!=0 && absoluteSums[2]!=0){ //going from activity to inactivity, end of a gesture
                recordData = false;
                DataStruct data = new DataStruct(x,y,z,totalG,timeStamp,dx,dy,dz);
               dataset.addElement(data);
               System.out.println(dataset);
                Global.numGesturesDetected ++;
                System.out.println("GESTURE END, Num gesture = " + Global.numGesturesDetected);
                Global.gestureSegmentsLock.writeLock().lock();
                try{
                   
                    Global.gestureSegments.addElement(new Vector(dataset));
                    Global.gestureSegmentsCondition.signal();
                }
                finally{
                    Global.gestureSegmentsLock.writeLock().unlock();
                }

                dataset.removeAllElements(); //clear dataset
            }
            absoluteSums[0] = 0;
            absoluteSums[1] = 0;
            absoluteSums[2] = 0;
        }
        if(recordData){
           DataStruct data = new DataStruct(x,y,z,totalG,timeStamp,dx,dy,dz);
           dataset.addElement(data);
        }
    }
    
    /**
     * Process telemetry data sent by remote SPOT. 
     * Pass the data gathered to the GraphView to be displayed.
     *
     * @param dg the packet containing the accelerometer data
     * @param twoG the scale that was used to collect the data (true = 2G, false = 6G)
     */
    private double[] receive (Datagram dg, boolean twoG) {
        boolean skipZeros = (index == 0);
        double returnVals[] = new double[8];
        int scale = twoG ? 0 : 1;
        try {
            String address = dg.getAddress();
            long timeStamp = dg.readLong();
            if (timeStampOffset <= 0) {
                timeStampOffset = timeStamp;
                timeStamp = 0;
            } else {
                timeStamp -= timeStampOffset;
            }
            int sampleSize = dg.readByte();         // Number of SensorData contained in the datagram
            for (int i = 0; i < sampleSize; i++) {
                int deltaT = dg.readShort();
                long sampleTime = timeStamp + (deltaT & 0x0ffffL);
               
                int xValue = dg.readShort();
                int yValue = dg.readShort();
                int zValue = dg.readShort();

                if (skipZeros &&                    // Ignore leading values until they become non-zero
                    ((Math.abs(xValue - (int)restOffsets[scale][0]) > 20) || 
                     (Math.abs(yValue - (int)restOffsets[scale][1]) > 20) || 
                     (Math.abs(zValue - (int)restOffsets[scale][2]) > 20))) {
                    skipZeros = false;
                    timeStampOffset += sampleTime;
                    timeStamp = -deltaT;
                    sampleTime = 0;
                }

                if (!skipZeros) {
                    double x  = (xValue - zeroOffsets[scale][0]) / gains[scale][0];        // Convert to G's
                    double y  = (yValue - zeroOffsets[scale][1]) / gains[scale][1];
                    double z  = (zValue - zeroOffsets[scale][2]) / gains[scale][2];
                    double z_gravity = (zValue - zeroOffsets[scale][2]) / gains[scale][2] + 1.0;

                   
                    /*
                    // Do the Kalman Filtering
                    double Q = 0.00001; // 10^-5
                    double R = 0;
                    double K = 0;
                    double cov_priori = 0;
                    double sample_priori = 0;
                    
                    // For X
                    R = 0.049450549;
                    if(index == 0) {
                        sample_priori = 0;
                        cov_priori = Q;
                    } else {
                        sample_priori = yDataX_KF[index-1];
                        cov_priori = yDataX_KF_Cov[index-1] + Q;
                    }
                    K = cov_priori / (cov_priori + R);
                    yDataX_KF[index] = sample_priori + (K * (x - sample_priori));
                    yDataX_KF_Cov[index] = (1-K) * cov_priori;
                    x = yDataX_KF[index];
                    
                    
                     // For Y
                    R = 0.049450549;
                    if(index == 0) {
                        sample_priori = 0;
                        cov_priori = Q;
                    } else {
                        sample_priori = yDataY_KF[index-1];
                        cov_priori = yDataY_KF_Cov[index-1] + Q;
                    }
                    K = cov_priori / (cov_priori + R);
                    yDataY_KF[index] = sample_priori + (K * (x - sample_priori));
                    yDataY_KF_Cov[index] = (1-K) * cov_priori;
                    y = yDataY_KF[index];
                    
                     // For Z
                    R = 0.049450549;
                    if(index == 0) {
                        sample_priori = 0;
                        cov_priori = Q;
                    } else {
                        sample_priori = yDataZ_KF[index-1];
                        cov_priori = yDataZ_KF_Cov[index-1] + Q;
                    }
                    K = cov_priori / (cov_priori + R);
                    yDataZ_KF[index] = sample_priori + (K * (x - sample_priori));
                    yDataZ_KF_Cov[index] = (1-K) * cov_priori;
                    z = yDataZ_KF[index];
                    */
                    double g = Math.sqrt(x*x + y*y + z*z);      // Square vector of the total Gs
                    double g_gravity = Math.sqrt(x*x + y*y + z_gravity*z_gravity);     
                    returnVals[0]=x;
                    returnVals[1]=y;
                    returnVals[2]=z_gravity;
                    returnVals[3]=g;
                    returnVals[4]=sampleTime;
                    returnVals[5]=(x-prevX)/deltaT;
                    returnVals[6]=(y-prevY)/deltaT;
                    returnVals[7]=(z-prevZ)/deltaT;
                    
                    
                    
                    prevX = x;
                    prevY = y;
                    prevZ = z;
                    if(g>1.3){
                        graphView.takeData(address, sampleTime, index, x, y, z, g, twoG);                     
                    }
                    else
                       graphView.takeData(address,sampleTime,index,0,0,0,0,twoG);
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVals;
    }

    private void setGOffsets() {
        if (graphView != null) {
            int scale = is2GScale() ? 0 : 1;
            graphView.setGOffset((zeroOffsets[scale][0] - restOffsets[scale][0]) / gains[scale][0],
                                 (zeroOffsets[scale][1] - restOffsets[scale][1]) / gains[scale][1],
                                 (zeroOffsets[scale][2] - restOffsets[scale][2]) / gains[scale][2]);
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
                        Datagram rdg = rcvConn.newDatagram(10);
                        rdg.reset();
                        rdg.setAddress(dg);
                        rdg.writeByte(DISPLAY_SERVER_AVAIL_REPLY);        // packet type
                        rdg.writeLong(macAddress);                        // requestor's ID
                        rcvConn.send(rdg);                                // broadcast it
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
                    xdg = (Radiogram)conn.newDatagram(10); // we never send more than 1 or 2 bytes
                    connected = true;
                    updateConnectionStatus(connected);
                    index = 0;
                    timeStampOffset = -1;
                    while (connected) {
                        try {
                            conn.receive(rdg);            // wait until we receive a reply
                        } catch (TimeoutException ex) {
                            continue;
                        }
                        byte packetType = rdg.readByte();
                        switch (packetType) {
                            case GET_ACCEL_INFO_REPLY:
                                scaleInUse = rdg.readByte();
                                System.out.println("Accelerometer scale is set to " + scaleInUse + "G");
                                updateConnectionStatus(connected);
                                System.out.println("Accelerometer zero offsets:");
                                for (int i = 0; i < 2; i++) {
                                    System.out.print((i == 0 ? "  2G: " : "  6G: "));
                                    for (int j = 0; j < 3; j++) {
                                        zeroOffsets[i][j] = (int)rdg.readDouble();
                                        System.out.print(zeroOffsets[i][j] + (j < 2 ? ", " : ""));
                                    }
                                    System.out.println();
                                }
                                break;
                            case GET_ACCEL_INFO2_REPLY:
                                System.out.println("Accelerometer gains:");
                                for (int i = 0; i < 2; i++) {
                                    System.out.print((i == 0 ? "  2G: " : "  6G: "));
                                    for (int j = 0; j < 3; j++) {
                                        gains[i][j] = (int)rdg.readDouble();
                                        System.out.print(gains[i][j] + (j < 2 ? ", " : ""));
                                    }
                                    System.out.println();
                                }
                                break;
                            case SET_ACCEL_SCALE_REPLY:
                                int newScale = rdg.readByte();
                                if (newScale > 0) {
                                    scaleInUse = newScale;
                                    System.out.println("Accelerometer scale now set to " + newScale + "G");
                                } else {
                                    System.out.println("Invalid Accelerometer scale requested!");
                                }
                                setGOffsets();
                                break;
                            case CALIBRATE_ACCEL_REPLY:
                                System.out.println("Accelerometer rest offsets:");
                                for (int i = 0; i < 2; i++) {
                                    System.out.print((i == 0 ? "  2G: " : "  6G: "));
                                    for (int j = 0; j < 3; j++) {
                                        restOffsets[i][j] = (int)rdg.readDouble();
                                        System.out.print(restOffsets[i][j] + (j < 2 ? ", " : ""));
                                    }
                                    System.out.println();
                                }
                                setGOffsets();
                                break;
                            case ACCEL_2G_DATA_REPLY:
                            case ACCEL_6G_DATA_REPLY:
                                double g[];
                                g = receive(rdg, packetType == ACCEL_2G_DATA_REPLY);
                                recognize(g);
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
