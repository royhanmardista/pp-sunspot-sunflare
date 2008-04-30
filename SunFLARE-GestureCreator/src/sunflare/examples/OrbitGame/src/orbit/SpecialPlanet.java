package sunflare.examples.OrbitGame.src.orbit;

public class SpecialPlanet extends Planet {

	private double mass;
	private boolean tagged;

	public SpecialPlanet() {
		super();
		tagged = false;
		numFrames=2;
	}

	public SpecialPlanet(Vector2 p,Vector2 v,Vector2 a,String sprite,double mass,double radius) {
		super(p,v,a,sprite,radius*2,radius*2);
		this.mass = mass;
		this.radius = radius;
		tagged = false;
		numFrames=2;
	}

	public void setTagged(){
		//System.out.println("\nTAGGED PLANET, YAY!!!!!\n");
		tagged = true;
		// set new image
		setFrame(1);
	}

	public boolean getTagged() {
		 return tagged;
	}

	public double getMass() {
		return mass;
	}

}
