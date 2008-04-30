package sunflare.examples.OrbitGame.src.orbit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class WinPage extends JPanel implements MouseListener, KeyListener
{	
	ImageIcon blackB1 = new ImageIcon("media/blackB1.jpg");
	ImageIcon blackB2 = new ImageIcon("media/blackB2.jpg");
	ImageIcon blackB3 = new ImageIcon("media/blackB3.jpg");
	ImageIcon blackB4 = new ImageIcon("media/blackB4.jpg");
	ImageIcon blackB5 = new ImageIcon("media/blackB5.jpg");
	Game game;
	SunflareListener sunflareListener = new SunflareListener();
	
	BinaryInput binaryInput=new BinaryInput();
	int prompt = 1; // set prompt for first page
	
	
	public WinPage(Game  game)
	{
		this.game = game;
		addMouseListener(this);
		addKeyListener(this);
	}
	public void setBinaryInput(BinaryInput binIn)
	{
		binaryInput=binIn;
		sunflareListener.setBinaryInput(binIn);
	}
	public SunflareListener getSunflareListener(){
		return sunflareListener;
	}
	/** Override the paint
	 *
	 **/
	public void paintComponent(Graphics g1)
	{
		super.paintComponent(g1);
		Graphics2D g=(Graphics2D)g1;
		if (prompt==1){
			g1.drawImage(blackB1.getImage(),0,0,800,600,null);	
			prompt=2;
		}	
		else if (prompt==2){
			g1.drawImage(blackB2.getImage(),0,0,800,600,null);	
			prompt=3;
		}
		else if (prompt==3){
			g1.drawImage(blackB3.getImage(),0,0,800,600,null);	
			prompt=4;
		}
		else if (prompt==4){
			g1.drawImage(blackB4.getImage(),0,0,800,600,null);	
			prompt=5;
		}
		else if (prompt==5){
			g1.drawImage(blackB5.getImage(),0,0,800,600,null);	
			prompt=6;
		}
		else if (prompt==6){
			g1.drawImage(blackB4.getImage(),0,0,800,600,null);	
			prompt=7;
		}
		else if (prompt==7){
			g1.drawImage(blackB3.getImage(),0,0,800,600,null);	
			prompt=8;
		}
		else { //prompt==8
			g1.drawImage(blackB2.getImage(),0,0,800,600,null);	
			prompt=1;
		}
	}
	/** Draw an individual SpaceObject.
	 *
	 **/
	
	///Whenever an event happens, update the binary input
	
	public void mousePressed(MouseEvent e)
	{
		if(binaryInput!=null)
			binaryInput.buttonChanged(true);
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
		
		if(binaryInput!=null)
			binaryInput.buttonChanged(true);
	}
	public void keyReleased(KeyEvent e)
	{
		if(binaryInput!=null)
			binaryInput.buttonChanged(false);
	}
	public void keyTyped(KeyEvent e){}
	
}