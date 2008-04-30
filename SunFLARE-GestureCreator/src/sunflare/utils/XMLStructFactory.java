package sunflare.utils;

import java.net.URL;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sunflare.plugin.PluginRef;

/**
 * Utility for creating PluginRef's from the XML file
 */
public final class XMLStructFactory {

	private XMLStructFactory() throws InstantiationException {
	    throw new InstantiationException("Don't instantiate XML Struct Factories!");
	}

        /**
         * Static function to retrieve a PluginRef
         */
	public static PluginRef getPluginRefs(Node node) {
	    Element pluginNodeRoot = (Element) node;

	    String name = null;
	    String application = null;
	    String classpath = null;
	    String description = null;

	    try {
	      name = pluginNodeRoot.getAttribute("name");
	      application = pluginNodeRoot.getAttribute("app");
	      classpath = pluginNodeRoot.getAttribute("class");
	      description = pluginNodeRoot.getAttribute("desc");
      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    PluginRef plugin = new PluginRef(name, application, description, classpath);

	    return plugin;
	  }
	
}
