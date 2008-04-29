package sunflare.examples.plugins;

import sunflare.server.Gesture;
import sunflare.plugin.Plugin;

public class Plugin2 implements Plugin{

	public void fireCallBack(Gesture g) {
		System.out.println("Plugin 2 fired callback");
	}
}

