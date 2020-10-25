package biz.tomar.storage.gif;

import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

/**
 * Responsible for doing all the disk IO related things
 */
public class IO {
	private IO() {
	}

	/**
	 * Load a bunch of files and convert them into frames
	 *
	 * @param files the files to load images from.
	 * @return the frames constructed from the files. A file not containing an
	 * image in a valid format will cause an InvalidArguementException to be
	 * thrown.
	 * be loaded will be represented by an errorshape.
	 */
	public static SingleFrame[] load(File[] files)
					throws
					IOException,
					IllegalArgumentException {
		Vector<SingleFrame> tmp = new Vector<>();
		for (File file : files) {
			Iterator<ImageReader> imageReadersBySuffix = ImageIO.getImageReadersBySuffix(getSuffix(file));
			if (!imageReadersBySuffix.hasNext()) {
				throw new IllegalArgumentException(file.getPath());
			}
			ImageReader reader = imageReadersBySuffix.next();
			//			if (reader instanceof GIFImageReader) {
			//				reader = new PatchedGIFImageReader(null);
			//			}
			reader.setInput(ImageIO.createImageInputStream(new FileInputStream(file)));
			int ub = reader.getNumImages(true);

			for (int x = 0;
			     x < ub;
			     x++) {
				BufferedImage img = reader.read(x);
				//if (img.getType() == 4) img = Util.convertIndexed(img);
				if (ub == 1) {
					tmp.add(new SingleFrame(img,
					                        file.getName()));
				} else {
					String name = String.format("%d_%s",
					                            x,
					                            file.getName());
					SingleFrame sf = new SingleFrame(img,
					                                 name);

					// Getting meta info from an animated GIF is a bit complicated...
					// ... try the quick and dirty method.
					try {
						IIOMetadata meta = reader.getImageMetadata(x);
						NodeList nodeList = meta.getAsTree("javax_imageio_gif_image_1.0")
						                        .getChildNodes();
						for (int count = 0;
						     count < nodeList.getLength();
						     count++) {
							IIOMetadataNode node = (IIOMetadataNode) nodeList.item(count);
							if (node.getNodeName()
							        .equals("GraphicControlExtension")) {
								sf.setShowtime(10 * (Integer.parseInt(node.getAttribute("delayTime"))));
								String dispose = node.getAttribute("disposalMethod");
								if (dispose.equals("none")) {
									sf.setDispose(0);
								}
								if (dispose.equals("doNotDispose")) {
									sf.setDispose(1);
								}
								if (dispose.equals("restoreToBackgroundColor")) {
									sf.setDispose(2);
								}
								if (dispose.equals("restoreToPrevious")) {
									sf.setDispose(3);
								}
							}
							if (node.getNodeName()
							        .equals("ImageDescriptor")) {
								int off_x = (Integer.parseInt(node.getAttribute("imageLeftPosition")));
								int off_y = (Integer.parseInt(node.getAttribute("imageTopPosition")));
								sf.setPosition(new Point(off_x,
								                         off_y));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					tmp.add(sf);
				}
			}
		}

		SingleFrame[] singleFrames = new SingleFrame[tmp.size()];
		tmp.copyInto(singleFrames);
		return singleFrames;
	}

	/**
	 * Write an animated GIF
	 *
	 * @param dest          File to save to
	 * @param frameSequence the FrameSequence to turn into an animation
	 * @param size          height and width of the animated GIF
	 * @param settings      other options.
	 */
	public static void export(File dest,
	                          FrameSequence frameSequence,
	                          Dimension size,
	                          Settings settings)
					throws
					IOException {
		AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
		animatedGifEncoder.start(new FileOutputStream(dest));
		animatedGifEncoder.setRepeat(settings.getRepeat());
		animatedGifEncoder.setQuality(settings.getQuality());
		animatedGifEncoder.setTransparent(settings.getTransparencyColor());
		SingleFrame[] singleFrames = frameSequence.getSingleFrames();
		for (SingleFrame singleFrame : singleFrames) {
			animatedGifEncoder.setDelay(singleFrame.getShowtime());
			animatedGifEncoder.setDispose(singleFrame.getDispose());
			animatedGifEncoder.addFrame(singleFrame.exportFrame(size,
			                                                    settings.getTransparencyColor()));
		}
		animatedGifEncoder.finish();
	}

	/**
	 * Deoptimize an animated GIF and save it
	 *
	 * @param dest     File to save to
	 * @param seq      the FrameSequence to turn into an animation
	 * @param size     height and width of the animated GIF
	 * @param settings other options.
	 */
	public static void exportDeoptimized(File dest,
	                                     FrameSequence seq,
	                                     Dimension size,
	                                     Settings settings)
					throws
					IOException {
		AnimatedGifEncoder e = new AnimatedGifEncoder();
		e.start(new FileOutputStream(dest));
		e.setRepeat(settings.getRepeat());
		e.setQuality(settings.getQuality());
		e.setTransparent(settings.getTransparencyColor());
		BufferedImage bufferedImage = new BufferedImage((int) size.getWidth(),
		                                                (int) size.getHeight(),
		                                                BufferedImage.TYPE_INT_ARGB);
		Graphics      bufferedImageGraphics = bufferedImage.getGraphics();
		SingleFrame[] singleFrames          = seq.getSingleFrames();
		for (SingleFrame singleFrame : singleFrames) {
			e.setDelay(singleFrame.getShowtime());
			e.setDispose(singleFrame.getDispose()); //Redraw background
			singleFrame.paint(bufferedImageGraphics);
			e.addFrame(bufferedImage);
		}
		bufferedImageGraphics.dispose();
		e.finish();
	}

	/**
	 * Extract frames from the sequence and save them as single files
	 *
	 * @param frameSequence the frame sequence
	 * @param dir           base directory
	 * @throws IOException on error
	 */
	public static void extract(FrameSequence frameSequence,
	                           File dir,
	                           Dimension size,
	                           Settings settings)
					throws
					IOException {
		SingleFrame[] singleFrames = frameSequence.getSingleFrames();
		for (SingleFrame singleFrame : singleFrames) {
			File f = new File(dir,
			                  singleFrame.toString());
			String[] singleFrameStrings = singleFrame.toString()
			                                         .split("\\.");
			String format = singleFrameStrings[singleFrameStrings.length - 1].toLowerCase();
			//			ImageIO.write(frameSequence.getFrames()[i].getRaw(),
			//			              format,
			//			              f);
			ImageIO.write(singleFrame.exportFrame(size,
			                                      settings.getTransparencyColor()),
			              format,
			              f);
		}
	}

	/**
	 * Locate an image in the resources folder and build an icon from it.
	 *
	 * @param fname filename (relative to resources/icons/)
	 * @param desc  Icon description
	 * @return the loaded icon
	 */
	public static ImageIcon createIcon(String fname,
	                                   String desc) {
		String format = String.format("gif/icons/%s",
		                              fname);
		URL imgURL = ClassLoader.getSystemResource(format);
		return new ImageIcon(imgURL,
		                     desc);
	}

	/**
	 * Helper function to determine filetype
	 *
	 * @param file file to look at
	 * @return the suffix (lowercase) or null
	 */
	private static String getSuffix(File file) {
		String[] tmp = file.getName()
		                   .split("\\.");
		return tmp[tmp.length - 1].toLowerCase();
	}
}