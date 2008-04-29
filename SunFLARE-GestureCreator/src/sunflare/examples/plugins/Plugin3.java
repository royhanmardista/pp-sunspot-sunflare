package examples.plugins;

import sunflare.server.Gesture;
import sunflare.plugin.Plugin;

public class Plugin3 implements Plugin{

	public void fireCallBack(Gesture g) {
		System.out.println("Plugin 3 fired callback");
	}
}
