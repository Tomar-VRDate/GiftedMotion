package de.onyxbits.giftedmotion;

import java.io.File;

/**
 * FileFilter, that only allows image files and optionally directories
 */
public class ImageFileFilter
				extends javax.swing.filechooser.FileFilter
				implements java.io.FileFilter {

	private final String[] ext = {"PNG",
	                              "JPG",
	                              "JPEG",
	                              "GIF",
	                              "BMP"};
	private final boolean  accept;

	/**
	 * Create a new FileFilter
	 *
	 * @param accept whether or not to also accept directories
	 */
	public ImageFileFilter(boolean accept) {
		this.accept = accept;
	}

	public String getDescription() {
		return Translations.get("imagefilefilter.desc");
	}

	public boolean accept(File file) {
		if (accept && file.isDirectory()) {
			return true;
		}
		for (String s : ext) {
			if (file.getName()
			        .toUpperCase()
			        .endsWith(s)) {
				return true;
			}
		}
		return false;
	}
}