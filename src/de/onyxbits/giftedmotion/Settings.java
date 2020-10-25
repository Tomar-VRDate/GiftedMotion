package de.onyxbits.giftedmotion;

import java.awt.*;

/**
 * The settings to use when rendering
 */
public class Settings {


	private Color transparencyColor = null;

	private int quality = 10;

	private int repeat = 0;

	/**
	 * The color to use for transparency
	 */
	public Color getTransparencyColor() {
		return transparencyColor;
	}

	public void setTransparencyColor(Color transparencyColor) {
		this.transparencyColor = transparencyColor;
	}

	/**
	 * Quality setting for the ditherring algorithm.
	 */
	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	/**
	 * How often to repeat the animation (0 loops forever).
	 */
	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
}