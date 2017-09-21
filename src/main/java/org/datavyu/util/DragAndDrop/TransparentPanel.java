package org.datavyu.util.DragAndDrop;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Transparent panel.
 */
public class TransparentPanel extends JPanel {

    /** Composite blend between underlying image and this super-imposed panel */
    private AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    /** Image that is overlaid */
    private BufferedImage image = null;

    /** Destination location. The image is centered around this location. */
    private Point destination = new Point(0, 0);

    /**
     * Creates a transparent panel.
     */
    public TransparentPanel() {
        setOpaque(false);
    }

    /**
     * Sets the image to overlay.
     *
     * @param image The image that is overlaid.
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Gets the image that is overlaid.
     *
     * @return Image overlaid.
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Set the destination for the overlaid image.
     *
     * @param destination A point that defines the destination.
     */
    public void setDestination(Point destination) {
        this.destination = destination;
    }

    /**
     * Point the component in by overlaying the image content.
     *
     * @param g Graphic object.
     */
    public void paintComponent(Graphics g) {
        if (image == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(composite);
        g2.drawImage(
                image,
                (int) (destination.getX() - (image.getWidth(this) / 2)),
                (int) (destination.getY() - (image.getHeight(this) / 2)),
                null);
    }
}