package de.onyxbits.giftedmotion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * The canvas on which to draw SingleFrames
 */
public class FrameCanvas
				extends JPanel
				implements FrameSequenceListener,
				           MouseListener,
				           MouseMotionListener {

	/**
	 * The sequence to draw
	 */
	private final FrameSequence frameSequence;

	/**
	 * Onionskin boolean
	 */
	private boolean onionskinEnabled = false;

	/**
	 * The tool used for transforming the selected image
	 */
	private TransformTool transformTool = new DragTool();

	/**
	 * Thread for flickering
	 */
	private FlickerThread flickerThread = new FlickerThread();

	/**
	 * Boolean to indicate which flickered image to show
	 */
	private boolean flickerShow = false;

	public FrameCanvas(FrameSequence frameSequence) {
		this.frameSequence = frameSequence;
		addMouseListener(this);
		addMouseMotionListener(this);

		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					transformTool.setShiftPressed(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					transformTool.setShiftPressed(false);
				}
			}
		});
	}

	public void paintComponent(Graphics graphics) {
		//long time = System.currentTimeMillis();
		super.paintComponent(graphics);
		Dimension size = getSize();

		if (frameSequence.getSingleFrame() == null) {
			graphics.clearRect(0,
			                   0,
			                   size.width,
			                   size.height);
			return;
		}

		BufferedImage previous = new BufferedImage(size.width,
		                                           size.height,
		                                           BufferedImage.TYPE_INT_ARGB);

		SingleFrame[] singleFrames = frameSequence.getSingleFrames();
		for (SingleFrame singleFrame : singleFrames) {
			singleFrame.paint(graphics);
			// Only draw the sequence up to the selected frame
			// FIXME: This is utterly inefficient!
			if (singleFrame == frameSequence.getSingleFrame()) {
				break;
			}

			// If the selected frame is not reached yet, dispose
			switch (singleFrame.getDispose()) {
				case 0: {
					break;
				}
				case 1: {
					Graphics previousGraphics = previous.getGraphics();
					singleFrame.paint(previousGraphics);
					previousGraphics.dispose();
					break;
				}
				case 2: {
					graphics.clearRect(0,
					                   0,
					                   size.width,
					                   size.height);
					break;
				}
				case 3: {
					graphics.clearRect(0,
					                   0,
					                   size.width,
					                   size.height);
					graphics.drawImage(previous,
					                   0,
					                   0,
					                   null);

					break;
				}
			}
		}
		//System.err.println("Time: "+(System.currentTimeMillis()-time));
	}

	public Dimension getPreferredSize() {
		return frameSequence.getExpansion();
	}

	public void dataChanged(FrameSequence frameSequence) {
		repaint();
	}

	public void setOnionskin(boolean on) {
		onionskinEnabled = on;
		repaint();
	}

	public void setFlicker(boolean fli) {
		if (fli) {
			flickerThread = new FlickerThread();
			flickerThread.start();
		} else {
			flickerShow = false;
			flickerThread = null;
		}
	}

	public TransformTool getTransformTool() {
		return transformTool;
	}

	public void setTransformTool(TransformTool transformTool) {
		this.transformTool = transformTool;
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e)  {}

	public void mousePressed(MouseEvent e) {
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		if (singleFrame == null) {
			return;
		}
		Point mousePos = e.getPoint();
		transformTool.setOffset(new Point(mousePos.x - singleFrame.getPosition().x,
		                                  mousePos.y - singleFrame.getPosition().y));
		transformTool.beginTransform(singleFrame,
		                             mousePos);
	}

	public void mouseReleased(MouseEvent e) {
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		if (singleFrame == null) {
			return;
		}
		frameSequence.fireDataChanged();
		Point mousePos = e.getPoint();
		transformTool.endTransform(singleFrame,
		                           mousePos);
	}

	public void mouseDragged(MouseEvent e) {
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		if (singleFrame == null) {
			return;
		}
		Point mousePos = e.getPoint();
		transformTool.transform(singleFrame,
		                        mousePos);
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
	}
}