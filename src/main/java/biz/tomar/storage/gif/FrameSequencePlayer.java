package biz.tomar.storage.gif;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * A thread, that steps through the animation.
 */
public class FrameSequencePlayer
				extends Thread {
	/**
	 * Play animation
	 */
	private final JButton play  = new JButton(IO.createIcon("Tango/22x22/actions/media-playback-start.png",
	                                                        Translations.get("core.play")));
	/**
	 * Pause animation
	 */
	private final JButton pause = new JButton(IO.createIcon("Tango/22x22/actions/media-playback-pause.png",
	                                                        Translations.get("core.pause")));

	private FrameSequence       frameSequence;
	private int                 repeat;
	private PlayerHelper        playerHelper;
	private boolean             running;
	private FrameSequencePlayer frameSequencePlayer = null;

	/**
	 * Construct a new Player
	 */
	public FrameSequencePlayer() {
	}

	/**
	 * Construct a new Player
	 *
	 * @param frameSequence the frame sequence to play.
	 * @param repeat        how often to repeat the animation (zero is infinite)
	 */
	public FrameSequencePlayer(FrameSequence frameSequence,
	                           int repeat) {
		this.setFrameSequence(frameSequence);
		this.setRepeat(repeat);
	}

	public JButton getPlay() {
		return play;
	}

	public void setPlay(ActionListener actionListener) {
		JButton play = getPlay();
		play.setToolTipText(((ImageIcon) play.getIcon()).getDescription());
		play.addActionListener(actionListener);
	}

	public JButton getPause() {
		return pause;
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

	public void setPause(ActionListener actionListener) {
		JButton pause = getPause();
		pause.setToolTipText(((ImageIcon) pause.getIcon()).getDescription());
		pause.addActionListener(actionListener);
		pause.setEnabled(false);
	}

	/**
	 * How often to repeat the animation
	 */
	public int getRepeat() {
		return repeat;
	}

	public boolean isRunning() {
		return running;
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

	@Override
	public void run() {
		try {
			int count = getRepeat();

			running = true;
			while (running) {
				FrameSequence frameSequence = getFrameSequence();
				SingleFrame   singleFrame   = frameSequence.getSingleFrame();
				if (singleFrame == null) {
					return;
				}

				Thread.sleep(singleFrame.getShowtime());
				int           idx          = 0;
				SingleFrame[] singleFrames = frameSequence.getSingleFrames();
				while (singleFrames[idx] != singleFrame) {
					idx++;
				}
				idx++;
				if (idx >= singleFrames.length) {
					idx = 0;
					count--;
				}
				frameSequence.setSingleFrame(singleFrames[idx]);
				SwingUtilities.invokeAndWait(getPlayerHelper());
				if (getRepeat() != 0 && count <= 0) {
					return;
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * The sequence to play
	 *
	 * @param frameSequence the frame sequence to play.
	 */
	public void setFrameSequence(FrameSequence frameSequence) {
		this.frameSequence = frameSequence;
		setPlayerHelper(new PlayerHelper(this.frameSequence));
	}

	/**
	 * @param repeat how often to repeat the animation (zero is infinite)
	 */
	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}

	public void togglePlayOrPause(FrameSequence frameSequence,
	                              int repeat) {
		try {
			if (play.isEnabled()) {
				frameSequencePlayer = play(frameSequence,
				                           repeat);
			} else {
				pause(frameSequencePlayer);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public FrameSequencePlayer play(FrameSequence frameSequence,
	                                int repeat) {
		FrameSequencePlayer frameSequencePlayer;
		frameSequencePlayer = new FrameSequencePlayer(frameSequence,
		                                              repeat);
		frameSequencePlayer.start();
		play.setEnabled(false);
		pause.setEnabled(true);
		return frameSequencePlayer;
	}

	public void pause(FrameSequencePlayer frameSequencePlayer) {
		frameSequencePlayer.exit();
		play.setEnabled(true);
		pause.setEnabled(false);
	}
}