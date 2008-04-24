/*
 * GestureCreatorGUI.java
 *
 * Created on April 23, 2008, 10:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
 * GUI creating code that makes a window that will allow a user to create
 * gestures. Allows the user to calibrate, connect, and disconnect from the
 * SunSPOT. Also, reads in data from the SunSPOT and records a gesture.
 *
 * @author Sean Bachelder
 */
public class GestureCreatorGUI extends JFrame
{
    public static final int MOVEMENTS_PER_GESTURE = 3;
    
    private AccelerometerListener listener = null;
    private BasicGestureRecognizer recognizer = null;
    private BasicGestureClassifier classifier = null;
    private GestureClassifier gestureClassifier = null;
    private boolean sendData = false;
    private boolean fixedData = false;
    private boolean clearedData = true;
    
    // window data
    private int windowSizeX = 500;
    private int windowSizeY = 500;
    private String windowTitle = "SunFLARE Gesture Creator";
    
    
    //
    // components
    //
    
    // buttons related to gestures
    private JButton buttonCreateNewGesture;
    private JButton buttonRecordGesture;
    private JButton buttonTestGesture;
    private JButton buttonSaveGesture;
    private JButton buttonAssignAction;
    private JButton buttonLoadGesture;
    
    // sunspot buttons
    private JButton buttonConnect;
    private JButton buttonCalibrate;
    private JButton buttonClear;
    
    // drawing panels
    private GestureDrawingPanel[] drawingPanel;
    
    // panels
    private JPanel panelGestureButtons;
    private JPanel panelSunSPOTInfo;
    private JPanel panelUserInstruction;
    private JPanel panelDrawingPanels;
    
    // labels for instruction
    private JLabel labelCreateNewGesture;
    private JLabel labelRecordGesture;
    private JLabel labelTestGesture;
    private JLabel labelValidateGesture;
    private JLabel labelSaveGesture;
    private JLabel labelAssignAction;
    private JLabel labelSeparator; // may not need
    
    // labels for sunspot info
    private JLabel labelConnected;
    
    // organizational panels
    private JPanel panelMain;
    private JPanel panelLeft;
    private JPanel panelRight;
    private JPanel panelBottom;
    private JPanel panelTop;
    
    /**
     * Creates and displays the GUI
     * @author Sean Bachelder
     */
    public static void main(String args[])
    {
        // Set system properties for Mac OS X before AWT & Swing get loaded - doesn't hurt if not on a MAC
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SunFLARE Gesture Creator");

        GestureCreatorGUI gui = new GestureCreatorGUI();
        gui.show();
    }
    
    /** Creates a new instance of GestureCreatorGUI */
    public GestureCreatorGUI()
    {
        initializeComponents(); // initializes all buttons, panels, etc...
        init();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // activates functions on startup and close
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        
        setTitle(windowTitle);
        setVisible(true);
        setMinimumSize(new Dimension(getWidth(),getHeight()));
        
        Global.mainWindow = this;
    }
    
    private void init()
    {
        if (listener == null) {
            listener = new AccelerometerListener();
            listener.start();
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
    }
    
    /**
     * Display the current connection status to a remote SPOT. 
     * Called by the AccelerometerListener whenever the radio connection status changes.
     *
     * @param conn true if now connected to a remote SPOT
     * @param msg the String message to display, includes the 
     */
    public void setConnectionStatus(boolean conn, String msg) {
        labelConnected.setText(msg);
        //blinkButton.setEnabled(conn);
        //pingButton.setEnabled(conn);
        buttonConnect.setEnabled(conn);
        if (!fixedData) {
            if (listener.is2GScale()) {
                //twoGRadioButton.setSelected(true);
            } else {
                //sixGRadioButton.setSelected(true);
            }
            //twoGRadioButton.setEnabled(conn);
            //sixGRadioButton.setEnabled(conn);
            buttonCreateNewGesture.setEnabled(conn);
            buttonRecordGesture.setEnabled(conn);
            buttonTestGesture.setEnabled(conn);
            buttonSaveGesture.setEnabled(conn);
            buttonAssignAction.setEnabled(conn);
            buttonLoadGesture.setEnabled(conn);
            buttonCalibrate.setEnabled(conn);
            buttonClear.setEnabled(conn);
        }
    }
    
    public void setGesture(int index, int id)
    {
        if (index >= 0 || index < MOVEMENTS_PER_GESTURE)
        {
            drawingPanel[index].setGesture(id);
        }
    }
    
    // clean exit
    private void doQuit()
    {
        listener.doQuit();
        recognizer.doQuit();
        classifier.doQuit();
        gestureClassifier.doQuit();
        System.exit(0);
    }
    
    private void initializeComponents()
    {
        // buttons related to gestures
        buttonCreateNewGesture = new JButton("New Gesture");
        buttonCreateNewGesture.setSize(new Dimension(140,90));
        buttonCreateNewGesture.setPreferredSize(new Dimension(140,90));
        buttonCreateNewGesture.setMaximumSize(new Dimension(140,90));
        buttonCreateNewGesture.setMinimumSize(new Dimension(140,90));
        buttonCreateNewGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateNewGestureActionPerformed(evt);
            }
        });
        
        buttonRecordGesture = new JButton("Record Gesture");
        buttonRecordGesture.setSize(new Dimension(140,90));
        buttonRecordGesture.setPreferredSize(new Dimension(140,90));
        buttonRecordGesture.setMaximumSize(new Dimension(140,90));
        buttonRecordGesture.setMinimumSize(new Dimension(140,90));
        buttonRecordGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRecordGestureActionPerformed(evt);
            }
        });
        
        buttonTestGesture = new JButton("Test Gesture");
        buttonTestGesture.setSize(new Dimension(140,90));
        buttonTestGesture.setPreferredSize(new Dimension(140,90));
        buttonTestGesture.setMaximumSize(new Dimension(140,90));
        buttonTestGesture.setMinimumSize(new Dimension(140,90));
        buttonTestGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTestGestureActionPerformed(evt);
            }
        });
        
        buttonSaveGesture = new JButton("Save Gesture");
        buttonSaveGesture.setSize(new Dimension(140,90));
        buttonSaveGesture.setPreferredSize(new Dimension(140,90));
        buttonSaveGesture.setMaximumSize(new Dimension(140,90));
        buttonSaveGesture.setMinimumSize(new Dimension(140,90));
        buttonSaveGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveGestureActionPerformed(evt);
            }
        });
        
        buttonAssignAction = new JButton("Assign Action");
        buttonAssignAction.setSize(new Dimension(140,90));
        buttonAssignAction.setPreferredSize(new Dimension(140,90));
        buttonAssignAction.setMaximumSize(new Dimension(140,90));
        buttonAssignAction.setMinimumSize(new Dimension(140,90));
        buttonAssignAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAssignActionActionPerformed(evt);
            }
        });
        
        buttonLoadGesture = new JButton("Load Gesture");
        buttonLoadGesture.setSize(new Dimension(140,90));
        buttonLoadGesture.setPreferredSize(new Dimension(140,90));
        buttonLoadGesture.setMaximumSize(new Dimension(140,90));
        buttonLoadGesture.setMinimumSize(new Dimension(140,90));
        buttonLoadGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadGestureActionPerformed(evt);
            }
        });
        
        // sunspot buttons
        buttonConnect = new JButton("Connect");
        buttonConnect.setSize(new Dimension(190,25));
        buttonConnect.setPreferredSize(new Dimension(190,25));
        buttonConnect.setMaximumSize(new Dimension(190,25));
        buttonConnect.setMinimumSize(new Dimension(190,25));
        buttonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConnectActionPerformed(evt);
            }
        });
        
        buttonCalibrate = new JButton("Calibrate");
        buttonCalibrate.setSize(new Dimension(190,25));
        buttonCalibrate.setPreferredSize(new Dimension(190,25));
        buttonCalibrate.setMaximumSize(new Dimension(190,25));
        buttonCalibrate.setMinimumSize(new Dimension(190,25));
        buttonCalibrate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCalibrateActionPerformed(evt);
            }
        });
        
        buttonClear = new JButton("Clear");
        buttonClear.setSize(new Dimension(190,25));
        buttonClear.setPreferredSize(new Dimension(190,25));
        buttonClear.setMaximumSize(new Dimension(190,25));
        buttonClear.setMinimumSize(new Dimension(190,25));
        buttonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });
        
        // labels for instruction
        labelCreateNewGesture = new JLabel("New Gesture");
        labelRecordGesture = new JLabel("Record Gesture");
        labelTestGesture = new JLabel("Test Gesture");
        labelValidateGesture = new JLabel("Validate Gesture");
        labelSaveGesture = new JLabel("Save Gesture");
        labelAssignAction = new JLabel("Assign Action");
        labelSeparator = new JLabel("*"); // may not need
        
        // labels for sunspot info
        labelConnected = new JLabel("Not Connected!");
        
               
        //
        // panels
        //
        
        // drawing panels
        drawingPanel = new GestureDrawingPanel[MOVEMENTS_PER_GESTURE];
        
        // gesture buttons
        panelGestureButtons = new JPanel();
        panelGestureButtons.setLayout(new BoxLayout(panelGestureButtons,BoxLayout.LINE_AXIS));
        panelGestureButtons.setBorder(BorderFactory.createTitledBorder("Commands"));
        panelGestureButtons.setSize(new Dimension(900,100));
        panelGestureButtons.setPreferredSize(new Dimension(900,100));
        panelGestureButtons.setMaximumSize(new Dimension(900,100));
        panelGestureButtons.setMinimumSize(new Dimension(900,100));
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonCreateNewGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonRecordGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonTestGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonSaveGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonAssignAction);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonLoadGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        
        // sunspot info panel
        panelSunSPOTInfo = new JPanel();
        panelSunSPOTInfo.setLayout(new BoxLayout(panelSunSPOTInfo,BoxLayout.PAGE_AXIS));
        panelSunSPOTInfo.setBorder(BorderFactory.createTitledBorder("SunSPOT Status"));
        panelSunSPOTInfo.setSize(new Dimension(200,150));
        panelSunSPOTInfo.setPreferredSize(new Dimension(200,150));
        panelSunSPOTInfo.setMaximumSize(new Dimension(200,150));
        panelSunSPOTInfo.setMinimumSize(new Dimension(200,150));
        panelSunSPOTInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSunSPOTInfo.add(Box.createVerticalGlue());
        labelConnected.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSunSPOTInfo.add(labelConnected);
        panelSunSPOTInfo.add(Box.createVerticalGlue());
        buttonConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSunSPOTInfo.add(buttonConnect);
        panelSunSPOTInfo.add(Box.createVerticalGlue());
        buttonCalibrate.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSunSPOTInfo.add(buttonCalibrate);
        panelSunSPOTInfo.add(Box.createVerticalGlue());
        buttonClear.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSunSPOTInfo.add(buttonClear);
        panelSunSPOTInfo.add(Box.createVerticalGlue());
        
        // user instruction panel
        panelUserInstruction = new JPanel();
        panelUserInstruction.setLayout(new BoxLayout(panelUserInstruction,BoxLayout.PAGE_AXIS));
        panelUserInstruction.setBorder(BorderFactory.createTitledBorder("Instructions"));
        panelUserInstruction.setSize(new Dimension(200,200));
        panelUserInstruction.setPreferredSize(new Dimension(200,200));
        panelUserInstruction.setMaximumSize(new Dimension(200,200));
        panelUserInstruction.setMinimumSize(new Dimension(200,200));
        panelUserInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelCreateNewGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelCreateNewGesture);
        labelRecordGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelRecordGesture);
        labelTestGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelTestGesture);
        labelValidateGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelValidateGesture);
        labelSaveGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelSaveGesture);
        labelAssignAction.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelAssignAction);
        panelUserInstruction.add(Box.createVerticalGlue());
        
        // panels for drawing the gesture
        panelDrawingPanels = new JPanel();
        panelDrawingPanels.setLayout(new BoxLayout(panelDrawingPanels,BoxLayout.LINE_AXIS));
        panelDrawingPanels.setBorder(BorderFactory.createTitledBorder("Gesture"));
        panelDrawingPanels.setSize(new Dimension(700,350));
        panelDrawingPanels.setPreferredSize(new Dimension(700,350));
        panelDrawingPanels.setMinimumSize(new Dimension(700,350));
        panelDrawingPanels.setMaximumSize(new Dimension(700,350));

        for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++) {
            try {
                drawingPanel[i] = new GestureDrawingPanel();
            } catch (IOException e) {
                
            }
        }
        
        panelDrawingPanels.add(Box.createHorizontalGlue());
        for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++)
        {
            drawingPanel[i].setBorder(BorderFactory.createLineBorder(Color.black));
            drawingPanel[i].setBackground(new Color(255,255,255));
            drawingPanel[i].setSize(new Dimension(200,300));
            drawingPanel[i].setPreferredSize(new Dimension(200,300));
            drawingPanel[i].setMinimumSize(new Dimension(200,300));
            drawingPanel[i].setMaximumSize(new Dimension(200,300));

            drawingPanel[i].setAlignmentY(Component.CENTER_ALIGNMENT);
            panelDrawingPanels.add(drawingPanel[i]);
            panelDrawingPanels.add(Box.createHorizontalGlue());
        }
        
        //
        // organizational panels
        //
        
        // left panel
        panelLeft = new JPanel();
        panelLeft.setLayout(new BoxLayout(panelLeft,BoxLayout.PAGE_AXIS));
        panelLeft.add(Box.createVerticalGlue());
        panelLeft.add(panelUserInstruction);
        panelLeft.add(Box.createVerticalGlue());
        panelLeft.add(panelSunSPOTInfo);
        panelLeft.add(Box.createVerticalGlue());
        
        // right panel
        panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight,BoxLayout.PAGE_AXIS));
        panelRight.add(Box.createVerticalGlue());
        panelRight.add(panelDrawingPanels);
        panelRight.add(Box.createVerticalGlue());
        //panelRight.add(panelGestureButtons);
        //panelRight.add(Box.createVerticalGlue());
        
        // top panel
        panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop,BoxLayout.LINE_AXIS));
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(panelLeft);
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(panelRight);
        panelTop.add(Box.createHorizontalGlue());
        
        // bottom panel
        panelBottom = new JPanel();
        panelBottom.setLayout(new BoxLayout(panelBottom,BoxLayout.LINE_AXIS));
        panelBottom.add(Box.createHorizontalGlue());
        panelBottom.add(panelGestureButtons);
        panelBottom.add(Box.createHorizontalGlue());
        
        // main panel
        panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain,BoxLayout.PAGE_AXIS));
        panelMain.add(Box.createVerticalGlue());
        panelMain.add(panelTop);
        panelMain.add(Box.createVerticalGlue());
        panelMain.add(panelBottom);
        panelMain.add(Box.createVerticalGlue());
        
        add(panelMain);
        pack();
    }
    
    //
    // event handlers
    //
    private void buttonCreateNewGestureActionPerformed(java.awt.event.ActionEvent evt)
    {
        
    }
    
    private void buttonRecordGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        sendData = !sendData;
        listener.doSendData(sendData);
        buttonRecordGesture.setText(sendData ? "Stop Recording" : "Record Gesture");
        clearedData = false;
    }
    
    private void buttonTestGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        
    }
    
    private void buttonSaveGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        
    }
    
    private void buttonAssignActionActionPerformed(java.awt.event.ActionEvent evt) 
    {
        
    }
    
    private void buttonLoadGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        
    }
    
    // sunspot stuff
    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) 
    {
        listener.clear();
        recognizer.clear();
        classifier.clear();
        gestureClassifier.clear();
        clearedData = true;
    }
    
    private void buttonCalibrateActionPerformed(java.awt.event.ActionEvent evt) 
    {
        listener.doCalibrate();
    }
    
    private void buttonConnectActionPerformed(java.awt.event.ActionEvent evt) 
    {
        listener.reconnect();
    }

    private void formWindowActivated(java.awt.event.WindowEvent evt) {                                     
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
    }
    
    private void formWindowClosed(java.awt.event.WindowEvent evt) {                                  
        doQuit();
    }  
}
