package biz.tomar.storage.gif;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;

/**
 * This class is a hack around for using fancy Java 6 API calls in
 * giftedmotion and still be able to run the program on older JRE
 * installations with reduced fancyness.
 * The purpose of this class is to encapsulate exceptions, thrown due to
 * missing APIs
 */
public class CatchOldJava {

	/**
	 * Try to open an URL in an external browser
	 *
	 * @param url the URL to open
	 */
	public static void openBrowser(String url)
					throws
					Exception {
		try {
			Desktop dt = Desktop.getDesktop();
			if (!dt.isSupported(Desktop.Action.BROWSE)) {
				throw new Exception();
			}
			dt.browse(new URI(url));

		} catch (java.lang.NoClassDefFoundError err) {
			// Likely ClassNotFoundError
			throw new Exception();
		}
	}

	/**
	 * Try to put imageicons on the window
	 *
	 * @param w the window to decorate
	 */
	public static void decorateWindow(Window w) {
		try {
			ArrayList<Image> images = new ArrayList<>();
			images.add(new ImageIcon(ClassLoader.getSystemResource("resources/logo-32x32.png")).getImage());
			images.add(new ImageIcon(ClassLoader.getSystemResource("resources/logo-48x48.png")).getImage());
			images.add(new ImageIcon(ClassLoader.getSystemResource("resources/logo-64x64.png")).getImage());
			images.add(new ImageIcon(ClassLoader.getSystemResource("resources/logo-96x96.png")).getImage());
			w.setIconImages(images);
		} catch (Throwable t) {
			// Really don't care. Its just deco
		}
	}
}
