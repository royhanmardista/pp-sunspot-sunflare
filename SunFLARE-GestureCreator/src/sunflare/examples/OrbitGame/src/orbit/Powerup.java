package sunflare.examples.OrbitGame.src.orbit;

public class Powerup extends SpaceObject {

	public static final int EXTRA_LIFE = 0;
	public static final int SPEED_BOOST = 1;
	public static final int NUM_TYPE_POWERUPS = 2;

	private int type;

	public Powerup(int type,Vector2 pos) {
		super(pos,new Vector2(),new Vector2(),null,64,64);

		alive = true;
		this.type = type;
		switch (type) {
		case EXTRA_LIFE:
			sprite = "extralife";
			break;
		case SPEED_BOOST:
			sprite = "speedboost";
			break;
		}
	}

	public int getType() {
		return type;
	}

	public String getAquiredMessage() {

		switch (type) {
		case EXTRA_LIFE:
			return "You got an extra life!";
		case SPEED_BOOST:
			return "You got a speed boost!";
		}

		return null;

	}

}
