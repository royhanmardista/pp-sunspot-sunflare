/*
 * Test.java
 *
 * Created on April 28, 2008, 9:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sunflare.examples;
import sunflare.service.SunFlareService;

/**
 *
 * @author Winnie
 */
public class Test {
    static private SunFlareService service;

    /** Creates a new instance of Test */
    public static void main (String[] args){
        service = new SunFlareService();
        //service.doBlink();
       
 
        while(true){
                    service.doSendData(true);
        }
    }
    
}
