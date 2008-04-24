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
    // constants
    public static final int MOVEMENTS_PER_GESTURE = 3;
    public enum State {
        STATE_INITIAL,
        STATE_NEW_GESTURE,
        STATE_RECORDING_GESTURE,
        STATE_DONE_RECORDING_GESTURE,
        STATE_TEST_GESTURE,
        STATE_VALIDATE_GESTURE,
        STATE_SAVE_GESTURE,
        STATE_ASSIGN_ACTION,
        STATE_LOAD_GESTURE
    }
//    public static final int STATE_NEW_GESTURE = 0;
//    public static final int STATE_RECORDING_GESTURE = 1;
//    public static final int STATE_DONE_RECORDING_GESTURE = 2;
//    public static final int STATE_TEST_GESTURE = 3;
//    public static final int STATE_VALIDATE_GESTURE = 3;
//    public static final int STATE_SAVE_GESTURE = 4;
//    public static final int STATE_ASSIGN_ACTION = 5;
//    public static final int STATE_LOAD_GESTURE = 6; // TODO: we'll see if this actually gets implemented
    
    // sunspot variables
    private AccelerometerListener listener = null;
    private BasicGestureRecognizer recognizer = null;
    private BasicGestureClassifier classifier = null;
    private GestureClassifier gestureClassifier = null;
    private boolean sendData = false;
    private boolean fixedData = false;
    private boolean clearedData = true;
    

    private State currentState = State.STATE_INITIAL;
    private Font currentStepFont = null;
    private Font normalFont = null;
    private Font completedStepFont = null;
    private Color highlightColor = null;
    private Color normalColor = null;
    private boolean recording = false;
    
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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // the doQuit() function exits the app
        
        // activates functions on startup and close
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        
        // initialize fonts to be used
        String fontName = getFont().getFontName(); // get default font name
        currentStepFont = new Font(fontName,Font.BOLD,16);
        normalFont = new Font(fontName,Font.BOLD,12);
        completedStepFont = new Font(fontName,Font.ITALIC,12);
        highlightColor = Color.blue;
        normalColor = Color.black;
        
        // set window variables
        setTitle(windowTitle);
        setVisible(true);
        setMinimumSize(new Dimension(getWidth(),getHeight())); 
        
        Global.mainWindow = this;
        
        // change the state to initial state
        changeState(State.STATE_NEW_GESTURE);
    }
    
    // initializes gesture identification/sunspot stuff
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
        if (conn)
        {
            // is connected
            labelConnected.setForeground(Color.green);
        }
        else
        {
            labelConnected.setForeground(Color.red);
        }
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
    
    /**
     * Sets the display at the specified index to the specified gesture
     * @param index Index of the display to be updated
     * @param id ID number of the gesture to be displayed
     */
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
    
    // initializes all the GUI components
    private void initializeComponents()
    {
        //
        // buttons
        //
        
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
        
        //
        // labels
        //
        
        // labels for instruction
        labelCreateNewGesture = new JLabel("New Gesture");
        labelRecordGesture = new JLabel("Record Gesture");
        labelTestGesture = new JLabel("Test Gesture");
        labelValidateGesture = new JLabel("Validate Gesture");
        labelSaveGesture = new JLabel("Save Gesture");
        labelAssignAction = new JLabel("Assign Action");
        
        // labels for sunspot info
        labelConnected = new JLabel("Not Connected");
        labelConnected.setForeground(Color.red);
               
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
    
    // changes the state of the system
    // updates user instruction panel to help guide them
    // updates any buttons that need to be enabled/disabled
    private void changeState(State state)
    {
        switch (state)
        {
            case STATE_NEW_GESTURE:
                if (currentState != state)
                {
                    // TODO: perhaps display a warning message to user asking if they are sure
                    // TODO: setup for a new gesture
                    labelCreateNewGesture.setFont(currentStepFont);
                    labelCreateNewGesture.setForeground(highlightColor);
                    labelRecordGesture.setFont(normalFont);
                    labelRecordGesture.setForeground(normalColor);
                    labelTestGesture.setFont(normalFont);
                    labelTestGesture.setForeground(normalColor);
                    labelValidateGesture.setFont(normalFont);
                    labelValidateGesture.setForeground(normalColor);
                    labelSaveGesture.setFont(normalFont);
                    labelSaveGesture.setForeground(normalColor);
                    labelAssignAction.setFont(normalFont);
                    labelAssignAction.setForeground(normalColor);
                    
                    // check to see if we're in the middle of recording
                    if (currentState == state.STATE_RECORDING_GESTURE)
                    {
                        // need to stop recording
                        recording = false;
                        sendData = !sendData;
                        listener.doSendData(sendData);
                        buttonRecordGesture.setText(sendData ? "Stop Recording" : "Record Gesture");
                        clearedData = false;
                    }
                    currentState = state;
                }
                break;
            case STATE_RECORDING_GESTURE:
                if (currentState != state)
                {
                    if (currentState != State.STATE_NEW_GESTURE)
                    {
                        // This means that the user is coming from some state where they've already
                        // recorded a gesture. Should this be allowed? Should they be forced to go to
                        // new gesture state?
                        // What if the user records the gesture and doesn't get the expected outcome?
                        // Probably shouldn't force them back NEW_GESTURE in that case...
                        // TODO: perhaps display a warning message to user asking if they are sure
                    }
                    labelCreateNewGesture.setFont(completedStepFont);
                    labelCreateNewGesture.setForeground(normalColor);
                    labelRecordGesture.setFont(currentStepFont);
                    labelRecordGesture.setForeground(highlightColor);
                    labelTestGesture.setFont(normalFont);
                    labelTestGesture.setForeground(normalColor);
                    labelValidateGesture.setFont(normalFont);
                    labelValidateGesture.setForeground(normalColor);
                    labelSaveGesture.setFont(normalFont);
                    labelSaveGesture.setForeground(normalColor);
                    labelAssignAction.setFont(normalFont);
                    labelAssignAction.setForeground(normalColor);
                    currentState = state;
                }
                break;
            case STATE_DONE_RECORDING_GESTURE:
                // TODO: is there much else to do here?
                // not much to do here
                currentState = state;
                break;
            case STATE_TEST_GESTURE:
                if (currentState != State.STATE_DONE_RECORDING_GESTURE)
                {
                    // a gesture hasn't been recorded yet!
                    // TODO: let the user know...
                }
                else
                {
                    // don't need to check if we're already in this state. if the user
                    // wants to test the gesture again, we'll allow it
                    labelCreateNewGesture.setFont(completedStepFont);
                    labelCreateNewGesture.setForeground(normalColor);
                    labelRecordGesture.setFont(completedStepFont);
                    labelRecordGesture.setForeground(normalColor);
                    labelTestGesture.setFont(currentStepFont);
                    labelTestGesture.setForeground(highlightColor);
                    labelValidateGesture.setFont(normalFont);
                    labelValidateGesture.setForeground(normalColor);
                    labelSaveGesture.setFont(normalFont);
                    labelSaveGesture.setForeground(normalColor);
                    labelAssignAction.setFont(normalFont);
                    labelAssignAction.setForeground(normalColor);
                    currentState = state;
                }
                break;
            case STATE_VALIDATE_GESTURE:
                // TODO: should the user be allowed to come here straight after recording the gesture?
                // should they be forced to test it first?
                // TODO: need to interact with the server and find out if gesture already exists
                // TODO: what happens if the gesture already exists? ask if the user wants to
                // assign a new action?
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelTestGesture.setFont(completedStepFont);
                labelTestGesture.setForeground(normalColor);
                labelValidateGesture.setFont(currentStepFont);
                labelValidateGesture.setForeground(highlightColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                labelAssignAction.setFont(normalFont);
                labelAssignAction.setForeground(normalColor);
                currentState = state;
                break;
            case STATE_SAVE_GESTURE:
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelTestGesture.setFont(completedStepFont);
                labelTestGesture.setForeground(normalColor);
                labelValidateGesture.setFont(completedStepFont);
                labelValidateGesture.setForeground(normalColor);
                labelSaveGesture.setFont(currentStepFont);
                labelSaveGesture.setForeground(highlightColor);
                labelAssignAction.setFont(normalFont);
                labelAssignAction.setForeground(normalColor);
                currentState = state;
                break;
            case STATE_ASSIGN_ACTION:
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelTestGesture.setFont(completedStepFont);
                labelTestGesture.setForeground(normalColor);
                labelValidateGesture.setFont(completedStepFont);
                labelValidateGesture.setForeground(normalColor);
                labelSaveGesture.setFont(completedStepFont);
                labelSaveGesture.setForeground(normalColor);
                labelAssignAction.setFont(currentStepFont);
                labelAssignAction.setForeground(highlightColor);
                currentState = state;
                break;
            case STATE_LOAD_GESTURE:
                currentState = state;
                break;
            default:
                // invalid state
                break;
        }
    }
    
    //
    // event handlers
    //
    private void buttonCreateNewGestureActionPerformed(java.awt.event.ActionEvent evt)
    {
        changeState(State.STATE_NEW_GESTURE);
    }
    
    private void buttonRecordGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        if (!recording) {
            // TODO: add in code to disable buttons: test, save, assign
            changeState(State.STATE_RECORDING_GESTURE);
            recording = true;
        }
        else
        {
            // TODO: add in code to enable buttons: test, save, assign
            changeState(State.STATE_DONE_RECORDING_GESTURE);
            recording = false;
        }
        sendData = !sendData;
        listener.doSendData(sendData);
        buttonRecordGesture.setText(sendData ? "Stop Recording" : "Record Gesture");
        clearedData = false;
    }
    
    private void buttonTestGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        changeState(State.STATE_TEST_GESTURE);        
    }
    
    private void buttonSaveGestureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        changeState(State.STATE_SAVE_GESTURE);
    }
    
    private void buttonAssignActionActionPerformed(java.awt.event.ActionEvent evt) 
    {
        changeState(State.STATE_ASSIGN_ACTION);
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
