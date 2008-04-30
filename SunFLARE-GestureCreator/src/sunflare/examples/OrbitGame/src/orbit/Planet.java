package sunflare.examples.OrbitGame.src.orbit;

public class Planet extends SpaceObject {

	private double mass;

	public Planet() {
		super();
	}

	public Planet(Vector2 p,Vector2 v,Vector2 a,String sprite,double mass,double radius) {
		super(p,v,a,sprite,radius*2,radius*2);
		this.mass = mass;
		this.radius = radius;
	}

	public double getMass() {
		return mass;
	}

}