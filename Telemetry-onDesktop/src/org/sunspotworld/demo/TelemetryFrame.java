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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.text.*;

/**
 * GUI creating code to make a window to display accelerometer data gathered
 * from a remote SPOT. Provides the user interface to interact with the SPOT
 * and to control the telemetry data collected.
 *
 * @author Ron Goldman<br>
 * date: May 2, 2006<br>
 * revised: August 1, 2007
 */
public class TelemetryFrame extends JFrame implements Printable {

    private static String version = "1.1";
    private static String versionDate = "August 1, 2007";
    private static int numWindows = 0;
    private static AccelerometerListener listener = null;
    private static BasicGestureRecognizer recognizer = null;
    private static BasicGestureClassifier classifier = null;
    private static GestureClassifier gestureClassifier = null;
    private static final Font footerFont = new Font("Serif", Font.PLAIN, 9);
    private static final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy  HH:mm z");

    private static Vector windows = new Vector();
    private static ImageIcon aboutIcon = null;
    
    private GraphView graphView;
    private JPanel axisView;
    private boolean sendData = false;
    private File file = null;
    private boolean fixedData = false;
    private boolean clearedData = true;

    private PrinterJob printJob = PrinterJob.getPrinterJob();
    private PageFormat pageFormat = printJob.defaultPage();
    
    

    /**
     * Create an initial new window and display it.
     *
     * @param args the command line arguments (ignored)
     */
    public static void main(String args[]) {
        // Set system properties for Mac OS X before AWT & Swing get loaded - doesn't hurt if not on a MAC
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TelemetryFrame");

        TelemetryFrame.frameGraph(null, new GraphView());       // create an empty window to collect telemetry
    }

    /**
     * Connect this window with a graph display and a file.
     */
    private static TelemetryFrame frameGraph(File file, GraphView graphView) {
        TelemetryFrame telemetryFrame = new TelemetryFrame(file);
        telemetryFrame.setGraphView(graphView);
        telemetryFrame.setVisible(true);
        String str = telemetryFrame.getTitle();
        if (!checkTitle(str)) {
            int i = 1;
            while (true) {
                if (checkTitle(str + "-" + i)) {
                    telemetryFrame.setTitle(str + "-" + i);
                    break;
                } else {
                    i++;
                }
            }
        }
        windows.add(telemetryFrame);
        telemetryFrame.setupGestureVisualization();
        Global.mainWindow = telemetryFrame;
        numWindows++;
        return telemetryFrame;
    }

    /**
     * Check that new window has a unique name.
     *
     * @param str proposed new window name
     * @return true if current name is unique, false if it is the same as another window
     */
    private static boolean checkTitle(String str) {
        boolean results = true;
        for (Enumeration e = windows.elements() ; e.hasMoreElements() ;) {
            TelemetryFrame fr = (TelemetryFrame)e.nextElement();
            if (str.equals(fr.getTitle())) {
                results = false;
                break;
            }
        }
        return results;
    }

    /**
     * Creates a new TelemetryFrame window.
     */
    public TelemetryFrame() {
        init(null);
    }
    
    /**
     * Creates a new TelemetryFrame window with an associated file.
     *
     * @param file the file to read/write accelerometer data from/to
     */
    public TelemetryFrame(File file) {
        init(file);
    }
    
    public void setGesture(int first, int second)
    {
        firstMovement.setGesture(first);
        secondMovement.setGesture(second);
    }

    public void setupGestureVisualization()
    {
        
        mainContainer = new javax.swing.JPanel();
        firstMovementContainer = new javax.swing.JPanel();
        secondMovementContainer = new javax.swing.JPanel();

        mainContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        firstMovementContainer.setBackground(new java.awt.Color(255, 255, 255));
        firstMovementContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("First Movement"));
        javax.swing.GroupLayout firstMovementContainerLayout = new javax.swing.GroupLayout(firstMovementContainer);
        firstMovementContainer.setLayout(firstMovementContainerLayout);
        firstMovementContainerLayout.setHorizontalGroup(
            firstMovementContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 252, Short.MAX_VALUE)
        );
        firstMovementContainerLayout.setVerticalGroup(
            firstMovementContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 227, Short.MAX_VALUE)
        );

        secondMovementContainer.setBackground(new java.awt.Color(255, 255, 255));
        secondMovementContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("Second Movement"));
        javax.swing.GroupLayout secondMovementContainerLayout = new javax.swing.GroupLayout(secondMovementContainer);
        secondMovementContainer.setLayout(secondMovementContainerLayout);
        secondMovementContainerLayout.setHorizontalGroup(
            secondMovementContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 252, Short.MAX_VALUE)
        );
        secondMovementContainerLayout.setVerticalGroup(
            secondMovementContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 227, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainContainerLayout = new javax.swing.GroupLayout(mainContainer);
        mainContainer.setLayout(mainContainerLayout);
        mainContainerLayout.setHorizontalGroup(
            mainContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(firstMovementContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(secondMovementContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        mainContainerLayout.setVerticalGroup(
            mainContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(secondMovementContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(mainContainerLayout.createSequentialGroup()
                .addComponent(firstMovementContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addComponent(mainContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(90, Short.MAX_VALUE)
                .addComponent(mainContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        mainContainer.setSize(300,300);
        mainContainer.setLocation(10,200);
        
        pack();
        
        try{
        firstMovement = new GestureDrawingPanel();
        secondMovement = new GestureDrawingPanel();
        }
        catch (Exception ex)
        {
        
        }
        
        firstMovement.setSize(100,100);
        //firstMovement.setGesture(2);
        firstMovement.setBackground(new Color(255,255,255));
        firstMovementContainer.add(firstMovement);
        firstMovement.setLocation(firstMovementContainer.getWidth()/2-firstMovement.getWidth()/2,
                firstMovementContainer.getHeight()/2-firstMovement.getHeight()/2);
        
        secondMovement.setSize(100,100);
        //secondMovement.setGesture(5);
        secondMovement.setBackground(new Color(255,255,255));
        secondMovementContainer.add(secondMovement);
        secondMovement.setLocation(secondMovementContainer.getWidth()/2-secondMovement.getWidth()/2,
                secondMovementContainer.getHeight()/2-secondMovement.getHeight()/2);
        
    }
    /**
     * Initialize the new TelemetryFrame
     */
    private void init(File file) {
        if (listener == null) {
            listener = new AccelerometerListener();         // only need one
            listener.start();
            aboutIcon = new ImageIcon(getClass().getResource("/org/sunspotworld/demo/racecar.gif"));
        }
        if (recognizer == null){
            recognizer = new BasicGestureRecognizer();
            recognizer.start();
        }
        if(classifier == null){
            classifier = new BasicGestureClassifier();
            classifier.start();
        }
        if(gestureClassifier == null){
            gestureClassifier = new GestureClassifier();
            gestureClassifier.start();
        }
        new Global();
        initComponents();
        clearButton = new javax.swing.JButton();
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(clearButton);
        setupAcceleratorKeys();
        this.file = file;
        if (file != null) {
            this.setTitle(file.getName());
            fixedData = true;
            clearedData = false;
            clearButton.setEnabled(false);
            
            sendDataButton.setEnabled(false);
            
            calibrateButton.setEnabled(false);
        }
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
    }
    
    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        listener.clear();
        recognizer.clear();
        classifier.clear();
        clearedData = true;
    }
    
    /**
     * Make sure the correct command key is used.
     */
    private void setupAcceleratorKeys() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, mask));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
    }

    /**
     * Set the GraphView to display accelerometer values for this window.
     */
    private void setGraphView(GraphView gv) {
        graphView = gv;
        //graphViewScrollPane.setViewportView(gv);
        //gv.setViewport(graphViewScrollPane.getViewport());
        //gv.setMaxGLabel(maxGLabel);
        //Integer fieldWidth = (Integer)filterWidthField.getValue();
        //graphView.setFilterWidth(fieldWidth.intValue() - 1);
        final GraphView gview = gv;
        axisView = new JPanel(){
            public Dimension getPreferredSize() {
                return new Dimension(GraphView.AXIS_WIDTH, gview.getPreferredSize().height);
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                gview.paintYaxis(g);
            }
        };
        axisView.setBackground(Color.WHITE);
        //y_axisPanel.add(axisView);
        gv.setAxisPanel(axisView);
        if (fixedData) {
            if (graphView.is2G()) {
                //twoGRadioButton.setSelected(true);
            } else {
                //sixGRadioButton.setSelected(true);
            }
            //twoGRadioButton.setEnabled(false);
            //sixGRadioButton.setEnabled(false);
        }
    }

    /**
     * Display the current connection status to a remote SPOT. 
     * Called by the AccelerometerListener whenever the radio connection status changes.
     *
     * @param conn true if now connected to a remote SPOT
     * @param msg the String message to display, includes the 
     */
    public void setConnectionStatus(boolean conn, String msg) {
        connStatusLabel.setText(msg);
        //blinkButton.setEnabled(conn);
        //pingButton.setEnabled(conn);
        reconnButton.setEnabled(conn);
        if (!fixedData) {
            if (listener.is2GScale()) {
                //twoGRadioButton.setSelected(true);
            } else {
                //sixGRadioButton.setSelected(true);
            }
            //twoGRadioButton.setEnabled(conn);
            //sixGRadioButton.setEnabled(conn);
            sendDataButton.setEnabled(conn);
            clearButton.setEnabled(conn);
            calibrateButton.setEnabled(conn);
        }
    }
    
    /**
     * Select a (new) file to save the accelerometer data in.
     */
    private void doSaveAs() {
        JFileChooser chooser;
        if (file != null) {
            chooser = new JFileChooser(file.getParent());
        } else {
            chooser = new JFileChooser(System.getProperty("user.dir"));
        }
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file.exists()) {
                int n = JOptionPane.showConfirmDialog(this, "The file: " + file.getName() + 
                                                      " already exists. Do you wish to replace it?",
                                                      "File Already Exists",
                                                      JOptionPane.YES_NO_OPTION);
                if (n != JOptionPane.YES_OPTION) {
                    return;                             // cancel the Save As command
                }
            }
            setTitle(file.getName());
            doSave();
        }
    }
    
    /**
     * Save the current accelerometer data to the file associated with this window.
     */
    private void doSave() {
        if (graphView.writeData(file)) {
            saveMenuItem.setEnabled(false);
        }
    }
    
    /**
     * Routine to print out each page of the current graph with a footer.
     *
     * @param g the graphics context to use to print
     * @param pageFormat how big is each page
     * @param pageIndex the page to print
     */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        double xscale = 0.5;
        double yscale = 0.75;
        int mx = 40;
        int my = 30;
        double x0 = pageFormat.getImageableX() + mx;
        double y0 = pageFormat.getImageableY() + my;
        double axisW = GraphView.AXIS_WIDTH * xscale;
        double w = pageFormat.getImageableWidth() - axisW - 2 * mx;
        double h = pageFormat.getImageableHeight() - 2 * my;
        int pagesNeeded = (int) (xscale * graphView.getMaxWidth() / w);
        if (pageIndex > pagesNeeded) {
            return(NO_SUCH_PAGE);
        } else {
            Graphics2D g2d = (Graphics2D)g;
            // first print our footer
            int y = (int) (y0 + h + 18);
            g2d.setPaint(Color.black);
            g2d.setFont(footerFont);
            g2d.drawString(dateFormat.format(new Date()).toString(), (int) (x0 + 5), y);
            if (file != null) {
                String name = file.getName();
                g2d.drawString(name, (int) (x0 + w/2 - 2 * name.length() / 2), y);
            }
            g2d.drawString((pageIndex + 1) + "/" + (pagesNeeded + 1), (int) (x0 + w - 20), y);
            
            // now print the Y-axis
            axisView.setDoubleBuffered(false);
            g2d.translate(x0, y0);
            g2d.scale(xscale, yscale);
            axisView.paint(g2d);
            axisView.setDoubleBuffered(true);

            // now have graph view print the next page
            // note: while the values to translate & setClip work they seem wrong. Why 2 * axisW ???
            graphView.setDoubleBuffered(false);
            g2d.translate(2 * axisW + 1 - (w * pageIndex) / xscale, 0);
            g2d.setClip((int)((w * pageIndex) / xscale + 2), 0, (int)(w / xscale), (int)(h / yscale));
            graphView.paint(g2d);
            graphView.setDoubleBuffered(true);
                    
            return(PAGE_EXISTS);
        }
    }

    /**
     * Routine to bring the user selected window to the front.
     *
     * @param evt the menu command with the name of the selected window
     */
    private void windowSelected(ActionEvent evt) {
        boolean found = false;
        String str = evt.getActionCommand();
        for (Enumeration e = windows.elements() ; e.hasMoreElements() ;) {
            TelemetryFrame fr = (TelemetryFrame)e.nextElement();
            if (str.equals(fr.getTitle())) {
                fr.setVisible(true);
                found = true;
                break;
            }
        }
    }


    /**
     * Cleanly exit.
     */
    private void doQuit() {
        listener.doQuit();
        recognizer.doQuit();
        classifier.doQuit();
        gestureClassifier.doQuit();
        System.exit(0);
    }
    
    // GUI code generated using NetBeans GUI editor:

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fullscaleGroup = new javax.swing.ButtonGroup();
        xZoomGroup = new javax.swing.ButtonGroup();
        yZoomGroup = new javax.swing.ButtonGroup();
        smoothGroup = new javax.swing.ButtonGroup();
        buttonPanel = new javax.swing.JPanel();
        calibrateButton = new javax.swing.JButton();
        sendDataButton = new javax.swing.JButton();
        reconnButton = new javax.swing.JButton();
        connStatusLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        closeMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        pagesetupMenuItem = new javax.swing.JMenuItem();
        printMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        quitMenuItem = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();

        getContentPane().setLayout(new java.awt.BorderLayout(0, 5));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sun SPOTs Telemetry Demo");
        setName("spotTelemetry");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowDeactivated(java.awt.event.WindowEvent evt) {
                formWindowDeactivated(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 5));

        buttonPanel.setAlignmentX(0.0F);
        buttonPanel.setAlignmentY(0.0F);
        buttonPanel.setMaximumSize(new java.awt.Dimension(566, 39));
        buttonPanel.setMinimumSize(new java.awt.Dimension(550, 30));
        buttonPanel.setPreferredSize(new java.awt.Dimension(550, 35));
        calibrateButton.setText("Calibrate");
        calibrateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrateButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(calibrateButton);

        sendDataButton.setText("Collect Data");
        sendDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendDataButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(sendDataButton);

        reconnButton.setText("Reconnect");
        reconnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reconnButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(reconnButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.CENTER);

        connStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        connStatusLabel.setText("Not connected");
        connStatusLabel.setFocusTraversalPolicyProvider(true);
        connStatusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        connStatusLabel.setMaximumSize(new java.awt.Dimension(320, 16));
        connStatusLabel.setMinimumSize(new java.awt.Dimension(145, 16));
        connStatusLabel.setPreferredSize(new java.awt.Dimension(245, 16));
        connStatusLabel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                connStatusLabelPropertyChange(evt);
            }
        });

        getContentPane().add(connStatusLabel, java.awt.BorderLayout.NORTH);

        fileMenu.setText("File");
        aboutMenuItem.setText("About...");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(aboutMenuItem);

        fileMenu.add(jSeparator5);

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newMenuItem);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        fileMenu.add(jSeparator3);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(closeMenuItem);

        fileMenu.add(jSeparator4);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);

        fileMenu.add(jSeparator1);

        pagesetupMenuItem.setText("Page Setup...");
        pagesetupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pagesetupMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(pagesetupMenuItem);

        printMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        printMenuItem.setText("Print...");
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(printMenuItem);

        fileMenu.add(jSeparator2);

        quitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        quitMenuItem.setText("Quit");
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(quitMenuItem);

        jMenuBar1.add(fileMenu);

        windowMenu.setText("Windows");
        windowMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                windowMenuMenuSelected(evt);
            }
        });

        jMenuBar1.add(windowMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this,
                "Sun SPOTs Telemetry Demo (Version " + version + ")\n\nA demo showing how to collect data from a SPOT and \nsend it to a desktop application to be displayed.\n\nAuthor: Ron Goldman, Sun Labs\nDate: " + versionDate,
                "About Telemetry Demo",
                JOptionPane.INFORMATION_MESSAGE,
                aboutIcon);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void pagesetupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pagesetupMenuItemActionPerformed
        // Ask user for page format (e.g., portrait/landscape)
        pageFormat = printJob.pageDialog(pageFormat);
    }//GEN-LAST:event_pagesetupMenuItemActionPerformed

    private void reconnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reconnButtonActionPerformed
        listener.reconnect();
    }//GEN-LAST:event_reconnButtonActionPerformed

    private void windowMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_windowMenuMenuSelected
        windowMenu.removeAll();
        for (Enumeration e = windows.elements() ; e.hasMoreElements() ;) {
            JMenuItem it = windowMenu.add(((TelemetryFrame)e.nextElement()).getTitle());
            it.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    windowSelected(evt);
                }
            });
        }
    }//GEN-LAST:event_windowMenuMenuSelected

    private void connStatusLabelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_connStatusLabelPropertyChange
        if (connStatusLabel.getText().startsWith("Connected")) {
            if (listener.is2GScale()) {
                //twoGRadioButton.setSelected(true);
            } else {
                //sixGRadioButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_connStatusLabelPropertyChange

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        listener.setGUI(this);
        if (clearedData) {
            listener.clear();
            recognizer.clear();
            classifier.clear();
        }
        if (listener.is2GScale()) {
            //twoGRadioButton.setSelected(true);
        } else {
            //sixGRadioButton.setSelected(true);
        }
    }//GEN-LAST:event_formWindowActivated

    private void formWindowDeactivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeactivated
        listener.setGUI(null);
    }//GEN-LAST:event_formWindowDeactivated

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        doQuit();
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        windows.remove(this);
        if (--numWindows <= 0) {
            doQuit();
        }
    }//GEN-LAST:event_formWindowClosed

    private void printMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printMenuItemActionPerformed
        printJob.setPrintable(this, pageFormat);
        if (printJob.printDialog()) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                printJob.print();
            } catch(PrinterException pe) {
                System.out.println("Error printing: " + pe);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }//GEN-LAST:event_printMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        doSaveAs();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if (file == null) {
            doSaveAs();
        } else {
            doSave();
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        JFileChooser chooser;
        if (file != null) {
            chooser = new JFileChooser(file.getParent());
        } else {
            chooser = new JFileChooser(System.getProperty("user.dir"));
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            GraphView gView = new GraphView();
            if (gView.readTelemetryFile(file)) {
                frameGraph(file, gView);
            }
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        frameGraph(null, new GraphView());
    }//GEN-LAST:event_newMenuItemActionPerformed

    private void calibrateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrateButtonActionPerformed
        listener.doCalibrate();
    }//GEN-LAST:event_calibrateButtonActionPerformed

    private void sendDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendDataButtonActionPerformed
        sendData = !sendData;
        listener.doSendData(sendData, graphView);
        sendDataButton.setText(sendData ? "Stop Data" : "Collect Data");
        saveMenuItem.setEnabled(true);
        clearedData = false;
    }//GEN-LAST:event_sendDataButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton calibrateButton;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JLabel connStatusLabel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.ButtonGroup fullscaleGroup;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pagesetupMenuItem;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JButton reconnButton;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton sendDataButton;
    private javax.swing.ButtonGroup smoothGroup;
    private javax.swing.JMenu windowMenu;
    private javax.swing.ButtonGroup xZoomGroup;
    private javax.swing.ButtonGroup yZoomGroup;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JButton clearButton;
    
    // for gesture visualization
    private javax.swing.JPanel firstMovementContainer;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel secondMovementContainer;    
    private GestureDrawingPanel firstMovement, secondMovement;
}
