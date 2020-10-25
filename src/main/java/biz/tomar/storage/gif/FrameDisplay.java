package biz.tomar.storage.gif;

import javax.swing.*;

/**
 * Wrapper around the actual canvas class, to glue it into the workspace
 */
public class FrameDisplay
				extends JInternalFrame {
	/**
	 * The frame canvas to draw upon
	 */
	private FrameCanvas frameCanvas;

	public FrameDisplay(FrameCanvas frameCanvas) {
		super("Preview",
		      true,
		      false,
		      false,
		      false);
		setFrameIcon(null);
		this.setFrameCanvas(frameCanvas);
		setContentPane(frameCanvas);
		pack();
	}

	/**
	 * Query the canvas
	 *
	 * @return the canvas displayed
	 */
	public FrameCanvas getFrameCanvas() {
		return frameCanvas;
	}

	public void setFrameCanvas(FrameCanvas frameCanvas) {
		this.frameCanvas = frameCanvas;
	}
}
