package es.usc.citius.servando.calendula.util.debug;

import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by alvaro.brey.vilas on 19/12/17.
 */

public class StethoHelper implements StethoHelperInterface {
    @Override
    public void init(Context context) {
        Stetho.initializeWithDefaults(context);
    }
}
