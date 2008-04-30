package sunflare.examples.OrbitGame.src.orbit;

import java.util.*;

/**
 * The spaceship object. The protagonist of the story. The hero of the game.
 * Awesomage.
 * @author Henry Yuen
 *
 */
public class Spaceship extends SpaceObject {

	private boolean thrusting;
	private long thrustAnim;
	private static final double thrustSize = 80;

	private double health;
	private long healthWait;

	public Spaceship() {
		thrusting = false;
	}
	public Spaceship(Vector2 p,Vector2 v,Vector2 a,String sprite,double width,double height) {
		super(p,v,a,sprite,width,height);
		thrustAnim = 0;
		healthWait = 0;
	}

	/**
	 * This determines the angle at which the angle is pointed, based
	 * on its velocity vector.
	 * @return angle at which the spaceship is pointed
	 */
	public double getAngle() {
		return Math.atan2(vel.y, vel.x);
	}

	/**
	 * Interacts the spaceship with the list of planets that the main game loop
	 * provides (these are the planets within a certain range).
	 * @param planets
	 */
	public void interact(Planet p) {

		//do the gravity calculations
		//get the distance
		Vector2 R = p.getPos().subVector(pos);
		double dist = R.getLength()-p.getRadius();
		R = R.getNormalized();
		Vector2 v;
		double radius = p.getRadius();
		double G=240;
		if (dist > radius) {
			v = R.scale(p.getMass()/(dist*dist)*G);
		} else {
			v = R.scale(p.getMass() * dist/(radius*radius*radius)*G);
		}

		accel = accel.addVector(v);

	}

	/**
	 * This actually moves the spaceship. If the spaceship is thrusting
	 * there will be an extra large acceleration in the direction that the
	 * spaceship is moving.
	 */
	public void update(long timeElapsed) {
		Vector2 a = accel;

		if (thrusting) {
			thrustAnim += timeElapsed;
			if (thrustAnim > 200) {
				thrustAnim = 0;
			}

			double angle = getAngle();
			Vector2 thrust = new Vector2(Math.cos(angle),Math.sin(angle));
			thrust = thrust.scale(thrustSize);

			a=a.addVector(thrust);
			sprite=thrustAnim<100?"spaceship":"spaceshipthrust";
		}
		else
			sprite="spaceship";

		vel = vel.addVector(a.scale((double)timeElapsed*0.007));
		//System.out.println("Velocity: "+vel+" Position: "+pos);
		pos = pos.addVector(vel.scale((double)timeElapsed*0.001));
		vel=vel.scale(0.995);
		accel.x = accel.y = 0;

		//update the health
		if (health < 100.0) {
			healthWait += timeElapsed;
			if (healthWait > 100) {
				health += 0.5;
				healthWait = 0;
			}
		}

	}

	public double getHealth() {
		return health;
	}

	public void takeDamage(double damage) {
		health -= damage;
	}

	public void setHealth(double h) {
		health = h;
	}

	public Vector2 predictAccel() {
		Vector2 a = new Vector2(accel.x,accel.y);

		if (thrusting) {
			double angle = getAngle();
			Vector2 thrust = new Vector2(Math.cos(angle),Math.sin(angle));
			thrust = thrust.scale(thrustSize);

			a=a.addVector(thrust);
		}

		return a;
	}
	public Vector2 predictVel(long timeElapsed,Vector2 a) {
		Vector2 v = new Vector2(vel.x,vel.y);
		v = v.addVector(a.scale((double)timeElapsed*0.007));
		return v;
	}

	public Vector2 predictPos(long timeElapsed,Vector2 v) {
		Vector2 p = new Vector2(pos.x,pos.y);
		p = p.addVector(v.scale((double)timeElapsed*0.001));
		return p;
	}

	public void setThrusting(boolean t) {
		thrusting = t;
	}
	public boolean isThrusting()
	{
		return thrusting;
	}

}
