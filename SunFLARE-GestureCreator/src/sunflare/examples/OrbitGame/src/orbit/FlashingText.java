package sunflare.examples.OrbitGame.src.orbit;

import javax.swing.*;
import java.awt.*;

/**
 * This text flashes for a duration of time.
 * This is helpful:
 * <http://www.apl.jhu.edu/~hall/java/Java2D-Tutorial.html>
 * @author Henry Yuen
 *
 */
public class FlashingText {

	String text;
	long flashLength;
	long life;
	long overallCount;
	long count;
	int x,y;
	int size;
	Color color;
	boolean on;
	boolean alive;


	public FlashingText(String text) {
		this.text = text;
		flashLength = 0;
		life = 0;
		overallCount = 0;
		x = y = size = 0;
		count = 0;
		color = null;
		on = true;
		alive = true;
	}

	public void setFlashLength(long length) {
		flashLength = length;
	}

	public void setLife(long life) {
		this.life = life;
	}

	public void setPos(int x,int y) {
		this.x = x;
		this.y = y;
	}

	public void setColor(Color c) {
		color = c;
	}

	public void setSize(int s) {
		size = s;
	}

	public boolean getAlive() {
		return alive;
	}

	public void update(long timeElapsed) {
		overallCount += timeElapsed;
		if (overallCount >= life) {
			alive = false;
		}
		/*
		count += timeElapsed;
		if (count >= flashLength) {
			count = 0;
			on = on ? false : true;
		}
		*/
	}

	public void paint() {
		if (on == false) return;
		if (alive == false) return;

		PrintManager.getInstance().print("large", text, x, y, color);

	}

}
