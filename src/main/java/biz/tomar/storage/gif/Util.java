package biz.tomar.storage.gif;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Util {
	public static BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(),
		                                    source.getHeight(),
		                                    source.getType());
		Graphics g = b.getGraphics();
		g.drawImage(source,
		            0,
		            0,
		            null);
		g.dispose();
		return b;
	}

	public static BufferedImage convertIndexed(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(),
		                                    source.getHeight(),
		                                    BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = b.getGraphics();
		g.drawImage(source,
		            0,
		            0,
		            null);
		g.dispose();
		return b;
	}

	/**
	 * Return a
	 *
	 * @param frameSequence
	 * @return The index of the last frame actually necessary to paint.
	 */
	public static int getFirstNecessaryFrame(FrameSequence frameSequence) {
		SingleFrame[] frames = frameSequence.getSingleFrames();
		for (int i = getFrameNumber(frameSequence,
		                            frameSequence.getSingleFrame());
		     i > 0;
		     i--) {
			if (frames[i].getDispose() == 2 || frames[i].getDispose() == 3) {
				return i;
			}
		}

		return 0;
	}

	public static int getFrameNumber(FrameSequence frameSequence,
	                                 SingleFrame singleFrame) {
		SingleFrame[] frameSequenceFrames = frameSequence.getSingleFrames();
		for (int i = 0;
		     i < frameSequenceFrames.length;
		     i++) {
			if (frameSequenceFrames[i] == singleFrame) {
				return i;
			}
		}
		return -1;
	}
}
