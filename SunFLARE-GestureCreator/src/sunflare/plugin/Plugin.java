package sunflare.plugin;

import sunflare.server.Gesture;

/**
 * The interface for all plugins to use
 */
public interface Plugin {
	
	public void fireCallBack(Gesture g);
	
}
