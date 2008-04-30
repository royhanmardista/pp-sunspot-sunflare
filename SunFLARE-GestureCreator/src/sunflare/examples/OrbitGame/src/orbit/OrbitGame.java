package sunflare.examples.OrbitGame.src.orbit;

import javax.swing.*;

import sunflare.service.SunFlareService;

import java.util.*;

public class OrbitGame
{
	public static void main(String[] args) throws Exception
	{
		
		Game game = new Game();
		//SunFlare.setGestureListener(game.getGestureListener());
                SunFlareService SunFlare = new SunFlareService(game.getSunflareListener());
                
		game.start();
		while (true) {
                    SunFlare.doSendData(true);
                }
	}
}