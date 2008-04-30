/*
 * Controller.java
 *
 * Created on April 24, 2008, 12:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.server;
import java.util.*;
import sunflare.plugin.PluginRef;
/**
 * This controller serves as the middle layer between the GUI and the backend
 * The GUI can call the methods in this class to alter system states
 * The controller thread determines the operations that need to be done according to the system state
 * @author Winnie
 */
public class Controller extends Thread{
    private boolean running = false;
    private static boolean debug = true;
    private Gesture recordedGesture = new Gesture();
    private int previousState = Global.SYS_IDLE;
    private PluginRef targetPluginRef = new PluginRef();
    /** Creates a new instance of Controller */
    public Controller() {
    }
    /** Prints debugging statements
     *  @param s Debugging string
     */
    private void debug(String s){
        if(debug)
            System.out.println("Controller: "+s);
    }
    /** The states handler that takes care of system states
     */
    public void runStateMachine() throws Exception{
        Global.systemStateLock.writeLock().lock();
        try{
            if(Global.systemState == Global.SYS_RECORDING_MODE){
                Global.gesturesLock.writeLock().lock();
                try{
                    //always get the most recent Gesture made
                    if(!Global.gestures.isEmpty())
                        recordedGesture = (Gesture)Global.gestures.lastElement();
                }finally{
                    Global.gesturesLock.writeLock().unlock();
                }
            } else if(Global.systemState == Global.SYS_GESTURE_RECORDED){
                //test if the recorded gesture already exists in db
              //  if(recordedGesture!=null && recordedGesture.getBasicGestures().size())
                recordedGesture.setPlugin(targetPluginRef);
                Global.gestureDBLock.writeLock().lock();
                try{
                    if(Global.gestureDB.gestureExists(recordedGesture)){
                        //notify GUI that the gesture cannot be accepted
                        debug("recorded gesture is"+recordedGesture);
                        Global.mainWindow.validationResults(false);
                        Global.systemState = Global.SYS_IDLE;
                    } else {
                        Global.mainWindow.validationResults(true);
                        Global.systemState = Global.SYS_IDLE;
                    }
                }finally{
                    Global.gestureDBLock.writeLock().unlock();
                }
            } else if(Global.systemState == Global.SYS_TEST_GESTURE){
                Global.gesturesLock.writeLock().lock();
                try{
                    if(!Global.gestures.isEmpty()){
                        if(((Gesture)(Global.gestures.firstElement())).isScanned()){
                            if(((Gesture)(Global.gestures.firstElement())).sameMovements(recordedGesture)){
                                //notify GUI that the test is successful
                                //the GUI is responsible for calling gestureTestedState() to change the system state
                                Global.systemState = Global.SYS_IDLE;
                                Global.mainWindow.endTest(true);
                            } else{
                                Global.mainWindow.endTest(false);
                                //debug("");//notify GUI that the test failed
                                Global.systemState = Global.SYS_IDLE;
                            }
                        }
                    }
                    //Controller is not responsible for the 'Cancel' case
                    //GUI needs to handle it and call the appropriate methods to alter system state
                }finally{
                    Global.gesturesLock.writeLock().unlock();
                }
            } else if(Global.systemState == Global.SYS_SAVE_GESTURE){
                Global.gestureDBLock.writeLock().lock();
                try{
                    Global.gestureDB.addGesture(recordedGesture);
                }finally{
                    Global.gestureDBLock.writeLock().unlock();
                }
                Global.systemState = Global.SYS_IDLE;
                Global.mainWindow.updateStatusBar("Gesture saved. Click on \"New Gesture\" to create another gesture");
            }
            
        }finally{
            Global.systemStateLock.writeLock().unlock();
        }
    }
    
    
    /** Clears private data*/
    public void clear() {
        // clear private data
    }
    /** Runs thread*/
    public void run() {
        System.out.println("Controller Thread started ...");
        hostLoop();
    }
    /** Main loop*/
    public void hostLoop(){
        running = true;
        while(running)
            try{
                runStateMachine();
            } catch(Exception e){
                debug(e.toString());
                running = false;
            }
    }
    /** Kills thread*/
    public void doQuit(){
        running = false;
    }
    
    
    
    //Method calls that let GestureCreatorGUI alter
    //the state of the system
    
    /** Changes system state to the desired state
     *  @param state New state
     */
    private void changeSystemState(int state){
        Global.systemStateLock.writeLock().lock();
        try{
            previousState = Global.systemState; //first save the previous state
            Global.systemState = state; //go to the new state
        }finally{
            Global.systemStateLock.writeLock().unlock();
        }
    }
    /** Changes system state to new gesture state
     */
    public void newGestureState(){
        //clear recordedgesture
        changeSystemState(Global.SYS_NEW_GESTURE);
        recordedGesture = new Gesture();
    }
    /** Clears the global gestures vector
     */
    private void clearGesturesVector(){
        //remove global gestures vector
        Global.gesturesLock.writeLock().lock();
        try{
            Global.gestures.removeAllElements();
        }finally{
            Global.gesturesLock.writeLock().unlock();
        }
    }
    
    /** Assign action state
     * @return a vector of all pluginrefs in pluginDB*/
    public Vector assignActionState(){
        changeSystemState(Global.SYS_ASSIGN_ACTION);
        Vector allPlugins;
        Global.pluginLayerLock.writeLock().lock();
        try{
            allPlugins = Global.pluginLayer.getAllPluginRefs();
        }finally{
            Global.pluginLayerLock.writeLock().unlock();
        }
        return allPlugins;
    }
    
    /** Action selected state
     * @param p PluginReference that the new gesture belongs to
     */
    public void actionSelectedState(PluginRef p){
        changeSystemState(Global.SYS_ACTION_SELECTED);
        targetPluginRef = p;
    }
    
    /** Gesture recording state
     */
    public void gestureRecordingState(){
        changeSystemState(Global.SYS_RECORDING_MODE);
        clearGesturesVector();
        recordedGesture = new Gesture();
    }
    /** Gesture recorded state
     */
    public void gestureRecordedState(){
        changeSystemState(Global.SYS_GESTURE_RECORDED);
        clearGesturesVector();
    }
    /** Testing Gesture state
     */
    public void testingGestureState(){
        clearGesturesVector();
        changeSystemState(Global.SYS_TEST_GESTURE);
    }
    /** Gesture tested state
     */
    public void gestureTestedState(){
        changeSystemState(Global.SYS_GESTURE_TESTED);
        clearGesturesVector();
    }
    /** Gesture saved state
     */
    public void gestureSavedState(){
        changeSystemState(Global.SYS_GESTURE_SAVED);
    }
    /** Save Gesture state
     */
    public void saveGestureState(){
        changeSystemState(Global.SYS_SAVE_GESTURE);
    }
    /** System idle state
     */
    public void systemIdle(){
        changeSystemState(Global.SYS_IDLE);
    }
    /** Revert to previous state
     */
    public void revertToPreviousState(){
        changeSystemState(previousState);
    }
    
}
