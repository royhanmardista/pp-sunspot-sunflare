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

import org.sunspotworld.demo.util.*;

import com.sun.spot.peripheral.radio.*;
import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.util.*;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.LIS3L02AQAccelerometer;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;

import java.io.IOException;

/**
 * Routines to control and read data from the SPOT's accelerometer and send them
 * via Radiogram packets to a display program running on a host computer.
 *<p>
 * The actual task scheduling is done by the PeriodicTask parent class.
 *<p>
 * Packets from the host application are received by the PacketReceiver class and
 * then dispatched to this class to respond to. The replies are sent back via the
 * PacketTransmitter class. The commands are defined in the PacketTypes class.
 * <p>
 * @author Ron Goldman<br>
 * Date: May 8, 2006, revised: August 1, 2007
 *
 * @see PeriodicTask
 * @see PacketHandler
 * @see PacketReceiver
 * @see PacketTransmitter
 * @see PacketTypes
 */
public class AccelMonitor extends PeriodicTask implements PacketHandler, PacketTypes {
    
    // Definitions for raw accelerometer packets
    private static final int ACC_HEADER_SIZE = 8 + 1;   // time (long) + num samples (byte)
    private static final int ACC_SAMPLE_SIZE = 4 * 2;   // delta t + acc_x + acc_y + acc_z (all shorts)
    private static final int ACC_SAMPLES_PER_PACKET = 
            (PacketTransmitter.SINGLE_PACKET_PAYLOAD_SIZE - ACC_HEADER_SIZE) / ACC_SAMPLE_SIZE;

    private LIS3L02AQAccelerometer acc;
    private ITriColorLED leds[];
    private int index = 0;
    private byte[] packetHdr = { ACCEL_2G_DATA_REPLY, ACCEL_6G_DATA_REPLY };

    private TelemetryMain main;
    private int sampleInterval;     // in milliseconds
    
    private PacketTransmitter xmit;
    private Radiogram currentPkt = null;
    private int currentSample = 0;
    private long startTime;

    /**
     * Create a new accelerometer controller.
     *
     * @param m reference to the main program getting commands from the host display
     * @param sampleInterval how often to sample the accelerometer, in milliseconds
     */
    public AccelMonitor(TelemetryMain m, int sampleInterval) {
        super(3, sampleInterval, Thread.MAX_PRIORITY);
        this.sampleInterval = sampleInterval;
        main = m;
        leds = EDemoBoard.getInstance().getLEDs();
        acc = (LIS3L02AQAccelerometer)EDemoBoard.getInstance().getAccelerometer();
        acc.setScale(LIS3L02AQAccelerometer.SCALE_2G);        // start using 2G scale
        index = 0;
    }
    
    
    /**
     * Get the PacketTransmitter & PacketReceiver to use to talk with the host.
     * Register the commands this class handles.
     *
     * @param xmit the PacketTransmitter to send packets
     * @param rcvr the PacketReceiver that will receive commands from the host and dispatch them to handlePacket()
     */
    public void setPacketConnection (PacketTransmitter xmit, PacketReceiver rcvr) {
        this.xmit = xmit;
        
        rcvr.registerHandler(this, GET_ACCEL_INFO_REQ);
        rcvr.registerHandler(this, SET_ACCEL_SCALE_REQ);
        rcvr.registerHandler(this, CALIBRATE_ACCEL_REQ);
        rcvr.registerHandler(this, SEND_ACCEL_DATA_REQ);
        rcvr.registerHandler(this, STOP_ACCEL_DATA_REQ);
    }

    /**
     * Callback from PacketReceiver when a new command is received from the host.
     * Note only the commands associated with the accelerometer are handled here.
     *
     * @param type the command
     * @param pkt the radiogram with any other required information
     */
    public void handlePacket(byte type, Radiogram pkt) {
        try {
            switch (type) {
                case GET_ACCEL_INFO_REQ:
                    getAccInfo();
                    break;
                case SET_ACCEL_SCALE_REQ:
                    setScale(pkt.readByte());
                    leds[1].setRGB(0,30, is2GScale() ? 0 : 30);
                    leds[1].setOn();        // green = 2G, blue-green = 6G
                    Utils.sleep(200);
                    leds[1].setOff();
                    break;
                case CALIBRATE_ACCEL_REQ:
                    if (!isRunning()) {
                        leds[1].setRGB(0,0,50);     // Blue = calibrating
                        leds[1].setOn();
                        calibrate();
                        leds[1].setOff();
                    }
                    break;
                case SEND_ACCEL_DATA_REQ:
                    start();
                    leds[1].setRGB(0, 30, is2GScale() ? 0 : 60);
                    leds[1].setOn();        // green = 2G, blue-green = 6G
                    break;
                case STOP_ACCEL_DATA_REQ:
                    stop();
                    leds[1].setOff();
                    break;
            }
        } catch (IOException ex) {
            main.closeConnection();
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
     * Send a packet to inform host of current accelerometer settings.
     * Tell if scale is 2G or 6G. Send zero offsets, gains & rest offsets.
     */
    public void getAccInfo() {
        try {
            Radiogram dg = xmit.newDataPacket(GET_ACCEL_INFO_REPLY);
            dg.writeByte((is2GScale() ? 2 : 6));
            double offsets[][] = acc.getZeroOffsets();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    dg.writeDouble(offsets[i][j]);
                }
            }
            xmit.send(dg);
            dg = xmit.newDataPacket(GET_ACCEL_INFO2_REPLY);
            offsets = acc.getGains();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    dg.writeDouble(offsets[i][j]);
                }
            }
            xmit.send(dg);
            dg = xmit.newDataPacket(CALIBRATE_ACCEL_REPLY);
            offsets = acc.getRestOffsets();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    dg.writeDouble(offsets[i][j]);
                }
            }
            xmit.send(dg);
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
            Radiogram dg = xmit.newDataPacket(SET_ACCEL_SCALE_REPLY);
            if (b == 2) {
                index = 0;
                acc.setScale(0);
                dg.writeByte(2);
            } else if (b == 6) {
                index = 1;
                acc.setScale(1);
                dg.writeByte(6);
            } else {
                dg.writeByte(0);
            }
            xmit.send(dg);
        } catch (IOException ex) {
            // ignore errors - display server can repeat request if need be
        }
    }
    
    /**
     * Have the accelerometer calculate the current rest offsets.
     * Send a packet back to the host with the 6 offset values.
     */
    public void calibrate () {
        try {
            Radiogram dg = xmit.newDataPacket(CALIBRATE_ACCEL_REPLY);
            acc.setRestOffsets();
            double offsets[][] = acc.getRestOffsets();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    dg.writeDouble(offsets[i][j]);
                }
            }
            xmit.send(dg);
        } catch (IOException ex) {
            // ignore errors - display server can repeat request if need be
        }
    }


    ///////////////////////////////////
    //
    // PeriodicTask overridden methods
    //
    ///////////////////////////////////
    
    /**
     * Routine called when task execution is about to start up.
     */
    public void starting() {
        currentPkt = null;
        System.out.println("Starting accelerometer sampler");
    }
    
    /**
     * Routine called when task execution is finished.
     */
    public void stopping() {
        try {
            if (currentPkt != null) {
                double offsets[][] = acc.getZeroOffsets();
                int deltaT = (int) (System.currentTimeMillis() - startTime);
                for (int i = currentSample; i < ACC_SAMPLES_PER_PACKET; i++) {
                    currentPkt.writeShort(deltaT + i);
                    for (int j = 0; j < 3; j++) {
                        currentPkt.writeShort((int)offsets[index][j]);   // fill out final packet
                    }
                }
                xmit.send(currentPkt);
                currentPkt = null;
            }
        } catch (IOException ie) {
            // ignore
        }
        System.out.println("Stopping accelerometer sampler");
    }


    /**
     * Called once per task period to pack up accelerometer readings.
     * When the packet is full it is queued for transmission.
     */
    public void doTask() {
        try {
            if (currentPkt == null) {
                startTime = System.currentTimeMillis();
                currentPkt = xmit.newDataPacket(packetHdr[index]);
                currentPkt.writeLong(startTime);
                currentPkt.writeByte(ACC_SAMPLES_PER_PACKET);
                currentSample = 0;
            }
            currentPkt.writeShort((int) (System.currentTimeMillis() - startTime));
            currentPkt.writeShort(acc.getRawX());
            currentPkt.writeShort(acc.getRawY());
            currentPkt.writeShort(acc.getRawZ());
            if (++currentSample >= ACC_SAMPLES_PER_PACKET) {
                xmit.send(currentPkt);
                currentPkt = null;
            }
        } catch (IOException ie) {
            main.queueMessage("IO exception: " + ie.toString());
        }
    }

    /** temporary fix until IService interface fixed */
    public void setName(String who){};
}
