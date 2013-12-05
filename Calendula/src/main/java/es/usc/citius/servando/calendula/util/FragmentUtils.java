package es.usc.citius.servando.calendula.util;

/**
 * Created by joseangel.pineiro on 12/5/13.
 */
public class FragmentUtils {

    public static String makeViewPagerFragmentName(int viewpagerId, int index) {
        return "android:switcher:" + viewpagerId + ":" + index;
    }
}
