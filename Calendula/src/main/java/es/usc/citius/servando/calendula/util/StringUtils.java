package es.usc.citius.servando.calendula.util;

/**
 * Created by varo on 25/11/16.
 */

public class StringUtils {

    public static String join(String[] parts, String separator) {
        StringBuilder b = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            b.append(separator);
            b.append(parts[i]);
        }
        return b.toString();
    }
}
