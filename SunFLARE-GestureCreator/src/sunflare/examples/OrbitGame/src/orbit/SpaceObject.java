/*
	name: SpaceObject
	description: This is the space object that is the base object for all the
		objects that reside in the world object there are a lot of object names
		in this object.

*/

package sunflare.examples.OrbitGame.src.orbit;

import java.awt.Image;
import javax.swing.ImageIcon;

public class SpaceObject {

	//for mechanics
	protected Vector2 pos;
	protected Vector2 vel;
	protected Vector2 accel;

	protected String sprite;

	//this is used for image drawing and collision detection
	protected  double width;
	protected  double height;

	protected  double radius;

	protected boolean alive;

	public SpaceObject() {
		pos = new Vector2();
		vel = new Vector2();
		accel = new Vector2();
		sprite = new String("null");
		width = height = radius = 0.0;

		currentFrame = 0;
		numFrames = 0;
		elapsedTime = 0;
		timePerFrame = 0;
		looping = false;

		alive = false;
	}

	public SpaceObject(Vector2 p,Vector2 v,Vector2 a,String sprite,double width,double height)
	{
		pos = p;
		vel = v;
		accel = a;
		this.sprite = sprite;
		this.width = width;
		this.height = height;
		this.radius = Math.min(width,height)/2.0;

		alive = false;
	}

	public boolean getAlive() { return alive; }
	public void setAlive(boolean alive) { this.alive = alive; }

	public Vector2 getPos() { return pos; }
	public Vector2 getVel() { return vel; }
	public Vector2 getAccel() { return accel; }
	public String getSprite() { return sprite; }
	public double getWidth() { return width; }
	public double getHeight() { return height; }
	public double getRadius() { return radius; }

	public void setPos(Vector2 p) { pos = p; }
	public void setVel(Vector2 v) { vel = v; }
	public void setAccel(Vector2 a) { accel = a; }

	public Image getFrame()
	{
		//return new ImageIcon("build/media/spaceship.jpg").getImage();
		return ResourceManager.getImage(sprite,currentFrame);
	}

	/*
	 * =======ANIMATION CODE BELOW HERE=====================================
	 */

	protected int currentFrame = 0;
	protected int timePerFrame =0;
	protected int elapsedTime =0;
	protected int numFrames = 0;
	protected boolean looping = false;

	/**
	 * This sets the initial animation properties
	 * @param timePerFrame	the number of milliseconds per frame
	 * @param numFrames		The total number of frames present
	 * @param looping		Set this to true if you want the animation to repeatedly loop
	 */
	public void setAnimationProperties(int timePerFrame,int numFrames,boolean looping) {
		this.timePerFrame = timePerFrame;
		this.numFrames = numFrames;
		this.looping = looping;
		currentFrame = 0;
		elapsedTime = 0;
	}

    public void animate(int msSinceLastTime) {
    	if (!looping) return;

    	elapsedTime+=msSinceLastTime;
    	if(elapsedTime>timePerFrame)
    	{
    		//This code here loops the animation
    		if(currentFrame==numFrames-1) {
            	currentFrame = 0;
    		}
    		else {
    			currentFrame++;
    		}
    		elapsedTime=0; //start counting from 0
    	}
    }

    public void nonLoopingAnimate(int msSinceLastTime) {
    	elapsedTime+=msSinceLastTime;
    	if(elapsedTime>timePerFrame)
    	{
    		//This code here loops the animation
    		if(currentFrame==numFrames-1) {
            	currentFrame = -1; //signifies end
    		}
    		else {
    			currentFrame++;
    		}
    		elapsedTime=0; //start counting from 0
    	}
    }
    
    
    public void setLooping(boolean loop) {
    	looping = loop;
    }

    public void setFrame(int f) {
    	System.out.println("\nFRAME: "+f+"\n");
    	if (f < numFrames && f >= 0)
    		currentFrame = f;
    }

    public void update(long timeElapsed)
    {
    	
    }
}


