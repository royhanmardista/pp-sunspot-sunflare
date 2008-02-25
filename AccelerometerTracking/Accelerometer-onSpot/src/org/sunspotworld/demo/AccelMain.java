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
 * AccelMain.java
 *
 * Routines to locate and connect to a display program running on a host computer.
 * The host can send a variety of requests that the SPOT needs to handle and
 * reply to.
 *
 * author: Ron Goldman  
 * date: May 8, 2006 
 */

import com.sun.spot.peripheral.radio.IProprietaryRadio;
import com.sun.spot.peripheral.radio.IRadioPolicyManager;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.peripheral.Spot;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.ChannelBusyException;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;

import java.io.*;
import javax.microedition.io.*;
import java.util.Random;

/**
 * Framework class to locate a remote service (on a host) and to connect to it
 * and handle subsequent commands. In this case to set or calibrate the SPOT's
 * accelerometer and to return a stream of accelerometer telemetry information. 
 *<p>
 * The SPOT uses the LEDs to display its status as follows:
 *<p>
 * LED 1:
 *<ul>
 *<li> Red = running, but not connected to host
 *<li> Green = connected to host display server
 *</ul>
 * LED 2:
 *<ul>
 *<li> Yellow = looking for host display server
 *<li> Blue = calibrating accelerometer
 *<li> Red blink = responding to a ping request
 *<li> Green = sending accelerometer values using 2G scale
 *<li> Blue-green = sending accelerometer values using 6G scale
 *</ul>
 *
 * @author Ron Goldman
 */
public class AccelMain extends Spotlet {
    private static final String VERSION = "1.0";
    private static final int CHANNEL_NUMBER = IProprietaryRadio.DEFAULT_CHANNEL;
    private static final short PAN_ID       = IRadioPolicyManager.DEFAULT_PAN_ID;
    private static final String BROADCAST_PORT = "42";
    private static final String CONNECTED_PORT = "43";
    private static final long SERVER_CHECK_INTERVAL = 10000;    // = 10 seconds
          
    // Command & reply codes for data packets
    
    /** Client command to locate a display server. */
    public static final byte LOCATE_DISPLAY_SERVER_REQ  = 1;    // sent to display host (broadcast)
    /** Host command to indicate it is restarting. */
    public static final byte DISPLAY_SERVER_RESTART     = 2;    // sent to any clients (broadcast)
    /** Host command to indicate it is quiting. */
    public static final byte DISPLAY_SERVER_QUITTING    = 3;    // (direct p2p)
    /** Host command to request the current accelerometer scale being used. */
    public static final byte GET_ACCEL_SCALE_REQ        = 4;
    /** Host command to specify the accelerometer scale to be used. */
    public static final byte SET_ACCEL_SCALE_REQ        = 5;
    /** Host command to request the accelerometer be calibrated. */
    public static final byte CALIBRATE_ACCEL_REQ        = 6;
    /** Host command to request accelerometer data be sent. */
    public static final byte SEND_ACCEL_DATA_REQ        = 7;
    /** Host command to request accelerometer data stop being sent. */
    public static final byte STOP_ACCEL_DATA_REQ        = 8;
    /** Host command to ping the remote SPOT and get the radio signal strength. */
    public static final byte PING_REQ                   = 9;

    /** Host reply to indicate it is available. */
    public static final byte DISPLAY_SERVER_AVAIL_REPLY = 11;
    /** Client reply to indicate the current accelerometer scale being used. */
    public static final byte GET_ACCEL_SCALE_REPLY      = 14;
    /** Client reply to indicate the current accelerometer scale being used. */
    public static final byte SET_ACCEL_SCALE_REPLY      = 15;
    /** Client reply to indicate the current accelerometer zero offsets. */
    public static final byte CALIBRATE_ACCEL_REPLY      = 16;
    /** Client reply with current accelerometer readings taken using the 2G scale. */
    public static final byte ACCEL_2G_DATA_REPLY        = 27;
    /** Client reply with current accelerometer readings taken using the 6G scale. */
    public static final byte ACCEL_6G_DATA_REPLY        = 67;
    /** Client reply to a ping includes the radio signal strength & battery level. */
    public static final byte PING_REPLY                 = 19;
    /** Client reply with any error message for the host to display. */
    public static final byte MESSAGE_REPLY              = 29;

    private boolean connected = false;
    private long serverAddress = 0;
    private long ourMacAddress;
    private Random random;
    private String messages[] = new String[50];
    private int stringIndex = 0;
    
    private AccelOutput accelOutput = null;
    private ITriColorLED [] leds;
    
    /**
     * Initialize any needed variables. Called by Spotlet.
     */
    public void initialize() {
        leds = EDemoBoard.getInstance().getLEDs ();
        leds[0].setRGB(50,0,0);     // Red = not active
        leds[0].setOn();
        // currently just use default channel + pan ID
        // Spot.getInstance().getRadioPolicyManager().setChannelNumber(CHANNEL_NUMBER);
        // Spot.getInstance().getRadioPolicyManager().setPanId(PAN_ID);

        accelOutput = new AccelOutput(this);

        ourMacAddress = new IEEEAddress(System.getProperty("IEEE_ADDRESS")).asLong();
        random = new Random(ourMacAddress);
        stringIndex = 0;
    }
    
    /**
     * Reinitialize any needed variables after an error. Called by Spotlet.
     */
    public void reinitialize() {
        leds[0].setRGB(50,0,0);     // Red = not active
        leds[0].setOn();
        stringIndex = 0;
        queueMessage("Reinitialized");
    }

    /**
     * Add a message to the queue to be sent to the host at a later time. 
     * Messages will be sent after the next Ping request arrives.
     *
     * @param msg the String to be sent
     */
    public void queueMessage (String msg) {
        if (stringIndex < messages.length) {
            messages[stringIndex++] = (msg.length() < Radiogram.MAX_LENGTH) ? msg : msg.substring(0, Radiogram.MAX_LENGTH - 1);
            // System.out.println("Queuing message: " + msg);
        } else {
            // System.out.println("No room in queue for message: " + msg);
        }
    }

    /**
     * Main application run loop. Called by Spotlet.
     */
    public void run() {
        System.out.println("Spot acceleration telemetry recorder  (version " + VERSION + ")");
        clientLoop();
    }
    
    /**
     * Try to locate a display server. Broadcast a service request packet and
     * listen for a reply from host. Timeout if no reply received.
     *
     * @param txConn the broadcast radiogram connection to use to send packets
     * @param xdg the packet to use for broadcasting the request
     * @param rcvConn the server radiogram connection to use to receive a reply
     * @param rdg the packet to use for receiving the reply
     *
     * @return true if a display server was located
     */
    private boolean locateDisplayServer (DatagramConnection txConn, Datagram xdg,
                                         RadiogramConnection rcvConn, Datagram rdg) {
        boolean result = false;
        serverAddress = 0;
        try {
            xdg.reset();
            xdg.writeByte(LOCATE_DISPLAY_SERVER_REQ);        // packet type
            int retry = 0;
            while (retry < 5) {
                try {
                    txConn.send(xdg);               // broadcast remote print request
                    break;
                } catch (ChannelBusyException ex) {
                    retry++;
                    Utils.sleep(random.nextInt(10) + 2);  // wait a random amount before retrying
                }
            }
            try {
                while (true) {                      // loop until we either get a good reply or timeout
                    rdg.reset();
                    rcvConn.receive(rdg);           // wait until we receive a request
                    if (rdg.readByte() == DISPLAY_SERVER_AVAIL_REPLY) { // type of packet
                        long replyAddress = rdg.readLong();
                        if (replyAddress == ourMacAddress) {
                            String addr = rdg.getAddress();
                            IEEEAddress ieeeAddr = new IEEEAddress(addr);
                            serverAddress = ieeeAddr.asLong();
                            result = true;
                        }
                    }
                }
            } catch (TimeoutException ex) { /* ignore - just return false */ }
        } catch (IOException ex)  { /* also ignore - just return false */ }

        return result;
    }

    /**
     * Routine to send a reply back to the host.
     *
     * @param txConn radiogram connection to use for sending
     * @param xdg packet to send
     *
     * @return true if packet was sent successfully.
     */
    private boolean sendPacket(RadiogramConnection txConn, Radiogram xdg) {
        boolean result = true;
        try {
            txConn.send(xdg);   // broadcast remote print request
        } catch (NoAckException nex) {
            result = false;
        } catch (IOException ex) {
            // ignore any other problems
        }
        return result;
    }
    
    /**
     * Internal loop to locate a remote display server, connect to it and handle received commands.
     */
    private void clientLoop () {
        DatagramConnection txConn = null;
        RadiogramConnection rcvConn = null;
        connected = false;
        while (true) {   // this outer loop is for retrying if the server restarts
            try {
                leds[0].setRGB(50,0,0);     // Red = not active
                leds[0].setOn();
                txConn = (DatagramConnection)Connector.open("radiogram://broadcast:" + BROADCAST_PORT);
                Datagram xdg = txConn.newDatagram(txConn.getMaximumLength()); 
                rcvConn = (RadiogramConnection)Connector.open("radiogram://:" + BROADCAST_PORT);
                rcvConn.setTimeout(300);             // timeout in 300 msec
                Radiogram rdg = (Radiogram)rcvConn.newDatagram(rcvConn.getMaximumLength());

                while (true) {   // loop to locate a remote print server
                    int tries = 0;
                    boolean found = false;
                    leds[1].setRGB(60,40,0);     // Yellow = looking for display server
                    leds[1].setOn();
                    do {
                        found = locateDisplayServer(txConn, xdg, rcvConn, rdg);
                        Utils.sleep(20);         // wait 20 msecs
                        ++tries;
                    } while (!found && tries < 5);
                    leds[1].setOff();
                    if (found) {
                        connected = true;
                        break;
                    } else {
                        Utils.sleep(SERVER_CHECK_INTERVAL);  // wait a while before looking again
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error trying to locate remote display server: " + ex.toString());
                ex.printStackTrace();
                connected = false;
            } finally {
                try {
                    leds[1].setOff();
                    txConn.close();
                    if (rcvConn != null) {
                        rcvConn.close();
                        rcvConn = null;
                    }
                } catch (IOException ex) { /* ignore */ }
            }
            
            if (connected) {
                handleCommands();
            }
        }
    }

    /**
     * Internal routine to get a command from host and pass request to AccelOutput
     * class to handle it and send a reply packet back to the host.
     */
    private void handleCommands() {
        RadiogramConnection rcvConn = null;
        try {
            leds[0].setRGB(0, 30, 0);       // Green = connected
            rcvConn = (RadiogramConnection)Connector.open("radiogram://" + serverAddress + ":" + CONNECTED_PORT);
            rcvConn.setTimeout(-1);             // no timeout
            Radiogram rdg = (Radiogram)rcvConn.newDatagram(rcvConn.getMaximumLength());
            accelOutput.setRadiogramConnection(rcvConn);
            Utils.sleep(200);                   // give host a little time to setup radio connection
            accelOutput.getScale();             // notify host which scale is currently in use

            while (connected) {
                rdg.reset();
                rcvConn.receive(rdg);           // listen for a command from display server
                byte packetType = rdg.readByte();
                switch (packetType) {
                    case DISPLAY_SERVER_RESTART:
                    case DISPLAY_SERVER_QUITTING:
                        connected = false;      // we need to reconnect to host
                        accelOutput.stop();
                        break;
                    case GET_ACCEL_SCALE_REQ:
                        accelOutput.getScale();
                        break;
                    case SET_ACCEL_SCALE_REQ:
                        accelOutput.setScale(rdg.readByte());
                        leds[1].setRGB(0,30, accelOutput.is2GScale() ? 0 : 30);
                        leds[1].setOn();        // green = 2G, blue-green = 6G
                        Utils.sleep(200);
                        leds[1].setOff();
                        break;
                    case CALIBRATE_ACCEL_REQ:
                        if (!accelOutput.isRunning()) {
                            leds[1].setRGB(0,0,50);     // Blue = calibrating
                            leds[1].setOn();
                            accelOutput.calibrate();
                            leds[1].setOff();
                        }
                        break;
                    case SEND_ACCEL_DATA_REQ:
                        new Thread(accelOutput).start();
                        leds[1].setRGB(0,30, accelOutput.is2GScale() ? 0 : 30);
                        leds[1].setOn();        // green = 2G, blue-green = 6G
                        break;
                    case STOP_ACCEL_DATA_REQ:
                        accelOutput.stop();
                        leds[1].setOff();
                        break;
                    case PING_REQ:
                        int linkQuality = rdg.getLinkQuality();
                        int corr = rdg.getCorr();
                        int rssi = rdg.getRssi();
                        int battery = Spot.getInstance().getPowerController().getVbatt();
                        rdg.reset();
                        rdg.writeByte(PING_REPLY);      // packet type
                        rdg.writeInt(linkQuality);      // how well we can hear server
                        rdg.writeInt(corr);
                        rdg.writeInt(rssi);
                        rdg.writeInt(battery);
                        if (!sendPacket(rcvConn, rdg)) {
                            connected = false;
                        }
                        leds[1].setRGB(40,0, 0);         // Red = ping
                        leds[1].setOn();
                        for (int im = 0; im < stringIndex; im++) {
                            Utils.sleep(30);                      // give host time to process packet
                            rdg.reset();
                            rdg.writeByte(MESSAGE_REPLY);      // packet type
                            rdg.writeUTF(messages[im]);
                            sendPacket(rcvConn, rdg);
                            // System.out.println("Sent message: " + messages[im]);
                        }
                        stringIndex = 0;
                        Utils.sleep(200);
                        leds[1].setOff();
                        break;
                }
            }
        } catch (IOException ex) {
            /* ignore */
            System.out.println("Error: " + ex);
        } finally {
            leds[1].setOff();
            leds[6].setOff();
            leds[7].setOff();
            if (rcvConn != null) {
                try {
                    rcvConn.close(); 
                    rcvConn = null;
                } catch (IOException ex) { /* ignore */ }
            }
        }
    }
    
}
