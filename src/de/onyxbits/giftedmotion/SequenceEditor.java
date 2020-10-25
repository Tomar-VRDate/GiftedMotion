package de.onyxbits.giftedmotion;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;

/**
 * Edit the frame sequence
 */
public class SequenceEditor
				extends JInternalFrame
				implements ActionListener,
				           FrameSequenceListener,
				           ChangeListener,
				           ListSelectionListener,
				           ItemListener {

	/**
	 * Dispose codes in readable form
	 */
	private final String[]           dcodes    = {Translations.get("sequenceeditor.dcodes.0"),
	                                              Translations.get("sequenceeditor.dcodes.1"),
	                                              Translations.get("sequenceeditor.dcodes.2"),
	                                              Translations.get("sequenceeditor.dcodes.3")};
	/**
	 * X Offset
	 */
	private final JSpinner           xoff      = new JSpinner(new SpinnerNumberModel(0,
	                                                                                 -1000000,
	                                                                                 1000000,
	                                                                                 1));
	/**
	 * Y Offset
	 */
	private final JSpinner           yoff      = new JSpinner(new SpinnerNumberModel(0,
	                                                                                 -1000000,
	                                                                                 1000000,
	                                                                                 1));
	/**
	 * Rotation degrees
	 */
	private final JSpinner           rotation  = new JSpinner(new SpinnerNumberModel(0,
	                                                                                 -360,
	                                                                                 360,
	                                                                                 0.01));
	/**
	 * Scale X
	 */
	private final JSpinner           scaleX    = new JSpinner(new SpinnerNumberModel(0,
	                                                                                 -1000000,
	                                                                                 1000000,
	                                                                                 1f));
	/**
	 * Scale Y
	 */
	private final JSpinner           scaleY    = new JSpinner(new SpinnerNumberModel(0,
	                                                                                 -1000000,
	                                                                                 1000000,
	                                                                                 1f));
	/**
	 * Peer for SingleFrame.showtime
	 */
	private final JSpinner           showtime  = new JSpinner(new SpinnerNumberModel(100,
	                                                                                 1,
	                                                                                 1000000,
	                                                                                 10));
	/**
	 * Peer for SingleFrame.dispose
	 */
	private final JComboBox<String>  dispose   = new JComboBox<>(dcodes);
	/**
	 * Move frame in sequence
	 */
	private final JButton            sooner    = new JButton(IO.createIcon("Tango/22x22/actions/go-up.png",
	                                                                       Translations.get("sequenceeditor.sooner")));
	/**
	 * Mode frame in sequence
	 */
	private final JButton            later     = new JButton(IO.createIcon("Tango/22x22/actions/go-down.png",
	                                                                       Translations.get("sequenceeditor.later")));
	/**
	 * Duplicate current frame
	 */
	private final JButton            duplicate = new JButton(IO.createIcon("Tango/22x22/actions/edit-copy.png",
	                                                                       Translations.get("sequenceeditor.copy")));
	/**
	 * Trash current frame
	 */
	private final JButton            delete    = new JButton(IO.createIcon("Tango/22x22/actions/edit-delete.png",
	                                                                       Translations.get("sequenceeditor.delete")));
	/**
	 * The checkbox to apply the changes to all frames
	 */
	private final JCheckBox          apply     = new JCheckBox(Translations.get("sequenceeditor.apply"),
	                                                           false);
	/**
	 * The framesequence, displayed
	 */
	private final FrameSequence      frameSequence;
	/**
	 * Lists all frames in the sequence
	 */
	private final JList<SingleFrame> singleFrameJList;

	public SequenceEditor(FrameSequence frameSequence) {
		super(Translations.get("sequenceeditor.sequenceeditor.title"),
		      false,
		      false,
		      false,
		      false);

		this.frameSequence = frameSequence;
		singleFrameJList = new JList<>(frameSequence.getSingleFrames());

		setContentPane(getContent());
		pack();

		sooner.addActionListener(this);
		later.addActionListener(this);
		duplicate.addActionListener(this);
		delete.addActionListener(this);
		//dispose.addChangeListener(this);
		dispose.addItemListener(this);
		singleFrameJList.addListSelectionListener(this);
		showtime.addChangeListener(this);
		xoff.addChangeListener(this);
		yoff.addChangeListener(this);
		rotation.addChangeListener(this);
		scaleX.addChangeListener(this);
		scaleY.addChangeListener(this);

		apply.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.apply"));
		sooner.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.sooner"));
		later.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.later"));
		duplicate.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.duplicate"));
		delete.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.delete"));
		dispose.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.dispose"));
		showtime.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.showtime"));
		xoff.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.xoff"));
		yoff.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.yoff"));
		scaleX.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.scaleX"));
		scaleY.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.scaleY"));
		rotation.setToolTipText(Translations.get("sequenceeditor.sequenceeditor.rotation"));

		dataChanged(frameSequence);
	}

	/**
	 * Build the contentpane
	 */
	private JPanel getContent() {

		JPanel order = new JPanel();
		order.setLayout(new BoxLayout(order,
		                              BoxLayout.Y_AXIS));
		order.setBorder(BorderFactory.createTitledBorder(Translations.get("sequenceeditor.getcontent.order")));
		JPanel buttons = new JPanel();
		buttons.add(sooner);
		buttons.add(later);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(duplicate);
		buttons.add(delete);

		//frlst.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		order.add(new JScrollPane(singleFrameJList));
		order.add(buttons);
    
/*
    JPanel settings = new JPanel();
    settings.setBorder(BorderFactory.createTitledBorder(Translations.get("sequenceeditor.getcontent.settings")));
    settings.setLayout(new GridLayout(5,0));
    settings.add(new JLabel(Translations.get("sequenceeditor.getcontent.showtime")));
    settings.add(showtime);
    settings.add(new JLabel(Translations.get("sequenceeditor.getcontent.dispose")));
    settings.add(dispose);
    settings.add(new JLabel(Translations.get("sequenceeditor.getcontent.xoff")));
    settings.add(xoff);
    settings.add(new JLabel(Translations.get("sequenceeditor.getcontent.yoff")));
    settings.add(yoff);
    settings.add(apply);
*/


		JLabel showtimeLabel = new JLabel(Translations.get("sequenceeditor.getcontent.showtime"));
		JLabel disposeLabel  = new JLabel(Translations.get("sequenceeditor.getcontent.dispose"));
		JLabel xoffLabel     = new JLabel(Translations.get("sequenceeditor.getcontent.xoff"));
		JLabel yoffLabel     = new JLabel(Translations.get("sequenceeditor.getcontent.yoff"));
		JLabel scaleXLabel   = new JLabel(Translations.get("sequenceeditor.getcontent.scaleX"));
		JLabel scaleYLabel   = new JLabel(Translations.get("sequenceeditor.getcontent.scaleY"));
		JLabel rotationLabel = new JLabel(Translations.get("sequenceeditor.getcontent.rotation"));

		JPanel        settings      = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		settings.setLayout(gridBagLayout);
		settings.setBorder(BorderFactory.createTitledBorder(Translations.get("sequenceeditor.getcontent.settings")));
		GridBagConstraints gridBagConstraints = new GridBagConstraints();


		// Component: showtimeLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(showtimeLabel,
		                             gridBagConstraints);
		settings.add(showtimeLabel);

		// Component: showtime
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(showtime,
		                             gridBagConstraints);
		settings.add(showtime);

		// Component: disposeLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(disposeLabel,
		                             gridBagConstraints);
		settings.add(disposeLabel);

		// Component: dispose
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(dispose,
		                             gridBagConstraints);
		settings.add(dispose);

		// Component: xoffLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(xoffLabel,
		                             gridBagConstraints);
		settings.add(xoffLabel);

		// Component: xoff
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(xoff,
		                             gridBagConstraints);
		settings.add(xoff);

		// Component: yoffLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(yoffLabel,
		                             gridBagConstraints);
		settings.add(yoffLabel);

		// Component: yoff
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(yoff,
		                             gridBagConstraints);
		settings.add(yoff);

		// Component: scaleXLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(scaleXLabel,
		                             gridBagConstraints);
		settings.add(scaleXLabel);

		// Component: scaleX
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(scaleX,
		                             gridBagConstraints);
		settings.add(scaleX);

		// Component: scaleYLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(scaleYLabel,
		                             gridBagConstraints);
		settings.add(scaleYLabel);

		// Component: scaleY
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(scaleY,
		                             gridBagConstraints);
		settings.add(scaleY);

		// Component: rotationLabel
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       10);
		gridBagLayout.setConstraints(rotationLabel,
		                             gridBagConstraints);
		settings.add(rotationLabel);

		// Component: rotation
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(0,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(rotation,
		                             gridBagConstraints);
		settings.add(rotation);

		// Component: apply
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 100.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.insets = new Insets(12,
		                                       1,
		                                       1,
		                                       1);
		gridBagLayout.setConstraints(apply,
		                             gridBagConstraints);
		settings.add(apply);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content,
		                                BoxLayout.Y_AXIS));
		content.add(order);
		content.add(settings);
		return content;
	}

	public void actionPerformed(ActionEvent e) {
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		if (singleFrame == null) {
			return;
		}
		Object src = e.getSource();

		if (src == dispose) {
			singleFrame.setDispose(0);
			for (int i = 0;
			     i < dcodes.length;
			     i++) {
				String dcode = dcodes[i];
				if (dcode.equals(dispose.getSelectedItem())) {
					singleFrame.setDispose(i);
				}
			}
			frameSequence.fireDataChanged();
		}

		if (src == sooner) {
			frameSequence.move(singleFrame,
			                   true);
		}
		if (src == later) {
			frameSequence.move(singleFrame,
			                   false);
		}
		if (src == duplicate) {
			frameSequence.add(new SingleFrame(singleFrame),
			                  frameSequence.getSelectedIndex());
		}
		if (src == delete) {
			int prevSeq = frameSequence.getSelectedIndex();
			frameSequence.remove(singleFrame);
			if (prevSeq
			    <= singleFrameJList.getModel()
			                       .getSize() - 1) {
				singleFrameJList.setSelectedIndex(prevSeq);
			} else {
				singleFrameJList.setSelectedIndex(singleFrameJList.getModel()
				                                                  .getSize() - 1);
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		if (singleFrame == null) {
			return;
		}

		Object        src          = e.getSource();
		SingleFrame[] singleFrames = frameSequence.getSingleFrames();
		if (src == showtime) {
			int val = (Integer) showtime.getValue();
			if (apply.isSelected()) {
				for (int i = 0;
				     i < singleFrames.length;
				     i++) {
					singleFrames[i].setShowtime(val);
				}
			} else {
				singleFrame.setShowtime((Integer) showtime.getValue());
			}
			frameSequence.fireDataChanged();
		}

		if (src == dispose) {
			int val = 0;
			for (int i = 0;
			     i < dcodes.length;
			     i++) {
				String dcode = dcodes[i];
				if (dcode.equals(dispose.getSelectedItem())) {
					val = i;
				}
			}
			if (apply.isSelected()) {
				for (SingleFrame frame : singleFrames) {
					frame.setDispose(val);
				}
			} else {
				singleFrame.setDispose(val);
			}
			frameSequence.fireDataChanged();
		}

		if (src == xoff || src == yoff) {
			int x = (Integer) Objects.requireNonNull(xoff)
			                         .getValue();
			int y = (Integer) Objects.requireNonNull(yoff)
			                         .getValue();
			Point val = new Point(x,
			                      y);
			if (apply.isSelected()) {
				for (SingleFrame frame : singleFrames) {
					frame.setPosition(val);
				}
			} else {
				singleFrame.setPosition(val);
			}
			frameSequence.fireDataChanged();
		}

		if (src == rotation) {
			double rot = (Double) Objects.requireNonNull(rotation)
			                             .getModel()
			                             .getValue();
			if (apply.isSelected()) {
				for (SingleFrame frame : singleFrames) {
					frame.setRotationDegrees(rot);
				}
			} else {
				singleFrame.setRotationDegrees(rot);
			}

			frameSequence.fireDataChanged();
		}

		if (src == scaleX || src == scaleY) {
			float scaleX = ((Double) Objects.requireNonNull(this.scaleX)
			                                .getValue()).floatValue();
			float scaleY = ((Double) Objects.requireNonNull(this.scaleY)
			                                .getValue()).floatValue();
			if (apply.isSelected()) {
				for (SingleFrame frame : singleFrames) {
					frame.setScaleX(scaleX);
					frame.setScaleY(scaleY);
				}
			} else {
				singleFrame.setScaleX(scaleX);
				singleFrame.setScaleY(scaleY);
			}
			frameSequence.fireDataChanged();
		}
	}

	public void dataChanged(FrameSequence frameSequence) {
		singleFrameJList.removeListSelectionListener(this);
		//dispose.removeChangeListener(this);
		dispose.removeItemListener(this);
		showtime.removeChangeListener(this);
		xoff.removeChangeListener(this);
		yoff.removeChangeListener(this);
		rotation.removeChangeListener(this);
		scaleX.removeChangeListener(this);
		scaleY.removeChangeListener(this);

		singleFrameJList.setListData(this.frameSequence.getSingleFrames());
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		singleFrameJList.setSelectedValue(singleFrame,
		                                  true);
		if (singleFrame != null) {
			dispose.setSelectedItem(dcodes[singleFrame.getDispose()]);
			showtime.setValue(singleFrame.getShowtime());
			xoff.setValue(singleFrame.getPosition().x);
			yoff.setValue(singleFrame.getPosition().y);
			scaleX.setValue((double) singleFrame.getScaleX());
			scaleY.setValue((double) singleFrame.getScaleY());
			rotation.setValue(singleFrame.getRotationDegrees());
		}

		singleFrameJList.addListSelectionListener(this);
		//dispose.addChangeListener(this);
		dispose.addItemListener(this);
		showtime.addChangeListener(this);
		xoff.addChangeListener(this);
		yoff.addChangeListener(this);
		rotation.addChangeListener(this);
		scaleX.addChangeListener(this);
		scaleY.addChangeListener(this);
	}

	public void valueChanged(ListSelectionEvent e) {
		frameSequence.setSingleFrame(singleFrameJList.getSelectedValue());
		frameSequence.fireDataChanged();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object src = e.getSource();
		if (src == dispose) {
			int val = 0;
			for (int i = 0;
			     i < dcodes.length;
			     i++) {
				String dcode = dcodes[i];
				if (dcode.equals(dispose.getSelectedItem())) {
					val = i;
				}
			}
			if (apply.isSelected()) {
				SingleFrame[] frameSequenceFrames = frameSequence.getSingleFrames();
				for (SingleFrame frameSequenceFrame : frameSequenceFrames) {
					frameSequenceFrame.setDispose(val);
				}
			} else {
				frameSequence.getSingleFrame()
				             .setDispose(val);
			}
			frameSequence.fireDataChanged();
		}
	}
}