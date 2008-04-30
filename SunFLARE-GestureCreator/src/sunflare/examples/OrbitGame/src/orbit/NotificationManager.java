package sunflare.examples.OrbitGame.src.orbit;

import java.util.ArrayList;

public class NotificationManager {

	private ArrayList<FlashingText> flashingTexts;
	private static NotificationManager instance = null;

	public static NotificationManager getInstance() {
		if (instance == null) instance = new NotificationManager();
		return instance;
	}

	private NotificationManager() {
		flashingTexts = new ArrayList<FlashingText>();
	}

	public void addFlashingText(FlashingText ft) {
		flashingTexts.add(ft);
	}

	public void update(long timeElapsed) {
		ArrayList<FlashingText> toRemove = new ArrayList<FlashingText>();

		for (FlashingText ft : flashingTexts) {
			ft.update(timeElapsed);
			if (!ft.getAlive()) {
				toRemove.add(ft);
			}
		}

		for (FlashingText ft : toRemove) flashingTexts.remove(ft);
		toRemove.clear();
	}

	public void paint() {
		for (FlashingText ft : flashingTexts) {
			ft.paint();
		}
	}


}
