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

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;

import com.sun.spot.peripheral.radio.*;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.util.*;

import java.io.*;
import javax.microedition.io.*;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Sample application that sends a stream of accelerometer telemetry
 * information back from the SPOT to a host application. 
 * <p>
 * This class establishes a radio connection with the host application.
 * The AccelMonitor class takes care of all accelerometer-related commands,
 * and sends the host a telemetry stream of accelerometer readings.
 * <p>
 * To simplify our work we make use of a number of utility helper classes: 
 * <ul>
 * <li>{@link LocateService} to locate a remote service (on a host)
 * <li>{@link PacketReceiver} to receive commands from the host application and 
 *     dispatch them to whatever classes registered to handle the command
 * <li>{@link PacketTransmitter} handles sending reply packets back to the host
 * <li>{@link PeriodicTask} provides for running a task, such as taking samples, 
       at a regular interval using the timer/counter hardware
 * <li>{@link Spotlet} start of a framework for robust SPOT applications. Provides
 *     simple lifecycle management of application code by handling any fatal
 *     errors the application causes.
 * </ul>
 * The host commands and replies are defined in the {@link PacketTypes} class.
 *
 *<p>
 * The SPOT uses the LEDs to display its status as follows:
 *<p>
 * LED 0:
 *<ul>
 *<li> Red = running, but not connected to host
 *<li> Green = connected to host display server
 *</ul>
 * LED 1:
 *<ul>
 *<li> Yellow = looking for host display server
 *<li> Blue = calibrating accelerometer
 *<li> Red blink = responding to a ping request
 *<li> Green = sending accelerometer values using 2G scale
 *<li> Blue-green = sending accelerometer values using 6G scale
 *</ul>
 * 
 * @author Ron Goldman<br>
 * Date: May 8, 2006, revised: August 1, 2007
 *
 * @see LocateService
 * @see LocateServiceListener
 * @see PacketHandler
 * @see PacketReceiver
 * @see PacketTransmitter
 * @see PacketTypes
 * @see Spotlet
 */
public class TelemetryMain extends Spotlet 
        implements LocateServiceListener, PacketHandler, PacketTypes {
    
    private static final long SERVICE_CHECK_INTERVAL = 10000;   // = 10 seconds
    private static final int ACCELEROMETER_SAMPLE_PERIOD = 10;  // read accelerometer every 10 msecs
    private static final String VERSION = "1.1";
    
    private boolean connected = false;
    private long serviceAddress = 0;
    private String messages[] = new String[50];
    private int stringIndex = 0;
    
    private LocateService locator;
    private PacketReceiver rcvr;
    private PacketTransmitter xmit;
    private RadiogramConnection hostConn;
    
    private AccelMonitor accelMonitor = null;
    private ITriColorLED leds[] = EDemoBoard.getInstance ().getLEDs();
    
    
    /////////////////////////////////////////////////////
    //
    // Lifecycle management - called from Spotlet class
    //
    /////////////////////////////////////////////////////
    
    /**
     * Initialize any needed variables. Called by Spotlet.
     */
    public void initialize() { 
        leds[0].setRGB(50,0,0);     // Red = not active
        leds[0].setOn();

        // create a service locator using the correct port & connection commands
        locator = new LocateService(this, BROADCAST_PORT, SERVICE_CHECK_INTERVAL,
                                    LOCATE_DISPLAY_SERVER_REQ,
                                    DISPLAY_SERVER_AVAIL_REPLY,
                                    4);
        locator.setStatusLed(leds[1]);
        
        // create the monitor to handle accelerometer commands
        accelMonitor = new AccelMonitor(this, ACCELEROMETER_SAMPLE_PERIOD);

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
     * Main application run loop. Called by Spotlet.
     */
    public void run() {
        System.out.println("Spot acceleration telemetry recorder  (version " + VERSION + ")");
        leds[0].setRGB(50,0,0);     // Red = not active
        leds[0].setOn();

        locator.start();            // look for host app and call serviceLocated() when found
        while (true) {
            Utils.sleep(30000);     // real work is done elsewhere; allow deep sleep while trying to connect
        }
    }


    ////////////////////////////////////////////////////////////////////
    //
    // Service connection management - called from LocateService class
    //
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Callback from LocateService when the host display service has been contacted.
     * This routine will setup both PacketReceiver & PacketTransmitter services
     * to handle communications with the host service. Registers which commands
     * this class handles.
     *
     * @param serviceAddress the IEEE address of the host display service
     */
    public void serviceLocated(long serviceAddress) {
        this.serviceAddress = serviceAddress;
        try {
            if (hostConn != null) {
                hostConn.close();
            }
            hostConn = (RadiogramConnection)Connector.open("radiogram://" +
                            IEEEAddress.toDottedHex(serviceAddress) + ":" + CONNECTED_PORT);
            hostConn.setTimeout(-1);    // no timeout
        } catch (IOException ex) {
            System.out.println("Failed to open connection to host: " + ex);
            closeConnection();
            return;
        }
        connected = true;
        leds[0].setRGB(0, 30, 0);           // Green = connected

        xmit = new PacketTransmitter(hostConn);     // set up thread to transmit replies to host
        xmit.setServiceName("Telemetry Xmitter");
        xmit.start();
        
        rcvr = new PacketReceiver(hostConn);        // set up thread to receive & dispatch commands
        rcvr.setServiceName("Telemetry Command Server");
        
        rcvr.registerHandler(this, DISPLAY_SERVER_RESTART);     // specify commands this class handles
        rcvr.registerHandler(this, DISPLAY_SERVER_QUITTING);
        rcvr.registerHandler(this, PING_REQ);
        rcvr.registerHandler(this, BLINK_LEDS_REQ);
        rcvr.start();

        accelMonitor.setPacketConnection(xmit, rcvr);
        accelMonitor.getAccInfo();     // notify host about accelerometer settings
    }

    /**
     * Called to declare that the connection to the host is no longer present.
     */
    public void closeConnection() {
        leds[0].setRGB(50,0,0);     // Red = not active
        locator.start();
        connected = false;
        accelMonitor.stop();
        xmit.stop();
        rcvr.stop();
    }
    

    //////////////////////////////////////////////////////////
    //
    // Command processing - called from PacketReceiver class
    //
    //////////////////////////////////////////////////////////
    
    /**
     * Callback from PacketReceiver when a new command is received from the host.
     * Note that commands associated with the accelerometer are dispatched directly
     * to the AccelMonitor class's handlePacket() method.
     * 
     * @param type the command
     * @param pkt the radiogram with any other required information
     */
    public void handlePacket(byte type, Radiogram pkt) {
        try {
            switch (type) {
                case DISPLAY_SERVER_RESTART:
                case DISPLAY_SERVER_QUITTING:
                    closeConnection();              // we need to reconnect to host
                    break;
                case PING_REQ:
                    leds[1].setRGB(40, 0, 0);       // Red = ping
                    leds[1].setOn();
                    sendPingReply(pkt);
                    leds[1].setOff();
                    break;
                case BLINK_LEDS_REQ:
                    blinkLEDs();
                    if (connected) {
                        leds[0].setRGB(0, 30, 0);   // Green = connected
                    } else {
                        leds[0].setRGB(50, 0, 0);   // Red = not active
                    }
                    leds[0].setOn();
                    break;
            }
        } catch (IOException ex) {
            closeConnection();
        }
    }
    
    // handle Ping command
    private void sendPingReply(Radiogram pkt) throws IOException {
        int linkQuality = pkt.getLinkQuality();
        int corr = pkt.getCorr();
        int rssi = pkt.getRssi();
        int battery = Spot.getInstance().getPowerController().getVbatt();
        Radiogram rdg = xmit.newDataPacket(PING_REPLY);
        rdg.writeInt(linkQuality);          // report how well we can hear server
        rdg.writeInt(corr);
        rdg.writeInt(rssi);
        rdg.writeInt(battery);
        xmit.send(rdg);
        for (int im = 0; im < stringIndex; im++) {
            Utils.sleep(30);                // give host time to process packet
            rdg = xmit.newDataPacket(MESSAGE_REPLY);
            rdg.writeUTF(messages[im]);
            xmit.send(rdg);
            // System.out.println("Sent message: " + messages[im]);
        }
        stringIndex = 0;
        Utils.sleep(200);
    }
    
    // handle Blink LEDs command
    private void blinkLEDs() {
        for (int i = 0; i < 10; i++) {          // blink LEDs 10 times = 10 seconds
            for (int j = 0; j < 8; j++) {
                leds[j].setColor(LEDColor.MAGENTA);
                leds[j].setOn();
            }
            Utils.sleep(250);
            for (int j = 0; j < 8; j++) {
                leds[j].setOff();
            }
            Utils.sleep(750);
        }
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

}
