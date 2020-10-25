package de.onyxbits.giftedmotion;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Preview an image file
 */
public class Preview
				extends JPanel {

	/**
	 * Preferred size of the preview canvas
	 */
	private Dimension preferred = new Dimension(1,
	                                            1);

	/**
	 * Image to show
	 */
	private BufferedImage img;


	public Preview() {
	}

	/**
	 * Display an image
	 *
	 * @param f the file to display. If it cannot be loaded, nothing will be
	 *          displayed.
	 */
	public void show(File f) {
		try {
			setImg(ImageIO.read(f));
			setPreferred(new Dimension(getImg().getWidth(),
			                           getImg().getHeight()));
		} catch (Exception e) {
			//e.printStackTrace(); // Debug only
			setImg(null);
		}
		//repaint();
		revalidate();
		repaint();
	}

	// Overridden
	public Dimension getPreferredSize() {
		return getPreferred();
	}

	// Overriden
	public void paint(Graphics gr) {
		Dimension size = getSize();
		gr.clearRect(0,
		             0,
		             size.width,
		             size.height);
		if (getImg() != null) {
			gr.drawImage(getImg(),
			             0,
			             0,
			             null);
		}
	}

	/**
	 * Preferredsize of the previewcanvas
	 */
	public Dimension getPreferred() {
		return preferred;
	}

	public void setPreferred(Dimension preferred) {
		this.preferred = preferred;
	}

	/**
	 * Image to show
	 */
	public BufferedImage getImg() {
		return img;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}
}