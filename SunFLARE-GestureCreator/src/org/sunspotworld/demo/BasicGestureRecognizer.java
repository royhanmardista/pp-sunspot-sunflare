/*
 * BasicGestureRecognizer.java
 *
 * Created on March 27, 2008, 11:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sunspotworld.demo;
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
    private final static double TIME_ALLOWANCE = 270;
    private boolean debug = false;
    
    /** Creates a new instance of BasicGestureRecognizer */
    public BasicGestureRecognizer() {

    }
    public void debug(String s){
        if(debug)
            System.out.println("BasicGestureRecognizer: " + s);
    }
    
    public void analyze(BasicGesture currentGesture){
        Vector pattern = new Vector();
        Vector dataset = currentGesture.getDataset();

        //NOTE: the following chunk of code analyzing slope pattern is not useless (at least for now)
        
        //look at dx
        for(int i=0; i<dataset.size(); i++){
            
         if(i==0)
                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
            
            else if(i>0 && ((SlopeWeight)(pattern.lastElement())).slope == (((DataStruct)dataset.elementAt(i)).getDx()>=0) ){
                ((SlopeWeight)(pattern.lastElement())).weight++;
            } else if(i>0 && (((SlopeWeight)(pattern.lastElement())).slope != (((DataStruct)dataset.elementAt(i)).getDx()>=0)) ){
                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
            }
            
        }
        
//        for(int j=0; j<pattern.size(); j++){
//            System.out.println(pattern.elementAt(j));
//        }
       // System.out.println("Current gesture segment's pattern " + pattern);
        currentGesture.setPattern(new Vector(pattern));
       
        
        //end useless code
        
        Global.basicGesturesLock.writeLock().lock();
        try{
        //in fact it should be combined with the previous gesture
        if(Global.basicGestures.size()>0 && currentGesture.getEndTimeStamp()-((BasicGesture)Global.basicGestures.lastElement()).getEndTimeStamp() <= TIME_ALLOWANCE){
            debug("COMBINING current gesture with previous");
            ((BasicGesture)Global.basicGestures.lastElement()).combine(currentGesture);
         //   System.out.println("Combined gesture's pattern = " + ((BasicGesture)Global.basicGestures.lastElement()).getPattern());
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
        if (sums[3] > 70)
            g.setActiveAxis("s");   //shaking
        else if(sums[0]>=sums[1] && sums[0]>=sums[2])
            g.setActiveAxis("x");
        else if(sums[1]>=sums[0] && sums[1]>=sums[2])
            g.setActiveAxis("y");
        else
            g.setActiveAxis("z");
        //System.out.println("sums: " + sums[0] + " " + sums[1] + " " + sums[2]);
    }
   
    
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
        analyze(currentGesture);
        
    }
    
    
    
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
    public void run() {
        System.out.println("BasicGesture Recognizer Thread started ...");
        hostLoop();
    }
    
    //main loop
    public void hostLoop(){
        running = true;
        while(running)
            try{
                recognizer();
            } catch(Exception e){
                System.out.println(e);
            }
    }
    public void doQuit(){
        running = false;
    }
}

