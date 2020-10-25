package de.onyxbits.giftedmotion;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Accessory for the Loading dialog
 */
public class LoadAccessory
				extends JPanel
				implements PropertyChangeListener {

	/**
	 * Lets the user pick a default showtime for each frame
	 */
	private JSpinner showtime = new JSpinner(new SpinnerNumberModel(100,
	                                                                0,
	                                                                1000000,
	                                                                10));

	/**
	 * Image preview canvas
	 */
	private Preview preview = new Preview();

	public LoadAccessory() {
		setLayout(new BoxLayout(this,
		                        BoxLayout.Y_AXIS));
		JScrollPane cont1 = new JScrollPane(getPreview());
		cont1.setPreferredSize(new Dimension(200,
		                                     150));
		add(cont1);
		cont1.setBorder(BorderFactory.createTitledBorder(Translations.get("loadaccessory.loadaccessory.preview")));
	}

	// Interface implemented
	public void propertyChange(PropertyChangeEvent event) {
		Preview preview = getPreview();
		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(event.getPropertyName())) {
			File file = (File) event.getNewValue();
			preview.show(file);
		}
		if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(event.getPropertyName())) {
			File[] files = (File[]) event.getNewValue();
			if (files == null || files.length == 0) {
				preview.show(null);
			} else {
				preview.show(files[files.length - 1]);
			}
		}
	}

	/**
	 * Lets the user pick a default showtime for each frame
	 */
	public JSpinner getShowtime() {
		return showtime;
	}

	public void setShowtime(JSpinner showtime) {
		this.showtime = showtime;
	}

	/**
	 * Image preview canvas
	 */
	public Preview getPreview() {
		return preview;
	}

	public void setPreview(Preview preview) {
		this.preview = preview;
	}
}