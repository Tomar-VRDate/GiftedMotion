package biz.tomar.storage.gif;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Core class
 */
public class Core
				extends JFrame
				implements WindowListener,
				           ActionListener,
				           ComponentListener,
				           MouseMotionListener,
				           MouseListener,
				           DropTargetListener {
	public static final  String         GIFTED_MOTION_VERSION = "Gifted Motion 2020-24-10";
	private static final String         VERSION               = setVersion();
	/**
	 * Back reference to the running program
	 */
	public static        Core           app;
	/**
	 * Quit program
	 */
	private final        JMenuItem      quit                  = new JMenuItem(Translations.get("core.quit"),
	                                                                          KeyEvent.VK_Q);
	/**
	 * Load files
	 */
	private final        JMenuItem      load                  = new JMenuItem(Translations.get("core.load"),
	                                                                          KeyEvent.VK_L);
	/**
	 * Close project
	 */
	private final        JMenuItem      close                 = new JMenuItem(Translations.get("core.close"),
	                                                                          KeyEvent.VK_L);
	/**
	 * Export as animated GIF
	 */
	private final        JMenuItem      export                = new JMenuItem(Translations.get("core.export"),
	                                                                          KeyEvent.VK_S);
	/**
	 * Export as deoptimized GIF
	 */
	private final        JMenuItem      deoptimize            = new JMenuItem(Translations.get("core.deoptimize"));
	/**
	 * Save the sequence as individual files
	 */
	private final        JMenuItem      extract               = new JMenuItem(Translations.get("core.extract"),
	                                                                          KeyEvent.VK_E);
	/**
	 * Display license
	 */
	private final        JMenuItem      license               = new JMenuItem(Translations.get("core.license"));
	/**
	 * Go to homepage
	 */
	private final        JMenuItem      handbook              = new JMenuItem(Translations.get("core.handbook"));
	/**
	 * Go to FAQ
	 */
	private final        JMenuItem      faq                   = new JMenuItem(Translations.get("core.faq"));
	/**
	 * Play animation
	 */
	private final        JButton        play
	                                                          = new JButton(IO.createIcon("Tango/22x22/actions/media"
	                                                                                      + "-playback-start.png",
	                                                                                      Translations.get("core.play")));
	/**
	 * Pause animation
	 */
	private final        JButton        pause
	                                                          = new JButton(IO.createIcon("Tango/22x22/actions/media"
	                                                                                      + "-playback-pause.png",
	                                                                                      Translations.get("core.pause")));
	/**
	 * Record (same as export)
	 */
	private final        JButton        record
	                                                          = new JButton(IO.createIcon("Tango/22x22/actions/document"
	                                                                                      + "-save.png",
	                                                                                      Translations.get("core.record")));
	/**
	 * Import (same as load)
	 */
	private final        JButton        open
	                                                          = new JButton(IO.createIcon("Tango/22x22/actions/document"
	                                                                                      + "-open.png",
	                                                                                      Translations.get("core.open")));
	/**
	 * Close project button
	 */
	private final        JButton        closeButton
	                                                          = new JButton(IO.createIcon("Tango/22x22/actions/system"
	                                                                                      + "-log-out.png",
	                                                                                      Translations.get("core.close")));
	/**
	 * Toggle displaying of the settings window
	 */
	private final        JButton        toggleSettings
	                                                          = new JButton(IO.createIcon("Tango/22x22/categories"
	                                                                                      + "/preferences-desktop.png",
	                                                                                      Translations.get("core.togglesettings")));
	/**
	 * Drag tool
	 */
	private final        JToggleButton  dragButton            = new JToggleButton(IO.createIcon("Misc/Drag.png",
	                                                                                            Translations.get("core.dragtool")));
	/**
	 * Rotate tool
	 */
	private final        JToggleButton  rotateButton          = new JToggleButton(IO.createIcon("Misc/Rotate.png",
	                                                                                            Translations.get("core.rotatetool")));
	/**
	 * Resize tool
	 */
	private final        JToggleButton  resizeButton          = new JToggleButton(IO.createIcon("Misc/Resize.png",
	                                                                                            Translations.get("core.scaletool")));
	/**
	 * Onionskin button
	 */
	private final        JToggleButton  onionButton           = new JToggleButton(IO.createIcon("Misc/onion.png",
	                                                                                            Translations.get("core.onion")));
	/**
	 * Settings editor
	 */
	private final        SettingsEditor settingsEditor        = new SettingsEditor();
	/**
	 * The main workspace
	 */
	private final        JDesktopPane   workspace             = new JDesktopPane();
	/**
	 * For displaying status messages
	 */
	private final        JLabel         status                = new JLabel();
	/**
	 * Button group for tools
	 */
	private final        ButtonGroup    toolGroup             = new ButtonGroup();
	/**
	 * Sequence Editor
	 */
	private              SequenceEditor sequenceEditor;
	/**
	 * Frame Display
	 */
	private              FrameDisplay   frameDisplay;
	/**
	 * The framesequence being worked upon
	 */
	private              FrameSequence  frameSequence;

	/**
	 * Directory, to open filedialogs with
	 */
	private File directory = new File(System.getProperty("user.dir"));

	/**
	 * Used for doing an animation preview
	 */
	private Player player;

	/**
	 * Construct a new instance of the program. There may only be one object
	 * of this class present.
	 */
	public Core() {
		//Mac OS compatibility things (fullscreen mode, icon setting)
		if (MacOSCompat.isMacOSX()) {
			MacOSCompat.enableFullScreenMode(this);
		}

		//Create dragdrop stuff
		new DropTarget(this,
		               this);

		// Wire listeners up
		load.addActionListener(this);
		extract.addActionListener(this);
		export.addActionListener(this);
		deoptimize.addActionListener(this);
		quit.addActionListener(this);
		faq.addActionListener(this);
		handbook.addActionListener(this);
		license.addActionListener(this);
		open.addActionListener(this);
		closeButton.addActionListener(this);
		play.addActionListener(this);
		pause.addActionListener(this);
		record.addActionListener(this);
		toggleSettings.addActionListener(this);
		close.addActionListener(this);
		dragButton.addActionListener(this);
		rotateButton.addActionListener(this);
		resizeButton.addActionListener(this);
		onionButton.addActionListener(this);

		// Fancy stuff
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
		                                           InputEvent.CTRL_DOWN_MASK));
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
		                                           InputEvent.CTRL_DOWN_MASK));
		export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		                                             InputEvent.CTRL_DOWN_MASK));
		handbook.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
		                                               0));
		open.setToolTipText(((ImageIcon) open.getIcon()).getDescription());
		closeButton.setToolTipText(((ImageIcon) closeButton.getIcon()).getDescription());
		play.setToolTipText(((ImageIcon) play.getIcon()).getDescription());
		pause.setToolTipText(((ImageIcon) pause.getIcon()).getDescription());
		toggleSettings.setToolTipText(((ImageIcon) toggleSettings.getIcon()).getDescription());
		record.setToolTipText(((ImageIcon) record.getIcon()).getDescription());
		dragButton.setToolTipText(((ImageIcon) (dragButton.getIcon())).getDescription());
		resizeButton.setToolTipText(((ImageIcon) (resizeButton.getIcon())).getDescription());
		rotateButton.setToolTipText(((ImageIcon) (rotateButton.getIcon())).getDescription());
		onionButton.setToolTipText(((ImageIcon) (onionButton.getIcon())).getDescription());

		pause.setEnabled(false);
		status.setBorder(new BevelBorder(BevelBorder.LOWERED));

		// Build menus
		JMenu file = new JMenu(Translations.get("core.core.file"));
		file.add(load);
		file.add(export);
		file.add(close);
		file.add(new JSeparator());
		file.add(extract);
		file.add(deoptimize);
		file.add(new JSeparator());
		file.add(quit);

		JMenu help = new JMenu(Translations.get("core.core.help"));
		help.add(handbook);
		help.add(faq);
		help.add(new JSeparator());
		help.add(license);

		JMenuBar mbar = new JMenuBar();
		mbar.add(file);
		mbar.add(Box.createHorizontalGlue());
		mbar.add(help);
		mbar.setBorder(new BevelBorder(BevelBorder.RAISED));
		setJMenuBar(mbar);

		// Build toolbar
		JToolBar tbar = new JToolBar();
		tbar.setRollover(true);
		tbar.setFloatable(true);
		tbar.add(open);
		tbar.add(record);
		tbar.add(closeButton);
		tbar.add(toggleSettings);

		tbar.addSeparator();
		tbar.add(play);
		tbar.add(pause);
		tbar.addSeparator();

		toolGroup.add(dragButton);
		toolGroup.add(rotateButton);
		toolGroup.add(resizeButton);

		tbar.add(dragButton);
		tbar.add(rotateButton);
		tbar.add(resizeButton);
		tbar.addSeparator();

		tbar.add(onionButton);

		dragButton.setSelected(true);

		//Enable/disable buttons
		disableButtons();

		// Put all together and display
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(tbar,
		            BorderLayout.NORTH);
		content.add(workspace,
		            BorderLayout.CENTER);
		content.add(status,
		            BorderLayout.SOUTH);
		setContentPane(content);
		workspace.add(settingsEditor);
		workspace.setDesktopManager(new BoundedDesktopManager());
		postStatus("");
	}

	private static String setVersion() {
		Package aPackage              = Core.class.getPackage();
		String  implementationVersion = aPackage.getImplementationVersion();
		String version = implementationVersion != null
		                 ? String.format("%s %s",
		                                 GIFTED_MOTION_VERSION,
		                                 implementationVersion)
		                 : GIFTED_MOTION_VERSION;
		return version;
	}

	/**
	 * Program version as shown in the title
	 */
	public static String getVERSION() {
		return VERSION;
	}

	//UI Disabling/Enabling

	public static void main(String[] args) {
		//Register TGA plugin
		//IIORegistry registry = IIORegistry.getDefaultInstance();
		//registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());

		if (MacOSCompat.isMacOSX()) {
			System.setProperty("apple.laf.useScreenMenuBar",
			                   "true");
			MacOSCompat.setAppIcon(new ImageIcon(ClassLoader.getSystemResource("resources/logo-96x96.png")).getImage());
		}

		/*try
		{
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		new Translations();
		app = new Core();
		app.setSize(new Dimension(800,
		                          600));
		app.setTitle(VERSION);
		app.setLocationRelativeTo(null);

		CatchOldJava.decorateWindow(app);

		app.setVisible(true);
		app.addWindowListener(app);

		// If commandlinearguments are given, try to load them as files. This isn't going away.
		if (args != null && args.length != 0) {
			File[] f = new File[args.length];
			for (int i = 0;
			     i < args.length;
			     i++) {
				f[i] = new File(args[i]);
			}
			try {
				SingleFrame[] getSingleFrames = IO.load(f);
				app.setFrameSequence(new FrameSequence(getSingleFrames));
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

	/**
	 * * Various event listeners interface implementations
	 **/

	public void enableButtons() {
		play.setEnabled(true);
		record.setEnabled(true);
		close.setEnabled(true);
		export.setEnabled(true);
		extract.setEnabled(true);
		toggleSettings.setEnabled(true);
		dragButton.setEnabled(true);
		rotateButton.setEnabled(true);
		closeButton.setEnabled(true);
		resizeButton.setEnabled(true);
		onionButton.setEnabled(true);
		deoptimize.setEnabled(true);
	}

	public void disableButtons() {
		play.setEnabled(false);
		record.setEnabled(false);
		close.setEnabled(false);
		export.setEnabled(false);
		extract.setEnabled(false);
		toggleSettings.setEnabled(false);
		dragButton.setEnabled(false);
		rotateButton.setEnabled(false);
		closeButton.setEnabled(false);
		resizeButton.setEnabled(false);
		onionButton.setEnabled(false);
		deoptimize.setEnabled(false);
	}

	public void windowClosing(WindowEvent e)     { handleQuit(); }

	public void focusLost(FocusEvent e)          {}

	public void windowOpened(WindowEvent e)      {}

	public void windowClosed(WindowEvent e)      {}

	public void windowIconified(WindowEvent e)   {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowActivated(WindowEvent e)   {}

	public void windowDeactivated(WindowEvent e) {}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src == quit) {
			handleQuit();
		}
		if (src == load || src == open) {
			handleLoad();
		}
		if (src == extract) {
			handleExtract();
		}
		if (src == export || src == record) {
			handleExport();
		}
		if (src == deoptimize) {
			handleDeoptimize();
		}
		if (src == handbook) {
			handleHandbook();
		}
		if (src == faq) {
			handleFAQ();
		}
		if (src == license) {
			handleLicense();
		}
		if (src == play || src == pause) {
			handlePlayPause();
		}
		if (src == toggleSettings) {
			handleTogglesettings();
		}
		if (src == close || src == closeButton) {
			handleClose();
		}
		if (src == dragButton) {
			frameDisplay.getFrameCanvas()
			            .setTransformTool(new DragTool());
		}
		if (src == rotateButton) {
			frameDisplay.getFrameCanvas()
			            .setTransformTool(new RotateTool());
		}
		if (src == resizeButton) {
			frameDisplay.getFrameCanvas()
			            .setTransformTool(new ScaleTool());
		}
		if (src == onionButton) {
			frameDisplay.getFrameCanvas()
			            .setOnionskin(onionButton.isSelected());
		}
	}

	public void componentHidden(ComponentEvent e) {}

	public void componentMoved(ComponentEvent e)  {}

	public void componentShown(ComponentEvent e)  {}

	public void componentResized(ComponentEvent e) {
		Component c = (Component) e.getSource();
		Integer[] size = {new Integer(c.getWidth()),
		                  new Integer(c.getHeight())};
		postStatus(Translations.get("core.componentresized",
		                            size));
	}

	public void mouseMoved(MouseEvent e) {}

	public void mouseDragged(MouseEvent e) {
		SingleFrame singleFrame = frameSequence.getSingleFrame();
		if (singleFrame == null) {
			return;
		}
		postStatus(frameDisplay.getFrameCanvas()
		                       .getTransformTool()
		                       .getStatus(singleFrame));
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e)  {}

	public void mouseClicked(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		if (frameSequence.getSingleFrame() == null) {
			return;
		}
		postStatus(frameDisplay.getFrameCanvas()
		                       .getTransformTool()
		                       .getStatus(frameSequence.getSingleFrame()));
	}

	public void mouseReleased(MouseEvent e) {
		postStatus("");
	}

	/**
	 * * Handlers for events created by GUI elements
	 **/

	public void handleQuit() {
		System.exit(0);
	}

	public void handleLoad() { //TODO: This should be refactored into some load function or something.
		try {
			postStatus(Translations.get("core.handleload.hint"));
			LoadAccessory loadAccessory = new LoadAccessory();

			JFileChooser jfc = new JFileChooser(directory);
			jfc.setMultiSelectionEnabled(true);
			jfc.setAccessory(loadAccessory);
			jfc.addPropertyChangeListener(loadAccessory);
			jfc.addChoosableFileFilter(new ImageFileFilter(true));
			if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
				postStatus("");
				return;
			}
			postStatus("");

			directory = jfc.getCurrentDirectory();

			File[] selected = jfc.getSelectedFiles();
			if (selected.length == 1 && selected[0].isDirectory()) {
				jfc.setCurrentDirectory(selected[0]);
				return;
			}

			SingleFrame[] frames = IO.load(selected);
			if (frames == null || frames.length == 0) {
				postStatus(Translations.get("core.handleload.nothing"));
				return;
			}

			if (frameSequence != null) {
				for (int i = 0;
				     i < frames.length;
				     i++) {
					frameSequence.add(frames[i],
					                  frameSequence.getSingleFrames().length);
				}
			} else {
				setFrameSequence(new FrameSequence(frames));
			}

			enableButtons();

			dragButton.setSelected(true);
			onionButton.setSelected(false);
			frameDisplay.getFrameCanvas()
			            .setOnionskin(false);
		} catch (IllegalArgumentException exp) {
			postStatus(Translations.get("core.handleload.illegalargumentexception",
			                            exp.getMessage()));
		} catch (IOException exp) {
			postStatus(exp.getMessage());
		} catch (Exception exp) {
			postStatus(Translations.get("core.handleload.exception"));
			exp.printStackTrace();
		}
	}

	public void handleExtract() {
		if (frameSequence == null || frameSequence.getSingleFrames().length == 0) {
			postStatus(Translations.get("core.handleextract.nothing"));
			return;
		}

		try {
			JFileChooser jfc = new JFileChooser(directory);
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setAcceptAllFileFilterUsed(false);
			if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			directory = jfc.getCurrentDirectory();
			IO.extract(frameSequence,
			           directory,
			           frameDisplay.getFrameCanvas()
			                       .getSize(),
			           settingsEditor.getSettings());
			postStatus(Translations.get("core.handleextract.saved"));
		} catch (IOException e) {
			postStatus(e.getMessage());
		}
	}

	public void handleExport() {
		try {
			postStatus("");
			if (frameSequence == null) {
				postStatus(Translations.get("core.handleexport.nothing"));
				return;
			}
			JFileChooser jfc = new JFileChooser(directory);
			jfc.setSelectedFile(new File(Translations.get("core.handleexport.defaultname")));
			if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File dest = jfc.getSelectedFile();
			directory = jfc.getCurrentDirectory();

			Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglassCursor);
			postStatus(Translations.get("core.handleexport.saving")); // No idea why this does not show!
			IO.export(dest,
			          frameSequence,
			          frameDisplay.getFrameCanvas()
			                      .getSize(),
			          settingsEditor.getSettings());
			postStatus(Translations.get("core.handleexport.finished"));
			hourglassCursor = new Cursor(Cursor.DEFAULT_CURSOR);
			setCursor(hourglassCursor);
		} catch (FileNotFoundException exp) {
			postStatus(Translations.get("core.handleexport.filenotfoundexception",
			                            exp.getMessage()));
		} catch (Exception exp) {
			postStatus(Translations.get("core.handleexport.exception"));
			exp.printStackTrace();
		}
	}

	public void handleDeoptimize() {
		Dimension size = frameDisplay.getFrameCanvas()
		                             .getSize();
		BufferedImage outputBuf = new BufferedImage((int) size.getWidth(),
		                                            (int) size.getHeight(),
		                                            BufferedImage.TYPE_INT_ARGB);
		for (int i = 0;
		     i < frameSequence.getSingleFrames().length;
		     i++) {
			Graphics    graphics    = outputBuf.getGraphics();
			SingleFrame singleFrame = frameSequence.getSingleFrames()[i];
			singleFrame.paint(graphics);
			singleFrame.setBufferedImage(Util.copyImage(outputBuf));

			//Change all gif loaded settings to how it would be rendered
			Point position = new Point(0,
			                           0);
			singleFrame.setPosition(position);
			singleFrame.setScaleX((int) size.getWidth());
			singleFrame.setScaleY((int) size.getHeight());
		}
	}

	public void handleLicense() {
		try {
			JInternalFrame jif = new JInternalFrame("",
			                                        false,
			                                        true,
			                                        false,
			                                        false);
			JEditorPane txt = new JEditorPane(getClass().getClassLoader()
			                                            .getResource("resources/LICENSE"));
			txt.setEditable(false);
			jif.setContentPane(new JScrollPane(txt));
			workspace.add(jif);
			jif.setMaximum(true);
			jif.show();
		} catch (Exception exp) {
			// ?!
			exp.printStackTrace();
		}
	}

	public void handleHandbook() {
		String url = "http://www.onyxbits.de/giftedmotion/handbook";
		try {
			// Wrap this
			CatchOldJava.openBrowser(url);
		} catch (Exception exp) {
			JOptionPane.showInternalMessageDialog(workspace,
			                                      Translations.get("core.handlehandbook.text",
			                                                       url),
			                                      Translations.get("core.handlehandbook.title"),
			                                      JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleFAQ() {
		String url = "http://www.onyxbits.de/faq/giftedmotion";
		try {
			// Wrap this
			CatchOldJava.openBrowser(url);
		} catch (Exception exp) {
			JOptionPane.showInternalMessageDialog(workspace,
			                                      Translations.get("core.handlehandbook.text",
			                                                       url),
			                                      Translations.get("core.handlehandbook.title"),
			                                      JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handlePlayPause() {
		onionButton.setSelected(false);
		frameDisplay.getFrameCanvas()
		            .setOnionskin(false);

		try {
			if (play.isEnabled()) {
				player = new Player(frameSequence,
				                    0);
				player.start();
				play.setEnabled(false);
				pause.setEnabled(true);
			} else {
				player.interrupt();
				player.join();
				play.setEnabled(true);
				pause.setEnabled(false);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void handleTogglesettings() {
		settingsEditor.setVisible(!settingsEditor.isVisible());
	}

	/**
	 * *  Utility functions
	 **/

	public void handleClose() {
		sequenceEditor.dispose();
		frameDisplay.dispose();
		frameSequence = null;
		disableButtons();
	}

	/**
	 * Dispatch a new FrameSequence to the application
	 *
	 * @param frameSequence the sequence to distribute to all gui elements
	 */
	public void setFrameSequence(FrameSequence frameSequence) {
		if (frameSequence == null) {
			return;
		} else {
			this.frameSequence = frameSequence;
		}

		if (sequenceEditor != null) {
			sequenceEditor.dispose();
		}
		if (frameDisplay != null) {
			frameDisplay.dispose();
		}

		sequenceEditor = new SequenceEditor(frameSequence);
		frameSequence.addFrameSequenceListener(sequenceEditor);
		workspace.add(sequenceEditor);
		sequenceEditor.setLocation(5,
		                           5);
		sequenceEditor.show();

		FrameCanvas frameCanvas = new FrameCanvas(frameSequence);
		frameCanvas.addComponentListener(this);
		frameCanvas.addMouseMotionListener(this);
		frameCanvas.addMouseListener(this);
		frameSequence.addFrameSequenceListener(frameCanvas);
		frameDisplay = new FrameDisplay(frameCanvas);
		workspace.add(frameDisplay);
		frameDisplay.setLocation(sequenceEditor.getSize().width + 10,
		                         5);
		frameDisplay.show();
	}

	/**
	 * Post a message to the status bar
	 *
	 * @param message message to post
	 */
	public void postStatus(String message) {
		if (message.equals("")) {
			status.setText(" ");
		} else {
			status.setText(message);
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_COPY);

		//Load dragged file
		Transferable transferable = dtde.getTransferable();
		DataFlavor[] flavors      = transferable.getTransferDataFlavors();

		try {

			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					@SuppressWarnings("unchecked") List<File> files = (List<File>) transferable.getTransferData(flavor);

					SingleFrame[] singleFrames = IO.load(files.toArray(new File[files.size()]));
					if (singleFrames.length == 0) {
						postStatus(Translations.get("core.handleload.nothing"));
						return;
					}

					if (frameSequence != null) {
						for (int i = 0;
						     i < singleFrames.length;
						     i++) {
							frameSequence.add(singleFrames[i],
							                  frameSequence.getSingleFrames().length);
						}
					} else {
						setFrameSequence(new FrameSequence(singleFrames));
					}

				}
			}

			enableButtons();
			dragButton.setSelected(true);
			onionButton.setSelected(false);
			frameDisplay.getFrameCanvas()
			            .setOnionskin(false);

			dtde.dropComplete(true);
		} catch (IllegalArgumentException exp) {
			postStatus(Translations.get("core.handleload.illegalargumentexception",
			                            exp.getMessage()));
		} catch (IOException exp) {
			postStatus(exp.getMessage());
		} catch (Exception exp) {
			postStatus(Translations.get("core.handleload.exception"));
			exp.printStackTrace();
		}
	}
}