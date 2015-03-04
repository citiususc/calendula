package es.usc.citius.servando.calendula.util;

/**
 * Created by joseangel.pineiro on 3/2/15.
 */
public class Strings {

    public static String toCamelCase(String s, String spacer) {
        String[] parts = s.split(spacer);
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + spacer + toProperCase(part);
        }
        return camelCaseString.replaceFirst(spacer, "");
    }

    public static String toProperCase(String s) {
        if (s.length() > 1)
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        else return s;
    }
}
