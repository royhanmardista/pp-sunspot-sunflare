package sunflare.examples.OrbitGame.src.orbit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class MiniMap
{
	private World world;
	private Rect viewport;
	private Rect miniScreen;
	private Rect playerView;
	private ScreenOverlay overlay;
	private double objectScale;
	
	public MiniMap(Rect screen,Rect view,Rect realView,World world)
	{
		this.world=world;
		this.viewport=view;
		this.miniScreen=screen;
		this.playerView=realView;
		objectScale=2.0;
	}
	public void paintComponent(Graphics2D g)
	{
		Composite comp = g.getComposite();

		//draw a rectangle
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.50f));

		g.setColor(Color.blue);
		g.fillRect((int)miniScreen.left,(int)miniScreen.top,(int)miniScreen.width,(int)miniScreen.height);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.90f));
		try
		{	
			ArrayList<SpaceObject> objs=world.getSpaceObjects();
			synchronized(objs)
			{
				for(SpaceObject so:objs)
				{
					drawObject(g,so);
				}
			}
		}catch(ConcurrentModificationException e){}
		Vector2 corner0=worldToScreen(playerView.left,playerView.top),corner1=worldToScreen(playerView.right,playerView.top),
			corner3=worldToScreen(playerView.left,playerView.bottom),corner2=worldToScreen(playerView.right,playerView.bottom);
		
		g.setColor(Color.green);
		g.drawLine((int)corner0.x,(int)corner0.y,(int)corner1.x,(int)corner1.y);
		g.drawLine((int)corner2.x,(int)corner2.y,(int)corner1.x,(int)corner1.y);
		g.drawLine((int)corner2.x,(int)corner2.y,(int)corner3.x,(int)corner3.y);
		g.drawLine((int)corner0.x,(int)corner0.y,(int)corner3.x,(int)corner3.y);
		
		g.setComposite(comp);
	}
	public void centerViewportAbout(Vector2 pos)
	{
		viewport.setCenter(pos);
	}
	private void drawObject(Graphics2D g,SpaceObject so)
	{
		g.setColor(Color.red);
		double diameter=so.getRadius()*0.06;
		if(so instanceof Spaceship)
		{
			g.setColor(Color.green);
			diameter=7;
		}
		if(so instanceof Asteroid)
		{
			g.setColor(Color.magenta);
			diameter=5;
		}
		if(so instanceof Powerup)
		{
			g.setColor(Color.green);
			diameter=5;
		}
		if(so instanceof SpecialPlanet)
		{
			g.setColor(Color.yellow);
			diameter=12;
		}
		Vector2 screenPos=worldToScreen(so.getPos());
		if(screenPos.x-diameter/2<miniScreen.left||screenPos.x+diameter/2>miniScreen.right)
			return;
		if(screenPos.y-diameter/2<miniScreen.top||screenPos.y+diameter/2>miniScreen.bottom)
			return;
		g.fillOval((int)(screenPos.x-diameter/2),(int)(screenPos.y-diameter/2),(int)diameter,(int)diameter);
	}
	private Vector2 worldToScreen(Vector2 pos)
	{
		return new Vector2(ScrollingScreen.transformSingleDimension(pos.x,viewport.left,viewport.right,miniScreen.left,miniScreen.right),
			ScrollingScreen.transformSingleDimension(pos.y,viewport.top,viewport.bottom,miniScreen.top,miniScreen.bottom));
	}
	private Vector2 worldToScreen(double x,double y)
	{
		return worldToScreen(new Vector2(x,y));
	}
}