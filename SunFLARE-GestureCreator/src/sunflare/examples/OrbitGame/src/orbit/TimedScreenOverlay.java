package sunflare.examples.OrbitGame.src.orbit;

import java.awt.Color;
import java.awt.Graphics2D;

public class TimedScreenOverlay extends ScreenOverlay {

	public final static int FADE_IN = 0;
	public final static int FADE_OUT = 0;

	long duration;
	long count;
	int mode;

	public TimedScreenOverlay(Graphics2D g,Color c,int x,int y,int width,int height,long duration,int mode) {
		super(g,c,new Rect(x,y,width,height));
		count = 0;
		this.duration = duration;
		this.mode = mode;
	}

	public void update(long timeElapsed) {
		count += timeElapsed;
	}

	public void paint() {
		float alpha;
		float ratio = (float)count/(float)duration;
		if (mode == FADE_IN) {
			//then it's going from total opacity to total transparency
			alpha = 1.0f - ratio;
		} else {
			alpha = ratio;
		}

		if (alpha < 0) alpha = 0.0f;
		if (alpha > 1.0f) alpha = 1.0f;

		alpha *= 255;

		super.paint(alpha);
	}


}
