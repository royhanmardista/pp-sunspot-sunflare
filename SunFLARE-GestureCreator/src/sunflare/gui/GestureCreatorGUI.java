/**
 * GestureCreatorGUI.java
 *
 * Created on April 23, 2008, 10:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import sunflare.server.*;
import sunflare.plugin.PluginRef;

/**
 * GUI creating code that makes a window that will allow a user to create
 * gestures. Allows the user to calibrate, connect, and disconnect from the
 * SunSPOT. Also, reads in data from the SunSPOT and records a gesture.
 *
 * @author Sean Bachelder
 */
public class GestureCreatorGUI extends JFrame {
    // constants
    public static final int MOVEMENTS_PER_GESTURE = 3;
    public enum State {
        STATE_INITIAL,
        STATE_NEW_GESTURE,
        STATE_RECORDING_GESTURE,
        STATE_DONE_RECORDING_GESTURE,
        STATE_TESTING_GESTURE,
        STATE_DONE_TESTING_GESTURE,
        STATE_VALIDATE_GESTURE,
        STATE_GESTURE_VALIDATED,
        STATE_SAVE_GESTURE,
        STATE_ASSIGN_ACTION,
        STATE_ACTION_SELECTED,
        STATE_NONE
    }
    
    // sunspot variables
    private AccelerometerListener listener = null;
    private BasicGestureRecognizer recognizer = null;
    private BasicGestureClassifier classifier = null;
    private GestureClassifier gestureClassifier = null;
    private Controller controller = null;
    private boolean sendData = false;
    private boolean clearedData = true;
        
    private State currentState = State.STATE_INITIAL;
    private State previousState = State.STATE_NONE;
    private Font currentStepFont = null;
    private Font normalFont = null;
    private Font completedStepFont = null;
    private Color highlightColor = null;
    private Color normalColor = null;
    private boolean recording = false;
    private boolean testing = false;
    private Semaphore validating = new Semaphore(0);
    private boolean gestureValidated = false;
    private boolean gestureSaved = false;
    
    // window data
    private int windowSizeX = 500;
    private int windowSizeY = 500;
    private String windowTitle = "SunFLARE Gesture Creator";
    
    //
    // components
    //
    private boolean debug = true;
    
    // status bar
    private JLabel statusBar;
    
    // buttons related to gestures
    private JButton buttonCreateNewGesture;
    private JButton buttonRecordGesture;
    private JButton buttonTestGesture;
    private JButton buttonSaveGesture;
    private JButton buttonAssignAction;
    
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
    /*public static void main(String args[]) {
        // Set system properties for Mac OS X before AWT & Swing get loaded - doesn't hurt if not on a MAC
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SunFLARE Gesture Creator");
        
        GestureCreatorGUI gui = new GestureCreatorGUI();
        gui.show();
    }*/
    
    /** 
     * Creates a new instance of GestureCreatorGUI
     */
    public GestureCreatorGUI() {
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
        currentStepFont = new Font(fontName, Font.BOLD, 16);
        normalFont = new Font(fontName, Font.BOLD, 12);
        completedStepFont = new Font(fontName, Font.ITALIC, 12);
        highlightColor = Color.blue;
        normalColor = Color.black;
        
        // set window variables
        setTitle(windowTitle);
        setVisible(true);
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        
        Global.mainWindow = this;
        
        // change the state to initial state
        updateStatusBar("Click on \"New Gesture\" to get started");
        changeState(State.STATE_INITIAL);
    }
    
    /**
     * Initializes server side
     */
    private void init() {
        if (listener == null) {
            listener = new AccelerometerListener();
            listener.start();
        }
        if (recognizer == null) {
            recognizer = new BasicGestureRecognizer();
            recognizer.start();
        }
        if (classifier == null) {
            classifier = new BasicGestureClassifier();
            classifier.start();
        }
        if (gestureClassifier == null) {
            gestureClassifier = new GestureClassifier();
            gestureClassifier.start();
        }
        if (controller == null) {
            controller = new Controller();
            controller.start();
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
        if (conn) {
            // is connected
            labelConnected.setForeground(Color.green);
            changeState(currentState);
        } else {
            labelConnected.setForeground(Color.red);
            buttonCreateNewGesture.setEnabled(false);
            buttonRecordGesture.setEnabled(false);
            buttonTestGesture.setEnabled(false);
            buttonSaveGesture.setEnabled(false);
            buttonAssignAction.setEnabled(false);
        }
        
        buttonCalibrate.setEnabled(conn);
        buttonClear.setEnabled(conn);
    }
    
    /**
     * Shows a dialog that displays all of the options for actions that
     * the user can assign an action to
     */
    public Object showAssignActionDialog(Vector actions) {
        //TODO: need to retrieve a list of actions from the server
        Object[] possibilities = actions.toArray();
        Object o = (Object)JOptionPane.showInputDialog(
                this,
                "Please select a third party application and an action for the gesture:",
                "Assign an action",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                possibilities[0]);
        
        return o;
    }
    
    /**
     * Sets the display at the specified index to the specified gesture.
     * If the GUI is in testing mode, instead of displaying the gesture in
     * a box, it will highlight the box in red if the new gesture doesn't match
     * the old and highlight the box in green if the new gesture does match
     * @param index Index of the display to be updated
     * @param id ID number of the gesture to be displayed
     */
    public void setGesture(int index, int id) {
        if (testing) {
            // if we're in testing mode, we should highlight the box in green if
            // that movement was performed correctly, and red if it wasn't...
            if (index >= 0 && index < MOVEMENTS_PER_GESTURE) {
                if (drawingPanel[index].getID() == id) {
                    drawingPanel[index].setBorder(BorderFactory.createLineBorder(Color.green,3));
                } else {
                    drawingPanel[index].setBorder(BorderFactory.createLineBorder(Color.red,3));
                }
            }
        } else if (recording) {
            
            if (index >= 0 && index < MOVEMENTS_PER_GESTURE) {
                drawingPanel[index].setGesture(id);
            }
//            if (index == MOVEMENTS_PER_GESTURE-1) {
//                toggleRecord();
//            }
        }
    }
    
    /**
     * Clears the gesture drawing boxes
     */
    public void clearGestureBoxes(){
        if(currentState!=State.STATE_TESTING_GESTURE){
            for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++) {
                drawingPanel[i].setGesture(GestureDrawingPanel.INVALID_GESTURE_ID);
            }
            repaint();
        }
    }
    
    /**
     * Resets the borders of the gesture drawing boxes to a thin black line
     */
    public void clearGestureBoxBorders() {
        for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++) {
            drawingPanel[i].setBorder(BorderFactory.createLineBorder(Color.black,1));
        }
        repaint();
    }
    
    /**
     * Ensures a clean exit
     */
    private void doQuit() {
        listener.doQuit();
        recognizer.doQuit();
        classifier.doQuit();
        gestureClassifier.doQuit();
        System.exit(0);
    }
    
    /**
     * Changes the state of the system
     * @param state system state to change to
     */
    private void changeState(State state) {
        // update the states
        previousState = currentState;
        currentState = state;
        
        switch (currentState) {
            case STATE_INITIAL:
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(normalFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(normalFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(normalFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(normalFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(false);
                buttonRecordGesture.setEnabled(false);
                buttonTestGesture.setEnabled(false);
                buttonSaveGesture.setEnabled(false);

                
                previousState = state.STATE_NONE;                
                break;
            case STATE_NEW_GESTURE:
                //if (currentState != state) {
                // TODO: perhaps display a warning message to user asking if they are sure
                // only do the above if the currentState is not equal to STATE_INITIAL...
                
                // TODO: setup for a new gesture
                
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(currentStepFont);
                labelCreateNewGesture.setForeground(highlightColor);
                labelAssignAction.setFont(normalFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(normalFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(normalFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(false);
                buttonTestGesture.setEnabled(false);
                buttonSaveGesture.setEnabled(false);
                
                
                // check to see if we're in the middle of recording
                if (previousState == State.STATE_RECORDING_GESTURE) {
                    // need to stop recording
                    recording = false;
                    sendData = !sendData;
                    listener.doSendData(sendData);
                    buttonRecordGesture.setText("Record Gesture");
                    clearedData = false;
                } else if (previousState == State.STATE_TESTING_GESTURE) {
                    // TODO: probably need to add more here...
                    buttonTestGesture.setText("Test Gesture");
                }
                //}
                break;
            case STATE_ASSIGN_ACTION:
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(currentStepFont);
                labelAssignAction.setForeground(highlightColor);
                labelRecordGesture.setFont(normalFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(normalFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(false);
                buttonTestGesture.setEnabled(false);
                buttonSaveGesture.setEnabled(false);

                
                Vector possibleActions = controller.assignActionState();
                Object o = showAssignActionDialog(possibleActions); // TODO: need return value and notify controller
                
                if (o != null) {
                    // action selected successfully
                    controller.actionSelectedState((PluginRef)o);
                    updateStatusBar("Action selected. Click on \"Record Gesture\" to continue");
                    changeState(State.STATE_ACTION_SELECTED);
                } else {
                    // no action selected
                    if (previousState != State.STATE_NEW_GESTURE) {
                        // user has already selected an action
                        System.out.print("Going back to " + previousState);
                        //updateStatusBar("");
                        changeState(previousState);
                    } else {
                        updateStatusBar("You must assign an action to continue. Click on \"Assign Action\" to choose one");
                        changeState(State.STATE_NEW_GESTURE);
                    }
                }
                break;
            case STATE_ACTION_SELECTED:
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(currentStepFont);
                labelAssignAction.setForeground(highlightColor);
                labelRecordGesture.setFont(normalFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(normalFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(true);
                buttonTestGesture.setEnabled(false);
                buttonSaveGesture.setEnabled(false);

                break;
            case STATE_RECORDING_GESTURE:
                //if (currentState != state) {
                if (previousState != State.STATE_ACTION_SELECTED) {
                    // This means that the user is coming from some state where they've already
                    // recorded a gesture. Should this be allowed? Should they be forced to go to
                    // new gesture state?
                    // What if the user records the gesture and doesn't get the expected outcome?
                    // Probably shouldn't force them back NEW_GESTURE in that case...
                    // TODO: perhaps display a warning message to user asking if they are sure
                }
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(completedStepFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(currentStepFont);
                labelRecordGesture.setForeground(highlightColor);
                labelValidateGesture.setFont(normalFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(false);
                buttonAssignAction.setEnabled(false);
                buttonRecordGesture.setEnabled(true);
                buttonTestGesture.setEnabled(false);
                buttonSaveGesture.setEnabled(false);

                //}
                break;
            case STATE_DONE_RECORDING_GESTURE:
                // TODO: is there much else to do here?
                // not much to do here
                
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(completedStepFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(currentStepFont);
                labelRecordGesture.setForeground(highlightColor);
                labelValidateGesture.setFont(normalFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(true);
                buttonTestGesture.setEnabled(true);
                buttonSaveGesture.setEnabled(false);

                break;
            case STATE_VALIDATE_GESTURE:
                // TODO: should the user be allowed to come here straight after recording the gesture?
                // should they be forced to test it first?
                // TODO: need to interact with the server and find out if gesture already exists
                // TODO: what happens if the gesture already exists? ask if the user wants to
                // assign a new action?
                
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(completedStepFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(currentStepFont);
                labelValidateGesture.setForeground(highlightColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(false);
                buttonAssignAction.setEnabled(false);
                buttonRecordGesture.setEnabled(false);
                buttonTestGesture.setEnabled(false);
                buttonSaveGesture.setEnabled(false);

                
                this.repaint(); // window doesn't get updated here for some reason..
                
                System.out.println("GUI: waiting to validate...");
                try {
                    validating.acquire(); // waits for controller thread to notify the gui that it's ok to continue
                } catch (InterruptedException e) {
                    // ??
                    System.out.println("interrupted??");
                }
                
                System.out.println("GUI: done validating...");
                // TODO: check to see if the validation succeeded or failed...
                if (gestureValidated) {
                    System.out.println("GUI: gesture was successfully validated");
                    updateStatusBar("Recorded gesture is unique. Continue by clicking the \"Test Gesture\" button");
                    changeState(State.STATE_GESTURE_VALIDATED);
                } else {
                    // TODO:let the user know and go back to action assigned state
                    JOptionPane.showMessageDialog(this, "The gesture performed already exists in the database for the assigned action", "Invalid gesture...", JOptionPane.ERROR_MESSAGE);
                    System.out.println("GUI: gesture was not validated");
                    clearGestureBoxes();
                    updateStatusBar("Recorded gesture matches an existing gesture in the database. Try recording a different gesture");
                    changeState(State.STATE_ACTION_SELECTED);
                }
                break;
            case STATE_GESTURE_VALIDATED:
                // update colors/fonts of user instruction window
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(completedStepFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(currentStepFont);
                labelValidateGesture.setForeground(highlightColor);
                labelTestGesture.setFont(normalFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(true);
                buttonTestGesture.setEnabled(true);
                buttonSaveGesture.setEnabled(false);

                break;
            case STATE_TESTING_GESTURE:
                if (previousState != State.STATE_GESTURE_VALIDATED) {
                    // a gesture hasn't been recorded and validated yet!
                    // TODO: let the user know...
                } else {
                    // don't need to check if we're already in this state. if the user
                    // wants to test the gesture again, we'll allow it
                    // update colors/fonts of user instruction window
                    labelCreateNewGesture.setFont(completedStepFont);
                    labelCreateNewGesture.setForeground(normalColor);
                    labelAssignAction.setFont(completedStepFont);
                    labelAssignAction.setForeground(normalColor);
                    labelRecordGesture.setFont(completedStepFont);
                    labelRecordGesture.setForeground(normalColor);
                    labelValidateGesture.setFont(completedStepFont);
                    labelValidateGesture.setForeground(normalColor);
                    labelTestGesture.setFont(currentStepFont);
                    labelTestGesture.setForeground(highlightColor);
                    labelSaveGesture.setFont(normalFont);
                    labelSaveGesture.setForeground(normalColor);
                    
                    // update enabled/disabled buttons
                    buttonCreateNewGesture.setEnabled(true);
                    buttonAssignAction.setEnabled(true);
                    buttonRecordGesture.setEnabled(true);
                    buttonTestGesture.setEnabled(true);
                    buttonSaveGesture.setEnabled(false);

                }
                break;
            case STATE_DONE_TESTING_GESTURE:
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(completedStepFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(completedStepFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(currentStepFont);
                labelTestGesture.setForeground(highlightColor);
                labelSaveGesture.setFont(normalFont);
                labelSaveGesture.setForeground(normalColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(true);
                buttonTestGesture.setEnabled(true);
                buttonSaveGesture.setEnabled(true);

                break;
            case STATE_SAVE_GESTURE:
                labelCreateNewGesture.setFont(completedStepFont);
                labelCreateNewGesture.setForeground(normalColor);
                labelAssignAction.setFont(completedStepFont);
                labelAssignAction.setForeground(normalColor);
                labelRecordGesture.setFont(completedStepFont);
                labelRecordGesture.setForeground(normalColor);
                labelValidateGesture.setFont(completedStepFont);
                labelValidateGesture.setForeground(normalColor);
                labelTestGesture.setFont(completedStepFont);
                labelTestGesture.setForeground(normalColor);
                labelSaveGesture.setFont(currentStepFont);
                labelSaveGesture.setForeground(highlightColor);
                
                // update enabled/disabled buttons
                buttonCreateNewGesture.setEnabled(true);
                buttonAssignAction.setEnabled(true);
                buttonRecordGesture.setEnabled(true);
                buttonTestGesture.setEnabled(true);
                buttonSaveGesture.setEnabled(true);

                break;
            default:
                // invalid state
                break;
        }
    }
    
    
    //
    // functions called by the server
    //
    
    /**
     * Called by the server when it is done validating. Also releases the semaphore
     * so the GUI can continue execution
     * @param validated true if the gesture is valid, false otherwise
     */
    public void validationResults(boolean validated) {
        gestureValidated = validated;
        validating.release();
    }  
        
    /**
     * Called by the server when the test has completed indicating whether or not
     * the test was successful. The user is notified via a message box if the test
     * was performed incorrectly.
     * @param success true if the test was successful, false otherwise
     */
    public void endTest(boolean success) {
        testing = false;
        sendData = false;
        listener.doSendData(sendData);
        buttonTestGesture.setText("Test Gesture");
        controller.systemIdle();
        if (success) {
            updateStatusBar("Gesture successfully tested! You may now save the gesture by clicking \"Save Gesture\"");
            changeState(State.STATE_DONE_TESTING_GESTURE);
        } else {
            JOptionPane.showMessageDialog(this, "The gesture was not performed correctly!", "Test failed...", JOptionPane.ERROR_MESSAGE);
            clearGestureBoxBorders();
            updateStatusBar("Gesture performed incorrectly! Click \"Test Gesture\" to try again");
            changeState(State.STATE_GESTURE_VALIDATED);
        }
    }
    
    public void updateStatusBar(String m) {
        statusBar.setText(m);
    }
    
    //
    // end functions called by server
    //
    
    /**
     * Clears data and resets gesture drawing box borders so a new test can be performed
     */
    private void resetTest() {
        clearData();
        clearGestureBoxBorders();
    }
            
    /**
     * Clears data on the server side
     */
    private void clearData() {
        listener.clear();
        recognizer.clear();
        classifier.clear();
        gestureClassifier.clear();
        clearedData = true;
    }
        

    //
    // event handlers
    //
    
    /**
     * Creates a new gesture in the system. Also prepares the system to begin the
     * process that will guide the user through creating a gesture
     */
    private void buttonCreateNewGestureActionPerformed(java.awt.event.ActionEvent evt) {
        clearGestureBoxBorders();
        clearGestureBoxes();
        clearData();
        for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++) {
            drawingPanel[i].setBorder(BorderFactory.createLineBorder(Color.black,1));
        }
        repaint();
        updateStatusBar("New gesture created. Click on \"Assign Action\" to continue");
        changeState(State.STATE_NEW_GESTURE);
        controller.newGestureState();
    }
    
    /**
     * Turns recording on or off
     */
    private void buttonRecordGestureActionPerformed(java.awt.event.ActionEvent evt) {
        toggleRecord();
    }
    
    /**
     * Turns recording on if it is currently off and vice versa. Also updates the text on 
     * the record gesture button to reflect the current state. If going from recording to
     * not-recording, the system will start validating the gesture recorded.
     */
    private void toggleRecord() {
        sendData = !sendData;
        listener.doSendData(sendData);
        buttonRecordGesture.setText(sendData ? "Stop Recording" : "Record Gesture");
        clearedData = false;
        
        if (!recording) {
            // TODO: add in code to disable buttons: test, save, assign
            clearGestureBoxBorders();
            clearGestureBoxes();
            clearData();
            repaint();
            updateStatusBar("Perform a gesture and press \"Stop Recording\" when done");
            changeState(State.STATE_RECORDING_GESTURE);
            recording = true;
            //controller.recordGesture();
            controller.gestureRecordingState();
            
        } else {
            // TODO: add in code to enable buttons: test, save, assign
            changeState(State.STATE_DONE_RECORDING_GESTURE);
            recording = false;
            //controller.stopRecording();
            updateStatusBar("Validating the gesture against existing gestures...");
            controller.gestureRecordedState();
            changeState(State.STATE_VALIDATE_GESTURE);
        }
    }
    
    /**
     * Toggles testing on and off. If turning testing on, data from the sunspot will
     * be sent to the server, the "Test Gesture" button will change to "Cancel Testing",
     * and the state will be changed to start testing the gesture. If turning testing
     * off, data from the sunspot will stop being sent to the server, "Cancel Testing"
     * will be changed back to "Test Gesture", and the system will go back to the
     * gesture validated state.
     */
    private void buttonTestGestureActionPerformed(java.awt.event.ActionEvent evt) {
        sendData = !sendData;
        listener.doSendData(sendData);
        buttonTestGesture.setText(sendData ? "Cancel Testing" : "Test Gesture");
        clearedData = false;
        
        
        if (!testing) {
            resetTest();
            updateStatusBar("Testing has begun. Perform the gesture as you did before!");
            changeState(State.STATE_TESTING_GESTURE);
            testing = true;
            //controller.recordGesture();
            controller.testingGestureState();
            
            
            //testerThread.start();
        } else {
            testing = false;
            //controller.stopRecording();
            controller.systemIdle();
            updateStatusBar("Testing has been canceled!");
            changeState(State.STATE_GESTURE_VALIDATED);
            //testerThread.interrupt();
        }
    }
    
    /**
     * Saves the gesture to the database
     */
    private void buttonSaveGestureActionPerformed(java.awt.event.ActionEvent evt) {
        updateStatusBar("Message: Saving the gesture...");
        changeState(State.STATE_SAVE_GESTURE);
        controller.saveGestureState();
    }
    
    /**
     * Changes the state of the system to assign an action to the gesture
     */
    private void buttonAssignActionActionPerformed(java.awt.event.ActionEvent evt) {
        updateStatusBar("Select a plugin from the drop down list");
        changeState(State.STATE_ASSIGN_ACTION);
    }
    
    /**
     * Clears data on the server side
     */
    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {
        clearData();
    }
    
    /**
     * Calibrates the sunspot
     */
    private void buttonCalibrateActionPerformed(java.awt.event.ActionEvent evt) {
        listener.doCalibrate();
    }
    
    /**
     * Reconnects to the sunspot
     */
    private void buttonConnectActionPerformed(java.awt.event.ActionEvent evt) {
        listener.reconnect();
    }
    
    /**
     * Performs startup actions when the window is first created
     */
    private void formWindowActivated(java.awt.event.WindowEvent evt) {
        listener.setGUI(this);
        if (clearedData) {
            clearData();
        }
        if (listener.is2GScale()) {
            //twoGRadioButton.setSelected(true);
        } else {
            //sixGRadioButton.setSelected(true);
        }
    }
    
    /**
     * Performs a clean quit when the user closes the window
     */
    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        doQuit();
    }

    //
    // end event listeners
    //

    /**
     * Prints out a string in debug mode
     */
    private void debug(String s) {
        if (debug)
            System.out.println("GestureCreatorGUI: " + s);
    }

    /**
     * Initializes all the GUI components
     */
    private void initializeComponents() {
        //
        // buttons
        //
        
        // buttons related to gestures
        int buttonWidth = 170;
        int buttonHeight = 90;
        buttonCreateNewGesture = new JButton("New Gesture");
        buttonCreateNewGesture.setSize(new Dimension(buttonWidth, buttonHeight));
        buttonCreateNewGesture.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        buttonCreateNewGesture.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        buttonCreateNewGesture.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
        buttonCreateNewGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateNewGestureActionPerformed(evt);
            }
        });
        
        buttonRecordGesture = new JButton("Record Gesture");
        buttonRecordGesture.setSize(new Dimension(buttonWidth, buttonHeight));
        buttonRecordGesture.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        buttonRecordGesture.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        buttonRecordGesture.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
        buttonRecordGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRecordGestureActionPerformed(evt);
            }
        });
        
        buttonTestGesture = new JButton("Test Gesture");
        buttonTestGesture.setSize(new Dimension(buttonWidth, buttonHeight));
        buttonTestGesture.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        buttonTestGesture.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        buttonTestGesture.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
        buttonTestGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTestGestureActionPerformed(evt);
            }
        });
        
        buttonSaveGesture = new JButton("Save Gesture");
        buttonSaveGesture.setSize(new Dimension(buttonWidth, buttonHeight));
        buttonSaveGesture.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        buttonSaveGesture.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        buttonSaveGesture.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
        buttonSaveGesture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveGestureActionPerformed(evt);
            }
        });
        
        buttonAssignAction = new JButton("Assign Action");
        buttonAssignAction.setSize(new Dimension(buttonWidth, buttonHeight));
        buttonAssignAction.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        buttonAssignAction.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        buttonAssignAction.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
        buttonAssignAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAssignActionActionPerformed(evt);
            }
        });
        
        
        // sunspot buttons
        buttonConnect = new JButton("Reconnect");
        buttonConnect.setSize(new Dimension(190, 25));
        buttonConnect.setPreferredSize(new Dimension(190, 25));
        buttonConnect.setMaximumSize(new Dimension(190, 25));
        buttonConnect.setMinimumSize(new Dimension(190, 25));
        buttonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConnectActionPerformed(evt);
            }
        });
        
        buttonCalibrate = new JButton("Calibrate");
        buttonCalibrate.setSize(new Dimension(190, 25));
        buttonCalibrate.setPreferredSize(new Dimension(190, 25));
        buttonCalibrate.setMaximumSize(new Dimension(190, 25));
        buttonCalibrate.setMinimumSize(new Dimension(190, 25));
        buttonCalibrate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCalibrateActionPerformed(evt);
            }
        });
        
        buttonClear = new JButton("Clear");
        buttonClear.setSize(new Dimension(190, 25));
        buttonClear.setPreferredSize(new Dimension(190, 25));
        buttonClear.setMaximumSize(new Dimension(190, 25));
        buttonClear.setMinimumSize(new Dimension(190, 25));
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
        panelGestureButtons.setLayout(new BoxLayout(panelGestureButtons, BoxLayout.LINE_AXIS));
        panelGestureButtons.setBorder(BorderFactory.createTitledBorder("Commands"));
        panelGestureButtons.setSize(new Dimension(900, 100));
        panelGestureButtons.setPreferredSize(new Dimension(900, 100));
        panelGestureButtons.setMaximumSize(new Dimension(900, 100));
        panelGestureButtons.setMinimumSize(new Dimension(900, 100));
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonCreateNewGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonAssignAction);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonRecordGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonTestGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        panelGestureButtons.add(buttonSaveGesture);
        panelGestureButtons.add(Box.createHorizontalGlue());
        
        // no load gesture for now...
        //panelGestureButtons.add(buttonLoadGesture);
        //panelGestureButtons.add(Box.createHorizontalGlue());
        
        // sunspot info panel
        panelSunSPOTInfo = new JPanel();
        panelSunSPOTInfo.setLayout(new BoxLayout(panelSunSPOTInfo, BoxLayout.PAGE_AXIS));
        panelSunSPOTInfo.setBorder(BorderFactory.createTitledBorder("SunSPOT Status"));
        panelSunSPOTInfo.setSize(new Dimension(200, 150));
        panelSunSPOTInfo.setPreferredSize(new Dimension(200, 150));
        panelSunSPOTInfo.setMaximumSize(new Dimension(200, 150));
        panelSunSPOTInfo.setMinimumSize(new Dimension(200, 150));
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
        panelUserInstruction.setLayout(new BoxLayout(panelUserInstruction, BoxLayout.PAGE_AXIS));
        panelUserInstruction.setBorder(BorderFactory.createTitledBorder("Instructions"));
        panelUserInstruction.setSize(new Dimension(200, 200));
        panelUserInstruction.setPreferredSize(new Dimension(200, 200));
        panelUserInstruction.setMaximumSize(new Dimension(200, 200));
        panelUserInstruction.setMinimumSize(new Dimension(200, 200));
        panelUserInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelCreateNewGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelCreateNewGesture);
        labelAssignAction.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelAssignAction);
        labelRecordGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelRecordGesture);
        labelValidateGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelValidateGesture);
        labelTestGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelTestGesture);
        labelSaveGesture.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelUserInstruction.add(Box.createVerticalGlue());
        panelUserInstruction.add(labelSaveGesture);
        panelUserInstruction.add(Box.createVerticalGlue());
        
        // panels for drawing the gesture
        panelDrawingPanels = new JPanel();
        panelDrawingPanels.setLayout(new BoxLayout(panelDrawingPanels, BoxLayout.LINE_AXIS));
        panelDrawingPanels.setBorder(BorderFactory.createTitledBorder("Gesture"));
        panelDrawingPanels.setSize(new Dimension(700, 350));
        panelDrawingPanels.setPreferredSize(new Dimension(700, 350));
        panelDrawingPanels.setMinimumSize(new Dimension(700, 350));
        panelDrawingPanels.setMaximumSize(new Dimension(700, 350));
        
        for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++) {
            try {
                drawingPanel[i] = new GestureDrawingPanel();
            } catch (IOException e) {
                
            }
        }
        
        panelDrawingPanels.add(Box.createHorizontalGlue());
        for (int i = 0; i < MOVEMENTS_PER_GESTURE; i++) {
            drawingPanel[i].setBorder(BorderFactory.createLineBorder(Color.black));
            drawingPanel[i].setBackground(new Color(255, 255, 255));
            drawingPanel[i].setSize(new Dimension(200, 300));
            drawingPanel[i].setPreferredSize(new Dimension(200, 300));
            drawingPanel[i].setMinimumSize(new Dimension(200, 300));
            drawingPanel[i].setMaximumSize(new Dimension(200, 300));
            
            drawingPanel[i].setAlignmentY(Component.CENTER_ALIGNMENT);
            panelDrawingPanels.add(drawingPanel[i]);
            panelDrawingPanels.add(Box.createHorizontalGlue());
        }
        
        //
        // organizational panels
        //
        
        // left panel
        panelLeft = new JPanel();
        panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.PAGE_AXIS));
        panelLeft.add(Box.createVerticalGlue());
        panelLeft.add(panelUserInstruction);
        panelLeft.add(Box.createVerticalGlue());
        panelLeft.add(panelSunSPOTInfo);
        panelLeft.add(Box.createVerticalGlue());
        
        // right panel
        panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.PAGE_AXIS));
        panelRight.add(Box.createVerticalGlue());
        panelRight.add(panelDrawingPanels);
        panelRight.add(Box.createVerticalGlue());
        //panelRight.add(panelGestureButtons);
        //panelRight.add(Box.createVerticalGlue());
        
        // top panel
        panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.LINE_AXIS));
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(panelLeft);
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(panelRight);
        panelTop.add(Box.createHorizontalGlue());
        
        // bottom panel
        panelBottom = new JPanel();
        panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.LINE_AXIS));
        panelBottom.add(Box.createHorizontalGlue());
        panelBottom.add(panelGestureButtons);
        panelBottom.add(Box.createHorizontalGlue());
        
        // main panel
        panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));
        panelMain.add(Box.createVerticalGlue());
        panelMain.add(panelTop);
        panelMain.add(Box.createVerticalGlue());
        panelMain.add(panelBottom);
        panelMain.add(Box.createVerticalGlue());
        
        //
        // create status bar
        //
        statusBar = new JLabel();
        statusBar.setPreferredSize(new Dimension(100,25));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setText(" Message: Click 'New Gesture' to create a new gesture!");
        getContentPane().add(statusBar,BorderLayout.SOUTH);
        add(panelMain);
        pack();
    }

}

