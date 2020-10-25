package biz.tomar.storage.gif;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * This class is responsible for i18n
 */
public class Translations {

	/**
	 * The "classname" of the resource containing the translations
	 */
	public static final String RESOURCES_I_18_N = "gif.i18n";

	/**
	 * Contains the translations
	 */
	private static ResourceBundle resourceBundle;


	/**
	 * Load the bundle
	 */
	public Translations() {
		resourceBundle = ResourceBundle.getBundle(RESOURCES_I_18_N,
		                                          new UTF8PropertyResourceBundleControl());
	}

	/**
	 * Fetch a key from the i18n file
	 *
	 * @return the translation for key
	 */
	public static String get(String key) {
		return get(key,
		           null);
	}

	/**
	 * Fetch a key from the i18n file and replace its variables with values
	 *
	 * @param args the replacement values of the variables in the string
	 */
	public static String get(String key,
	                         Object... args) {
		String val;
		try {
			val = resourceBundle.getString(key);
		} catch (Exception e) {
			System.err.println(MessageFormat.format("Missing translation: {0}",
			                                        key));
			val = "";
		}

		if (args == null) {
			return val;
		} else {
			MessageFormat formatter = new MessageFormat(val);
			return formatter.format(args);
		}
	}

	/**
	 * Fetch a key from the i18n file and replace its variable with a value
	 *
	 * @param key the replacement key of the variable in the string
	 * @param arg the replacement value of the variable in the string
	 */
	public static String get(String key,
	                         Object arg) {
		Object[] tmp = {arg};
		String value = get(key,
		                   tmp);
		return value;
	}
}