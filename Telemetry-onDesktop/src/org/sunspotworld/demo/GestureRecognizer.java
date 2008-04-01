/*
 * GestureRecognizer.java
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
public class GestureRecognizer extends Thread{
        private boolean running = true;
        private int index = 0;
        private static int gestureSegmentsIndex = 0;
        private boolean prevTimeStamp = false;
        private final static double TIME_ALLOWANCE = 270; 
        private Vector gestures = new Vector();

        /** Creates a new instance of GestureRecognizer */
    public GestureRecognizer() {
        gestures = new Vector();
    }
    

    public void patternMatching(Gesture currentGesture){
        double sums[] = {0,0,0,0};
        Vector pattern = new Vector();
        Vector dataset = currentGesture.getDataset();
       
      
        
        
        //determine the active axis by calculating the absolute sums of each curve
        //the greatest sum means it has more activity
        for(int i=0; i<dataset.size(); i++){
            sums[0] += Math.abs(((DataStruct)dataset.elementAt(i)).getX());
            sums[1] += Math.abs(((DataStruct)dataset.elementAt(i)).getY());
            //sums[2] += Math.abs(((DataStruct)dataset.elementAt(i)).getZ()-1);
            sums[2] = 0;
            sums[3] += ((DataStruct)dataset.elementAt(i)).getTotalG();
        }
        if (sums[3] > 70)
            currentGesture.setActiveAxis("s");   //shaking
        else if(sums[0]>=sums[1] && sums[0]>=sums[2])
            currentGesture.setActiveAxis("x");
        else if(sums[1]>=sums[0] && sums[1]>=sums[2])
            currentGesture.setActiveAxis("y");
        else
            currentGesture.setActiveAxis("z");
        
        //look at dx
        for(int i=0; i<dataset.size(); i++){
            
            if(i==0)
                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
            
            else if(i>0 && ((SlopeWeight)(pattern.lastElement())).slope == (((DataStruct)dataset.elementAt(i)).getDx()>=0) ){
                ((SlopeWeight)(pattern.lastElement())).weight++;
            }
            else if(i>0 && (((SlopeWeight)(pattern.lastElement())).slope != (((DataStruct)dataset.elementAt(i)).getDx()>=0)) ){
                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
            }
            
        }

        for(int j=0; j<pattern.size(); j++){
            System.out.println(pattern.elementAt(j));
        }
        System.out.println("Current gesture segment's pattern " + pattern);
        currentGesture.setPattern(new Vector(pattern));
        //in fact it should be combined with the previous gesture
        if(gestures.size()>0 && currentGesture.getEndTimeStamp()-((Gesture)gestures.lastElement()).getEndTimeStamp() <= TIME_ALLOWANCE){
            System.out.println("COMBINING current gesture with previous");
            ((Gesture)gestures.lastElement()).combine(currentGesture);
            System.out.println("Combined gesture's pattern = " + ((Gesture)gestures.lastElement()).getPattern());
        }
        else{
            gestures.addElement(currentGesture);
        }
        
        System.out.println("Total number of gestures = " + gestures.size());
        System.out.println((Gesture)gestures.lastElement());
        if(((Gesture)gestures.lastElement()).getActiveAxis().equals("x"))
            System.out.println(leftOrRight((Gesture)gestures.lastElement()));
        else if(((Gesture)gestures.lastElement()).getActiveAxis().equals("y"))
            System.out.println(forwardOrBackward((Gesture)gestures.lastElement()));
        else if(((Gesture)gestures.lastElement()).getActiveAxis().equals("s"))
            System.out.println("Shake");
        
        
//        Gesture tempGesture = (Gesture)gestures.lastElement();
//        Vector tempPattern = tempGesture.getPattern();
//        Vector simplifiedPattern = new Vector();
//        int maxWeight = 0;
//        int maxIndex = -1;
//        int limitWeight = 1000;
//        int currentWeight;
//        //take the max 3 elements in the pattern
//        for(int k=0; k<3; k++){
//            for(int i=0; i<pattern.size(); i++){
//                currentWeight = ((SlopeWeight)tempPattern.elementAt(i)).weight;
//               if(currentWeight > maxWeight && currentWeight <= limitWeight && i != maxIndex){
//                   maxIndex = i;
//                   maxWeight = currentWeight;
//               }
//            }
//            if(maxIndex>=0)
//                simplifiedPattern.addElement(tempPattern.elementAt(maxIndex));
//            limitWeight = maxWeight;
//            maxWeight = 0;
//            maxIndex = -1;
//        }
        
            

    }
    
    
    public String leftOrRight(Gesture g){
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
            return "Left";
        else
            return "Right";
        
    }
       public String forwardOrBackward(Gesture g){
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
            return "Backward";
        else
            return "Forward";
        
    }
    
    public void recognizer() throws InterruptedException{
       Gesture currentGesture = new Gesture();
       Global.gestureSegmentsLock.writeLock().lock();
        
        try{
          if(gestureSegmentsIndex >= Global.gestureSegments.size())
              Global.gestureSegmentsCondition.await();
        
          if(gestureSegmentsIndex < Global.gestureSegments.size()){
              Vector dataset = (Vector)Global.gestureSegments.elementAt(gestureSegmentsIndex);
              //get the timestamp of the last data point in the set
              double timeStamp = ((DataStruct)(((Vector)(((Vector)Global.gestureSegments).lastElement())).lastElement())).getTimeStamp();
              currentGesture = new Gesture(dataset, timeStamp);
              //check timeStamp of the previous gesture to see if they are actually one single gesture
           
                  //do some checking to see if it should be combined with the previous gesture segment
              gestureSegmentsIndex++;
          }
              
          
        }
        finally{
            Global.gestureSegmentsLock.writeLock().unlock();
        }
        patternMatching(currentGesture);
        
    }
    
    
    
    public void clear() {
        gestures.removeAllElements();
        // clear private data
    }
    public void run() {
            System.out.println("Gesture Recognizer Thread started ...");
            hostLoop();
        }
    
    //main loop
    public void hostLoop(){
        running = true;
        while(running)
        try{
            recognizer();
        }
        catch(Exception e){
        System.out.println(e);
        }
    }
    public void doQuit(){
        running = false;
    }
}

