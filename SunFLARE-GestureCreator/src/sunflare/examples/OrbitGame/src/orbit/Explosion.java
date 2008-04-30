package sunflare.examples.OrbitGame.src.orbit;

/*
 * Models an explosion. An explosion is a space object that can animate but dies after it ends
 * its animation life.
 */
public class Explosion extends SpaceObject{

	public Explosion(Vector2 p, String sprite, double width, double height) {
		super(p, new Vector2(0,0), new Vector2(0,0), sprite, width, height);
		setAnimationProperties(20, 18, true);
		alive = true;
	}
	
	public void animate(int msSinceLastTime) {
		if(currentFrame!=-1) {
			super.nonLoopingAnimate(msSinceLastTime);
		} else {
			looping=false;
			alive = false;
		}
	}
}
