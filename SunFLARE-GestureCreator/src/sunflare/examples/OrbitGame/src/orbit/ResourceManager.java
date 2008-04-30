package sunflare.examples.OrbitGame.src.orbit;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

public class ResourceManager {

	private static HashMap<String, BufferedImage> imageMap;
	private static ResourceManager instance = new ResourceManager();

	private ResourceManager() {
		imageMap = new HashMap<String, BufferedImage>();
	}

	private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT );
        } catch (HeadlessException e) {
        	e.printStackTrace();
        }
        if (bimage == null) {
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

	public static void addImageSequence(String imagePath, int numImages, String sequenceName) {
		Image loadedImage = Toolkit.getDefaultToolkit().getImage(imagePath);
		BufferedImage globalImg = toBufferedImage(loadedImage);
		int parseWidth = globalImg.getWidth() / numImages;
		int height = globalImg.getHeight();
		for(int x=0; x < numImages; x++) {
			BufferedImage i = globalImg.getSubimage(x*parseWidth, 0, parseWidth, height);
			imageMap.put(sequenceName+x, i);
		}
	}

	public static Image getImage(String key, int frame) {
		String hashKey = key + frame;
		return imageMap.get(hashKey);
	}
}
