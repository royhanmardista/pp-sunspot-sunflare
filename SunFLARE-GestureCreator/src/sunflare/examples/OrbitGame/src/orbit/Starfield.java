package sunflare.examples.OrbitGame.src.orbit;

import java.util.*;

/**
 * Animates the stars
 * @author Henry Yuen
 *
 */
public class Starfield {

	private final int NUM_STARS = 50;	//per layer

	ArrayList<Star> starLayer[];

	public Starfield() {
		starLayer = new ArrayList[3];
		for (int l=0;l<3;l++)
			starLayer[l] = new ArrayList<Star>();

		//generate stars
		generateStars();
	}

	/**
	 * this will randomly populate the screen with stars
	 *
	 */
	private void generateStars() {

		for (int l=0;l<3;l++) {
			for (int s=0;s<NUM_STARS;s++) {
				Star star = new Star(l);
				starLayer[l].add(star);
			}

		}
	}

	/**
	 * updates the individual stars
	 */
	public void update(long timeElapsed,Vector2 dir) {
		for (int l=0;l<3;l++) {
			for (Star s : starLayer[l]) {
				s.update(timeElapsed,dir);
			}
		}
	}

	/**
	 * This returns the stars in the particular layer, 0-2.
	 */
	public ArrayList<Star>[] getStarLayers() {
		return starLayer;
	}

}
