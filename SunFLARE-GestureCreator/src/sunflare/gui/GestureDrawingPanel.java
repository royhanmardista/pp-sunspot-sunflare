/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sunflare.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * A JPanel where visual gestures will be drawn
 * @author Sean
 */
public class GestureDrawingPanel  extends JPanel {
    // constants for the different gestures
    public static final int INVALID_GESTURE_ID = 0;
    public static final int STRAIGHT_RIGHT = 1;
    public static final int STRAIGHT_LEFT = 2;
    public static final int STRAIGHT_UP = 3;
    public static final int STRAIGHT_DOWN = 4;
    public static final int STRAIGHT_FORWARD = 5;
    public static final int STRAIGHT_BACKWARD = 6;
    public static final int SHAKE = 7;
    public static final int MAX = 7; // should always be the same as last gesture
    
    private Point locGesture;
    private int gestureID;
    private BufferedImage gestureImage;
    private BufferedImage imageArrowLeft;
    private BufferedImage imageArrowRight;
    private BufferedImage imageArrowUp;
    private BufferedImage imageArrowDown;
    private BufferedImage imageArrowForward;
    private BufferedImage imageArrowBackward;
    private BufferedImage imageShake;
 
    /**
     * Creates a new panel and loads all the images
     */
    public GestureDrawingPanel() throws IOException {
        gestureImage = null;
        gestureID = INVALID_GESTURE_ID;
        locGesture = new Point();
        
        // load all the images
        String path = "left_arrow.jpg";
        imageArrowLeft = ImageIO.read(new File(path));
        
        path = "right_arrow.jpg";
        imageArrowRight = ImageIO.read(new File(path));
        
        path = "up_arrow.jpg";
        imageArrowUp = ImageIO.read(new File(path));
        
        path = "down_arrow.jpg";
        imageArrowDown = ImageIO.read(new File(path));
        
        path = "shake.jpg";
        imageShake = ImageIO.read(new File(path));
        
        path = "forward_arrow.jpg";
        imageArrowForward = ImageIO.read(new File(path));
        
        path = "backward_arrow.jpg";
        imageArrowBackward = ImageIO.read(new File(path));
    }
 
    /**
     * Paints the gesture images onto the panel
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gestureID <= INVALID_GESTURE_ID)
        {
            // draw a large white box to cover up any old stuff...
            g.setColor(Color.white);
            g.fillRect(0,0,this.getWidth(),this.getHeight());
        }
        else
        {
            g.drawImage(gestureImage, locGesture.x, locGesture.y, this);
        }
    }
    
    /**
     * Sets the gesture to be displayed
     * @param gesture to be displayed
     */
    public void setGesture(int id)
    {
        if (id <= INVALID_GESTURE_ID || id > MAX)
        {
            gestureID = INVALID_GESTURE_ID;
            return;
        }
        else
        {
            gestureID = id;
        }
        
        // assign image to the second gesture
        switch (gestureID)
        {
            case STRAIGHT_UP:
                gestureImage = imageArrowUp;
                break;
            case STRAIGHT_RIGHT:
                gestureImage = imageArrowRight;
                break;
            case STRAIGHT_DOWN:
                gestureImage = imageArrowDown;
                break;
            case STRAIGHT_LEFT:
                gestureImage = imageArrowLeft;
                break;
            case STRAIGHT_FORWARD:
                gestureImage = imageArrowForward;
                break;
            case STRAIGHT_BACKWARD:
                gestureImage = imageArrowBackward;
                break;
            case SHAKE:
                gestureImage = imageShake;
                break;
            default:
                gestureImage = null;
        }
        locGesture.x = getWidth() / 2 - gestureImage.getWidth() / 2;
        locGesture.y = getHeight() / 2 - gestureImage.getHeight() / 2;
        repaint();
    }
    
    /**
     * Returns gesture currently being displayed
     * @return gesture being displayed
     */
    public int getID()
    {
        return gestureID;
    }
 
//    public Dimension getPreferredSize()
//    {
//        return null;
//    }
}