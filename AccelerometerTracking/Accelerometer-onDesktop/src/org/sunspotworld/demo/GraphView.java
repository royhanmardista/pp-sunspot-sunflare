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
 * GraphView.java
 *
 * Store telemetry data and display it.
 *
 * author: Ron Goldman  
 * date: May 2, 2006 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * A simple class to store 1 minute of telemetry data, display it on the screen, 
 * do some simple filtering of the data and read/write it to a file. 
 *
 * @author Ron Goldman
 */
public class GraphView extends JPanel {

    /** Specifies the width of the y-axis display */
    public static final int AXIS_WIDTH = 60;
    private static final int MSEC_PER_PIXEL = 5;
    private static final int MSEC_OF_DATA = 60000;
    private static final int PREFERRED_HEIGHT = 500;
    
    private static final String Z_GS = "Z  -  Gs";
    private static final String Y_GS = "Y  -  Gs";
    private static final String X_GS = "X  -  Gs";
    private static final String TOTAL_GS = "Total Gs";

    static private final Color G_COLOR = new Color(255, 140, 0); // Dark Orange
    static private final Color X_COLOR = new Color(0, 150, 0); // Medium Green
    static private final Color Y_COLOR = Color.BLUE;
    static private final Color Z_COLOR = Color.RED;

    private int orgX, orgY; // Pixel coord of the X and Y origin of the graph.
    private int yMinPixels; // Pixel coord of the minimal point on the Y axis
    private int orgG;       // as above but for the total Gs graph

    private int borderX, borderY; // Width/height of region outside graph in pixels.

    private double yMin, yMax; // min and max data values (not pixels, data).

    private int xMin, xMax, indexMax;

    private int scaleZoomY = 1;     // factor to expand the y-axis by: 1, 2, 4, or 8
    private int scaleZoomX = 2;     // factor/2 to expand the x-axis by: 1/2 1, 2, or 4
    private double scaleX;          // pixels / millisecond
    private double scaleY;          // pixels / G's'
    
    private String id = "";
    private boolean twoG = true;    // using 2G or 6G accelerometer range
    
    private long[] timeMS;          // reported latest time for each guy.

    private double[] yDataG;        // Raw data collected as it comes in
    private double[] yDataX;
    private double[] yDataY;
    private double[] yDataZ;
    private int[] yGraphValsG;      // y-Data as pixels
    private int[] yGraphValsX;
    private int[] yGraphValsY;
    private int[] yGraphValsZ;

    private int[] xVals;            // x's used when drawing polyline in graph

    private int currentXs;          // most recent position on graph.

    private JPanel axisPanel = null;
    private double maxG = 0;
    private JLabel maxGLabel = null;
    private boolean showX = true;
    private boolean showY = true;
    private boolean showZ = true;
    private boolean showG = true;
    private JViewport port = null;
    private Rectangle viewRect = null;
    private boolean smooth = false;
    private boolean boxcar = true;
    private int filterWidth = 10;
    private int halfWindowSize = filterWidth / 2;
    private int smoothIndex = -1;
    private boolean fileData = false;
    
    private double[] vel;
    private double[] dist;

    /** Creates a new instance of GraphPanel */
    public GraphView() {
        init();
    }

    /**
     * Specify the panel displaying the Y-axis.
     *
     * @param ax the panel to be used to display the y-axis
     */
    public void setAxisPanel (JPanel ax) {
        axisPanel = ax;
        resetDisplaySize();
    }

    /**
     * Returns whether the accelerometer data using the 2G or 6G scales.
     *
     * @return true if using the 2G scale, false if using the 6G scale
     */
    public boolean is2G () {
        return twoG;
    }

    /**
     * Returns the height of the viewport displaying the graphed data.
     *
     * @return the height of the enclosing viewport
     */
    private int getContainerHeight() {
        return (port != null) ? port.getHeight() : PREFERRED_HEIGHT;
    }
    
    /**
     * Update the display size of the graphed data. Called after changing the X zoom factor or
     * after changing the number of samples displayed.
     */
    private void setDisplaySize () {
        int oldWidth = getWidth();
        if (port != null) {
            viewRect = port.getViewRect();
        }
        int preferredWidth = scaleZoomX * MSEC_OF_DATA / (2 * MSEC_PER_PIXEL) + 120;
        if (fileData) {
            preferredWidth = scaleZoomX * getMaxWidth() / 2;
        }
        int preferredHeight = getContainerHeight();
        Dimension size = new Dimension(preferredWidth, preferredHeight);
        setPreferredSize(size);
        setSize(size);
        if (port != null) {
            int newWidth = getWidth();
            int curXpos = viewRect.x + viewRect.width / 2;
            port.setViewPosition(new Point(curXpos * newWidth / oldWidth - viewRect.width / 2, 0));
            viewRect = port.getViewRect();
        }
    }

    /**
     * Update the display size of the graphed data after the window is resized.
     */
    private void resetDisplaySize() {
        setDisplaySize();
        setGraphingAttributes();
        redrawData();
        repaint();
        if (axisPanel != null) {
            axisPanel.setSize(axisPanel.getWidth(), getContainerHeight());
            axisPanel.repaint();
        }
    }

    /**
     * Set up our data structures (= simple arrays) and define some constants
     */
    private void init () {
        setBackground(Color.WHITE);
        borderY = 20;
        borderX = AXIS_WIDTH;
        setDisplaySize();
        setGraphingAttributes();
        xVals = new int[xMax - xMin];
        timeMS = new long[xMax - xMin];
        yDataG = new double[xMax - xMin];
        yGraphValsG = new int[xMax - xMin];
        yDataX = new double[xMax - xMin];
        yGraphValsX = new int[xMax - xMin];
        yDataY = new double[xMax - xMin];
        yGraphValsY = new int[xMax - xMin];
        yDataZ = new double[xMax - xMin];
        yGraphValsZ = new int[xMax - xMin];

        vel = new double[xMax - xMin];
        dist = new double[xMax - xMin];

        // Fill in initial values
        for (int i = 0; i < xVals.length; i++) {
            xVals[i]  = (int) (orgX + i * scaleX);
        }
        
        indexMax = currentXs = 0;
        repaint();
    }

    /**
     * Set parameters for drawing graph appropriately.
     */
    private void setGraphingAttributes () {
        xMin = 0;                               // min sample array index used
        xMax = MSEC_OF_DATA / MSEC_PER_PIXEL;   // max sample array index used.
        scaleX = scaleZoomX / (double) (2 * MSEC_PER_PIXEL);   // pixel / msec
        orgX = 0;

        yMin = -6.0;
        yMax =  6.0;
        double yMaxP = yMax / scaleZoomY;
        double yMinP = yMin / scaleZoomY;

        yMinPixels = (getContainerHeight() - borderY);
        scaleY = scaleZoomY * (yMinPixels - borderY) / (yMax - yMin);
        orgY = (int)(yMinPixels + yMinP * scaleY);
        orgG = orgY; // to show G's at bottom set to yMinPixels;
    }

    /**
     * Return the width that the current telemetry data occupies.
     *
     * @return the width (in pixels) that the current telemetry data occupies
     */
    public int getMaxWidth () {
        return xVals[indexMax == 0 ? (xVals.length - 1) : indexMax] + borderX;
    }


    /* Routines to read & write telemetry data to a file */
    
    /**
     * Write the current acceleration telemetry data out to a file.
     *
     * @param file the file to write the data into
     * @return true if successful, false otherwise
     */
    public boolean writeData (File file) {
        boolean results = false;
        try {
            BufferedWriter logFile = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < indexMax; i++) {
                logFile.write(id + ";" + timeMS[i] + ";" + i + ";" +
                              yDataX[i] + ";" + yDataY[i] + ";" + yDataZ[i] + ";" + 
                              yDataG[i] + ";" + twoG + ";" + "\n");
            }
            results = true;
            logFile.close();
        } catch (IOException ex) {
            System.out.println("Error writing out file: " + ex);
        }
        return results;
    }
    
    /**
     * Read in acceleration telemetry data from a file.
     *
     * @param file the file to read the data from
     * @return true if successful, false otherwise
     */
    public boolean readTelemetryFile(File file) {
        boolean results = false;
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                try {
                    StringTokenizer stk = new StringTokenizer(str, ";");
                    String address = stk.nextToken();
                    long timeMS  = Long.parseLong(stk.nextToken());
                    int index  = Integer.parseInt(stk.nextToken());
                    double x  = Double.parseDouble(stk.nextToken());
                    double y  = Double.parseDouble(stk.nextToken());
                    double z  = Double.parseDouble(stk.nextToken());
                    double g = Double.parseDouble(stk.nextToken());
                    // if Java 1.5 can use Boolean.parseBoolean(stk.nextToken());
                    boolean twoG = ("true".compareToIgnoreCase(stk.nextToken()) == 0);
                    takeData(address, timeMS, index, x, y, z, g, twoG);
                } catch (NoSuchElementException nex) {
                    // just ignore malformed lines
                    System.err.println("Unparsable line in telemetry file: " + str);
                }
            }
            results = true;
            finishSmoothing();
            fileData = true;
            setDisplaySize();
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + file.getPath());
        } catch (IOException e) {
            System.out.println("Error reading data from file: " + file.getName() + " : " + e);
        }
        return results;
    }

    /**
     * Notification that live data will be sent or sending of data has ended.
     *
     * @param sending true if about to receive data, false if data stream is finished
     */
    public void liveData (boolean sending) {
        if (sending) {
            // nothing to do here
        } else {
            finishSmoothing();
        }
    }

    /**
     * Add new telemetry data from remote SPOT.
     *
     * @param id IEEE address of SPOT sending the data
     * @param tMS time in milliseconds when the data was recorded
     * @param index index of this reading
     * @param g the combined accelerations of all three dimensions (in gravities)
     * @param x the x-axis acceleration (in gravities)
     * @param y the y-axis acceleration (in gravities)
     * @param z the z-axis acceleration (in gravities)
     * @param twoG true if measured using the 2 G accelerometer scale
     */
    public void takeData (String id, long tMS, int index, double x, double y, double z, double g, boolean twoG) {
        if (tMS > MSEC_OF_DATA || index >= yGraphValsG.length) {
            return;  // ignore any more data than we can display
        }
        
        this.id = id;
        this.twoG = twoG;
        
        if (g > maxG){
            maxG = g;
            if (maxGLabel != null) {
                maxGLabel.setText(Double.toString(maxG));
            }
        }
        
        indexMax = currentXs = index;
        timeMS[index] = tMS;

        xVals[index] = (int) (orgX + tMS * scaleX);

        yDataG[index] = g;

        yDataX[index] = x;
        yDataY[index] = y;
        yDataZ[index] = z;
        
        yGraphValsG[index] = (int) ((double) orgG - (g * scaleY));
        yGraphValsX[index] = (int) ((double) orgY - (x * scaleY));
        yGraphValsY[index] = (int) ((double) orgY - (y * scaleY));
        yGraphValsZ[index] = (int) ((double) orgY - (z * scaleY));
        
        if (smooth && index > halfWindowSize) {
            smooth(index - halfWindowSize);
        }

        repaint();
        if (viewRect != null && xVals[index] >= (viewRect.x + viewRect.width)) {
            port.setViewPosition(new Point(viewRect.x + viewRect.width / 2, 0));
            viewRect = port.getViewRect();
        }
    }
    
    /**
     * Smooth the data using either a boxcar or triangle filter.
     *
     * @param i the index of the data sample to compute
     */
    private void smooth (int i){
        int i0 = (i > halfWindowSize) ? i - halfWindowSize : 0;
        int i1 = ((i + halfWindowSize) < indexMax) ? i + halfWindowSize : indexMax;
        int wt = boxcar ? 1 : ((i > halfWindowSize) ? 1 : (2 * (halfWindowSize - i + 1) - 1));
        double xs, ys, zs, gs;
        xs = ys = zs = gs = 0.0;
        for (int j = i0; j <= i1; j++) {
            xs += wt * yDataX[j];
            ys += wt * yDataY[j];
            zs += wt * yDataZ[j];
            gs += wt * yDataG[j];
            if (!boxcar) {
                if (j < i) {
                    wt += 2;
                } else {
                    wt -= 2;
                }
            }
        }
        double samples = boxcar ? filterWidth : (2 * halfWindowSize * (halfWindowSize + 2) + 1); // smooth + 1; // i1 - i0 + 1;
        yGraphValsG[i] = (int) ((double) orgY - ((gs / samples) * scaleY));
        yGraphValsX[i] = (int) ((double) orgY - ((xs / samples) * scaleY));
        yGraphValsY[i] = (int) ((double) orgY - ((ys / samples) * scaleY));
        yGraphValsZ[i] = (int) ((double) orgY - ((zs / samples) * scaleY));
    }
    
    /**
     * Smooth the final samples after all data has been received .
     */
    private void finishSmoothing() {
        if (smooth) {
            for (int i = (indexMax > halfWindowSize) ? (indexMax - halfWindowSize) : 0; i <= indexMax; i++) {
                smooth(i);
            }
        }
    }
    
    /**
     * Redraw the data after a change to the scale or filtering.
     */
    private void redrawData () {
        int maxGp = orgG;
        scaleY = scaleZoomY * (yMinPixels - borderY) / (yMax - yMin);
        scaleX = scaleZoomX / (double) (2 * MSEC_PER_PIXEL);
        orgG = orgY; // (int)(orgY - ((yMin / scaleZoomY) * scaleY));
        for (int i = 0; i < indexMax; i++) {
            xVals[i] = (int) (orgX + timeMS[i] * scaleX);
            if (smooth) {
                smooth(i);
            } else {
                yGraphValsG[i] = (int) ((double) orgG - (yDataG[i] * scaleY));
                yGraphValsX[i] = (int) ((double) orgY - (yDataX[i] * scaleY));
                yGraphValsY[i] = (int) ((double) orgY - (yDataY[i] * scaleY));
                yGraphValsZ[i] = (int) ((double) orgY - (yDataZ[i] * scaleY));
            }
            if (yGraphValsG[i] < maxGp){
                maxGp = yGraphValsG[i];
            }
        }
        maxG = (orgG - maxGp) / scaleY; 
        if (maxGLabel != null) {
            maxGLabel.setText(Double.toString(maxG));
        }
    }
    
    
    /**
     * Integrate the accelerometer readings to get velocity and position.
     *
     * This is a nice idea in theory but the accelerometer readings have some drift which
     * causes a large error to build up. In particular even with measurements that are taken
     * for an object that starts & finishes at rest, the final velocity values are nowhere
     * near zero.
     *
     * Trying to offset the acceleration readings to adjust the zero point was able to give
     * a surprisingly good total distance (+/- 2 inches over 187 feet!), but the adjustment
     * was different for each set of collected data. Also intermediate distances were off
     * by +/- 10 inches. So it might be possible to use this for analyzing data after a run,
     * but not for real-time use while collecting data.
     *
     * Code below assumes distance travelled is along the Y-axis.
     *
     * Note: this code is not currently called. It is just provided as an example.
     */
    private void calculatePositionsViaIntegration() {
        System.out.println("\n\nPosition -- filter window = " + (filterWidth+1));
        dist[0] = 0;
        vel[0] = 0;
        int si = 0;
        double sd = 0;
        for (int i = 1; i < indexMax; i++) {
            double t = (timeMS[i] - timeMS[i-1]) / 1000.0;
            vel[i] = vel[i-1] + 32.0 * t * (orgY - yGraphValsY[i]) / scaleY;
            dist[i] = dist[i-1] + t * (vel[i-1] + vel[i]) / 2;
            if ((i % 100 == 0) || (i == (indexMax - 1))) {
                System.out.println(timeMS[i] + " v = " + vel[i] + " d = " + dist[i]);
            }
        }
    }
    

    /* Routines to connect with GUI components */
    
    /**
     * Connect us with the view port that is displaying us. Needed so we can 
     * auto scroll as data is entered.
     *
     * @param viewport the JViewport to scroll to control what data is displayed
     */
    public void setViewport(JViewport viewport) {
        port = viewport;
        viewRect = port.getViewRect();
        final GraphView gv = this;
        port.addComponentListener(new ComponentAdapter() {
            public void componentResized (ComponentEvent e) {
                gv.resetDisplaySize();
            }
        });
    }

    /**
     * Label to use to display maximum G force recorded.
     *
     * @param lab the label to use to display the maximum G force encountered
     */
    public void setMaxGLabel (JLabel lab) {
        maxGLabel = lab;
        maxGLabel.setText(Double.toString(maxG));
    }

    
    /* Command routines called by the user via the GUI */
    
    /**
     * Flush any current data and clear the display.
     */
    public void clearGraph () {
        indexMax = currentXs = 0;
        smoothIndex = -1;
        repaint();
        port.setViewPosition(new Point(0,0));
        viewRect = port.getViewRect();
        fileData = false;
    }    

    /**
     * Enable/disable the display of the combined G forces.
     *
     * @param b true if the combined G forces should be displayed
     */
    public void setShowG (boolean b) {
        showG = b;
        repaint();
    }

    /**
     * Enable/disable the display of the x-axis G forces.
     *
     * @param b true if the x-axis G forces should be displayed
     */
    public void setShowX (boolean b) {
        showX = b;
        repaint();
    }

    /**
     * Enable/disable the display of the y-axis G forces.
     *
     * @param b true if the y-axis G forces should be displayed
     */
    public void setShowY (boolean b) {
        showY = b;
        repaint();
    }

    /**
     * Enable/disable the display of the z-axis G forces.
     *
     * @param b true if the z-axis G forces should be displayed
     */
    public void setShowZ (boolean b) {
        showZ = b;
        repaint();
    }

    /**
     * Enable/disable the smoothing of the data with a filter.
     *
     * @param b true if the data displayed should be smoothed.
     */
    public void setSmooth (boolean b) {
        smooth = b;
        redrawData();
        repaint();
    }

    /**
     * Select which filter to use when smoothing the data.
     *
     * @param b true for the boxcar filter, false for the triangle filter
     */
    public void setFiltertype (boolean b) {
        boxcar = b;
        if (smooth) {
            redrawData();
            repaint();
        }
    }
    
    /**
     * Specify the filter's window size.
     *
     * @param w the number of samples to use when filtering
     */
    public void setFilterWidth (int w) {
        filterWidth = w;
        if ((filterWidth % 2) == 1) {       // make sure filterWidth is even
            filterWidth++;
        }
        halfWindowSize = filterWidth / 2;
        if (smooth) {
            redrawData();
            repaint();
        }
    }
    
    /**
     * Set the scale factor for the x-axis.
     *
     * @param s the scale / 2, so s = 1, means 1/2 size, s = 4 means double size
     */
    public void setZoomX (int s) {
        scaleZoomX = s;
        setDisplaySize();
        redrawData();
        repaint();
    }

    /**
     * Set the scale factor for the y-axis.
     *
     * @param s the scale, so s = 1, means normal size, s = 2 means double size
     */
    public void setZoomY (int s) {
        scaleZoomY = s;
        redrawData();
        repaint();
    }

    /**
     * Cause the display to be repainted. Also repaints the y-axis panel.
     */
    public void repaint() {
        super.repaint();
        if (axisPanel != null) {
            axisPanel.repaint();
        }
    }

    /**
     * Paint the X-axis & the G forces recorded. The Y-axis is now drawn in a separate panel.
     *
     * @param g the graphics component to use to paint things
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        // paintYaxis(g);               // now drawn in special axis panel
        paintXaxis(g);
        paintLegend(g);
        drawData(g);
    }

    /**
     * Paint a legend showing what color is used to draw each acceleration component.
     * Only paint the legend for forces that are being displayed.
     *
     * @param g the graphics component to use to paint things
     */
    private void paintLegend(Graphics g) {
        int xpos = orgX + 40;
        int x0 = xpos + 20;
        int x1 = x0 + 85;
        int ypos = 20;
        int y0 = ypos - 5;
        int ydelta = 15;
        g.setColor(Color.GRAY);
        g.drawString("Time in seconds", xpos, ypos);
        ypos += ydelta + 1;
        y0 += ydelta + 1;
        if (showX) {        // Draw X acceleration in Gs
            g.setColor(X_COLOR);
            g.drawString("aX", xpos, ypos);
            g.drawLine(x0, y0, x1, y0);
            ypos += ydelta;
            y0 += ydelta;
        }
        if (showY) {        // Draw Y acceleration in Gs
            g.setColor(Y_COLOR);
            g.drawString("aY", xpos, ypos);
            g.drawLine(x0, y0, x1, y0);
            ypos += ydelta;
            y0 += ydelta;
        }
        if (showZ) {        // Draw Z acceleration in Gs
            g.setColor(Z_COLOR);
            g.drawString("aZ", xpos, ypos);
            g.drawLine(x0, y0, x1, y0);
            ypos += ydelta;
            y0 += ydelta;
        }
        if (showG) {    // Draw the total acceleration in Gs
            g.setColor(G_COLOR);
            g.drawString("|a|", xpos - 1, ypos);
            g.drawLine(x0, y0, x1, y0);
            ypos += ydelta;
            y0 += ydelta;
        }
    }

    /**
     * Draw the G forces recorded.
     *
     * @param g the graphics component to use
     */
    private void drawData(Graphics g) {
        if (showG) {    // Draw the total acceleration in Gs
            g.setColor(G_COLOR);
            g.drawPolyline(xVals, yGraphValsG, indexMax);
        }
        if (showX) {        // Draw X acceleration in Gs
            g.setColor(X_COLOR);
            g.drawPolyline(xVals, yGraphValsX, indexMax);
        }
        if (showY) {        // Draw Y acceleration in Gs
            g.setColor(Y_COLOR);
            g.drawPolyline(xVals, yGraphValsY, indexMax);
        }
        if (showZ) {        // Draw Z acceleration in Gs
            g.setColor(Z_COLOR);
            g.drawPolyline(xVals, yGraphValsZ, indexMax);
        }
    }

    /**
     * Draw the tick marks on the acceleration axis (= y-axis).
     * The y-axis is now drawn in a separate display panel.
     *
     * @param g the graphics component to use
     */
    public void paintYaxis (Graphics g) {
        int y;
        int orgX = AXIS_WIDTH - 1;
        double yMaxP = yMax / scaleZoomY;
        double yMinP = yMin / scaleZoomY;
        
        // Y axis (line)
        g.drawLine(orgX, yMinPixels, orgX, yMinPixels - (int) ((yMaxP - yMinP) * scaleY));

        // Paint big deltas
        double dt = (yMaxP - yMinP) / 6.0;
        for (double t = 0.0; t <= (yMaxP - yMinP); t = t + dt) {
            y = yMinPixels - (int) (t * scaleY);
            if (Math.abs(orgY - y) < 2) { y = orgY; }   // make sure no round off error for y = 0
            double val = yMinP + t;
            g.drawString(((y == orgY) ? "0 G" : (val + "")), orgX - 40 + (val < 0 ? -8 : 0), y);
            g.drawLine(orgX - 12, y, orgX, y);
        }
        
        // Paint smaller deltas
        dt = (yMaxP - yMinP) / 12.0;
        for (double t = 0; t <= (yMaxP - yMinP); t = t + dt) {
            y = yMinPixels - (int) (t * scaleY);
            if (Math.abs(orgY - y) < 2) { y = orgY; }   // make sure no round off error for y = 0
            g.drawLine(orgX - 4, y, orgX, y);
        }
    }

    /**
     * Draw the tick marks on the time axis (= x-axis)
     *
     * @param g the graphics component to use
     */
    private void paintXaxis (Graphics g) {
        double x;

        // X axis (line)
        g.drawLine(orgX, orgY, orgX + (xMax - xMin) * scaleZoomX / 2, orgY);
        
        // Paint big deltas
        double dx = (1000 * scaleX * 2 / scaleZoomX);
        for (x = dx; x <= xMax * scaleZoomX / 2; x += dx) {
            g.drawString("" + (x / (200 * scaleZoomX / 2)), (int)(orgX + x - 5), orgY + 25);
            g.drawLine(orgX + (int)x, orgY - 12, orgX + (int)x, orgY + 12);
        }
        
        // Paint smaller deltas
        dx = (100 * scaleX * 2 / scaleZoomX);
        for (x = dx; x <= xMax * scaleZoomX / 2; x += dx) {
            int extra = (((int)x % (dx * 5)) == 0) ? 3 : 0;
            g.drawLine(orgX + (int)x, orgY - 5 - extra, orgX + (int)x, orgY + 5 + extra);
        }
    }

}
