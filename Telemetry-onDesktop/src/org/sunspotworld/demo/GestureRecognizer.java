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
       
        private Vector data = new Vector();
        private Vector gestureSegments = new Vector();
        private Vector gestures = new Vector();
    /** Creates a new instance of GestureRecognizer */
    public GestureRecognizer() {
        
    }
    

    public void patternMatching(Gesture currentGesture){
//        double sums[] = {0,0,0};
        Vector pattern = new Vector();
        Vector dataset = currentGesture.getDataset();
//        Vector derivatives = new Vector();
        //determine the inactive axis
//        
//        for(int i=0; i<dataset.size(); i++){
//            
//            sums[0] += ((DataStruct)dataset.elementAt(i)).getX();
//            sums[1] += ((DataStruct)dataset.elementAt(i)).getY();
//            sums[2] += ((DataStruct)dataset.elementAt(i)).getZ();
//            
//        }
//        if(sums[0]<=sums[1] && sums[0]<=sums[2])
//            currentGesture.setInactiveAxis("x");
//        else if(sums[1]<=sums[0] && sums[1]<=sums[2])
//            currentGesture.setInactiveAxis("y");
//        else
//            currentGesture.setInactiveAxis("z");
//        
        //look at dx
        for(int i=0; i<dataset.size(); i++){
            //System.out.println("*** " + ((DataStruct)dataset.elementAt(i)).getDx());
            if(i==0)
                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
            
            else if(i>1 && ((SlopeWeight)(pattern.lastElement())).slope == (((DataStruct)dataset.elementAt(i)).getDx()>=0) )
                ((SlopeWeight)(pattern.lastElement())).samples++;
            
            else if(i>1 && ((SlopeWeight)(pattern.lastElement())).slope != (((DataStruct)dataset.elementAt(i)).getDx()>=0) )
                pattern.addElement(new SlopeWeight(((DataStruct)dataset.elementAt(i)).getDx()>=0,1));
            
        }
        System.out.println("pattern size = "+pattern.size());
                
                
//        
//        if(currentGesture.getInactiveAxis().equals("x")){
//            ((DataStruct)dataset.elementAt(i)).getDx();        
//        
       
    }
    public void recognizer() throws InterruptedException{
       Gesture currentGesture = new Gesture();
       Global.gestureSegmentsLock.writeLock().lock();
        
        try{
          if(gestureSegmentsIndex >= Global.gestureSegments.size())
              Global.gestureSegmentsCondition.await();
        
          if(gestureSegmentsIndex < Global.gestureSegments.size()){
              Vector dataset = (Vector)Global.gestureSegments.elementAt(gestureSegmentsIndex);
              System.out.println("Processing gesture signal..num gestures = " + Global.numGesturesDetected);
              System.out.println("dataset size "+ dataset.size());
              currentGesture = new Gesture(dataset);
              //check timeStamp of the previous gesture to see if they are actually one single gesture
           
                  //do some checking to see if it should be combined with the previous gesture segment

//              for(int i = 0; i < dataset.size(); i++){
//            
//              }
              gestureSegmentsIndex++;
          }
              
          
        }
        finally{
            Global.gestureSegmentsLock.writeLock().unlock();
        }
        patternMatching(currentGesture);
        
    }
    
    
    
    public void clear() {
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

