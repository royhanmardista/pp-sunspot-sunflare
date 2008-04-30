package sunflare.examples.OrbitGame.src.orbit;
import sun.audio.*;    //import the sun.audio package
import java.io.*;
import java.applet.*;
import java.util.*;
public class SoundManager {
private static HashMap<String, AudioStream> soundTrackVector;
private java.applet.AudioClip winclip;
public SoundManager(){
	// Open an input stream  to the audio file.
	try{
	
		winclip=java.applet.Applet.newAudioClip(new java.net.URL("file:media/victory.wav"));
		winclip.loop();
	}
	catch(Exception e){
		System.out.println("Error in inputting from file");
	}
}
	
}
