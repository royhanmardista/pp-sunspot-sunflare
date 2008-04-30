package sunflare.examples.OrbitGame.src.orbit;

/*
 * An Asteroid models an asteroid. We think of it as a small planet.
 */
public class Asteroid extends Planet {
	
	/*
	 * Represents the mass of the asteroid. This affects the gravitational force that the asteroid exerts
	 * on the spaceship and other asteroids.
	 */
	private double mass;

	public Asteroid(Vector2 pos, Vector2 vel, Vector2 acc, String sprite, double mass, double radius) {
		super(pos,vel,acc,sprite,radius*2,radius*2);
		this.mass = mass;
		this.radius = radius;
		alive = true;
		setAnimationProperties(20, 25, true);
	}

	public double getMass() {
		return mass;
	}

	/*
	 * Overriden update function of Planet. An asteroid differs from a planet in the sense that it actually moves
	 * this game. It's essentially a small planet that moves.
	 */
	public void update(long timeElapsed) {
		//System.out.println("Velocity: "+vel+" Position: "+pos);
		pos = pos.addVector(vel.scale((double)timeElapsed*0.001));
	}
}
