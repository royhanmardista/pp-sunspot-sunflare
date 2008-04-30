package sunflare.examples.OrbitGame.src.orbit;

import java.awt.geom.*;
import java.awt.*;

public class ScreenOverlay {

	Color color;
	int width,height;
	Rect area;
	Graphics2D graphics;

	public ScreenOverlay(Graphics2D g,Color c,int width,int height) {
		graphics = g;
		color = c;
		this.width = (int)width;
		this.height = (int)height;
		area=new Rect(0,0,width,height);
	}
	public ScreenOverlay(Graphics2D g,Color c,Rect area) {
		graphics = g;
		color = c;
		this.area=area;
		this.width=(int)area.width;
		this.height=(int)area.height;
	}
	public void paint(float alpha) {
		//Composite comp = graphics.getComposite();
		//graphics.setComposite(
		//		AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		
		graphics.setColor(color);
		graphics.fillRect((int)area.left,(int)area.top,(int)area.width,(int)area.height);
		//graphics.setComposite(comp);
		System.out.println("Paint minimap "+area);
	}
	public void paint(Graphics2D g,float alpha) {
		//Composite comp = graphics.getComposite();
		//graphics.setComposite(
		//		AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		
		g.setColor(color);
		g.fillRect((int)area.left,(int)area.top,(int)area.width,(int)area.height);
		//graphics.setComposite(comp);
		System.out.println("Paint minimap "+area);
	}
	

}
