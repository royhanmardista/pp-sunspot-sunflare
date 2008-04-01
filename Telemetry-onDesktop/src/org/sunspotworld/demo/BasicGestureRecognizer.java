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
    private Vector basicGestures = new Vector();
    
    /** Creates a new instance of BasicGestureRecognizer */
    public BasicGestureRecognizer() {
        basicGestures = new Vector();
    }
    
    
    public void patternMatching(BasicGesture currentGesture){
        Vector pattern = new Vector();
        Vector dataset = currentGesture.getDataset();

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
       
        //in fact it should be combined with the previous gesture
        if(basicGestures.size()>0 && currentGesture.getEndTimeStamp()-((BasicGesture)basicGestures.lastElement()).getEndTimeStamp() <= TIME_ALLOWANCE){
            System.out.println("COMBINING current gesture with previous");
            ((BasicGesture)basicGestures.lastElement()).combine(currentGesture);
         //   System.out.println("Combined gesture's pattern = " + ((BasicGesture)basicGestures.lastElement()).getPattern());
             calculateActiveAxis((BasicGesture)basicGestures.lastElement());
        } else{
            basicGestures.addElement(currentGesture);
            calculateActiveAxis(currentGesture);
        }
        
        System.out.println("Total number of basicGestures = " + basicGestures.size());
       // System.out.println((BasicGesture)basicGestures.lastElement());
        
        recognizeBasicGesture(((BasicGesture)basicGestures.lastElement()));
        
        
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
    
    public void leftOrRight(BasicGesture g){
        Vector dataset = g.getDataset();
        double max = -1000, min = 1000;
        int maxIndex = -1, minIndex = -1;
        for(int i=0; i<dataset.size(); i++){
            double currX = ((DataStruct)dataset.elementAt(i)).getX();
            if(currX > max){
                max = currX;
                maxIndex = i;
            }
            if(currX < min){
                min = currX;
                minIndex = i;
            }
        }
        if(minIndex > maxIndex)
            System.out.println("Left");
        else
            System.out.println("Right");
        
    }
    public void forwardOrBackward(BasicGesture g){
        Vector dataset = g.getDataset();
        double max = -1000, min = 1000;
        int maxIndex = -1, minIndex = -1;
        for(int i=0; i<dataset.size(); i++){
            double currY = ((DataStruct)dataset.elementAt(i)).getY();
            if(currY > max){
                max = currY;
                maxIndex = i;
            }
            if(currY < min){
                min = currY;
                minIndex = i;
            }
        }
        if(minIndex > maxIndex)
            System.out.println("Backward");
        else
            System.out.println("Forward");
        
    }
    public void upOrDown(BasicGesture g){
        Vector dataset = g.getDataset();
        double max = -1000, min = 1000;
        int maxIndex = -1, minIndex = -1;
        for(int i=0; i<dataset.size(); i++){
            double currZ = ((DataStruct)dataset.elementAt(i)).getZ();
            if(currZ > max){
                max = currZ;
                maxIndex = i;
            }
            if(currZ < min){
                min = currZ;
                minIndex = i;
            }
        }
        if(minIndex > maxIndex)
            System.out.println("Up");
        else
            System.out.println("Down");
        System.out.println("minT/" + ((DataStruct)dataset.elementAt(minIndex)).getTimeStamp() + " maxT/" + ((DataStruct)dataset.elementAt(maxIndex)).getTimeStamp());
    }
    public void recognizeBasicGesture(BasicGesture g){
        if(g.getActiveAxis().equals("x"))
            leftOrRight(g);
        else if(g.getActiveAxis().equals("y"))
            forwardOrBackward(g);
        else if(g.getActiveAxis().equals("z"))
            upOrDown(g);
        else if(g.getActiveAxis().equals("s"))
            System.out.println("Shake");
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
        patternMatching(currentGesture);
        
    }
    
    
    
    public void clear() {
        basicGestures.removeAllElements();
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

