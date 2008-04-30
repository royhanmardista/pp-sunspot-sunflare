package sunflare.examples.OrbitGame.src.orbit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class MainPage extends JPanel implements MouseListener, KeyListener
{
	//ImageIcon titlePrompt = new ImageIcon("media/titlePrompt.png");
	ImageIcon sceneT = new ImageIcon("media/sceneT.jpg");
	ImageIcon sceneT2 = new ImageIcon("media/sceneT2.jpg");
	BinaryInput binaryInput=new BinaryInput();
	int prompt = 0; // set prompt for first page
	Game game;

	public MainPage(Game game)
	{
		this.game = game;
		addMouseListener(this);
		addKeyListener(this);
	}
	public void setBinaryInput(BinaryInput binIn)
	{
		binaryInput=binIn;
	}
	/** Override the paint
	 *
	 **/
	public void paintComponent(Graphics g1)
	{
		super.paintComponent(g1);
		Graphics2D g=(Graphics2D)g1;
		if (prompt==1){
			g1.drawImage(sceneT2.getImage(),0,0,400,364,null);
			System.out.println("\nPrompt == 1\n");
			prompt=0;
			try {
				Thread.sleep(230);}
			catch(Exception e){	}
		}
		else{
			g1.drawImage(sceneT.getImage(),0,0,400,364,null);
			System.out.println("\nPrompt == 0\n");
			prompt=1;
			try {
				Thread.sleep(230);}
			catch(Exception e){	}
		}

	}
	/** Draw an individual SpaceObject.
	 *
	 **/

	///Whenever an event happens, update the binary input

	public void mousePressed(MouseEvent e)
	{
		if(binaryInput!=null) {
			binaryInput.buttonChanged(true);
			game.setState(Game.INIT_GAME);
		}
			requestFocus();
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseReleased(MouseEvent e)
	{
		if(binaryInput!=null)
			binaryInput.buttonChanged(false);

	}
	public void mouseClicked(MouseEvent e){}
	public void mouseExited(MouseEvent e)
	{
		if(binaryInput!=null)
			binaryInput.buttonChanged(false);
	}

	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode()==27)///27=esc, 32=space
			System.exit(0);

		if(binaryInput!=null) {
			binaryInput.buttonChanged(true);
			game.setState(Game.INIT_GAME);
		}
	}
	public void keyReleased(KeyEvent e)
	{
		if(binaryInput!=null)
			binaryInput.buttonChanged(false);
	}
	public void keyTyped(KeyEvent e){}
}