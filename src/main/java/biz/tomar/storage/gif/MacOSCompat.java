package biz.tomar.storage.gif;

import java.awt.*;
import java.lang.reflect.Method;

//import com.apple.eawt.Application;

public class MacOSCompat {
	public static boolean isMacOSX() {
		return System.getProperty("os.name")
		             .contains("Mac OS X");
	}

	public static void enableFullScreenMode(Window window) {
		String className  = "com.apple.eawt.FullScreenUtilities";
		String methodName = "setWindowCanFullScreen";

		try {
			Class<?> clazz = Class.forName(className);
			Method method = clazz.getMethod(methodName,
			                                Window.class,
			                                boolean.class);
			method.invoke(null,
			              window,
			              true);
		} catch (Throwable t) {
			System.err.println("Full screen mode is not supported");
			t.printStackTrace();
		}
	}

	public static void setAppIcon(Image i) {
		//Leave it out for now.
		//Application application = Application.getApplication();
		//application.setDockIconImage(i);
	}
}
