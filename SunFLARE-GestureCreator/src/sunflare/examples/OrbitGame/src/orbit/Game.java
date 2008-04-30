package sunflare.examples.OrbitGame.src.orbit;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * This is the main game class the
 * @author Shivani Srivastava, Prateek Tandon, Aadarsh Patel, Candice Zimmerman, Henry Yuen
 *
 */
public class Game implements Runnable{

	public static final int START_SCREEN =0;
	public static final int INIT_GAME = 1;
	public static final int GAME  =2;
	public static final int DIED_SEQUENCE =3;
	public static final int LOSS_SCREEN =4;
	public static final int NEXT_LEVEL =5;
	private JFrame gameFrame;
	private Thread thread;

	private int state;
	private int currentLevel;
	private int points;
	private int lives;

	private long levelSeed;
	private World world;
	private BinaryInput binIn;

	private ScrollingScreen scroll;
	private Rect viewport;
	private Rect screen;

	private MainPage startScreen;
	private WinPage winScreen;



	public Game() throws Exception {
		gameFrame = new JFrame();
		thread=new Thread(this);
		//Rect screen=new Rect(0,0,800,620);
		//Rect screen=new Rect(0,0,400,385);

		screen=new Rect(0,0,800,600);

		gameFrame.setSize((int)screen.width,(int)screen.height);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.setLocation(200,100);

		binIn=new BinaryInput();
		startScreen = new MainPage(this);
		startScreen.setBinaryInput(binIn);
		winScreen = new WinPage(this);
		winScreen.setBinaryInput(binIn);
		viewport=new Rect(0,0,1000,800);

		try {
			loadResources();

		} catch (Exception e) {
			throw new Exception(e);
		}
		gameFrame.setSize(800,600);
		gameFrame.setResizable(false);
		gameFrame.setVisible(true);
		setState(START_SCREEN);
		gameFrame.setContentPane(startScreen);

	}
	public SunflareListener getSunflareListener(){
		
			return winScreen.getSunflareListener();
		
	}

	public long getLevelSeed() {
		return levelSeed;
	}
	public int getNumToBeTagged() {
		return world.getNumTargetsLeft();
	}
	public int getLives() {
		return lives;
	}

	public int getState() {
		return state;
	}
	public void start()
	{
		thread.start();
	}
	public void setState(int state) {
		this.state = state;
		//System.out.println("STATE:" + state);
		/*
		switch(state) {
			case START_SCREEN:
				setStartScreenState();
				return;
			case INIT_GAME:
				setInitGameState();
				return;
			case GAME:
				setGameState();
				return;
			case NEXT_LEVEL:
				setNextLevelState();
				return;
			case DIED_SEQUENCE:
				setDiedSequenceState();
				return;
			case LOSS_SCREEN
				setDiedSequenceState();
				return;
		}*/

	}

	private void loadResources() throws Exception {
		PrintManager.getInstance().setGraphics((Graphics2D)gameFrame.getGraphics());
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/rocketS.png",1,"spaceship");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/star.gif",4,"star");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/planet1.png",1,"planet0");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/planet2.png",1,"planet1");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/planet7.2.png",1,"planet2");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/planet7.split.png",2,"planetTarget2");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/explosion.gif", 18, "explosion");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/smoke.gif", 6, "smoke");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/rocketSthrust.png",1,"spaceshipthrust");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/FinalAsteroid.png", 25, "asteroid");

		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/speedboost.png",1,"speedboost");
		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/extralife.png",1,"extralife");

		ResourceManager.addImageSequence("src/sunflare/examples/OrbitGame/src/orbit/media/arrow.gif", 1, "pointer");


		//add the fonts of the game
		PrintManager.getInstance().addFont("small", new Font("Comic Sans MS",Font.PLAIN,12));
		PrintManager.getInstance().addFont("medium", new Font("Comic Sans MS",Font.PLAIN,16));
		PrintManager.getInstance().addFont("large", new Font("Comic Sans MS",Font.PLAIN,24));
		PrintManager.getInstance().addFont("huge", new Font("Comic Sans MS",Font.PLAIN,48));
	}

	private void updateStartScreenState() {
		//gameFrame.setContentPane(startScreen);
		gameFrame.repaint();
		gameFrame.validate();
	}

	private void setLossScreenState() {
	}
	/**
	 *
	 **/
	private void updateInitGameState() {
		//System.out.println("GAME INITING: ");

		currentLevel = 0;
		points = 0;
		lives = 10;
		viewport=new Rect(0,0,1000,800);

		world = new World(this);

		world.setBinaryInput(binIn);
		world.setViewport(viewport);
		//world.populate(currentLevel);
		scroll = new ScrollingScreen(this,screen,viewport,world);
		scroll.setBinaryInput(binIn);
		gameFrame.setContentPane(scroll);
		//gameFrame.setSize(800,600);

		//MainPage mainP = new MainPage();
		//WinPage winP = new WinPage();

		//BinaryInput binIn = new BinaryInput();

		//mainP.setBinaryInput(binIn);
		//winP.setBinaryInput(binIn);

		//scroll.setBinaryInput(binIn);


		setState(NEXT_LEVEL);
	/*
		for (int i = 0; i<10000; i++){
				for (int j = 0; j<100000; j++){} // busy loop
		}
		while(true){
			setContentPane(mainP);
			repaint();
			validate();
			try {
				Thread.sleep(230);}
			catch(Exception e){	}
		}
		pack();
		validate();
		repaint();

>>>>>>> .r159
		scroll.requestFocus();

		setState(NEXT_LEVEL);*/
	}
	/** Intermediate state that populates world and prepares game for the next level
	 *
	 **/
	private void updateNextLevelState() {
		//System.out.println("next level state");

		++currentLevel;
		levelSeed=System.currentTimeMillis();
		//clear the world of the extant objects, and repopulate
		world.populate(currentLevel);
		//pack();
		//validate();
		//repaint();
		//scroll.requestFocus();
		setState(GAME);
	}
	/** State machine, will continuously run (until esc/windowclose) and update whatever state its in.  States change the game's state by themselves
	 *
	 **/
	public void run()
	{
		long start=System.currentTimeMillis();

		while(true)
		{
			//System.out.println("KO: ");
			long curr=System.currentTimeMillis();
			long millis=curr-start;
			start=curr;
			switch(state) {
				case START_SCREEN:
					updateStartScreenState();
					break;
				case INIT_GAME:
					updateInitGameState();
					break;
				case GAME:
					updateGameState(millis);
					break;
				case NEXT_LEVEL:
					updateNextLevelState();
					break;
				case DIED_SEQUENCE:
					updateDiedSequenceState();
					break;
				case LOSS_SCREEN:
					updateLossScreen();
					break;
			}
			//System.out.println("State: "+state);
			try{
				Thread.sleep(15);
			}catch(Exception e){}
		}
	}
	private void updateLossScreen()
	{
		
		setState(START_SCREEN);
		gameFrame.setContentPane(startScreen);
		
	}
	/** Main game loop - updates world and draws it
	 *
	 **/
	private void updateGameState(long millis) {
		//System.out.println("GAME HERE: ");
		world.update(millis);
		//gameFrame.repaint();
		gameFrame.validate();
		//scroll.validate();
		//scroll.paintComponent(scroll.getGraphics());
		scroll.repaint();
/*
		while(true)
		{
			//System.out.println("KO: ");
			long curr=System.currentTimeMillis();
			long millis=curr-start;
			start=curr;
			world.update(millis);

			//repaint();
			//scroll.repaint();
			gameFrame.validate();
			//scroll.paint(scroll.getGraphics());
			//scroll.repaint();
			//gameFrame.repaint();
			try{
				Thread.sleep(1500);
			}catch(Exception e){}
		}
		*/
	}

	public void incrementLife() {
		++lives;
	}
	/** When you die, this state will update
	 *
	 **/
	private void updateDiedSequenceState() {
		
	//decrement lives

		--lives;
		if (lives < 0) {
			setState(LOSS_SCREEN);
			return;
		}
		Graphics2D g2d = (Graphics2D)gameFrame.getGraphics();
		//run through the died sequence state
		long start=System.currentTimeMillis();
		while(true)
		{
			//wait for the user to press the button
			if (binIn.getButtonState() == 1) {
				break;
			}
			long curr=System.currentTimeMillis();
			long millis=curr-start;
			start=curr;
			world.update(millis);
			gameFrame.repaint();
			try{
				Thread.sleep(15);
			}catch(Exception e){}
		}
		world.populate(currentLevel);
		setState(GAME);
	}
}