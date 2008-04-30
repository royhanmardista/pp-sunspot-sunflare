/*
 * BasicGestureRecognizer.java
 *
 * Created on March 27, 2008, 11:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.server;
import java.util.Vector;
import java.util.concurrent.locks.*;

/**
 *
 * @author Winnie
 */
public class BasicGestureRecognizer extends Thread{
    private boolean running = true;
    private int index = 0;
    private static int gestureSegmentsIndex = 0;
    private boolean prevTimeStamp = false;
    private final static double TIME_ALLOWANCE_BTW_BASIC_GESTURE_SEGMENTS = 270;
    private boolean debug = false;
    
    /** Empty Constructor. Creates a new instance of BasicGestureRecognizer */
    public BasicGestureRecognizer() {

    }
    /** Prints out debugging statements
     * @param The debugging string
     */
    private void debug(String s){
        if(debug)
            System.out.println("BasicGestureRecognizer: " + s);
    }
    /** Adds parameter to Global.basicGestures
     * @param currentGesture BasicGesture that is being added
     */
    public void analyze(BasicGesture currentGesture){
        Vector pattern = new Vector();
        Vector dataset = currentGesture.getDataset();

        //NOTE: the following chunk of code analyzing slope pattern is not useless (at least for now)
        
        //look at dx
//        for(int i=0; i<dataset.size(); i++){
//            
//         if(i==0)
//                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
//            
//            else if(i>0 && ((SlopeWeight)(pattern.lastElement())).slope == (((DataStruct)dataset.elementAt(i)).getDx()>=0) ){
//                ((SlopeWeight)(pattern.lastElement())).weight++;
//            } else if(i>0 && (((SlopeWeight)(pattern.lastElement())).slope != (((DataStruct)dataset.elementAt(i)).getDx()>=0)) ){
//                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
//            }
//            
//        }
//        
//
//        currentGesture.setPattern(new Vector(pattern));
       
        
        //end unused code
        
        Global.basicGesturesLock.writeLock().lock();
        try{
        //checks to see if it should be combined with the previous gesture
        if(Global.basicGestures.size()>0 && currentGesture.getEndTimeStamp()-((BasicGesture)Global.basicGestures.lastElement()).getEndTimeStamp() <= TIME_ALLOWANCE_BTW_BASIC_GESTURE_SEGMENTS){
            debug("COMBINING current gesture with previous");
            ((BasicGesture)Global.basicGestures.lastElement()).combine(currentGesture);
         //   debug("Combined gesture's pattern = " + ((BasicGesture)Global.basicGestures.lastElement()).getPattern());
             calculateActiveAxis((BasicGesture)Global.basicGestures.lastElement());
        } else{
            Global.basicGestures.addElement(currentGesture);
            calculateActiveAxis(currentGesture);
        }
        
        debug("Total number of basicGestures = " + Global.basicGestures.size());

        }
        finally{
            Global.basicGesturesLock.writeLock().unlock();
        }
        
    }
    /** Sets the active axis for the given BasicGesture
     * @param g The BasicGesture that you want to set active axis
     */
    
    public void calculateActiveAxis(BasicGesture g){
        double sums[] = {0,0,0,0};
        
        Vector dataset = g.getDataset();
        
        //determine the active axis by calculating the absolute sums of each curve
        //the greatest sum means it has more activity
        for(int i=0; i<dataset.size(); i++){
            sums[0] += Math.abs(((DataStruct)dataset.elementAt(i)).getX());
            sums[1] += Math.abs(((DataStruct)dataset.elementAt(i)).getY());
            sums[2] += Math.abs(((DataStruct)dataset.elementAt(i)).getZ());
            //sums[2] = 0;
            sums[3] += ((DataStruct)dataset.elementAt(i)).getTotalG();
        }
        if (sums[3] > 80)
            g.setActiveAxis("s");   //shaking
        else if(sums[0]>=sums[1] && sums[0]>=sums[2])
            g.setActiveAxis("x");
        else if(sums[1]>=sums[0] && sums[1]>=sums[2])
            g.setActiveAxis("y");
        else
            g.setActiveAxis("z");
        //debug("sums: " + sums[0] + " " + sums[1] + " " + sums[2]);
    }
   
   /**  Takes a the Global.gestureSegments vector and converts it to a BasicGesture which may or may not be a complete BasicGesture
    */
    
    public void recognizer() throws InterruptedException{
        BasicGesture currentGesture = new BasicGesture();
        Global.gestureSegmentsLock.writeLock().lock();
        
        try{
            if(gestureSegmentsIndex >= Global.gestureSegments.size())
                Global.gestureSegmentsCondition.await();
            
            if(gestureSegmentsIndex < Global.gestureSegments.size()){
                Vector dataset = (Vector)Global.gestureSegments.elementAt(gestureSegmentsIndex);
                //get the timestamp of the last data point in the set
                double timeStamp = ((DataStruct)(((Vector)(((Vector)Global.gestureSegments).lastElement())).lastElement())).getTimeStamp();
                currentGesture = new BasicGesture(dataset, timeStamp);
                gestureSegmentsIndex++;
            }
            
            
        } finally{
            Global.gestureSegmentsLock.writeLock().unlock();
        }
        analyze(currentGesture); //see if this BasicGesture needs to be combined to the previous one
        
    }
    
    /** Clears private data
     */
    
    public void clear() {
        Global.basicGesturesLock.writeLock().lock();
        try{
            Global.basicGestures.removeAllElements();
        }
        finally{
            Global.basicGesturesLock.writeLock().unlock();
        }
        
        // clear private data
    }
    /** Runs the thread
     */
    
    public void run() {
        System.out.println("BasicGesture Recognizer Thread started ...");
        hostLoop();
    }

    public int getGestureSegmentsIndex() {
        return gestureSegmentsIndex;
    }
    
    /** Main loop
     */
    public void hostLoop(){
        running = true;
        while(running)
            try{
                recognizer();
            } catch(Exception e){
                debug(e.toString());
            }
    }
    /** Kills thread
     */
    public void doQuit(){
        running = false;
    }
}

