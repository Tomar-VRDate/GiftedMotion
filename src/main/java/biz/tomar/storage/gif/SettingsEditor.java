package biz.tomar.storage.gif;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Display settings to be used for exporting the frames into the actual
 * animated GIF.
 */
public class SettingsEditor
				extends JInternalFrame
				implements ChangeListener,
				           ActionListener,
				           MouseListener {
	/**
	 * The colorchooser for the transparency color
	 */
	private final JColorChooser colorChooser = new JColorChooser(Color.MAGENTA);

	/**
	 * The transparency color
	 */
	private final JButton transparencyColor = new JButton(new ColorIcon(Color.MAGENTA,
	                                                                    16,
	                                                                    16));

	/**
	 * Quality of the dithering
	 */
	private final JSpinner quality = new JSpinner(new SpinnerNumberModel(1,
	                                                                     1,
	                                                                     256,
	                                                                     1));

	/**
	 * How often to repeat
	 */
	private JSpinner repeat = new JSpinner(new SpinnerNumberModel(0,
	                                                              -1,
	                                                              10000,
	                                                              1));

	public SettingsEditor() {
		super(Translations.get("settingseditor.settingseditor.title"),
		      false,
		      true,
		      false,
		      false);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setContentPane(getContent());

		JColorChooser colorChooser = getColorChooser();
		colorChooser.setPreviewPanel(new JPanel());
		colorChooser.getSelectionModel()
		            .addChangeListener(this);

		JButton transparencyColor = getTransparencyColor();
		transparencyColor.addActionListener(this);
		transparencyColor.setToolTipText(Translations.get("settingseditor.settingseditor.trans"));

		JSpinner quality = getQuality();
		quality.setToolTipText(Translations.get("settingseditor.settingseditor.quality"));

		JSpinner repeat = getRepeat();
		repeat.setToolTipText(Translations.get("settingseditor.settingseditor.repeat"));
		pack();
	}

	/**
	 * Build the contentpane
	 */
	private JPanel getContent() {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content,
		                                BoxLayout.Y_AXIS));
		JColorChooser colorChooser = getColorChooser();
		content.add(colorChooser);

		JPanel ctrl = new JPanel();
		ctrl.setLayout(new GridLayout(3,
		                              2));

		ctrl.add(new JLabel(Translations.get("settingseditor.getcontent.trans")));
		JButton transparencyColor = getTransparencyColor();
		ctrl.add(transparencyColor);

		ctrl.add(new JLabel(Translations.get("settingseditor.getcontent.dither")));
		JSpinner quality = getQuality();
		ctrl.add(quality);

		ctrl.add(new JLabel(Translations.get("settingseditor.getcontent.repeat")));
		JSpinner repeat = getRepeat();
		ctrl.add(repeat);

		content.add(ctrl);
		return content;
	}

	/**
	 * The colorchooser for the transparency color
	 */
	public JColorChooser getColorChooser() {
		return colorChooser;
	}

	public Color getChosenColor() {
		JColorChooser colorChooser = getColorChooser();
		Color         chosenColor  = colorChooser.getColor();
		return chosenColor;
	}

	public void setChosenColor(Color color) {
		setChosenTransparencyColor(color);
		JColorChooser colorChooser = getColorChooser();
		colorChooser.setColor(color);
	}

	/**
	 * The transparency color
	 */
	public JButton getTransparencyColor() {
		return transparencyColor;
	}

	public Color getChosenTransparencyColor() {
		JButton   transparencyColor     = getTransparencyColor();
		ColorIcon transparencyColorIcon = (ColorIcon) transparencyColor.getIcon();
		Color     color                 = transparencyColorIcon.getColor();
		return color;
	}

	public void setChosenTransparencyColor(Color chosenColor) {
		JButton transparencyColor = getTransparencyColor();
		ColorIcon colorIcon = new ColorIcon(chosenColor,
		                                    16,
		                                    16);
		transparencyColor.setIcon(colorIcon);
	}

	/**
	 * Quality of the dithering
	 */
	public JSpinner getQuality() {
		return quality;
	}

	/**
	 * How often to repeat
	 */
	public JSpinner getRepeat() {
		return repeat;
	}

	public void setRepeat(JSpinner repeat) {
		this.repeat = repeat;
	}

	/**
	 * Query user preferences
	 *
	 * @return a settings object, reflecting the user preferences
	 */
	public Settings getSettings() {
		Settings settings = new Settings();
		Color    color    = getChosenTransparencyColor();
		settings.setTransparencyColor(color);

		JSpinner quality      = getQuality();
		Object   qualityValue = quality.getValue();
		settings.setQuality((Integer) qualityValue);

		JSpinner repeat      = getRepeat();
		Object   repeatValue = repeat.getValue();
		settings.setRepeat((Integer) repeatValue);
		return settings;
	}

	public void stateChanged(ChangeEvent evt) {
		Color chosenColor = getChosenColor();
		setChosenTransparencyColor(chosenColor);
	}

	public void actionPerformed(ActionEvent e) {
		setChosenColor(null);
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		int y = getY();
		if (y < 0) {
			int x = getX();
			setLocation(x,
			            0);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) {}
}