package de.onyxbits.giftedmotion;

import com.sun.imageio.plugins.common.ReaderUtil;
import com.sun.imageio.plugins.gif.GIFImageMetadata;
import com.sun.imageio.plugins.gif.GIFStreamMetadata;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PatchedGIFImageReader
				extends ImageReader {

	// Constants used to control interlacing.
	static final int[] interlaceIncrement = {8,
	                                         8,
	                                         4,
	                                         2,
	                                         -1};
	static final int[] interlaceOffset    = {0,
	                                         4,
	                                         2,
	                                         1,
	                                         -1};

	// Per-stream settings
	// The current ImageInputStream source.
	ImageInputStream  imageInputStream   = null;
	// True if the file header including stream metadata has been read.
	boolean           gotHeader          = false;
	// Global metadata, read once per input setting.
	GIFStreamMetadata gifStreamMetadata  = null;
	// The current image index
	int               currIndex          = -1;
	// Metadata for image at 'currIndex', or null.
	GIFImageMetadata  gifImageMetadata   = null;
	// A List of Longs indicating the stream positions of the
	// start of the metadata for each image.  Entries are added
	// as needed.
	List<Long>        imageStartPosition = new ArrayList<>();
	// Length of metadata for image at 'currIndex', valid only if
	// imageMetadata != null.
	int               imageMetadataLength;
	// The number of images in the stream, if known, otherwise -1.
	int               numImages          = -1;
	// Variables used by the LZW decoding process
	byte[]            block              = new byte[255];
	int               blockLength        = 0;
	int               bitPos             = 0;
	int               nextByte           = 0;
	int               initCodeSize;
	int               clearCode;
	int               eofCode;
	// 32-bit lookahead buffer
	int               next32Bits         = 0;
	// Try if the end of the data blocks has been found,
	// and we are simply draining the 32-bit buffer
	boolean           lastBlockFound     = false;
	// The image to be written.
	BufferedImage     bufferedImage      = null;
	// The image's tile.
	WritableRaster    writableRaster     = null;
	// The image dimensions (from the stream).
	int               width              = -1, height = -1;
	// The pixel currently being decoded (in the stream's coordinates).
	int streamX = -1, streamY = -1;
	// The number of rows decoded
	int rowsDone = 0;

	// End per-stream settings
	// The current interlace pass, starting with 0.
	int       interlacePass = 0;
	Rectangle sourceRegion;
	int       sourceXSubsampling;
	int       sourceYSubsampling;
	int       sourceMinProgressivePass;
	int       sourceMaxProgressivePass;
	Point     destinationOffset;
	Rectangle destinationRegion;
	// Used only if IIOReadUpdateListeners are present
	int       updateMinY;
	int       updateYStep;
	boolean   decodeThisRow = true;

	// BEGIN LZW STUFF
	int    destY = 0;
	byte[] rowBuf;

	public PatchedGIFImageReader(ImageReaderSpi originatingProvider) {super(originatingProvider);}

	// Take input from an ImageInputStream
	public void setInput(Object input,
	                     boolean seekForwardOnly,
	                     boolean ignoreMetadata) {
		super.setInput(input,
		               seekForwardOnly,
		               ignoreMetadata);
		if (input != null) {
			if (!(input instanceof ImageInputStream)) {
				throw new IllegalArgumentException("input not an ImageInputStream!");
			}
			this.imageInputStream = (ImageInputStream) input;
		} else {
			this.imageInputStream = null;
		}

		// Clear all values based on the previous stream contents
		resetStreamSettings();
	}

	public int getNumImages(boolean allowSearch)
					throws
					IIOException {
		if (imageInputStream == null) {
			throw new IllegalStateException("Input not set!");
		}
		if (seekForwardOnly && allowSearch) {
			throw new IllegalStateException("seekForwardOnly and allowSearch can't both be true!");
		}

		if (numImages > 0) {
			return numImages;
		}
		if (allowSearch) {
			this.numImages = locateImage(Integer.MAX_VALUE) + 1;
		}
		return numImages;
	}

	// Throw an IndexOutOfBoundsException if index < minIndex,
	// and bump minIndex if required.
	private void checkIndex(int imageIndex) {
		if (imageIndex < minIndex) {
			throw new IndexOutOfBoundsException("imageIndex < minIndex!");
		}
		if (seekForwardOnly) {
			minIndex = imageIndex;
		}
	}

	public int getWidth(int imageIndex)
					throws
					IIOException {
		checkIndex(imageIndex);

		int index = locateImage(imageIndex);
		if (index != imageIndex) {
			throw new IndexOutOfBoundsException();
		}
		readMetadata();
		return gifImageMetadata.imageWidth;
	}

	public int getHeight(int imageIndex)
					throws
					IIOException {
		checkIndex(imageIndex);

		int index = locateImage(imageIndex);
		if (index != imageIndex) {
			throw new IndexOutOfBoundsException();
		}
		readMetadata();
		return gifImageMetadata.imageHeight;
	}

	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
					throws
					IIOException {
		checkIndex(imageIndex);

		int index = locateImage(imageIndex);
		if (index != imageIndex) {
			throw new IndexOutOfBoundsException();
		}
		readMetadata();

		List<ImageTypeSpecifier> l = new ArrayList<>(1);

		byte[] colorTable;
		if (gifImageMetadata.localColorTable != null) {
			colorTable = gifImageMetadata.localColorTable;
		} else {
			colorTable = gifStreamMetadata.globalColorTable;
		}

		// Normalize color table length to 2^1, 2^2, 2^4, or 2^8
		int length = colorTable.length / 3;
		int bits;
		if (length == 2) {
			bits = 1;
		} else if (length == 4) {
			bits = 2;
		} else if (length == 8 || length == 16) {
			// Bump from 3 to 4 bits
			bits = 4;
		} else {
			// Bump to 8 bits
			bits = 8;
		}
		int    lutLength = 1 << bits;
		byte[] r         = new byte[lutLength];
		byte[] g         = new byte[lutLength];
		byte[] b         = new byte[lutLength];

		// Entries from length + 1 to lutLength - 1 will be 0
		int rgbIndex = 0;
		for (int i = 0;
		     i < length;
		     i++) {
			r[i] = colorTable[rgbIndex++];
			g[i] = colorTable[rgbIndex++];
			b[i] = colorTable[rgbIndex++];
		}

		byte[] a = null;
		if (gifImageMetadata.transparentColorFlag) {
			a = new byte[lutLength];
			Arrays.fill(a,
			            (byte) 255);

			// Some files erroneously have a transparent color index
			// of 255 even though there are fewer than 256 colors.
			int idx = Math.min(gifImageMetadata.transparentColorIndex,
			                   lutLength - 1);
			a[idx] = (byte) 0;
		}

		//		int[] bitsPerSample = new int[1];
		//		bitsPerSample[0] = bits;
		ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createIndexed(r,
		                                                                         g,
		                                                                         b,
		                                                                         a,
		                                                                         bits,
		                                                                         DataBuffer.TYPE_BYTE);
		l.add(imageTypeSpecifier);
		return l.iterator();
	}

	public ImageReadParam getDefaultReadParam() {
		return new ImageReadParam();
	}

	public IIOMetadata getStreamMetadata()
					throws
					IIOException {
		readHeader();
		return gifStreamMetadata;
	}

	public IIOMetadata getImageMetadata(int imageIndex)
					throws
					IIOException {
		checkIndex(imageIndex);

		int index = locateImage(imageIndex);
		if (index != imageIndex) {
			throw new IndexOutOfBoundsException("Bad image index!");
		}
		readMetadata();
		return gifImageMetadata;
	}

	private void initNext32Bits() {
		next32Bits = block[0] & 0xff;
		next32Bits |= (block[1] & 0xff) << 8;
		next32Bits |= (block[2] & 0xff) << 16;
		next32Bits |= block[3] << 24;
		nextByte = 4;
	}

	// Load a block (1-255 bytes) at a time, and maintain
	// a 32-bit lookahead buffer that is filled from the left
	// and extracted from the right.
	//
	// When the last block is found, we continue to
	//
	private int getCode(int codeSize,
	                    int codeMask)
					throws
					IOException {
		if (bitPos + codeSize > 32) {
			return eofCode; // No more data available
		}

		int code = (next32Bits >> bitPos) & codeMask;
		bitPos += codeSize;

		// Shift in a byte of new data at a time
		while (bitPos >= 8 && !lastBlockFound) {
			next32Bits >>>= 8;
			bitPos -= 8;

			// Check if current block is out of bytes
			if (nextByte >= blockLength) {
				// Get next block size
				blockLength = imageInputStream.readUnsignedByte();
				if (blockLength == 0) {
					lastBlockFound = true;
					return code;
				} else {
					int left = blockLength;
					int off  = 0;
					while (left > 0) {
						int nbytes = imageInputStream.read(block,
						                                   off,
						                                   left);
						off += nbytes;
						left -= nbytes;
					}
					nextByte = 0;
				}
			}

			next32Bits |= block[nextByte++] << 24;
		}

		return code;
	}

	public void initializeStringTable(int[] prefix,
	                                  byte[] suffix,
	                                  byte[] initial,
	                                  int[] length) {
		int numEntries = 1 << initCodeSize;
		for (int i = 0;
		     i < numEntries;
		     i++) {
			prefix[i] = -1;
			suffix[i] = (byte) i;
			initial[i] = (byte) i;
			length[i] = 1;
		}

		// Fill in the entire table for robustness against
		// out-of-sequence codes.
		for (int i = numEntries;
		     i < 4096;
		     i++) {
			prefix[i] = -1;
			length[i] = 1;
		}

		// tableIndex = numEntries + 2;
		// codeSize = initCodeSize + 1;
		// codeMask = (1 << codeSize) - 1;
	}

	private void outputRow() {
		// Clip against ImageReadParam
		int width = Math.min(sourceRegion.width,
		                     destinationRegion.width * sourceXSubsampling);
		int destX = destinationRegion.x;

		if (sourceXSubsampling == 1) {
			writableRaster.setDataElements(destX,
			                               destY,
			                               width,
			                               1,
			                               rowBuf);
		} else {
			for (int x = 0;
			     x < width;
			     x += sourceXSubsampling, destX++) {
				writableRaster.setSample(destX,
				                         destY,
				                         0,
				                         rowBuf[x] & 0xff);
			}
		}

		// Update IIOReadUpdateListeners, if any
		if (updateListeners != null) {
			int[] bands = {0};
			// updateYStep will have been initialized if
			// updateListeners is non-null
			processImageUpdate(bufferedImage,
			                   destX,
			                   destY,
			                   width,
			                   1,
			                   1,
			                   updateYStep,
			                   bands);
		}
	}

	private void computeDecodeThisRow() {
		this.decodeThisRow = (destY < destinationRegion.y + destinationRegion.height) && (streamY >= sourceRegion.y) && (
						streamY
						< sourceRegion.y + sourceRegion.height) && (((streamY - sourceRegion.y) % sourceYSubsampling) == 0);
	}

	private void outputPixels(byte[] string,
	                          int len) {
		if (interlacePass < sourceMinProgressivePass || interlacePass > sourceMaxProgressivePass) {
			return;
		}

		for (int i = 0;
		     i < len;
		     i++) {
			if (streamX >= sourceRegion.x) {
				rowBuf[streamX - sourceRegion.x] = string[i];
			}

			// Process end-of-row
			++streamX;
			if (streamX == width) {
				// Update IIOReadProgressListeners
				++rowsDone;
				processImageProgress(100.0F * rowsDone / height);

				if (decodeThisRow) {
					outputRow();
				}

				streamX = 0;
				if (gifImageMetadata.interlaceFlag) {
					streamY += interlaceIncrement[interlacePass];
					if (streamY >= height) {
						// Inform IIOReadUpdateListeners of end of pass
						if (updateListeners != null) {
							processPassComplete(bufferedImage);
						}

						++interlacePass;
						if (interlacePass > sourceMaxProgressivePass) {
							return;
						}
						streamY = interlaceOffset[interlacePass];
						startPass(interlacePass);
					}
				} else {
					++streamY;
				}

				// Determine whether pixels from this row will
				// be written to the destination
				this.destY = destinationRegion.y + (streamY - sourceRegion.y) / sourceYSubsampling;
				computeDecodeThisRow();
			}
		}
	}

	// END LZW STUFF

	private void readHeader()
					throws
					IIOException {
		if (gotHeader) {
			return;
		}
		if (imageInputStream == null) {
			throw new IllegalStateException("Input not set!");
		}

		// Create an object to store the stream metadata
		this.gifStreamMetadata = new GIFStreamMetadata();

		try {
			imageInputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

			byte[] signature = new byte[6];
			imageInputStream.readFully(signature);

			String version = new StringBuilder().append((char) signature[3])
			                                    .append((char) signature[4])
			                                    .append((char) signature[5])
			                                    .toString();
			gifStreamMetadata.version = version;

			gifStreamMetadata.logicalScreenWidth = imageInputStream.readUnsignedShort();
			gifStreamMetadata.logicalScreenHeight = imageInputStream.readUnsignedShort();

			int     packedFields         = imageInputStream.readUnsignedByte();
			boolean globalColorTableFlag = (packedFields & 0x80) != 0;
			gifStreamMetadata.colorResolution = ((packedFields >> 4) & 0x7) + 1;
			gifStreamMetadata.sortFlag = (packedFields & 0x8) != 0;
			int numGCTEntries = 1 << ((packedFields & 0x7) + 1);

			gifStreamMetadata.backgroundColorIndex = imageInputStream.readUnsignedByte();
			gifStreamMetadata.pixelAspectRatio = imageInputStream.readUnsignedByte();

			if (globalColorTableFlag) {
				gifStreamMetadata.globalColorTable = new byte[3 * numGCTEntries];
				imageInputStream.readFully(gifStreamMetadata.globalColorTable);
			} else {
				gifStreamMetadata.globalColorTable = null;
			}

			// Found position of metadata for image 0
			imageStartPosition.add(imageInputStream.getStreamPosition());
		} catch (IOException e) {
			throw new IIOException("I/O error reading header!",
			                       e);
		}

		gotHeader = true;
	}

	private boolean skipImage()
					throws
					IIOException {
		// Stream must be at the beginning of an image descriptor
		// upon exit

		try {
			while (true) {
				int blockType = imageInputStream.readUnsignedByte();

				if (blockType == 0x2c) {
					imageInputStream.skipBytes(8);

					int packedFields = imageInputStream.readUnsignedByte();
					if ((packedFields & 0x80) != 0) {
						// Skip color table if any
						int bits = (packedFields & 0x7) + 1;
						imageInputStream.skipBytes(3 * (1 << bits));
					}

					imageInputStream.skipBytes(1);

					int length;
					do {
						length = imageInputStream.readUnsignedByte();
						imageInputStream.skipBytes(length);
					} while (length > 0);

					return true;
				} else if (blockType == 0x3b) {
					return false;
				} else if (blockType == 0x21) {
					int label = imageInputStream.readUnsignedByte();

					int length;
					do {
						length = imageInputStream.readUnsignedByte();
						imageInputStream.skipBytes(length);
					} while (length > 0);
				} else if (blockType == 0x0) {
					// EOF
					return false;
				} else {
					int length;
					do {
						length = imageInputStream.readUnsignedByte();
						imageInputStream.skipBytes(length);
					} while (length > 0);
				}
			}
		} catch (EOFException e) {
			return false;
		} catch (IOException e) {
			throw new IIOException("I/O error locating image!",
			                       e);
		}
	}

	private int locateImage(int imageIndex)
					throws
					IIOException {
		readHeader();

		try {
			// Find closest known index
			int index = Math.min(imageIndex,
			                     imageStartPosition.size() - 1);

			// Seek to that position
			Long l = imageStartPosition.get(index);
			imageInputStream.seek(l);

			// Skip images until at desired index or last image found
			while (index < imageIndex) {
				if (!skipImage()) {
					--index;
					return index;
				}

				Long l1 = imageInputStream.getStreamPosition();
				imageStartPosition.add(l1);
				++index;
			}
		} catch (IOException e) {
			throw new IIOException("Couldn't seek!",
			                       e);
		}

		if (currIndex != imageIndex) {
			gifImageMetadata = null;
		}
		currIndex = imageIndex;
		return imageIndex;
	}

	// Read blocks of 1-255 bytes, stop at a 0-length block
	private byte[] concatenateBlocks()
					throws
					IOException {
		byte[] data = new byte[0];
		while (true) {
			int length = imageInputStream.readUnsignedByte();
			if (length == 0) {
				break;
			}
			byte[] newData = new byte[data.length + length];
			System.arraycopy(data,
			                 0,
			                 newData,
			                 0,
			                 data.length);
			imageInputStream.readFully(newData,
			                           data.length,
			                           length);
			data = newData;
		}

		return data;
	}

	// Stream must be positioned at start of metadata for 'currIndex'
	private void readMetadata()
					throws
					IIOException {
		if (imageInputStream == null) {
			throw new IllegalStateException("Input not set!");
		}

		try {
			// Create an object to store the image metadata
			this.gifImageMetadata = new GIFImageMetadata();

			long startPosition = imageInputStream.getStreamPosition();
			while (true) {
				int blockType = imageInputStream.readUnsignedByte();
				if (blockType == 0x2c) { // Image Descriptor
					gifImageMetadata.imageLeftPosition = imageInputStream.readUnsignedShort();
					gifImageMetadata.imageTopPosition = imageInputStream.readUnsignedShort();
					gifImageMetadata.imageWidth = imageInputStream.readUnsignedShort();
					gifImageMetadata.imageHeight = imageInputStream.readUnsignedShort();

					int     idPackedFields      = imageInputStream.readUnsignedByte();
					boolean localColorTableFlag = (idPackedFields & 0x80) != 0;
					gifImageMetadata.interlaceFlag = (idPackedFields & 0x40) != 0;
					gifImageMetadata.sortFlag = (idPackedFields & 0x20) != 0;
					int numLCTEntries = 1 << ((idPackedFields & 0x7) + 1);

					if (localColorTableFlag) {
						// Read color table if any
						gifImageMetadata.localColorTable = new byte[3 * numLCTEntries];
						imageInputStream.readFully(gifImageMetadata.localColorTable);
					} else {
						gifImageMetadata.localColorTable = null;
					}

					// Record length of this metadata block
					this.imageMetadataLength = (int) (imageInputStream.getStreamPosition() - startPosition);

					// Now positioned at start of LZW-compressed pixels
					return;
				} else if (blockType == 0x21) { // Extension block
					int label = imageInputStream.readUnsignedByte();

					if (label == 0xf9) { // Graphics Control Extension
						int gceLength       = imageInputStream.readUnsignedByte(); // 4
						int gcePackedFields = imageInputStream.readUnsignedByte();
						gifImageMetadata.disposalMethod = (gcePackedFields >> 2) & 0x3;
						gifImageMetadata.userInputFlag = (gcePackedFields & 0x2) != 0;
						gifImageMetadata.transparentColorFlag = (gcePackedFields & 0x1) != 0;

						gifImageMetadata.delayTime = imageInputStream.readUnsignedShort();
						gifImageMetadata.transparentColorIndex = imageInputStream.readUnsignedByte();

						int terminator = imageInputStream.readUnsignedByte();
					} else if (label == 0x1) { // Plain text extension
						int length = imageInputStream.readUnsignedByte();
						gifImageMetadata.hasPlainTextExtension = true;
						gifImageMetadata.textGridLeft = imageInputStream.readUnsignedShort();
						gifImageMetadata.textGridTop = imageInputStream.readUnsignedShort();
						gifImageMetadata.textGridWidth = imageInputStream.readUnsignedShort();
						gifImageMetadata.textGridHeight = imageInputStream.readUnsignedShort();
						gifImageMetadata.characterCellWidth = imageInputStream.readUnsignedByte();
						gifImageMetadata.characterCellHeight = imageInputStream.readUnsignedByte();
						gifImageMetadata.textForegroundColor = imageInputStream.readUnsignedByte();
						gifImageMetadata.textBackgroundColor = imageInputStream.readUnsignedByte();
						gifImageMetadata.text = concatenateBlocks();
					} else if (label == 0xfe) { // Comment extension
						byte[] comment = concatenateBlocks();
						if (gifImageMetadata.comments == null) {
							gifImageMetadata.comments = new ArrayList<>();
						}
						gifImageMetadata.comments.add(comment);
					} else if (label == 0xff) { // Application extension
						int    blockSize     = imageInputStream.readUnsignedByte();
						byte[] applicationID = new byte[8];
						byte[] authCode      = new byte[3];

						// read available data
						byte[] blockData = new byte[blockSize];
						imageInputStream.readFully(blockData);

						int offset = copyData(blockData,
						                      0,
						                      applicationID);
						offset = copyData(blockData,
						                  offset,
						                  authCode);

						byte[] applicationData = concatenateBlocks();

						if (offset < blockSize) {
							int    len  = blockSize - offset;
							byte[] data = new byte[len + applicationData.length];

							System.arraycopy(blockData,
							                 offset,
							                 data,
							                 0,
							                 len);
							System.arraycopy(applicationData,
							                 0,
							                 data,
							                 len,
							                 applicationData.length);

							applicationData = data;
						}

						// Init lists if necessary
						if (gifImageMetadata.applicationIDs == null) {
							gifImageMetadata.applicationIDs = new ArrayList<>();
							gifImageMetadata.authenticationCodes = new ArrayList<>();
							gifImageMetadata.applicationData = new ArrayList<>();
						}
						gifImageMetadata.applicationIDs.add(applicationID);
						gifImageMetadata.authenticationCodes.add(authCode);
						gifImageMetadata.applicationData.add(applicationData);
					} else {
						// Skip over unknown extension blocks
						int length = 0;
						do {
							length = imageInputStream.readUnsignedByte();
							imageInputStream.skipBytes(length);
						} while (length > 0);
					}
				} else if (blockType == 0x3b) { // Trailer
					throw new IndexOutOfBoundsException("Attempt to read past end of image sequence!");
				} else {
					throw new IIOException("Unexpected block type " + blockType + "!");
				}
			}
		} catch (IIOException iioe) {
			throw iioe;
		} catch (IOException ioe) {
			throw new IIOException("I/O error reading image metadata!",
			                       ioe);
		}
	}

	private int copyData(byte[] src,
	                     int offset,
	                     byte[] dst) {
		int len  = dst.length;
		int rest = src.length - offset;
		if (len > rest) {
			len = rest;
		}
		System.arraycopy(src,
		                 offset,
		                 dst,
		                 0,
		                 len);
		return offset + len;
	}

	private void startPass(int pass) {
		if (updateListeners == null) {
			return;
		}

		int y     = 0;
		int yStep = 1;
		if (gifImageMetadata.interlaceFlag) {
			y = interlaceOffset[interlacePass];
			yStep = interlaceIncrement[interlacePass];
		}

		int[] computeUpdatedPixels = ReaderUtil.computeUpdatedPixels(sourceRegion,
		                                                             destinationOffset,
		                                                             destinationRegion.x,
		                                                             destinationRegion.y,
		                                                             destinationRegion.x + destinationRegion.width - 1,
		                                                             destinationRegion.y + destinationRegion.height - 1,
		                                                             sourceXSubsampling,
		                                                             sourceYSubsampling,
		                                                             0,
		                                                             y,
		                                                             destinationRegion.width,
		                                                             (destinationRegion.height + yStep - 1) / yStep,
		                                                             1,
		                                                             yStep);

		// Initialized updateMinY and updateYStep
		this.updateMinY = computeUpdatedPixels[1];
		this.updateYStep = computeUpdatedPixels[5];

		// Inform IIOReadUpdateListeners of new pass
		int[] bands = {0};

		processPassStarted(bufferedImage,
		                   interlacePass,
		                   sourceMinProgressivePass,
		                   sourceMaxProgressivePass,
		                   0,
		                   updateMinY,
		                   1,
		                   updateYStep,
		                   bands);
	}

	public BufferedImage read(int imageIndex,
	                          ImageReadParam param)
					throws
					IIOException {
		if (imageInputStream == null) {
			throw new IllegalStateException("Input not set!");
		}
		checkIndex(imageIndex);

		int index = locateImage(imageIndex);
		if (index != imageIndex) {
			throw new IndexOutOfBoundsException("imageIndex out of bounds!");
		}

		clearAbortRequest();
		readMetadata();

		// A null ImageReadParam means we use the default
		if (param == null) {
			param = getDefaultReadParam();
		}

		// Initialize the destination image
		Iterator<ImageTypeSpecifier> imageTypes = getImageTypes(imageIndex);
		this.bufferedImage = getDestination(param,
		                                    imageTypes,
		                                    gifImageMetadata.imageWidth,
		                                    gifImageMetadata.imageHeight);
		this.writableRaster = bufferedImage.getWritableTile(0,
		                                                    0);
		this.width = gifImageMetadata.imageWidth;
		this.height = gifImageMetadata.imageHeight;
		this.streamX = 0;
		this.streamY = 0;
		this.rowsDone = 0;
		this.interlacePass = 0;

		// Get source region, taking subsampling offsets into account,
		// and clipping against the true source bounds

		this.sourceRegion = new Rectangle(0,
		                                  0,
		                                  0,
		                                  0);
		this.destinationRegion = new Rectangle(0,
		                                       0,
		                                       0,
		                                       0);
		computeRegions(param,
		               width,
		               height,
		               bufferedImage,
		               sourceRegion,
		               destinationRegion);
		this.destinationOffset = new Point(destinationRegion.x,
		                                   destinationRegion.y);

		this.sourceXSubsampling = param.getSourceXSubsampling();
		this.sourceYSubsampling = param.getSourceYSubsampling();
		this.sourceMinProgressivePass = Math.max(param.getSourceMinProgressivePass(),
		                                         0);
		this.sourceMaxProgressivePass = Math.min(param.getSourceMaxProgressivePass(),
		                                         3);

		this.destY = destinationRegion.y + (streamY - sourceRegion.y) / sourceYSubsampling;
		computeDecodeThisRow();

		// Inform IIOReadProgressListeners of start of image
		processImageStarted(imageIndex);
		startPass(0);

		this.rowBuf = new byte[width];

		try {
			// Read and decode the image data, fill in theImage
			this.initCodeSize = imageInputStream.readUnsignedByte();

			// Read first data block
			this.blockLength = imageInputStream.readUnsignedByte();
			int left = blockLength;
			int off  = 0;
			while (left > 0) {
				int nbytes = imageInputStream.read(block,
				                                   off,
				                                   left);
				left -= nbytes;
				off += nbytes;
			}

			this.bitPos = 0;
			this.nextByte = 0;
			this.lastBlockFound = false;
			this.interlacePass = 0;

			// Init 32-bit buffer
			initNext32Bits();

			this.clearCode = 1 << initCodeSize;
			this.eofCode = clearCode + 1;

			int code, oldCode = 0;

			int[]  prefix  = new int[4096];
			byte[] suffix  = new byte[4096];
			byte[] initial = new byte[4096];
			int[]  length  = new int[4096];
			byte[] string  = new byte[4096];

			initializeStringTable(prefix,
			                      suffix,
			                      initial,
			                      length);
			int tableIndex = (1 << initCodeSize) + 2;
			int codeSize   = initCodeSize + 1;
			int codeMask   = (1 << codeSize) - 1;

			while (!abortRequested()) {
				code = getCode(codeSize,
				               codeMask);

				if (code == clearCode) {
					initializeStringTable(prefix,
					                      suffix,
					                      initial,
					                      length);
					tableIndex = (1 << initCodeSize) + 2;
					codeSize = initCodeSize + 1;
					codeMask = (1 << codeSize) - 1;

					code = getCode(codeSize,
					               codeMask);
					if (code == eofCode) {
						// Inform IIOReadProgressListeners of end of image
						processImageComplete();
						return bufferedImage;
					}
				} else if (code == eofCode) {
					// Inform IIOReadProgressListeners of end of image
					processImageComplete();
					return bufferedImage;
				} else {
					int newSuffixIndex;
					if (code < tableIndex) {
						newSuffixIndex = code;
					} else { // code == tableIndex
						newSuffixIndex = oldCode;
						if (code != tableIndex) {
							// warning - code out of sequence
							// possibly data corruption
							processWarningOccurred("Out-of-sequence code!");
						}
					}

					try {
						int ti = tableIndex;

						int oc = oldCode;

						prefix[ti] = oc;
						suffix[ti] = initial[newSuffixIndex];
						initial[ti] = initial[oc];
						length[ti] = length[oc] + 1;

						++tableIndex;
						if ((tableIndex == (1 << codeSize)) && (tableIndex < 4096)) {
							++codeSize;
							codeMask = (1 << codeSize) - 1;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						//Die.
						//Pretend that the clear code was found.
						initializeStringTable(prefix,
						                      suffix,
						                      initial,
						                      length);
						tableIndex = (1 << initCodeSize) + 2;
						codeSize = initCodeSize + 1;
						codeMask = (1 << codeSize) - 1;

						code = getCode(codeSize,
						               codeMask);
						if (code == eofCode) {
							// Inform IIOReadProgressListeners of end of image
							processImageComplete();
							return bufferedImage;
						}
					}
				}

				// Reverse code
				int c   = code;
				int len = length[c];
				for (int i = len - 1;
				     i >= 0;
				     i--) {
					string[i] = suffix[c];
					c = prefix[c];
				}

				outputPixels(string,
				             len);
				oldCode = code;
			}

			processReadAborted();
			return bufferedImage;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IIOException("I/O error reading image!",
			                       e);
		}
	}

	/**
	 * Remove all settings including global settings such as
	 * <code>Locale</code>s and listeners, as well as stream settings.
	 */
	public void reset() {
		super.reset();
		resetStreamSettings();
	}

	/**
	 * Remove local settings based on parsing of a stream.
	 */
	private void resetStreamSettings() {
		gotHeader = false;
		gifStreamMetadata = null;
		currIndex = -1;
		gifImageMetadata = null;
		imageStartPosition = new ArrayList<>();
		numImages = -1;

		// No need to reinitialize 'block'
		blockLength = 0;
		bitPos = 0;
		nextByte = 0;

		next32Bits = 0;
		lastBlockFound = false;

		bufferedImage = null;
		writableRaster = null;
		width = -1;
		height = -1;
		streamX = -1;
		streamY = -1;
		rowsDone = 0;
		interlacePass = 0;
	}
}
