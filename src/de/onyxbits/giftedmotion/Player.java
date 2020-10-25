package de.onyxbits.giftedmotion;

import javax.swing.*;

/**
 * A thread, that steps through the animation.
 */
public class Player
				extends Thread {

	private FrameSequence frameSequence;

	private int repeat;

	private PlayerHelper playerHelper;
	private boolean      running;

	/**
	 * Construct a new Player
	 *
	 * @param repeat how often to repeat the animation (zero is infinite)
	 * @seq the framesequence to play.
	 */
	public Player(FrameSequence frameSequence,
	              int repeat) {
		this.setFrameSequence(frameSequence);
		this.setRepeat(repeat);
		setPlayerHelper(new PlayerHelper(frameSequence));
	}

	public void run() {
		try {
			int count = getRepeat();

			running = true;
			while (running) {
				if (getFrameSequence().getSingleFrame() == null) {
					return;
				}

				Thread.sleep(getFrameSequence().getSingleFrame()
				                               .getShowtime());
				int idx = 0;
				while (getFrameSequence().getSingleFrames()[idx] != getFrameSequence().getSingleFrame()) {
					idx++;
				}
				idx++;
				if (idx >= getFrameSequence().getSingleFrames().length) {
					idx = 0;
					count--;
				}
				getFrameSequence().setSingleFrame(getFrameSequence().getSingleFrames()[idx]);
				SwingUtilities.invokeAndWait(getPlayerHelper());
				if (getRepeat() != 0 && count <= 0) {
					return;
				}
			}
		} catch (Exception exp) {
		}
	}

	public boolean exit() {
		if (running) {
			running = false;
		}
		return !running;
	}

	/**
	 * The sequence to play
	 */
	public FrameSequence getFrameSequence() {
		return frameSequence;
	}

	public void setFrameSequence(FrameSequence frameSequence) {
		this.frameSequence = frameSequence;
	}

	/**
	 * How often to repeat the animation
	 */
	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}

	/**
	 * Needed to fire events from the event dispatcher thread
	 */
	public PlayerHelper getPlayerHelper() {
		return playerHelper;
	}

	public void setPlayerHelper(PlayerHelper playerHelper) {
		this.playerHelper = playerHelper;
	}
}