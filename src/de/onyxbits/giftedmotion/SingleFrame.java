package de.onyxbits.giftedmotion;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * A container for holding one frame of animation
 */
public class SingleFrame {

	/**
	 * The raw BufferedImage, as loaded from disk
	 */
	private BufferedImage bufferedImage;

	/**
	 * only x and y are used to specified where to draw the raw image on the
	 * canvas.
	 */
	private Point position;

	/**
	 * An amount of rotation degrees to rotate the image by
	 * 0 = Correct direction
	 */
	private double rotationDegrees = 0;

	/**
	 * Scale amount X (basically just scaled dimensions)
	 */

	private float scaleX;
	/**
	 * Scale amount Y (basically just scaled dimensions)
	 */

	private float scaleY;
	/**
	 * How long to show this frame in the final animation (in ms).
	 */
	private int   showtime = 100;

	/**
	 * What to do with the previous frame. According to the GIF spec: <br>
	 * 0 - undefined<br>
	 * 1 - Do not dispose between frames.<br>
	 * 2 - Overwrite the image area with the background color.<br>
	 * 3 - Overwrite the image area with what was there prior to rendering<br>
	 * the image.
	 */
	private int dispose = 3;

	/**
	 * Name for identifying purposes
	 */
	private String name;


	/**
	 * Create a new Frame
	 *
	 * @param bufferedImage the raw iamge as loaded from disk
	 * @param name          A name for identification purposes
	 */
	public SingleFrame(BufferedImage bufferedImage,
	                   String name) {
		this.setBufferedImage(bufferedImage);
		this.setName(name);
		setPosition(new Point(0,
		                      0));

		setScaleX(bufferedImage.getWidth());
		setScaleY(bufferedImage.getHeight());
	}

	/**
	 * Create a copy of a Frame
	 *
	 * @param singleFrame Frame to be copied
	 */
	public SingleFrame(SingleFrame singleFrame) {
		this.setBufferedImage(singleFrame.getBufferedImage());
		this.setName(singleFrame.getName());
		setRotationDegrees(singleFrame.getRotationDegrees());
		setPosition(new Point(singleFrame.getPosition()));
		setShowtime(singleFrame.getShowtime());
		setScaleX(singleFrame.getScaleX());
		setScaleY(singleFrame.getScaleY());
	}

	/**
	 * Query the dimension of the raw image
	 */
	public Dimension getSize() {
		BufferedImage bufferedImage = getBufferedImage();
		return new Dimension(bufferedImage.getWidth(),
		                     bufferedImage.getHeight());
	}

	/**
	 * Draw this frame on a canvas
	 *
	 * @param g the grpahics object to render to
	 */
	public void paint(Graphics g) {
		Graphics2D      graphics2D = (Graphics2D) g;
		AffineTransform at         = new AffineTransform();
		float           scaleX     = getScaleX();
		float           scaleY     = getScaleY();
		at.translate(scaleX / 2,
		             scaleY / 2);
		double rotationDegrees = getRotationDegrees();
		at.rotate(rotationDegrees);
		at.translate(-scaleX / 2,
		             -scaleY / 2);

		BufferedImage bufferedImage = getBufferedImage();
		at.scale(scaleX / bufferedImage.getWidth(),
		         scaleY / bufferedImage.getHeight());
		Point position = getPosition();
		graphics2D.drawImage(bufferedImage,
		                     new AffineTransformOp(at,
		                                           AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
		                     position.x,
		                     position.y);
	}

	/**
	 * Produce an image, that can directly be assembled into an animated GIF
	 *
	 * @param size  Size of the final frame in pixels.
	 * @param trans the color to replace transparent pixels with.
	 * @return a BufferedImage of the desired size with the transparent pixels
	 * replaced by the specified solid color.
	 */
	public BufferedImage exportFrame(Dimension size,
	                                 Color trans) {
		BufferedImage ret = new BufferedImage(size.width,
		                                      size.height,
		                                      BufferedImage.TYPE_INT_ARGB);
		Graphics gr = ret.createGraphics();
		gr.setColor(trans);
		gr.fillRect(0,
		            0,
		            size.width,
		            size.height);
		paint(gr);
		//gr.drawImage(raw,position.x,position.y,null);
		return ret;
	}

	public String toString() { return getName(); }

	/**
	 * The raw BufferedImage, as loaded from disk
	 */
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}

	/**
	 * only x and y are used to specified where to draw the raw image on the
	 * canvas.
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Specify position of the raw image on the canvas
	 *
	 * @param position the x and y coordinate of where to draw the raw image on the
	 *                 canvas.
	 */
	public void setPosition(Point position) { this.position = position; }

	/**
	 * An amount of rotation degrees to rotate the image by
	 * 0 = Correct direction
	 */
	public double getRotationDegrees() {
		return rotationDegrees;
	}

	public void setRotationDegrees(double rotationDegrees) {
		this.rotationDegrees = rotationDegrees;
	}

	/**
	 * Scale amount (basically just scaled dimensions)
	 */
	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	/**
	 * How long to show this frame in the final animation (in ms).
	 */
	public int getShowtime() {
		return showtime;
	}

	public void setShowtime(int showtime) {
		this.showtime = showtime;
	}

	/**
	 * What to do with the previous frame. According to the GIF spec: <br>
	 * 0 - undefined<br>
	 * 1 - Do not dispose between frames.<br>
	 * 2 - Overwrite the image area with the background color.<br>
	 * 3 - Overwrite the image area with what was there prior to rendering<br>
	 * the image.
	 */
	public int getDispose() {
		return dispose;
	}

	public void setDispose(int dispose) {
		this.dispose = dispose;
	}

	/**
	 * For identifying purposes
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
